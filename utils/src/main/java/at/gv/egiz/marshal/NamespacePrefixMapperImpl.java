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
package at.gv.egiz.marshal;

//import com.sun.xml.internal.bind.marshaller.NamespacePrefixMapper;
import java.util.HashMap;
import java.util.Map;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class NamespacePrefixMapperImpl extends NamespacePrefixMapper {

  private static final Log log = LogFactory.getLog(NamespacePrefixMapperImpl.class);

  protected static final Map<String, String> prefixMap = new HashMap<String, String>();
  
  static {
    prefixMap.put("http://www.w3.org/2001/XMLSchema-instance", "xsi");
    prefixMap.put("http://www.w3.org/2000/09/xmldsig#", "dsig");
    prefixMap.put("http://www.buergerkarte.at/namespaces/securitylayer/1.2#", "sl");
    prefixMap.put("http://www.buergerkarte.at/cardchannel", "cc");
    prefixMap.put("http://www.w3.org/2001/04/xmldsig-more#", "ecdsa");
    prefixMap.put("http://reference.e-government.gv.at/namespace/persondata/20020228#", "pr");
    prefixMap.put("urn:oasis:names:tc:SAML:1.0:assertion", "saml");
    prefixMap.put("http://uri.etsi.org/01903/v1.1.1#", "xades");
    prefixMap.put("http://www.buergerkarte.at/namespaces/securitylayer/20020225#", "sl10");
    prefixMap.put("http://www.buergerkarte.at/namespaces/securitylayer/20020831#", "sl11");
  }
  
  
  @Override
  public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {

    if (log.isTraceEnabled()) {
      log.trace("prefix for namespace " + namespaceUri + " requested");
    }
    
    String prefix = prefixMap.get(namespaceUri);
    
    return (prefix != null) ? prefix : suggestion;
  }

  /**
   * Returns a list of namespace URIs that should be declared
   * at the root element.
   * <p>
   * By default, the JAXB RI produces namespace declarations only when
   * they are necessary, only at where they are used. Because of this
   * lack of look-ahead, sometimes the marshaller produces a lot of
   * namespace declarations that look redundant to human eyes. For example,
   */
  @Override
  public String[] getPreDeclaredNamespaceUris() {
    return new String[]{ "http://www.buergerkarte.at/namespaces/securitylayer/1.2#" };
  }
}
