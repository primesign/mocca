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
package at.gv.egiz.stal.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps XML Algorithms to JCE Hash names.
 * 
 */
public class JCEAlgorithmNames {

  private Map<String, String> hashNameMap = new HashMap<String, String>();

  public static String[] JCE_HASH_NAMES = { "SHA-1" };
  
  public static String[] SHA_1_ALGORITMS = {
      "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha1",
      "http://www.w3.org/2000/09/xmldsig#rsa-sha1" };

  private static JCEAlgorithmNames instance = new JCEAlgorithmNames();

  private JCEAlgorithmNames() {
    for (String alg : SHA_1_ALGORITMS) {
      registerHash(alg, JCE_HASH_NAMES[0]);
    }
  }

  public static String getJCEHashName(String xmlAlgorithmURI) {
     return instance.hashNameMap.get(xmlAlgorithmURI);
  }

  public void registerHash(String xmlAlgorithmURI, String jceName) {
    hashNameMap.put(xmlAlgorithmURI, jceName);
  }
}
