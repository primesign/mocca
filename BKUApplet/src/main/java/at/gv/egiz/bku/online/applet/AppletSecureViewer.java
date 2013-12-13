/*
 * Copyright 2011 by Graz University of Technology, Austria
 * MOCCA has been developed by the E-Government Innovation Center EGIZ, a joint
 * initiative of the Federal Chancellery Austria and Graz University of Technology.
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 */


package at.gv.egiz.bku.online.applet;

import iaik.me.security.CryptoException;
import iaik.me.security.MessageDigest;

import java.awt.event.ActionListener;
import java.security.DigestException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.bku.gui.BKUGUIFacade;
import at.gv.egiz.bku.smccstal.SecureViewer;
import at.gv.egiz.stal.HashDataInput;
import at.gv.egiz.stal.impl.ByteArrayHashDataInput;
import at.gv.egiz.stal.service.GetHashDataInputFault;
import at.gv.egiz.stal.service.STALPortType;
import at.gv.egiz.stal.service.types.GetHashDataInputResponseType;
import at.gv.egiz.stal.service.types.GetHashDataInputType;
import at.gv.egiz.stal.signedinfo.ReferenceType;
import at.gv.egiz.stal.signedinfo.SignedInfoType;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class AppletSecureViewer implements SecureViewer {

  private static final Logger log = LoggerFactory.getLogger(AppletSecureViewer.class);

  protected BKUGUIFacade gui;
  protected STALPortType stalPort;
  protected String sessId;
  protected List<HashDataInput> verifiedDataToBeSigned;

  public AppletSecureViewer(BKUGUIFacade gui, STALPortType stalPort,
          String sessId) {
    if (gui == null) {
      throw new NullPointerException("GUI must not be null");
    }
    if (stalPort == null) {
      throw new NullPointerException("STAL port must not be null");
    }
    if (sessId == null) {
      throw new NullPointerException("session id must not be null");
    }
    this.gui = gui;
    this.stalPort = stalPort;
    this.sessId = sessId;
  }

  /**
   * retrieves the data to be signed for
   * @param signedReferences
   * @param okListener
   * @param okCommand
   * @param cancelListener
   * @param cancelCommand
   * @throws java.security.DigestException
   * @throws java.lang.Exception
   */
  @Override
  public void displayDataToBeSigned(SignedInfoType signedInfo,
          ActionListener okListener, String okCommand)
          throws DigestException, Exception {
    
    if (verifiedDataToBeSigned == null) {
      log.info("Retrieve data to be signed for dsig:SignedInfo {}.", signedInfo.getId());
      List<GetHashDataInputResponseType.Reference> hdi = 
              getHashDataInput(signedInfo.getReference());
      verifiedDataToBeSigned = verifyHashDataInput(signedInfo.getReference(),
              hdi);
    }
    if (verifiedDataToBeSigned.size() > 0) {
      gui.showSecureViewer(verifiedDataToBeSigned, okListener, okCommand);
    } else {
      throw new Exception("No data to be signed (apart from any QualifyingProperties or a Manifest)");
    }
  }

  /**
   * Get all hashdata inputs that contain an ID attribute but no Type attribute.
   * @param signedReferences
   * @return
   * @throws at.gv.egiz.stal.service.GetHashDataInputFault
   */
  private List<GetHashDataInputResponseType.Reference> getHashDataInput(List<ReferenceType> signedReferences)
          throws GetHashDataInputFault, Exception {
    GetHashDataInputType request = new GetHashDataInputType();
    request.setSessionId(sessId);

    for (ReferenceType signedRef : signedReferences) {
      //don't get Manifest, QualifyingProperties, ...
      if (signedRef.getType() == null) {
        String signedRefId = signedRef.getId();
        if (signedRefId != null) {
          log.trace("Requesting hashdata input for reference {}.", signedRefId);
          GetHashDataInputType.Reference ref = new GetHashDataInputType.Reference();
          ref.setID(signedRefId);
          request.getReference().add(ref);

        } else {
          throw new Exception("Cannot resolve signature data for dsig:Reference without Id attribute");
        }
      }
    }

    if (request.getReference().size() < 1) {
      log.error("No signature data (apart from any QualifyingProperties or a Manifest) for session {}.", sessId);
      throw new Exception("No signature data (apart from any QualifyingProperties or a Manifest)");
    }

    if (log.isDebugEnabled()) {
      log.debug(
          "WebService call GetHashDataInput for {} references in session {}.",
          request.getReference().size(), sessId);
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
  private List<HashDataInput> verifyHashDataInput(List<ReferenceType> signedReferences, List<GetHashDataInputResponseType.Reference> hashDataInputs)
          throws DigestException, NoSuchAlgorithmException, Exception {

    ArrayList<HashDataInput> verifiedHashDataInputs = new ArrayList<HashDataInput>();

    for (ReferenceType signedRef : signedReferences) {
      if (signedRef.getType() == null) {
        log.info("Verifying digest for signed reference {}.", signedRef.getId());

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
        String filename = hashDataInput.getFilename();

        if (hdi == null) {
          throw new Exception("No hashdata input for reference " + signedRefId + " provided by service");
        }
        if (log.isDebugEnabled()) {
          log.debug("Digesting reference " + signedRefId + " (" + mimeType + ";" + encoding + ")");
        }

        byte[] hashDataInputDigest;
        if ((signedRef.getURI() != null) && signedRef.getURI().startsWith("CMSExcludedByteRange:")) {
          String range = signedRef.getURI().substring(21);
          int sep = range.indexOf('-');
          int from = Integer.parseInt(range.substring(0, sep));
          int to = Integer.parseInt(range.substring(sep+1));

          Arrays.fill(hdi, from, to+1, (byte)0);

          byte[] hashData = new byte[hdi.length - ((to+1) - from)];
          if (from > 0)
            System.arraycopy(hdi, 0, hashData, 0, from);
          if ((to+1) < hdi.length)
            System.arraycopy(hdi, to+1, hashData, from, hdi.length - (to+1));
          hashDataInputDigest = digest(hashData, signedDigestAlg);
        } else {
          hashDataInputDigest = digest(hdi, signedDigestAlg);
        }

        log.debug("Comparing digest to claimed digest value for reference {}.", signedRefId);
        if (!Arrays.equals(hashDataInputDigest, signedDigest)) {
          log.error("Bad digest value for reference {}.", signedRefId);
          throw new DigestException("Bad digest value for reference " + signedRefId);
        }

        verifiedHashDataInputs.add(new ByteArrayHashDataInput(hdi, signedRefId, mimeType, encoding, filename));
      }
    }

    return verifiedHashDataInputs;
  }

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
      mdAlg = "RIPEMD160";
    } else {
      throw new NoSuchAlgorithmException("Failed to verify digest value: unsupported digest algorithm " + mdAlg);
    }

    MessageDigest md;
    try {
      md = MessageDigest.getInstance(mdAlg);
    } catch (CryptoException e) {
      throw new NoSuchAlgorithmException(e);
    }
    return md.digest(hashDataInput);
  }
}
