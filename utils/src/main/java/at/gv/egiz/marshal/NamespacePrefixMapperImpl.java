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


package at.gv.egiz.marshal;

//import com.sun.xml.internal.bind.marshaller.NamespacePrefixMapper;
import java.util.HashMap;
import java.util.Map;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class NamespacePrefixMapperImpl extends NamespacePrefixMapper {

  private final Logger log = LoggerFactory.getLogger(NamespacePrefixMapperImpl.class);

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
    prefixMap.put("http://uri.etsi.org/01903/v1.3.2#", "xades");
    prefixMap.put("http://uri.etsi.org/01903#", "xades");
    prefixMap.put("http://www.buergerkarte.at/namespaces/securitylayer/20020225#", "sl10");
    prefixMap.put("http://www.buergerkarte.at/namespaces/securitylayer/20020831#", "sl11");
  }
  
  
  @Override
  public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {

    log.trace("Prefix for namespace {} requested.", namespaceUri);
    
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
