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
package at.gv.egiz.idlink;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import at.gv.egiz.bku.utils.urldereferencer.StreamData;
import at.gv.egiz.bku.utils.urldereferencer.URLDereferencer;

public class IdentityLinkTransformer {
  
  protected static Log log = LogFactory.getLog(IdentityLinkTransformer.class);

  /**
   * The transformer factory.
   */
  private static SAXTransformerFactory factory;

  /**
   * The instance to be returned by {@link #getInstance()}.
   */
  private static IdentityLinkTransformer instance;
  
  /**
   * Returns an instance of this <code>IdentityLinkTransfomer</code>.
   * 
   * @return an instance of this <code>IdentityLinkTransformer</code>
   */
  public static IdentityLinkTransformer getInstance() {
    if (instance == null) {
      instance = new IdentityLinkTransformer();
      factory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
    }
    return instance;
  }

  /**
   * Sets the given <code>domainIdentifier</code> on the corresponding
   * node of the given <code>idLink</code>.
   * <p>This method may be used to cope with a flaw in the IssuerTemplate-Stylesheets
   * used to transform a <code>CompressedIdentitiyLink</code> into an
   * <code>IdentityLink</code>. Some IssuerTemplate-Stylesheets do not
   * consider the <code>pr:Type</code> element value of the 
   * <code>CompressedIdentityLink</code> and render a <code>pr:Type</code> 
   * element value of <code>urn:publicid:gv.at:baseid</code>
   * into the <code>IdentityLink</code> structure. This method allows to
   * set the <code>pr:Type</code> element value on the given <code>idLink</code>
   * after the transformation.
   * </p>
   * 
   * @param idLink the <code>IdentityLink</code> element or one of it's ancestors.
   * Must not be <code>null</code>.
   * 
   * @param domainIdentifier the value to be set for the <code>pr:Type</code> element
   * 
   * @throws NullPointerException if <code>idLink</code> is <code>null</code>.
   */
  public static void setDomainIdentifier(Node idLink, String domainIdentifier) {
    
    Element element;
    if (idLink instanceof Element) {
      element = (Element) idLink;
    } else if (idLink instanceof Document) {
      element = ((Document) idLink).getDocumentElement();
    } else if (idLink != null) {
      Document document = idLink.getOwnerDocument();
      element = document.getDocumentElement();
    } else {
      throw new NullPointerException("Parameter 'idLink' must no be null.");
    }
    
    NodeList nodeList = element.getElementsByTagNameNS(
        "http://reference.e-government.gv.at/namespace/persondata/20020228#",
        "Type");

    for (int i = 0; i < nodeList.getLength(); i++) {
      if (nodeList.item(i) instanceof Element) {
        Element typeElement = (Element) nodeList.item(i);
        NodeList children = typeElement.getChildNodes();
        for (int j = 0; j < children.getLength(); j++) {
          if (children.item(j) instanceof Text) {
            ((Text) children.item(j)).setNodeValue(domainIdentifier);
          }
        }
      }
    }
    
  }
  
  /**
   * Mapping of issuer template URIs to transformation templates.
   */
  private Map<String, Templates> templates = new HashMap<String, Templates>();
  
  /**
   * Private constructor.
   */
  private IdentityLinkTransformer() {
  }

  /**
   * Transforms an identity link <code>source</code> to <code>result</code> with
   * the given issuer template from the <code>stylesheetURL</code>.
   * 
   * @param stylesheetURL
   *          the URL of the issuer template to be used for transformation
   * @param source
   *          the compressed identity link source
   * @param result
   *          the transformed identity link result
   * 
   * @throws MalformedURLException
   *           if the given <code>stylesheetURL</code> is not a valid
   *           <code>http</code> or <code>https</code> URL.
   * @throws IOException
   *           if dereferencing the <code>stylesheetURL</code> fails.
   * @throws TransformerConfigurationException
   *           if creating a transformation template from the dereferenced
   *           stylesheet fails.
   * @throws TransformerException
   *           if transforming the identity link fails.
   */
  public void transformIdLink(String stylesheetURL, Source source, Result result) throws IOException, TransformerException {

    Templates templ = templates.get(stylesheetURL);
    
    if (templ == null) {

      // TODO: implement stylesheet cache
      URL url = new URL(stylesheetURL);

      if (!"http".equalsIgnoreCase(url.getProtocol()) && !"https".equalsIgnoreCase(url.getProtocol())) {
        throw new MalformedURLException("Protocol " + url.getProtocol() + " not supported for IssuerTemplate URL.");
      }
      
      URLDereferencer dereferencer = URLDereferencer.getInstance();
      StreamData data = dereferencer.dereference(url.toExternalForm(), null);
      
      log.trace("Trying to create issuer template.");
      templ = factory.newTemplates(new StreamSource(data.getStream()));
      log.trace("Successfully created issuer template");

      templates.put(stylesheetURL, templ);

    }
    
    Transformer transformer = templ.newTransformer();
    
    transformer.transform(source, result);
    
  }
  
}
