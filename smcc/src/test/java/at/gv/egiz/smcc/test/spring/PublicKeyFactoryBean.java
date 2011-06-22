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



package at.gv.egiz.smcc.test.spring;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import org.springframework.beans.factory.FactoryBean;

public class PublicKeyFactoryBean implements FactoryBean {

  private byte[] encodedKey;
  
  private String algorithm;

  /**
   * @param encodedKey the encodedKey to set
   */
  public void setEncodedKey(byte[] encodedKey) {
    this.encodedKey = encodedKey;
  }

  /**
   * @param algorithm the algorithm to set
   */
  public void setAlgorithm(String algorithm) {
    this.algorithm = algorithm;
  }

  @Override
  public Object getObject() throws Exception {
    KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
    return keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
  }

  @Override
  public Class<?> getObjectType() {
    return PublicKey.class;
  }

  @Override
  public boolean isSingleton() {
    return false;
  }

}
