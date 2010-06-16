/*
* Copyright 2009 Federal Chancellery Austria and
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

package at.gv.egiz.smcc.test.spring;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

import org.springframework.beans.factory.FactoryBean;

public class PrivateKeyFactoryBean implements FactoryBean {

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
    return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
  }

  @Override
  public Class<?> getObjectType() {
    return PrivateKey.class;
  }

  @Override
  public boolean isSingleton() {
    return false;
  }

}
