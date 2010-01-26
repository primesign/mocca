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
package at.gv.egiz.bku.slcommands.impl;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.bku.slcommands.SLCommandContext;
import at.gv.egiz.bku.slexceptions.SLCommandException;
import at.gv.egiz.stal.InfoboxReadRequest;
import at.gv.egiz.stal.STALRequest;

/**
 * An implementation of the {@link Infobox} <em>Certificates</em> as 
 * specified in Security Layer 1.2. 
 * 
 * @author mcentner
 */
public class CertificatesInfoboxImpl extends AbstractAssocArrayInfobox {
  
  /**
   * Logging facility.
   */
  private static Log log = LogFactory.getLog(CertificatesInfoboxImpl.class);

  /**
   * The valid keys.
   */
  public static final String[] CERTIFICATES_KEYS = new String[] {
    "SecureSignatureKeypair", 
    "CertifiedKeypair" };
  
  @Override
  public String getIdentifier() {
    return "Certificates";
  }

  @Override
  public String[] getKeys() {
    return CERTIFICATES_KEYS;
  }

  @Override
  public boolean isValuesAreXMLEntities() {
    return false;
  }

  @Override
  public Map<String, Object> getValues(List<String> certificates, SLCommandContext cmdCtx) throws SLCommandException {
    
    STALHelper stalHelper = new STALHelper(cmdCtx.getSTAL());
    
    if (certificates != null && !certificates.isEmpty()) {
      
      List<STALRequest> stalRequests = new ArrayList<STALRequest>();

      // get certificates
      InfoboxReadRequest infoboxReadRequest;
      for (int i = 0; i < certificates.size(); i++) {
        infoboxReadRequest = new InfoboxReadRequest();
        infoboxReadRequest.setInfoboxIdentifier(certificates.get(i));
        stalRequests.add(infoboxReadRequest);
      }

      stalHelper.transmitSTALRequest(stalRequests);

      List<X509Certificate> x509Certs = stalHelper.getCertificatesFromResponses();
      
      Map<String, Object> values = new HashMap<String, Object>();

      for (int i = 0; i < certificates.size(); i++) {
        try {
          values.put(certificates.get(i), x509Certs.get(i).getEncoded());
        } catch (CertificateEncodingException e) {
          log.error("Failed to encode certificate.", e);
          throw new SLCommandException(4000);
        }
      }
      
      return values;
      
    } else {
      
      return new HashMap<String, Object>();
      
    }
    
    
  }

}
