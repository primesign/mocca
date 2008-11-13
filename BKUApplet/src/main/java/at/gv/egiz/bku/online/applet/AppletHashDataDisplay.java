/*
 * Copyright 2008 Federal Chancellery Austria and
 * Graz University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.gv.egiz.bku.online.applet;

import at.gv.egiz.bku.gui.BKUGUIFacade;
import java.security.DigestException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.bku.smccstal.SignRequestHandler;
import at.gv.egiz.stal.HashDataInput;
import at.gv.egiz.stal.impl.ByteArrayHashDataInput;
import at.gv.egiz.stal.service.GetHashDataInputFault;
import at.gv.egiz.stal.service.STALPortType;
import at.gv.egiz.stal.service.types.GetHashDataInputResponseType;
import at.gv.egiz.stal.service.types.GetHashDataInputType;
import at.gv.egiz.stal.signedinfo.DigestMethodType;
import at.gv.egiz.stal.signedinfo.ReferenceType;
import java.security.NoSuchAlgorithmException;

/**
 * A SignRequesthandler that obtains hashdata inputs from a STAL webservice and
 * displays these either within the applet or in a separate frame.
 * The internal viewer displays plaintext data only, other mimetypes can be saved to disk.
 * The standalone (frame) viewer displays all mimetypes.
 * 
 * (This class depends on STALService and therefore is not part of BKUCommonGUI.)
 * 
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class AppletHashDataDisplay extends SignRequestHandler {

  public static enum DISPLAY {
    applet, frame
  }
  private static final Log log = LogFactory.getLog(AppletHashDataDisplay.class);
  protected STALPortType stalPort;
  protected String sessId;
  protected DISPLAY display;

  public AppletHashDataDisplay(STALPortType stalPort, String sessId, DISPLAY display) {
    if (stalPort == null || sessId == null) {
      throw new NullPointerException("STAL port must not be null");
    }
    this.sessId = sessId;
    this.stalPort = stalPort;
    this.display = display;
  }

  @Override
  public void displayHashDataInputs(List<ReferenceType> signedReferences) throws DigestException, Exception {

    List<GetHashDataInputResponseType.Reference> hdi = getHashDataInput(signedReferences);
    List<HashDataInput> verifiedHashDataInputs = verifyHashDataInput(signedReferences, hdi);

    if (verifiedHashDataInputs.size() > 1) {
      gui.showHashDataInputDialog(verifiedHashDataInputs, false, this, "ok");
    } else if (verifiedHashDataInputs.size() == 1) {
      gui.showHashDataInputDialog(verifiedHashDataInputs, display==DISPLAY.frame, this, "ok");
    } else {
      throw new Exception("No signature data (apart from any QualifyingProperties or a Manifest)");
    }
  }

  /**
   * Get all hashdata inputs that contain an ID attribute but no Type attribute.
   * @param signedReferences
   * @return
   * @throws at.gv.egiz.stal.service.GetHashDataInputFault
   */
  private List<GetHashDataInputResponseType.Reference> getHashDataInput(List<ReferenceType> signedReferences) throws GetHashDataInputFault, Exception {
    GetHashDataInputType request = new GetHashDataInputType();
    request.setSessionId(sessId);

//    HashMap<String, ReferenceType> idSignedRefMap = new HashMap<String, ReferenceType>();
    for (ReferenceType signedRef : signedReferences) {
      //don't get Manifest, QualifyingProperties, ...
      if (signedRef.getType() == null) {
        String signedRefId = signedRef.getId();
        if (signedRefId != null) {
          if (log.isTraceEnabled()) {
            log.trace("requesting hashdata input for reference " + signedRefId);
          }
//          idSignedRefMap.put(signedRefId, signedRef);
          GetHashDataInputType.Reference ref = new GetHashDataInputType.Reference();
          ref.setID(signedRefId);
          request.getReference().add(ref);

        } else {
          throw new Exception("Cannot resolve signature data for dsig:Reference without Id attribute");
        }
      }
    }

    if (log.isDebugEnabled()) {
      log.debug("WebService call GetHashDataInput for " + request.getReference().size() + " references in session " + sessId);
    }
    GetHashDataInputResponseType response = stalPort.getHashDataInput(request);
    return response.getReference();
  }

  /**
   * Verifies all signed references and returns STAL HashDataInputs
   * @param signedReferences
   * @param hashDataInputs
   * @return
   * @throws java.security.DigestException
   * @throws java.security.NoSuchAlgorithmException
   * @throws Exception if no hashdata input is provided for a signed reference
   */
  private List<HashDataInput> verifyHashDataInput(List<ReferenceType> signedReferences, List<GetHashDataInputResponseType.Reference> hashDataInputs) throws DigestException, NoSuchAlgorithmException, Exception {

    ArrayList<HashDataInput> verifiedHashDataInputs = new ArrayList<HashDataInput>();

    for (ReferenceType signedRef : signedReferences) {
      if (signedRef.getType() == null) {
        log.info("Verifying digest for signed reference " + signedRef.getId());

        String signedRefId = signedRef.getId();
        byte[] signedDigest = signedRef.getDigestValue();
        String signedDigestAlg = null;
        if (signedRef.getDigestMethod() != null) {
          signedDigestAlg = signedRef.getDigestMethod().getAlgorithm();
        } else {
          throw new NoSuchAlgorithmException("Failed to verify digest value for reference " + signedRefId + ": no digest algorithm");
        }

        // usually, there is just one item here
        GetHashDataInputResponseType.Reference hashDataInput = null;
        for (GetHashDataInputResponseType.Reference hdi : hashDataInputs) {
          if (signedRefId.equals(hdi.getID())) {
            hashDataInput = hdi;
            break;
          }
        }
        if (hashDataInput == null) {
          throw new Exception("No hashdata input for reference " + signedRefId + " returned by service");
        }

        byte[] hdi = hashDataInput.getValue();
        String mimeType = hashDataInput.getMimeType();
        String encoding = hashDataInput.getEncoding();

        if (hdi == null) {
          throw new Exception("No hashdata input for reference " + signedRefId + " provided by service");
        }
        if (log.isDebugEnabled()) {
          log.debug("Got HashDataInput " + signedRefId + " (" + mimeType + ";" + encoding + ")");
        }

        byte[] hashDataInputDigest = digest(hdi, signedDigestAlg);

        if (log.isDebugEnabled()) {
          log.debug("Comparing digest values... ");
        }
//        log.warn("***************** DISABLED HASHDATA VERIFICATION");
        if (!Arrays.equals(hashDataInputDigest, signedDigest)) {
          log.error("Bad digest value for reference " + signedRefId);
          throw new DigestException("Bad digest value for reference " + signedRefId);
        }

        verifiedHashDataInputs.add(new ByteArrayHashDataInput(hdi, signedRefId, mimeType, encoding));
      }
    }
    
    return verifiedHashDataInputs;
  }

  //TODO
  private byte[] digest(byte[] hashDataInput, String mdAlg) throws NoSuchAlgorithmException {
    if ("http://www.w3.org/2000/09/xmldsig#sha1".equals(mdAlg)) {
      mdAlg = "SHA-1";
    } else if ("http://www.w3.org/2001/04/xmlenc#sha256".equals(mdAlg)) {
      mdAlg = "SHA-256";
    } else if ("http://www.w3.org/2001/04/xmlenc#sha224".equals(mdAlg)) {
      mdAlg = "SHA-224";
    } else if ("http://www.w3.org/2001/04/xmldsig-more#sha224".equals(mdAlg)) {
      mdAlg = "SHA-224";
    } else if ("http://www.w3.org/2001/04/xmldsig-more#sha384".equals(mdAlg)) {
      mdAlg = "SHA-384";
    } else if ("http://www.w3.org/2001/04/xmlenc#sha512".equals(mdAlg)) {
      mdAlg = "SHA-512";
    } else if ("http://www.w3.org/2001/04/xmldsig-more#md2".equals(mdAlg)) {
      mdAlg = "MD2";
    } else if ("http://www.w3.org/2001/04/xmldsig-more#md5".equals(mdAlg)) {
      mdAlg = "MD5";
    } else if ("http://www.w3.org/2001/04/xmlenc#ripemd160".equals(mdAlg)) {
      mdAlg = "RipeMD-160";
    } else {
      throw new NoSuchAlgorithmException("Failed to verify digest value: unsupported digest algorithm " + mdAlg);
    }

    MessageDigest md = MessageDigest.getInstance(mdAlg);
    return md.digest(hashDataInput);
  }
}
