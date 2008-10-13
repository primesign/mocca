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

import at.gv.egiz.bku.smccstal.SMCCSTALRequestHandler;
import at.gv.egiz.bku.smccstal.SignRequestHandler;
import at.gv.egiz.stal.HashDataInput;
import at.gv.egiz.stal.impl.ByteArrayHashDataInput;
import at.gv.egiz.stal.service.STALPortType;
import at.gv.egiz.stal.service.types.GetHashDataInputResponseType;
import at.gv.egiz.stal.service.types.GetHashDataInputType;
import at.gv.egiz.stal.signedinfo.DigestMethodType;
import at.gv.egiz.stal.signedinfo.ReferenceType;
import java.security.DigestException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author clemens
 */
public class WebServiceSignRequestHandler extends SignRequestHandler {

  private static final Log log = LogFactory.getLog(WebServiceSignRequestHandler.class);
  STALPortType stalPort;
  String sessId;

    public WebServiceSignRequestHandler(String sessId, STALPortType stalPort) {
    if (stalPort == null || sessId == null) {
      throw new NullPointerException("STAL port must not be null");
    }
    this.sessId = sessId;
    this.stalPort = stalPort;
  }

  @Override
  public void displayHashDataInputs(List<ReferenceType> signedReferences) throws Exception {
  
    GetHashDataInputType request = new GetHashDataInputType();
    request.setSessionId(sessId);

    HashMap<String, ReferenceType> idSignedRefMap = new HashMap<String, ReferenceType>();
    for (ReferenceType signedRef : signedReferences) {
      //don't get Manifest, QualifyingProperties, ...
      if (signedRef.getType() == null) {
        String signedRefId = signedRef.getId();
        if (signedRefId != null) {
          if (log.isTraceEnabled()) {
            log.trace("requesting hashdata input for reference " + signedRefId);
          }
          idSignedRefMap.put(signedRefId, signedRef);
          GetHashDataInputType.Reference ref = new GetHashDataInputType.Reference();
          ref.setID(signedRefId);
          request.getReference().add(ref);

        } else {
          throw new Exception("Cannot resolve HashDataInput for reference without Id attribute");
        }
      }
    }

    if (log.isDebugEnabled()) {
      log.debug("Calling GetHashDataInput for " + request.getReference().size() + " references in session " + sessId);
    }
    GetHashDataInputResponseType response = stalPort.getHashDataInput(request);
    ArrayList<HashDataInput> hashDataInputs = new ArrayList<HashDataInput>();

    //hashdata inputs returned from service
    HashMap<String, GetHashDataInputResponseType.Reference> idRefMap = new HashMap<String, GetHashDataInputResponseType.Reference>();
    for (GetHashDataInputResponseType.Reference reference : response.getReference()) {
      String id = reference.getID();
      byte[] hdi = reference.getValue();
      if (hdi == null) {
        throw new Exception("Did not receive hashdata input for reference " + id);
      }
      idRefMap.put(id, reference);
    }
    
    for (String signedRefId : idSignedRefMap.keySet()) {
      log.info("validating hashdata input for reference " + signedRefId);
      
      GetHashDataInputResponseType.Reference reference = idRefMap.get(signedRefId);
      if (reference == null) {
        throw new Exception("No hashdata input for reference " + signedRefId + " returned by service");
      }
      
//    }
//    
//    for (GetHashDataInputResponseType.Reference reference : response.getReference()) {
//
//      String id = reference.getID();
      byte[] hdi = reference.getValue();
      String mimeType = reference.getMimeType();
      String encoding = reference.getEncoding();

      if (hdi == null) {
        throw new Exception("No hashdata input provided for reference " + signedRefId);
      }
      if (log.isDebugEnabled()) {
        log.debug("Got HashDataInput " + signedRefId + " (" + mimeType + ";" + encoding + ")");
      }

      ReferenceType dsigRef = idSignedRefMap.get(signedRefId);
      DigestMethodType dm = dsigRef.getDigestMethod();
      
      if (dm == null) {
        throw new Exception("Failed to verify digest value for reference " + signedRefId + ": no digest algorithm");
      }
      String mdAlg = dm.getAlgorithm();
      if ("http://www.w3.org/2000/09/xmldsig#sha1".equals(mdAlg))
        mdAlg = "SHA-1";
      else if ("http://www.w3.org/2001/04/xmlenc#sha256".equals(mdAlg))
        mdAlg = "SHA-256";
      else if ("http://www.w3.org/2001/04/xmlenc#sha224 ".equals(mdAlg))
        mdAlg = "SHA-224";
      else if ("http://www.w3.org/2001/04/xmldsig-more#sha224  ".equals(mdAlg))
        mdAlg = "SHA-224";
      else if ("http://www.w3.org/2001/04/xmldsig-more#sha384".equals(mdAlg))
        mdAlg = "SHA-384";
      else if ("http://www.w3.org/2001/04/xmlenc#sha512".equals(mdAlg))
        mdAlg = "SHA-512";
      else if ("http://www.w3.org/2001/04/xmldsig-more#md2 ".equals(mdAlg))
        mdAlg = "MD2";
      else if ("http://www.w3.org/2001/04/xmldsig-more#md5".equals(mdAlg))
        mdAlg = "MD5";
      else if ("http://www.w3.org/2001/04/xmlenc#ripemd160 ".equals(mdAlg))
        mdAlg = "RipeMD-160";
      else {
        throw new Exception("Failed to verify digest value for reference " + signedRefId + ": unsupported digest algorithm " + mdAlg);
      }
      MessageDigest md = MessageDigest.getInstance(mdAlg);
      byte[] hdiDigest = md.digest(hdi);
      if (log.isDebugEnabled())
        log.debug("Comparing digest values... "); 
      if (!Arrays.equals(hdiDigest, dsigRef.getDigestValue())) {
        log.error("digest values differ: " + new String(hdiDigest) + ", " + new String(dsigRef.getDigestValue()));
        throw new DigestException("Bad digest value for reference " + signedRefId + ": " + new String(dsigRef.getDigestValue()));
      }
      hashDataInputs.add(new ByteArrayHashDataInput(hdi, signedRefId, mimeType, encoding));
    }
    
    gui.showHashDataInputDialog(hashDataInputs, this, "ok");
  }

  @Override
  public SMCCSTALRequestHandler newInstance() {
    return new WebServiceSignRequestHandler(this.sessId, this.stalPort);
  }
}
