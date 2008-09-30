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
 *
 * @author clemens
 */
public class WSSignRequestHandler extends SignRequestHandler {

  private static final Log log = LogFactory.getLog(WSSignRequestHandler.class);
  STALPortType stalPort;
  String sessId;

  public WSSignRequestHandler(String sessId, STALPortType stalPort) {
    if (stalPort == null || sessId == null) {
      throw new NullPointerException("STAL port must not be null");
    }
    this.sessId = sessId;
    this.stalPort = stalPort;
  }

  @Override
  public List<HashDataInput> getCashedHashDataInputs(List<ReferenceType> signedReferences) throws Exception {

    GetHashDataInputType request = new GetHashDataInputType();
    request.setSessionId(sessId);

    HashMap<String, ReferenceType> idRefMap = new HashMap<String, ReferenceType>();
    for (ReferenceType reference : signedReferences) {
      //don't get Manifest, QualifyingProperties, ...
      if (reference.getType() == null) {
        String referenceId = reference.getId();
        if (referenceId != null) {
          idRefMap.put(referenceId, reference);
          GetHashDataInputType.Reference ref = new GetHashDataInputType.Reference();
          ref.setID(referenceId);
          request.getReference().add(ref);

        } else {
          throw new Exception("Cannot resolve HashDataInput for reference without Id attribute");
        }
      }
    }

    if (log.isDebugEnabled()) {
      log.debug("Calling GetHashDataInput for session " + sessId);
    }
    GetHashDataInputResponseType response = stalPort.getHashDataInput(request);
    ArrayList<HashDataInput> hashDataInputs = new ArrayList<HashDataInput>();

    for (GetHashDataInputResponseType.Reference reference : response.getReference()) {

      String id = reference.getID();
      byte[] hdi = reference.getValue();
      if (hdi == null) {
        throw new Exception("Failed to resolve digest value for reference " + id);
      }
      String mimeType = reference.getMimeType();
      String encoding = reference.getEncoding();

      if (log.isDebugEnabled()) {
        log.debug("Got HashDataInput " + id + " (" + mimeType + ";" + encoding + ")");
      }

      ReferenceType dsigRef = idRefMap.get(id);
      DigestMethodType dm = dsigRef.getDigestMethod();
      if (dm == null) {
        throw new Exception("Failed to verify digest value for reference " + id + ": no digest algorithm");
      }
      //TODO 
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
      MessageDigest md = MessageDigest.getInstance(mdAlg);
      byte[] hdiDigest = md.digest(hdi);
      if (log.isDebugEnabled())
        log.debug("Comparing digest values... "); 
      if (!Arrays.equals(hdiDigest, dsigRef.getDigestValue())) {
        log.error("digest values differ: " + new String(hdiDigest) + ", " + new String(dsigRef.getDigestValue()));
        throw new DigestException("Bad digest value for reference " + id + ": " + dsigRef.getDigestValue());
      }
      hashDataInputs.add(new ByteArrayHashDataInput(hdi, id, mimeType, encoding));
    }
    return hashDataInputs;
  }

  @Override
  public SMCCSTALRequestHandler newInstance() {
    return new WSSignRequestHandler(this.sessId, this.stalPort);
  }
}
