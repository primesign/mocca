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


package at.gv.egiz.bku.slcommands.impl;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  private final Logger log = LoggerFactory.getLogger(CertificatesInfoboxImpl.class);

  /**
   * The valid keys.
   */
  public static final String[] CERTIFICATES_KEYS = new String[] {
    "SecureSignatureKeypair", 
    "CertifiedKeypair",
    "00",
    "01",
    "02",
    "03",
    "04",
    "05",
    "06",
    "07",
    "08",
    "09",
    "10",
    "11",
    "12",
    "13",
    "14"};
  
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
