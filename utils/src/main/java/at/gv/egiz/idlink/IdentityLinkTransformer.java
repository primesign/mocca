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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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

  private class IdLTransformer {
    
    /**
     * Is transformer in use?
     */
    private boolean inUse = false;
    
    /**
     * How often has this transformer been used?
     */
    private int timesUsed = 0;
    
    /**
     * The time this transformer has been created.
     */
    private long created;
    
    /**
     * When has this transformer been used the last time?
     */
    private long lastTimeUsed;
    
    /**
     * Average performance in milliseconds. 
     */
    private long time;
    
    /**
     * Time used for initialization.
     */
    private long initTime;
    
    /**
     * The stylesheet transformer.
     */
    private Templates templates;
    
    /**
     * Stylesheet URL.
     */
    private String stylesheetURL;

    /**
     * 
     * @param stylesheetURL
     * @throws IOException
     * @throws TransformerConfigurationException
     */
    public IdLTransformer(String stylesheetURL) throws IOException, TransformerConfigurationException {
      
      created = System.currentTimeMillis();
      
      // TODO: implement stylesheet cache
      this.stylesheetURL = stylesheetURL;
      URL url = new URL(stylesheetURL);

      if (!"http".equalsIgnoreCase(url.getProtocol()) && !"https".equalsIgnoreCase(url.getProtocol())) {
        throw new MalformedURLException("Protocol " + url.getProtocol() + " not supported for IssuerTemplate URL.");
      }
      
      URLDereferencer dereferencer = URLDereferencer.getInstance();
      StreamData data = dereferencer.dereference(url.toExternalForm(), null);
      
      StreamSource source = new StreamSource(data.getStream());
      log.trace("Trying to creating template from stylesheet");
      templates = factory.newTemplates(source);
      log.trace("Successfully created stylesheet template");
      initTime = System.currentTimeMillis() - created;
      
    }
    
    public void transform(Source xmlSource, Result outputTarget) throws TransformerException {
      long t0 = System.currentTimeMillis();
      try {
        Transformer transformer = templates.newTransformer();
        transformer.transform(xmlSource, outputTarget);
      } catch (TransformerException e) {
        throw e;
      } finally {
        inUse = false;
        long t1 = System.currentTimeMillis();
        time += (t1 - t0);
        timesUsed++;
        lastTimeUsed = System.currentTimeMillis();
      }
    }
    
    /**
     * @return <code>true</code> if this transformer is in use, or <code>false</code> otherwise
     */
    public boolean isInUse() {
      return inUse;
    }

    @Override
    public String toString() {
      StringBuffer str = new StringBuffer();
      str.append("Transformer ").append(stylesheetURL)
        .append("\n    created ").append(new Date(created)).append(" used ").append(
          timesUsed).append(" times, (init ").append(initTime).append("ms / ")
        .append(((float) time) / timesUsed).append("ms avg) last time ").append(new Date(lastTimeUsed));
      return str.toString();
    }
    
  }
  
  /**
   * The transfomer factory.
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
   * The pool of <code>Transformer</code>.
   */
  private Map<String, List<IdLTransformer>> pool;
  
  /**
   * Private constructor.
   */
  private IdentityLinkTransformer() {
    pool = new HashMap<String, List<IdLTransformer>>();
  }
  
  private IdLTransformer getFreeTransfomer(String stylesheetURL) throws TransformerConfigurationException, IOException {
    
    IdLTransformer transformer = null;
    
    List<IdLTransformer> transfomerList = pool.get(stylesheetURL);
    if (transfomerList == null) {
      transfomerList = new ArrayList<IdLTransformer>();
      pool.put(stylesheetURL, transfomerList);
    }
    
    for (IdLTransformer candTransformer : transfomerList) {
      if (!candTransformer.inUse) {
        transformer = candTransformer;
        break;
      }
    }
    
    if (transformer == null) {
      transformer = new IdLTransformer(stylesheetURL);
      transfomerList.add(transformer);
    }
    
    transformer.inUse = true;
    return transformer;
    
  }
  
  public void transformIdLink(String stylesheetURL, Source source, Result result) throws IOException, TransformerException {
    log.trace("Trying to get free IdentityLinkTransformer for issuer template '" + stylesheetURL + "'.");
    IdLTransformer transformer = getFreeTransfomer(stylesheetURL);
    log.trace("Trying to transform IdentityLink.");
    transformer.transform(source, result);
    log.trace("IdentityLink transformed successfully. " + getStatistics());
  }
  
  public String getStatistics() {
    
    StringBuffer str = new StringBuffer();
    Iterator<String> keys = pool.keySet().iterator();
    int count = 0;
    while (keys.hasNext()) {
      String stylesheetURL = (String) keys.next();
      str.append("Stylesheet URL: ").append(stylesheetURL);
      Iterator<IdLTransformer> transformer = pool.get(stylesheetURL).iterator();
      while (transformer.hasNext()) {
        IdLTransformer idLTransformer = (IdLTransformer) transformer.next();
        str.append("\n  ").append(idLTransformer);
        count++;
      }
    }
    str.append("\n(").append(count).append(" transformer)");
    return str.toString();
  }
  
}
