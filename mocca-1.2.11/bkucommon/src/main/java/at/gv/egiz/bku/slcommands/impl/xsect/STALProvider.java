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
package at.gv.egiz.bku.slcommands.impl.xsect;

import iaik.xml.crypto.XmldsigMore;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.Signature;
import java.util.HashMap;
import java.util.Map;

import javax.xml.crypto.dsig.SignatureMethod;

/**
 * A security provider implementation that provides {@link Signature} implementations
 * based on STAL.
 * 
 * @author mcentner
 */
public class STALProvider extends Provider {

  private static final long serialVersionUID = 1L;
  
  private static String IMPL_PACKAGE_NAME = "at.gv.egiz.bku.slcommands.impl.xsect";
  
  public STALProvider() {
    
    super("STAL", 1.0, "Security Token Abstraction Layer Provider");
  
    final Map<String, String> map = new HashMap<String, String>();

    // TODO: register further algorithms
    map.put("Signature." + SignatureMethod.RSA_SHA1,
        IMPL_PACKAGE_NAME + ".STALSignature");
    map.put("Signature." + XmldsigMore.SIGNATURE_ECDSA_SHA1, 
        IMPL_PACKAGE_NAME + ".STALSignature");
    map.put("Signature." + XmldsigMore.SIGNATURE_RSA_SHA256, 
        IMPL_PACKAGE_NAME + ".STALSignature");
    map.put("Signature." + XmldsigMore.SIGNATURE_ECDSA_SHA256, 
        IMPL_PACKAGE_NAME + ".STALSignature");
    map.put("Signature." + XmldsigMore.SIGNATURE_ECDSA_SHA512, 
        IMPL_PACKAGE_NAME + ".STALSignature");


    AccessController.doPrivileged(new PrivilegedAction<Void>() {
      @Override
      public Void run() {
        putAll(map);
        return null;
      }
    });
  
  }
  
}
