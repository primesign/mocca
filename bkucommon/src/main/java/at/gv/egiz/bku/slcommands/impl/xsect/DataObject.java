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

import iaik.xml.crypto.dom.DOMCryptoContext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLObject;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.crypto.dsig.spec.XPathFilter2ParameterSpec;
import javax.xml.crypto.dsig.spec.XPathType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSException;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSParser;
import org.w3c.dom.ls.LSSerializer;

import at.buergerkarte.namespaces.securitylayer._1.Base64XMLLocRefOptRefContentType;
import at.buergerkarte.namespaces.securitylayer._1.DataObjectInfoType;
import at.buergerkarte.namespaces.securitylayer._1.MetaInfoType;
import at.buergerkarte.namespaces.securitylayer._1.TransformsInfoType;
import at.gv.egiz.bku.binding.HttpUtil;
import at.gv.egiz.bku.slexceptions.SLCommandException;
import at.gv.egiz.bku.slexceptions.SLRequestException;
import at.gv.egiz.bku.slexceptions.SLRuntimeException;
import at.gv.egiz.bku.utils.urldereferencer.StreamData;
import at.gv.egiz.bku.utils.urldereferencer.URLDereferencer;
import at.gv.egiz.dom.DOMUtils;
import at.gv.egiz.slbinding.impl.XMLContentType;

/**
 * This class represents a <code>DataObject</code> of an XML-Signature
 * created by the security layer command <code>CreateXMLSignature</code>.
 * 
 * @author mcentner
 */
public class DataObject {

  /**
   * Logging facility.
   */
  private static Log log = LogFactory.getLog(DataObject.class);
  
  /**
   * DOM Implementation.
   */
  private static final String DOM_LS_3_0 = "LS 3.0";

  /**
   * The array of the default preferred MIME type order.
   */
  private static final String[] DEFAULT_PREFFERED_MIME_TYPES = 
    new String[] {
      "application/xhtml+xml", 
      "text/plain"
    };
  
  /**
   * The DOM implementation used.
   */
  private DOMImplementationLS domImplLS;

  /**
   * The signature context.
   */
  private SignatureContext ctx;
  
  /**
   * The Reference for this DataObject.
   */
  private XSECTReference reference;
  
  /**
   * The XMLObject for this DataObject.
   */
  private XMLObject xmlObject;
  
  /**
   * The MIME-Type of the digest input.
   */
  private String mimeType;
  
  /**
   * An optional description of the digest input.
   */
  private String description;
  
  /**
   * Creates a new instance.
   * 
   * @param document the document of the target signature
   */
  public DataObject(SignatureContext signatureContext) {
    this.ctx = signatureContext;
    
    DOMImplementationRegistry registry;
    try {
      registry = DOMImplementationRegistry.newInstance();
    } catch (Exception e) {
      log.error("Failed to get DOMImplementationRegistry.", e);
      throw new SLRuntimeException("Failed to get DOMImplementationRegistry.");
    }

    domImplLS = (DOMImplementationLS) registry.getDOMImplementation(DOM_LS_3_0);
    if (domImplLS == null) {
      log.error("Failed to get DOMImplementation " + DOM_LS_3_0);
      throw new SLRuntimeException("Failed to get DOMImplementation " + DOM_LS_3_0);
    }

  }

  /**
   * @return the reference
   */
  public Reference getReference() {
    return reference;
  }

  /**
   * @return the xmlObject
   */
  public XMLObject getXmlObject() {
    return xmlObject;
  }

  /**
   * @return the mimeType
   */
  public String getMimeType() {
    return mimeType;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Configures this DataObject with the information provided within the given
   * <code>sl:DataObjectInfo</code>.
   * 
   * @param dataObjectInfo
   *          the <code>sl:DataObjectInfo</code>
   * 
   * @throws SLCommandException
   *           if configuring this DataObject with the information provided in
   *           the <code>sl:DataObjectInfo</code> fails.
   * @throws SLRequestException
   *           if the information provided in the <code>sl:DataObjectInfo</code>
   *           does not conform to the security layer specification.
   * @throws NullPointerException
   *           if <code>dataObjectInfo</code> is <code>null</code>
   */
  public void setDataObjectInfo(DataObjectInfoType dataObjectInfo) throws SLCommandException, SLRequestException {
  
    Base64XMLLocRefOptRefContentType dataObject = dataObjectInfo.getDataObject();
    String structure = dataObjectInfo.getStructure();
    
    // select and unmarshal an appropriate transformation path if provided
    // and set the final data meta information
    XSECTTransforms transforms = createTransformsAndSetFinalDataMetaInfo(dataObjectInfo.getTransformsInfo());
  
    if ("enveloping".equals(structure)) {
      
      // configure this DataObject as an enveloped DataObject
      setEnvelopedDataObject(dataObject, transforms);
      
    } else if ("detached".equals(structure)) {
      
      // configure this DataObject as an detached DataObject
      setDetachedDataObject(dataObject, transforms);
      
    } 
    // other values are not allowed by the schema and are therefore ignored
    
  }

  /**
   * Configures this DataObject as an enveloped DataObject with the information
   * provided within the given <code>sl:DataObject</code>.
   * 
   * @param dataObject
   *          the <code>sl:DataObject</code>
   * @param transforms
   *          an optional <code>Transforms</code> element (may be
   *          <code>null</code>)
   * 
   * @throws SLCommandException
   *           if configuring this DataObject with the information provided in
   *           the <code>sl:DataObject</code> fails.
   * @throws SLRequestException
   *           if the information provided in the <code>sl:DataObject</code>
   *           does not conform to the security layer specification.
   * @throws NullPointerException
   *           if <code>dataObject</code> is <code>null</code>
   */
  private void setEnvelopedDataObject(
      Base64XMLLocRefOptRefContentType dataObject, XSECTTransforms transforms)
      throws SLCommandException, SLRequestException {
    
    String reference = dataObject.getReference();
    if (reference == null) {
      //
      // case A
      //
      // The Reference attribute is not used; the content of sl:DataObject represents the data object. 
      // If the data object is XML-coded (the sl:XMLContent element is used in sl:DataObject), then it 
      // must be incorporated in the signature structure as parsed XML.
      //

      if (dataObject.getBase64Content() != null) {

        log.debug("Adding DataObject (Base64Content) without a reference URI.");

        // create XMLObject
        XMLObject xmlObject = createXMLObject(new ByteArrayInputStream(dataObject.getBase64Content()));

        setXMLObjectAndReferenceBase64(xmlObject, transforms);
        
      } else if (dataObject.getXMLContent() != null) {
        
        log.debug("Adding DataObject (XMLContent) without a reference URI.");

        // create XMLObject
        DocumentFragment content = parseDataObject((XMLContentType) dataObject.getXMLContent());
        XMLObject xmlObject = createXMLObject(content);
        
        setXMLObjectAndReferenceXML(xmlObject, transforms);
        
      } else if (dataObject.getLocRefContent() != null) {
        
        log.debug("Adding DataObject (LocRefContent) without a reference URI.");
        
        setEnvelopedDataObject(dataObject.getLocRefContent(), transforms);
        
      } else {
        
        // not allowed
        log.info("XML structure of the command request contains an " +
                "invalid combination of optional elements or attributes. " +
            "DataObject of structure='enveloped' without a reference must contain content.");
        throw new SLRequestException(3003);
        
      }
      
    } else {
      
      if (dataObject.getBase64Content() == null &&
          dataObject.getXMLContent() == null &&
          dataObject.getLocRefContent() == null) {

        //
        // case B
        //
        // The Reference attribute contains a URI that must be resolved by the 
        // Citizen Card Environment to obtain the data object. 
        // The content of sl:DataObject remains empty
        //
        
        log.debug("Adding DataObject from reference URI '" + reference + "'.");
        
        setEnvelopedDataObject(reference, transforms);
        
      } else {
        
        // not allowed
        log.info("XML structure of the command request contains an " +
            "invalid combination of optional elements or attributes. " +
        "DataObject of structure='enveloped' with reference must not contain content.");
        throw new SLRequestException(3003);
        
      }
      
      
    }
    
  }

  /**
   * Configures this DataObject as an enveloped DataObject with the content to
   * be dereferenced from the given <code>reference</code>.
   * 
   * @param reference
   *          the <code>reference</code> URI
   * @param transforms
   *          an optional <code>Transforms</code> element (may be
   *          <code>null</code>)
   * 
   * @throws SLCommandException
   *           if dereferencing the given <code>reference</code> fails, or if
   *           configuring this DataObject with the data dereferenced from the
   *           given <code>reference</code> fails.
   * @throws NullPointerException
   *           if <code>reference</code> is <code>null</code>
   */
  private void setEnvelopedDataObject(String reference, XSECTTransforms transforms) throws SLCommandException {

    if (reference == null) {
      throw new NullPointerException("Argument 'reference' must not be null.");
    }
    
    // dereference URL
    URLDereferencer dereferencer = URLDereferencer.getInstance();
    
    StreamData streamData;
    try {
      streamData = dereferencer.dereference(reference, ctx.getDereferencerContext());
    } catch (IOException e) {
      log.info("Failed to dereference XMLObject from '" + reference + "'.", e);
      throw new SLCommandException(4110);
    }

    Node childNode;
    
    String contentType = streamData.getContentType();
    if (contentType.startsWith("text/xml")) {
  
      // If content type is text/xml parse content.
      String charset = HttpUtil.getCharset(contentType, true);
      
      Document doc = parseDataObject(streamData.getStream(), charset);
      
      childNode = doc.getDocumentElement();
      
      if (childNode == null) {
        log.info("Failed to parse XMLObject from '" + reference + "'.");
        throw new SLCommandException(4111);
      }
      
      XMLObject xmlObject = createXMLObject(childNode);
      
      setXMLObjectAndReferenceXML(xmlObject, transforms);
      
    } else {
  
      // Include content Base64 encoded.
      XMLObject xmlObject = createXMLObject(streamData.getStream());
      
      setXMLObjectAndReferenceBase64(xmlObject, transforms);
      
    }
    
  }

  /**
   * Configures this DataObject as an detached DataObject with the information
   * provided in the given <code>sl:DataObject</code> and optionally
   * <code>transforms</code>.
   * 
   * @param dataObject
   *          the <code>sl:DataObject</code>
   * @param transforms
   *          an optional Transforms object, may be <code>null</code>
   * 
   * @throws SLCommandException
   *           if configuring this DataObject with the information provided in
   *           the <code>sl:DataObject</code> fails.
   * @throws SLRequestException
   *           if the information provided in the <code>sl:DataObject</code>
   *           does not conform to the security layer specification.
   * @throws NullPointerException
   *           if <code>dataObject</code> is <code>null</code>
   */
  private void setDetachedDataObject(
      Base64XMLLocRefOptRefContentType dataObject, XSECTTransforms transforms)
      throws SLCommandException, SLRequestException {
    
    String referenceURI = dataObject.getReference();
    
    if (referenceURI == null) {
      
      // not allowed
      log.info("XML structure of the command request contains an " +
          "invalid combination of optional elements or attributes. " +
      "DataObject of structure='detached' must contain a reference.");
      throw new SLRequestException(3003);

    } else {
      
      DigestMethod dm;
      try {
        dm = ctx.getAlgorithmMethodFactory().createDigestMethod(ctx);
      } catch (NoSuchAlgorithmException e) {
        log.error("Failed to get DigestMethod.", e);
        throw new SLCommandException(4006);
      } catch (InvalidAlgorithmParameterException e) {
        log.error("Failed to get DigestMethod.", e);
        throw new SLCommandException(4006);
      }
      
      String idValue = ctx.getIdValueFactory().createIdValue("Reference");
      
      reference = new XSECTReference(referenceURI, dm, transforms, null, idValue);
      
      // case D:
      //
      // The Reference attribute contains a URI that is used by the Citizen Card
      // Environment to code the reference to the data object as part of the XML
      // signature (attribute URI in the dsig:Reference) element. The content of
      // sl:DataObject represents the data object.
      
      if (dataObject.getLocRefContent() != null) {
        String locRef = dataObject.getLocRefContent();
        try {
          this.reference.setDereferencer(new LocRefDereferencer(ctx.getDereferencerContext(), locRef));
        } catch (URISyntaxException e) {
          log.info("Invalid URI '" + locRef + "' in DataObject.", e);
          throw new SLCommandException(4003);
        } catch (IllegalArgumentException e) {
          log.info("LocRef URI of '" + locRef + "' not supported in DataObject. ", e);
          throw new SLCommandException(4003);
        }
      } else if (dataObject.getBase64Content() != null) {
        byte[] base64Content = dataObject.getBase64Content();
        this.reference.setDereferencer(new ByteArrayDereferencer(base64Content));
      } else if (dataObject.getXMLContent() != null) {
        XMLContentType xmlContent = (XMLContentType) dataObject.getXMLContent();
        byte[] bytes = xmlContent.getRedirectedStream().toByteArray();
        this.reference.setDereferencer(new ByteArrayDereferencer(bytes));
      } else {
        
        // case C:
        //
        // The Reference attribute contains a URI that must be resolved by the
        // Citizen Card Environment to obtain the data object. The Reference
        // attribute contains a URI that is used by the Citizen Card Environment
        // to code the reference to the data object as part of the XML signature
        // (attribute URI in the dsig:Reference) element. The content of
        // sl:DataObject remains empty.

      }
      
    }     
  }

  /**
   * Returns the preferred <code>sl:TransformInfo</code> from the given list of
   * <code>transformInfos</code>, or <code>null</code> if none of the given
   * <code>transformInfos</code> is preferred over the others.
   * 
   * @param transformsInfos
   *          a list of <code>sl:TransformInfo</code>s
   * 
   * @return the selected <code>sl:TransformInfo</code> or <code>null</code>, if
   *         none is preferred over the others
   */
  private TransformsInfoType selectPreferredTransformsInfo(List<TransformsInfoType> transformsInfos) {
    
    Map<String, TransformsInfoType> mimeTypes = new HashMap<String, TransformsInfoType>();
    
    StringBuilder debugString = null;
    if (log.isDebugEnabled()) {
      debugString = new StringBuilder();
      debugString.append("Got " + transformsInfos.size() + " TransformsInfo(s):");
    }
    
    for (TransformsInfoType transformsInfoType : transformsInfos) {
      MetaInfoType finalDataMetaInfo = transformsInfoType.getFinalDataMetaInfo();
      String mimeType = finalDataMetaInfo.getMimeType();
      String description = finalDataMetaInfo.getDescription();
      mimeTypes.put(mimeType, transformsInfoType);
      if (debugString != null) {
        debugString.append("\n FinalDataMetaInfo: MIME-Type=");
        debugString.append(mimeType);
        if (description != null) {
          debugString.append(" ");
          debugString.append(description);
        }
      }
    }

    if (debugString != null) {
      log.debug(debugString);
    }

    // look for preferred transform
    for (String mimeType : DEFAULT_PREFFERED_MIME_TYPES) {
      if (mimeTypes.containsKey(mimeType)) {
        return mimeTypes.get(mimeType);
      }
    }
    
    // no preferred transform
    return null;
    
  }

  /**
   * Create an instance of <code>ds:Transforms</code> from the given
   * <code>sl:TransformsInfo</code>.
   * 
   * @param transformsInfo
   *          the <code>sl:TransformsInfo</code>
   * 
   * @return a corresponding unmarshalled <code>ds:Transforms</code>, or
   *         <code>null</code> if the given <code>sl:TransformsInfo</code> does
   *         not contain a <code>dsig:Transforms</code> element
   * 
   * @throws SLRequestException
   *           if the <code>ds:Transforms</code> in the given
   *           <code>transformsInfo</code> are not valid or cannot be parsed.
   * 
   * @throws MarshalException
   *           if the <code>ds:Transforms</code> in the given
   *           <code>transformsInfo</code> cannot be unmarshalled.
   */
  private XSECTTransforms createTransforms(TransformsInfoType transformsInfo) throws SLRequestException, MarshalException {
    
    ByteArrayOutputStream redirectedStream = ((at.gv.egiz.slbinding.impl.TransformsInfoType) transformsInfo).getRedirectedStream();
    byte[] transformBytes = (redirectedStream != null) ? redirectedStream.toByteArray() : null;
    
    if (transformBytes != null && transformBytes.length > 0) {

      // debug
      if (log.isTraceEnabled()) {
        StringBuilder sb = new StringBuilder();
        sb.append("Trying to parse transforms:\n");
        sb.append(new String(transformBytes, Charset.forName("UTF-8")));
        log.trace(sb);
      }
      
      DOMImplementationLS domImplLS = DOMUtils.getDOMImplementationLS();
      LSInput input = domImplLS.createLSInput();
      input.setByteStream(new ByteArrayInputStream(transformBytes));

      LSParser parser = domImplLS.createLSParser(
          DOMImplementationLS.MODE_SYNCHRONOUS, null);
      DOMConfiguration domConfig = parser.getDomConfig();
      SimpleDOMErrorHandler errorHandler = new SimpleDOMErrorHandler();
      domConfig.setParameter("error-handler", errorHandler);
      domConfig.setParameter("validate", Boolean.FALSE);

      Document document;
      try {
        document = parser.parse(input);
      } catch (DOMException e) {
        log.info("Failed to parse dsig:Transforms.", e);
        throw new SLRequestException(3002);
      } catch (LSException e) {
        log.info("Failed to parse dsig:Transforms.", e);
        throw new SLRequestException(3002);
      }

      // adopt ds:Transforms
      Element documentElement = document.getDocumentElement();
      Node adoptedTransforms = ctx.getDocument().adoptNode(documentElement);

      DOMCryptoContext context = new DOMCryptoContext();

      // unmarshall ds:Transforms
      return new XSECTTransforms(context, adoptedTransforms);

    } else {
      return null;
    }
    
  }
  
  /**
   * Sets the <code>mimeType</code> and the <code>description</code> value
   * for this DataObject.
   * 
   * @param metaInfoType the <code>sl:FinalMetaDataInfo</code>
   * 
   * @throws NullPointerException if <code>metaInfoType</code> is <code>null</code>
   */
  private void setFinalDataMetaInfo(MetaInfoType metaInfoType) {
    
    this.mimeType = metaInfoType.getMimeType();
    this.description = metaInfoType.getDescription();
    
  }
  
  /**
   * Selects an appropriate transformation path (if present) from the given list
   * of <code>sl:TransformInfos</code>, sets the corresponding final data meta info and
   * returns the corresponding unmarshalled <code>ds:Transforms</code>.
   * 
   * @param transformsInfos the <code>sl:TransformInfos</code>
   * 
   * @return the unmarshalled <code>ds:Transforms</code>, or <code>null</code> if
   * no transformation path has been selected.
   * 
   * @throws SLRequestException if the given list <code>ds:TransformsInfo</code> contains
   * an invalid <code>ds:Transforms</code> element, or no suitable transformation path
   * can be found. 
   */
  private XSECTTransforms createTransformsAndSetFinalDataMetaInfo(
      List<TransformsInfoType> transformsInfos) throws SLRequestException {

    TransformsInfoType preferredTransformsInfo = selectPreferredTransformsInfo(transformsInfos);
    // try preferred transform
    if (preferredTransformsInfo != null) {

      try {
        XSECTTransforms transforms = createTransforms(preferredTransformsInfo);
        setFinalDataMetaInfo(preferredTransformsInfo.getFinalDataMetaInfo());
        return transforms;
      } catch (MarshalException e) {
      
        String mimeType = preferredTransformsInfo.getFinalDataMetaInfo().getMimeType();
        log.info("Failed to unmarshal preferred transformation path (MIME-Type="
            + mimeType + ").", e);
      
      }

    }

    // look for another suitable transformation path
    for (TransformsInfoType transformsInfoType : transformsInfos) {
      
      try {
        XSECTTransforms transforms = createTransforms(transformsInfoType);
        setFinalDataMetaInfo(transformsInfoType.getFinalDataMetaInfo());
        return transforms;
      } catch (MarshalException e) {

        String mimeType = transformsInfoType.getFinalDataMetaInfo().getMimeType();
        log.info("Failed to unmarshal transformation path (MIME-Type="
            + mimeType + ").", e);
      }
      
    }

    // no suitable transformation path found
    throw new SLRequestException(3003);
    
  }

  /**
   * Create an XMLObject with the Base64 encoding of the given
   * <code>content</code>.
   * 
   * @param content
   *          the to-be Base64 encoded content
   * @return an XMLObject with the Base64 encoded <code>content</code>
   */
  private XMLObject createXMLObject(InputStream content) {
    
    Text textNode;
    try {
      textNode = at.gv.egiz.dom.DOMUtils.createBase64Text(content, ctx.getDocument());
    } catch (IOException e) {
      log.error(e);
      throw new SLRuntimeException(e);
    }

    DOMStructure structure = new DOMStructure(textNode);
    
    String idValue = ctx.getIdValueFactory().createIdValue("Object");

    return ctx.getSignatureFactory().newXMLObject(Collections.singletonList(structure), idValue, null, null);
    
  }
  
  /**
   * Create an XMLObject with the given <code>content</code> node.
   * 
   * @param content the content node
   * 
   * @return an XMLObject with the given <code>content</code>
   */
  private XMLObject createXMLObject(Node content) {
    
    String idValue = ctx.getIdValueFactory().createIdValue("Object");
    
    List<DOMStructure> structures = Collections.singletonList(new DOMStructure(content));
    
    return ctx.getSignatureFactory().newXMLObject(structures, idValue, null, null);
    
  }

  /**
   * Sets the given <code>xmlObject</code> and creates and sets a corresponding
   * <code>Reference</code>.
   * <p>
   * A transform to Base64-decode the xmlObject's content is inserted at the top
   * of to the optional <code>transforms</code> if given, or to a newly created
   * <code>Transforms</code> element if <code>transforms</code> is
   * <code>null</code>.
   * 
   * @param xmlObject
   *          the XMLObject
   * @param transforms
   *          an optional <code>Transforms</code> element (may be
   *          <code>null</code>)
   * 
   * @throws SLCommandException
   *           if creating the Reference fails
   * @throws NullPointerException
   *           if <code>xmlObject</code> is <code>null</code>
   */
  private void setXMLObjectAndReferenceBase64(XMLObject xmlObject, XSECTTransforms transforms) throws SLCommandException {
    
    // create reference URI
    //
    // NOTE: the ds:Object can be referenced directly, as the Base64 transform
    // operates on the text() of the input nodelist.
    //
    String referenceURI = "#" + xmlObject.getId();
  
    // create Base64 Transform
    Transform transform;
    try {
      transform = ctx.getSignatureFactory().newTransform(Transform.BASE64, (TransformParameterSpec) null);
    } catch (NoSuchAlgorithmException e) {
      // algorithm must be present
      throw new SLRuntimeException(e);
    } catch (InvalidAlgorithmParameterException e) {
      // algorithm does not take parameters
      throw new SLRuntimeException(e);
    }
    
    if (transforms == null) {
      transforms = new XSECTTransforms(Collections.singletonList(transform)); 
    } else {
      transforms.insertTransform(transform);
    }
  
    DigestMethod dm;
    try {
      dm = ctx.getAlgorithmMethodFactory().createDigestMethod(ctx);
    } catch (NoSuchAlgorithmException e) {
      log.error("Failed to get DigestMethod.", e);
      throw new SLCommandException(4006);
    } catch (InvalidAlgorithmParameterException e) {
      log.error("Failed to get DigestMethod.", e);
      throw new SLCommandException(4006);
    }
    String id = ctx.getIdValueFactory().createIdValue("Reference");
    
    this.xmlObject = xmlObject;
    this.reference = new XSECTReference(referenceURI, dm, transforms, null, id);
    
  }

  /**
   * Sets the given <code>xmlObject</code> and creates and sets a corresponding
   * <code>Reference</code>.
   * <p>
   * A transform to select the xmlObject's content is inserted at the top of to
   * the optional <code>transforms</code> if given, or to a newly created
   * <code>Transforms</code> element if <code>transforms</code> is
   * <code>null</code>.
   * </p>
   * 
   * @param xmlObject
   *          the XMLObject
   * @param transforms
   *          an optional <code>Transforms</code> element (may be
   *          <code>null</code>)
   * 
   * @throws SLCommandException
   *           if creating the Reference fails
   * @throws NullPointerException
   *           if <code>xmlObject</code> is <code>null</code>
   */
  private void setXMLObjectAndReferenceXML(XMLObject xmlObject, XSECTTransforms transforms) throws SLCommandException {
    
    // create reference URI
    String referenceURI = "#" + xmlObject.getId();
    
    // create Transform to select ds:Object's children
    Transform xpathTransform;
    Transform c14nTransform;
    try {

      XPathType xpath = new XPathType("id(\"" + xmlObject.getId() + "\")/node()", XPathType.Filter.INTERSECT);
      List<XPathType> xpaths = Collections.singletonList(xpath);
      XPathFilter2ParameterSpec params = new XPathFilter2ParameterSpec(xpaths);

      xpathTransform = ctx.getSignatureFactory().newTransform(Transform.XPATH2, params);

      // add exclusive canonicalization to avoid signing the namespace context of the ds:Object
      c14nTransform = ctx.getSignatureFactory().newTransform(CanonicalizationMethod.EXCLUSIVE, (TransformParameterSpec) null);
      
    } catch (NoSuchAlgorithmException e) {
      // algorithm must be present
      throw new SLRuntimeException(e);
    } catch (InvalidAlgorithmParameterException e) {
      // params must be appropriate
      throw new SLRuntimeException(e);
    }
  
    if (transforms == null) {
      List<Transform> newTransfroms = new ArrayList<Transform>();
      newTransfroms.add(xpathTransform);
      newTransfroms.add(c14nTransform);
      transforms = new XSECTTransforms(newTransfroms);
    } else {
      transforms.insertTransform(xpathTransform);
    }
    
    DigestMethod dm;
    try {
      dm = ctx.getAlgorithmMethodFactory().createDigestMethod(ctx);
    } catch (NoSuchAlgorithmException e) {
      log.error("Failed to get DigestMethod.", e);
      throw new SLCommandException(4006);
    } catch (InvalidAlgorithmParameterException e) {
      log.error("Failed to get DigestMethod.", e);
      throw new SLCommandException(4006);
    }
    String id = ctx.getIdValueFactory().createIdValue("Reference");

    this.xmlObject = xmlObject;
    this.reference = new XSECTReference(referenceURI, dm, transforms, null, id);
    
  }

  /**
   * Parses the given <code>xmlContent</code> and returns a corresponding
   * document fragment.
   * 
   * <p>
   * The to-be parsed content is surrounded by <dummy> ... </dummy> elements to
   * allow for mixed (e.g. Text and Element) content in XMLContent.
   * </p>
   * 
   * @param xmlContent
   *          the XMLContent to-be parsed
   * 
   * @return a document fragment containing the parsed nodes
   * 
   * @throws SLCommandException
   *           if parsing the given <code>xmlContent</code> fails
   * 
   * @throws NullPointerException
   *           if <code>xmlContent</code> is <code>null</code>
   */
  private DocumentFragment parseDataObject(XMLContentType xmlContent) throws SLCommandException {
    
    ByteArrayOutputStream redirectedStream =  xmlContent.getRedirectedStream();
  
    // Note: We can assume a fixed character encoding of UTF-8 for the
    // content of the redirect stream as the content has already been parsed
    // and serialized again to the redirect stream.
    
    List<InputStream> inputStreams = new ArrayList<InputStream>();
    try {
      // dummy start element
      inputStreams.add(new ByteArrayInputStream("<dummy>".getBytes("UTF-8")));
  
      // content
      inputStreams.add(new ByteArrayInputStream(redirectedStream.toByteArray()));
      
      // dummy end element
      inputStreams.add(new ByteArrayInputStream("</dummy>".getBytes("UTF-8")));
    } catch (UnsupportedEncodingException e) {
      throw new SLRuntimeException(e);
    }
    
    SequenceInputStream inputStream = new SequenceInputStream(Collections.enumeration(inputStreams));
  
    // parse DataObject
    Document doc = parseDataObject(inputStream, "UTF-8");
    
    Element documentElement = doc.getDocumentElement();
  
    if (documentElement == null ||
        !"dummy".equals(documentElement.getLocalName())) {
      log.info("Failed to parse DataObject XMLContent.");
      throw new SLCommandException(4111);
    }
    
    DocumentFragment fragment = doc.createDocumentFragment();
    while (documentElement.getFirstChild() != null) {
      fragment.appendChild(documentElement.getFirstChild());
    }
    
    // log parsed document
    if (log.isTraceEnabled()) {
      
      StringWriter writer = new StringWriter();
      
      writer.write("DataObject:\n");
      
      LSOutput output = domImplLS.createLSOutput();
      output.setCharacterStream(writer);
      output.setEncoding("UTF-8");
      LSSerializer serializer = domImplLS.createLSSerializer();
      serializer.getDomConfig().setParameter("xml-declaration", Boolean.FALSE);
      serializer.write(fragment, output);
      
      log.trace(writer.toString());
    }
    
    return fragment;
    
  }

  /**
   * Parses the given <code>inputStream</code> using the given
   * <code>encoding</code> and returns the parsed document.
   * 
   * @param inputStream
   *          the to-be parsed input
   * 
   * @param encoding
   *          the encoding to be used for parsing the given
   *          <code>inputStream</code>
   * 
   * @return the parsed document
   * 
   * @throws SLCommandException
   *           if parsing the <code>inputStream</code> fails.
   * 
   * @throws NullPointerException
   *           if <code>inputStram</code> is <code>null</code>
   */
  private Document parseDataObject(InputStream inputStream, String encoding) throws SLCommandException {

    LSInput input = domImplLS.createLSInput();
    input.setByteStream(inputStream);

    if (encoding != null) {
      input.setEncoding(encoding);
    }
    
    LSParser parser = domImplLS.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, null);
    DOMConfiguration domConfig = parser.getDomConfig();
    SimpleDOMErrorHandler errorHandler = new SimpleDOMErrorHandler();
    domConfig.setParameter("error-handler", errorHandler);
    domConfig.setParameter("validate", Boolean.FALSE);

    Document doc;
    try {
      doc = parser.parse(input);
    } catch (DOMException e) {
      log.info("Existing XML document cannot be parsed.", e);
      throw new SLCommandException(4111);
    } catch (LSException e) {
      log.info("Existing XML document cannot be parsed. ", e);
      throw new SLCommandException(4111);
    }
    
    if (errorHandler.hasErrors()) {
      // log errors
      if (log.isInfoEnabled()) {
        List<String> errorMessages = errorHandler.getErrorMessages();
        StringBuffer sb = new StringBuffer();
        for (String errorMessage : errorMessages) {
          sb.append(" ");
          sb.append(errorMessage);
        }
        log.info("Existing XML document cannot be parsed. " + sb.toString());
      }
      throw new SLCommandException(4111);
    }

    return doc;
    
  }


}
