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
import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class NamespacePrefixMapperImpl extends NamespacePrefixMapper {

  private static final Log log = LogFactory.getLog(NamespacePrefixMapperImpl.class);

  @Override
  public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {

    if (log.isTraceEnabled()) {
      log.trace("prefix for namespace " + namespaceUri + " requested");
    }
    if ("http://www.w3.org/2001/XMLSchema-instance".equals(namespaceUri)) {
      return NamespacePrefix.XSI_PREFIX;
    }

    if ("http://www.w3.org/2000/09/xmldsig#".equals(namespaceUri)) {
      return NamespacePrefix.XMLDSIG_PREFIX;
    }

    if ("http://www.buergerkarte.at/namespaces/securitylayer/1.2#".equals(namespaceUri)) {
      return NamespacePrefix.SL_PREFIX;
    }

    if ("http://www.buergerkarte.at/cardchannel".equals(namespaceUri)) {
      return NamespacePrefix.CARDCHANNEL_PREFIX;
    }

    if ("http://www.w3.org/2001/04/xmldsig-more#".equals(namespaceUri)) {
      return NamespacePrefix.ECDSA_PREFIX;
    }

    if ("http://reference.e-government.gv.at/namespace/persondata/20020228#".equals(namespaceUri)) {
      return NamespacePrefix.PERSONDATA_PREFIX;
    }

    if ("urn:oasis:names:tc:SAML:1.0:assertion".equals(namespaceUri)) {
      return NamespacePrefix.SAML10_PREFIX;
    }

    if ("http://uri.etsi.org/01903/v1.1.1#".equals(namespaceUri)) {
      return NamespacePrefix.XADES_PREFIX;
    }
    
    return suggestion;
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
