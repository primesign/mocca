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



package at.gv.egiz.mocca.id;

import java.security.Key;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.X509Data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdLinkKeySelector extends KeySelector {
  
  private static Logger log = LoggerFactory.getLogger(IdLinkKeySelector.class);
  
  private IdLink idLink;
  
  public IdLinkKeySelector(IdLink idLink) {
    super();
    if (idLink == null) {
      throw new NullPointerException("Parameter 'idLink' must not be null.");
    }
    this.idLink = idLink;
  }

  @Override
  public KeySelectorResult select(KeyInfo keyInfo, Purpose purpose,
      AlgorithmMethod method, XMLCryptoContext context)
      throws KeySelectorException {
    
    if (purpose != Purpose.VERIFY) {
      throw new KeySelectorException("KeySelector does not support purpose "
          + purpose + ".");
    }
    
    try {
      for (Object ki : keyInfo.getContent()) {
        if (ki instanceof X509Data) {
          for (Object xd : ((X509Data) ki).getContent()) {
            if (xd instanceof X509Certificate) {
              final PublicKey publicKey = ((X509Certificate) xd).getPublicKey();
              if (idLink.getCitizenPublicKeys().contains(publicKey)) {
                log.trace("Found matching key {} in identiy link and KeyInfo.", publicKey);
                return new KeySelectorResult() {
                  @Override
                  public Key getKey() {
                    return publicKey;
                  }
                };
              }
            }
          }
        }
      }
    } catch (MarshalException e) {
      log.info("Failed to get public keys from identity link.", e);
      throw new KeySelectorException(e);
    }
    
    log.info("Did not find matching public keys in the identity link and the KeyInfo.");
    return null;
  }

}
