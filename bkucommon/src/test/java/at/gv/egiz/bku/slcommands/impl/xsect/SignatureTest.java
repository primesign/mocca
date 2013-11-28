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


package at.gv.egiz.bku.slcommands.impl.xsect;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import iaik.xml.crypto.XSecProvider;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLObject;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.DigestMethodParameterSpec;
import javax.xml.crypto.dsig.spec.SignatureMethodParameterSpec;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import at.buergerkarte.namespaces.securitylayer._1_2_3.CreateXMLSignatureRequestType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.DataObjectInfoType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.ObjectFactory;
import at.buergerkarte.namespaces.securitylayer._1_2_3.SignatureInfoCreationType;
import at.gv.egiz.bku.slexceptions.SLCommandException;
import at.gv.egiz.bku.slexceptions.SLRequestException;
import at.gv.egiz.bku.slexceptions.SLViewerException;
import at.gv.egiz.bku.utils.urldereferencer.StreamData;
import at.gv.egiz.bku.utils.urldereferencer.URLDereferencer;
import at.gv.egiz.bku.utils.urldereferencer.URLDereferencerImpl;
import at.gv.egiz.bku.utils.urldereferencer.URLProtocolHandler;
import at.gv.egiz.dom.DOMUtils;
import at.gv.egiz.slbinding.RedirectEventFilter;
import at.gv.egiz.slbinding.RedirectUnmarshallerListener;

public class SignatureTest {

  private class AlgorithmMethodFactoryImpl implements AlgorithmMethodFactory {
  
    @Override
    public CanonicalizationMethod createCanonicalizationMethod(
        SignatureContext signatureContext) {
      
      XMLSignatureFactory signatureFactory = signatureContext.getSignatureFactory();
      
      try {
        return signatureFactory.newCanonicalizationMethod(CanonicalizationMethod.EXCLUSIVE, (C14NMethodParameterSpec) null);
      } catch (Exception e) {
        throw new RuntimeException(e);
      } 
    }
  
    @Override
    public DigestMethod createDigestMethod(SignatureContext signatureContext) {
  
      XMLSignatureFactory signatureFactory = signatureContext.getSignatureFactory();
  
      try {
        return signatureFactory.newDigestMethod(DigestMethod.SHA1, (DigestMethodParameterSpec) null);
      } catch (Exception e) {
        throw new RuntimeException(e);
      } 
    }
  
    @Override
    public SignatureMethod createSignatureMethod(
        SignatureContext signatureContext) {
  
      XMLSignatureFactory signatureFactory = signatureContext.getSignatureFactory();
  
      try {
        return signatureFactory.newSignatureMethod(SignatureMethod.RSA_SHA1, (SignatureMethodParameterSpec) null);
      } catch (Exception e) {
        throw new RuntimeException(e);
      } 
  
    }

    @Override
    public String getSignatureAlgorithmURI() {
      return SignatureMethod.RSA_SHA1;
    }

    @Override
    public String getDigestAlgorithmURI() {
      return DigestMethod.SHA1;
    }
    
  }

  private static final String RESOURCE_PREFIX = "at/gv/egiz/bku/slcommands/impl/";
  
  private static Unmarshaller unmarshaller;
  
  private static PrivateKey privateKey;

  private static X509Certificate certificate;
  
  private static URLDereferencer urlDereferencer;
  
  @BeforeClass
  public static void setUpClass() throws JAXBException, NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException, UnrecoverableKeyException {
    
    XSecProvider.addAsProvider(true);
    
    String packageName = ObjectFactory.class.getPackage().getName();
    packageName += ":"
        + org.w3._2000._09.xmldsig_.ObjectFactory.class.getPackage().getName();
    JAXBContext jaxbContext = JAXBContext.newInstance(packageName);

    unmarshaller = jaxbContext.createUnmarshaller();
    
    initURLDereferencer();
    
    ClassLoader classLoader = SignatureTest.class.getClassLoader();
    InputStream certStream = classLoader.getResourceAsStream(RESOURCE_PREFIX + "Cert.p12");
    assertNotNull("Certificate not found.", certStream);
        
    char[] passwd = "1622".toCharArray();
    
    KeyStore keystore = KeyStore.getInstance("PKCS12");
    keystore.load(certStream, passwd);
    String firstAlias = keystore.aliases().nextElement();
    certificate = (X509Certificate) keystore.getCertificate(firstAlias);
    privateKey = (PrivateKey) keystore.getKey(firstAlias, passwd);
    
    urlDereferencer = URLDereferencerImpl.getInstance();
    
  }

  private static void initURLDereferencer() {
    
    URLDereferencerImpl.getInstance().registerHandler("testlocal", new URLProtocolHandler() {
      
      @Override
      public StreamData dereference(String url)
          throws IOException {

        ClassLoader classLoader = SignatureTest.class.getClassLoader();
        
        String filename = url.split(":", 2)[1];

        InputStream stream = classLoader.getResourceAsStream(RESOURCE_PREFIX + filename);
        
        if (stream == null) {

          throw new IOException("Failed to resolve resource '" + url + "'.");

        } else {

          String contentType;
          if (filename.endsWith(".xml")) {
            contentType = "text/xml";
          } else if (filename.endsWith(".txt")) {
            contentType = "text/plain";
          } else {
            contentType = "";
          }
          
          return new StreamData(url, contentType, stream);
          
        }
        
      }

      @Override
      public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
        // TODO Auto-generated method stub
        
      }

      @Override
      public void setSSLSocketFactory(SSLSocketFactory socketFactory) {
        // TODO Auto-generated method stub
        
      }
      
    });
    
  }
  
  private Object unmarshal(String file) throws XMLStreamException, JAXBException {

    ClassLoader classLoader = SignatureTest.class.getClassLoader();
    InputStream resourceStream = classLoader.getResourceAsStream(RESOURCE_PREFIX + file);
    assertNotNull(resourceStream);

    XMLInputFactory inputFactory = XMLInputFactory.newInstance();
    XMLEventReader eventReader = inputFactory.createXMLEventReader(resourceStream);
    RedirectEventFilter redirectEventFilter = new RedirectEventFilter();
    XMLEventReader filteredReader = inputFactory.createFilteredReader(eventReader, redirectEventFilter);

    unmarshaller.setListener(new RedirectUnmarshallerListener(redirectEventFilter));

    return unmarshaller.unmarshal(filteredReader);
    
  }
  
  //
  //
  // SignatureInfo
  //
  //
  
  private SignatureInfoCreationType unmarshalSignatureInfo(String file) throws JAXBException, XMLStreamException {

    Object object = unmarshal(file);

    Object requestType = ((JAXBElement<?>) object).getValue();
    
    assertTrue(requestType instanceof CreateXMLSignatureRequestType);
    
    SignatureInfoCreationType signatureInfo = ((CreateXMLSignatureRequestType) requestType).getSignatureInfo(); 
    
    assertNotNull(signatureInfo);
    
    return signatureInfo;
    
  }

  @Test
  public void testSetSignatureInfo_Base64_1() throws JAXBException, SLCommandException, XMLStreamException {

    SignatureInfoCreationType signatureInfo = unmarshalSignatureInfo("SignatureInfo_Base64_1.xml");
    
    Signature signature = new Signature(urlDereferencer, new IdValueFactoryImpl(), null, false);
    
    signature.setSignatureInfo(signatureInfo);
    
    Node parent = signature.getParent();
    Node nextSibling = signature.getNextSibling();

    assertNotNull(parent);
    assertTrue("urn:document".equals(parent.getNamespaceURI()));
    assertTrue("XMLDocument".equals(parent.getLocalName()));
    
    assertNotNull(nextSibling);
    assertTrue("urn:document".equals(nextSibling.getNamespaceURI()));
    assertTrue("Paragraph".equals(nextSibling.getLocalName()));

  }

  @Test
  public void testSetSignatureInfo_Base64_14_1() throws JAXBException, SLCommandException, XMLStreamException {

    SignatureInfoCreationType signatureInfo = unmarshalSignatureInfo("SignatureInfo_Base64_1.xml");
    
    Signature signature = new Signature(urlDereferencer, new IdValueFactoryImpl(), null, true);
    
    signature.setSignatureInfo(signatureInfo);
    
    Node parent = signature.getParent();
    Node nextSibling = signature.getNextSibling();

    assertNotNull(parent);
    assertTrue("urn:document".equals(parent.getNamespaceURI()));
    assertTrue("XMLDocument".equals(parent.getLocalName()));
    
    assertNotNull(nextSibling);
    assertTrue("urn:document".equals(nextSibling.getNamespaceURI()));
    assertTrue("Paragraph".equals(nextSibling.getLocalName()));

  }

  @Test
  public void testSetSignature_Base64_2() throws JAXBException, SLCommandException, XMLStreamException {

    SignatureInfoCreationType signatureInfo = unmarshalSignatureInfo("SignatureInfo_Base64_2.xml");
    
    Signature signature = new Signature(urlDereferencer, new IdValueFactoryImpl(), null, false);
    
    signature.setSignatureInfo(signatureInfo);
    
    Node parent = signature.getParent();
    Node nextSibling = signature.getNextSibling();

    assertNotNull(parent);
    assertTrue("XMLDocument".equals(parent.getLocalName()));
    
    assertNotNull(nextSibling);
    assertTrue("Paragraph".equals(nextSibling.getLocalName()));
    
  }

  @Test
  public void testSetSignature_Base64_14_2() throws JAXBException, SLCommandException, XMLStreamException {

    SignatureInfoCreationType signatureInfo = unmarshalSignatureInfo("SignatureInfo_Base64_2.xml");
    
    Signature signature = new Signature(urlDereferencer, new IdValueFactoryImpl(), null, true);
    
    signature.setSignatureInfo(signatureInfo);
    
    Node parent = signature.getParent();
    Node nextSibling = signature.getNextSibling();

    assertNotNull(parent);
    assertTrue("XMLDocument".equals(parent.getLocalName()));
    
    assertNotNull(nextSibling);
    assertTrue("Paragraph".equals(nextSibling.getLocalName()));
    
  }

  @Test
  public void testSetSignature_Base64_3() throws JAXBException, SLCommandException, XMLStreamException {

    SignatureInfoCreationType signatureInfo = unmarshalSignatureInfo("SignatureInfo_Base64_3.xml");
    
    Signature signature = new Signature(urlDereferencer, new IdValueFactoryImpl(), null, false);
    
    signature.setSignatureInfo(signatureInfo);
    
    Node parent = signature.getParent();
    Node nextSibling = signature.getNextSibling();

    assertNotNull(parent);
    assertTrue("XMLDocument".equals(parent.getLocalName()));
    
    assertNotNull(nextSibling);
    assertTrue("Paragraph".equals(nextSibling.getLocalName()));

  }

  @Test
  public void testSetSignature_Base64_14_3() throws JAXBException, SLCommandException, XMLStreamException {

    SignatureInfoCreationType signatureInfo = unmarshalSignatureInfo("SignatureInfo_Base64_3.xml");
    
    Signature signature = new Signature(urlDereferencer, new IdValueFactoryImpl(), null, true);
    
    signature.setSignatureInfo(signatureInfo);
    
    Node parent = signature.getParent();
    Node nextSibling = signature.getNextSibling();

    assertNotNull(parent);
    assertTrue("XMLDocument".equals(parent.getLocalName()));
    
    assertNotNull(nextSibling);
    assertTrue("Paragraph".equals(nextSibling.getLocalName()));

  }

  @Test
  public void testSetSignatureInfo_XMLContent_1() throws JAXBException, SLCommandException, XMLStreamException {

    SignatureInfoCreationType signatureInfo = unmarshalSignatureInfo("SignatureInfo_XMLContent_1.xml");
    
    Signature signature = new Signature(urlDereferencer, new IdValueFactoryImpl(), null, false);
    
    signature.setSignatureInfo(signatureInfo);
    
    Node parent = signature.getParent();
    Node nextSibling = signature.getNextSibling();

    assertNotNull(parent);
    assertTrue("urn:document".equals(parent.getNamespaceURI()));
    assertTrue("Whole".equals(parent.getLocalName()));

    assertNull(nextSibling);

  }

  @Test
  public void testSetSignatureInfo_XMLContent_14_1() throws JAXBException, SLCommandException, XMLStreamException {

    SignatureInfoCreationType signatureInfo = unmarshalSignatureInfo("SignatureInfo_XMLContent_1.xml");
    
    Signature signature = new Signature(urlDereferencer, new IdValueFactoryImpl(), null, true);
    
    signature.setSignatureInfo(signatureInfo);
    
    Node parent = signature.getParent();
    Node nextSibling = signature.getNextSibling();

    assertNotNull(parent);
    assertTrue("urn:document".equals(parent.getNamespaceURI()));
    assertTrue("Whole".equals(parent.getLocalName()));

    assertNull(nextSibling);

  }

  @Test
  public void testSetSignature_Reference_1() throws JAXBException, SLCommandException, XMLStreamException {

    SignatureInfoCreationType signatureInfo = unmarshalSignatureInfo("SignatureInfo_Reference_1.xml");
    
    Signature signature = new Signature(urlDereferencer, new IdValueFactoryImpl(), null, false);
    
    signature.setSignatureInfo(signatureInfo);
    
    Node parent = signature.getParent();
    Node nextSibling = signature.getNextSibling();
    
    assertNotNull(parent);
    assertTrue("urn:document".equals(parent.getNamespaceURI()));
    assertTrue("Paragraph".equals(parent.getLocalName()));
    
    assertNull(nextSibling);
    
  }

  @Test
  public void testSetSignature_Reference_14_1() throws JAXBException, SLCommandException, XMLStreamException {

    SignatureInfoCreationType signatureInfo = unmarshalSignatureInfo("SignatureInfo_Reference_1.xml");
    
    Signature signature = new Signature(urlDereferencer, new IdValueFactoryImpl(), null, true);
    
    signature.setSignatureInfo(signatureInfo);
    
    Node parent = signature.getParent();
    Node nextSibling = signature.getNextSibling();
    
    assertNotNull(parent);
    assertTrue("urn:document".equals(parent.getNamespaceURI()));
    assertTrue("Paragraph".equals(parent.getLocalName()));
    
    assertNull(nextSibling);
    
  }

  //
  //
  // DataObject
  //
  //

  private List<DataObjectInfoType> unmarshalDataObjectInfo(String file) throws JAXBException, XMLStreamException {

    Object object = unmarshal(file);

    Object requestType = ((JAXBElement<?>) object).getValue();
    
    assertTrue(requestType instanceof CreateXMLSignatureRequestType);
    
    List<DataObjectInfoType> dataObjectInfos = ((CreateXMLSignatureRequestType) requestType).getDataObjectInfo();
    
    assertNotNull(dataObjectInfos);
    
    return dataObjectInfos;
    
  }

  private void signAndMarshalSignature(Signature signature) throws MarshalException, XMLSignatureException, SLCommandException, SLViewerException {

    Node parent = signature.getParent();
    Node nextSibling = signature.getNextSibling();
    
    DOMSignContext signContext = (nextSibling == null) 
      ? new DOMSignContext(privateKey, parent)
      : new DOMSignContext(privateKey, parent, nextSibling);
      
    signature.sign(signContext);
    
    Document document = signature.getDocument();
    
    DOMImplementationLS domImplLS = DOMUtils.getDOMImplementationLS();
    LSOutput output = domImplLS.createLSOutput();
    output.setByteStream(System.out);
    
    LSSerializer serializer = domImplLS.createLSSerializer();
    serializer.getDomConfig().setParameter("namespaces", Boolean.FALSE);
    serializer.write(document, output);
    
  }

  @Test
  public void testDataObject_Base64Content_1() throws JAXBException, SLCommandException, XMLStreamException, SLRequestException, MarshalException, XMLSignatureException, SLViewerException {

    List<DataObjectInfoType> dataObjectInfos = unmarshalDataObjectInfo("DataObjectInfo_Base64Content_1.xml");
    
    Signature signature = new Signature(urlDereferencer, new IdValueFactoryImpl(), new AlgorithmMethodFactoryImpl(), false);
    
    for (DataObjectInfoType dataObjectInfo : dataObjectInfos) {
      signature.addDataObject(dataObjectInfo);
    }
    
    signature.setSignerCertificate(certificate);
    
    signature.buildXMLSignature();
    
    signAndMarshalSignature(signature);
    
    List<Reference> references = signature.getReferences();
    assertTrue(references.size() == 2);

    Reference reference = references.get(0);
    assertNotNull(reference.getId());
    
    List<?> transforms = reference.getTransforms();
    assertTrue(transforms.size() == 1);
    
    Transform transform = (Transform) transforms.get(0);
    assertTrue(Transform.BASE64.equals(transform.getAlgorithm()));
  
    List<XMLObject> objects = signature.getXMLObjects();
    assertNotNull(objects);
    assertTrue("Size " + objects.size() + " but should be 2.", objects.size() == 2);
    
    XMLObject object = objects.get(0);
    
    assertTrue(("#" + object.getId()).equals(reference.getURI()));
    
  }

  @Test
  public void testDataObject_Base64Content_14_1() throws JAXBException, SLCommandException, XMLStreamException, SLRequestException, MarshalException, XMLSignatureException, SLViewerException {

    List<DataObjectInfoType> dataObjectInfos = unmarshalDataObjectInfo("DataObjectInfo_Base64Content_1.xml");
    
    Signature signature = new Signature(urlDereferencer, new IdValueFactoryImpl(), new AlgorithmMethodFactoryImpl(), true);
    
    for (DataObjectInfoType dataObjectInfo : dataObjectInfos) {
      signature.addDataObject(dataObjectInfo);
    }
    
    signature.setSignerCertificate(certificate);
    
    signature.buildXMLSignature();
    
    signAndMarshalSignature(signature);
    
    List<Reference> references = signature.getReferences();
    assertTrue(references.size() == 2);

    Reference reference = references.get(0);
    assertNotNull(reference.getId());
    
    List<?> transforms = reference.getTransforms();
    assertTrue(transforms.size() == 1);
    
    Transform transform = (Transform) transforms.get(0);
    assertTrue(Transform.BASE64.equals(transform.getAlgorithm()));
  
    List<XMLObject> objects = signature.getXMLObjects();
    assertNotNull(objects);
    assertTrue("Size " + objects.size() + " but should be 2.", objects.size() == 2);
    
    XMLObject object = objects.get(0);
    
    assertTrue(("#" + object.getId()).equals(reference.getURI()));
    
  }

  @Test
  public void testDataObject_XMLContent_1() throws JAXBException, SLCommandException, XMLStreamException, SLRequestException, MarshalException, XMLSignatureException, SLViewerException {

    System.out.println("\n ****************** testDataObject_XMLContent_1 \n");

    List<DataObjectInfoType> dataObjectInfos = unmarshalDataObjectInfo("DataObjectInfo_XMLContent_1.xml");
    
    Signature signature = new Signature(urlDereferencer, new IdValueFactoryImpl(), new AlgorithmMethodFactoryImpl(), false);
    
    for (DataObjectInfoType dataObjectInfo : dataObjectInfos) {
      signature.addDataObject(dataObjectInfo);
    }
    
    signature.setSignerCertificate(certificate);
    
    signature.buildXMLSignature();
    
    signAndMarshalSignature(signature);

    List<Reference> references = signature.getReferences();
    assertTrue(references.size() == 2);

    Reference reference = references.get(0);
    assertNotNull(reference.getId());
    
    List<?> transforms = reference.getTransforms();
    assertTrue(transforms.size() == 2);
    
    Transform transform = (Transform) transforms.get(0);
    assertTrue(Transform.XPATH2.equals(transform.getAlgorithm()));
  
    List<XMLObject> objects = signature.getXMLObjects();
    assertNotNull(objects);
    assertTrue("Size " + objects.size() + " but should be 2.", objects.size() == 2);
    
    XMLObject object = objects.get(0);
    
    assertTrue(("#" + object.getId()).equals(reference.getURI()));
    
  }

  @Test
  public void testDataObject_XMLContent_14_1() throws JAXBException, SLCommandException, XMLStreamException, SLRequestException, MarshalException, XMLSignatureException, SLViewerException {

    System.out.println("\n ****************** testDataObject_XMLContent_1 \n");

    List<DataObjectInfoType> dataObjectInfos = unmarshalDataObjectInfo("DataObjectInfo_XMLContent_1.xml");
    
    Signature signature = new Signature(urlDereferencer, new IdValueFactoryImpl(), new AlgorithmMethodFactoryImpl(), true);
    
    for (DataObjectInfoType dataObjectInfo : dataObjectInfos) {
      signature.addDataObject(dataObjectInfo);
    }
    
    signature.setSignerCertificate(certificate);
    
    signature.buildXMLSignature();
    
    signAndMarshalSignature(signature);

    List<Reference> references = signature.getReferences();
    assertTrue(references.size() == 2);

    Reference reference = references.get(0);
    assertNotNull(reference.getId());
    
    List<?> transforms = reference.getTransforms();
    assertTrue(transforms.size() == 2);
    
    Transform transform = (Transform) transforms.get(0);
    assertTrue(Transform.XPATH2.equals(transform.getAlgorithm()));
  
    List<XMLObject> objects = signature.getXMLObjects();
    assertNotNull(objects);
    assertTrue("Size " + objects.size() + " but should be 2.", objects.size() == 2);
    
    XMLObject object = objects.get(0);
    
    assertTrue(("#" + object.getId()).equals(reference.getURI()));
    
  }

  @Test
  public void testDataObject_XMLContent_2() throws JAXBException, SLCommandException, XMLStreamException, SLRequestException, MarshalException, XMLSignatureException, SLViewerException {

    System.out.println("\n ****************** testDataObject_XMLContent_2 \n");

    List<DataObjectInfoType> dataObjectInfos = unmarshalDataObjectInfo("DataObjectInfo_XMLContent_2.xml");
    
    Signature signature = new Signature(urlDereferencer, new IdValueFactoryImpl(), new AlgorithmMethodFactoryImpl(), false);
    
    for (DataObjectInfoType dataObjectInfo : dataObjectInfos) {
      signature.addDataObject(dataObjectInfo);
    }
    
    signature.setSignerCertificate(certificate);
    
    signature.buildXMLSignature();
    
    signAndMarshalSignature(signature);

    List<Reference> references = signature.getReferences();
    assertTrue(references.size() == 2);

    Reference reference = references.get(0);
    assertNotNull(reference.getId());
    
    List<?> transforms = reference.getTransforms();
    assertTrue(transforms.size() == 2);
    
    Transform transform = (Transform) transforms.get(0);
    assertTrue(Transform.XPATH2.equals(transform.getAlgorithm()));
  
    List<XMLObject> objects = signature.getXMLObjects();
    assertNotNull(objects);
    assertTrue("Size " + objects.size() + " but should be 2.", objects.size() == 2);
    
    XMLObject object = objects.get(0);
    
    assertTrue(("#" + object.getId()).equals(reference.getURI()));
    
  }

  @Test
  public void testDataObject_XMLContent_14_2() throws JAXBException, SLCommandException, XMLStreamException, SLRequestException, MarshalException, XMLSignatureException, SLViewerException {

    System.out.println("\n ****************** testDataObject_XMLContent_2 \n");

    List<DataObjectInfoType> dataObjectInfos = unmarshalDataObjectInfo("DataObjectInfo_XMLContent_2.xml");
    
    Signature signature = new Signature(urlDereferencer, new IdValueFactoryImpl(), new AlgorithmMethodFactoryImpl(), true);
    
    for (DataObjectInfoType dataObjectInfo : dataObjectInfos) {
      signature.addDataObject(dataObjectInfo);
    }
    
    signature.setSignerCertificate(certificate);
    
    signature.buildXMLSignature();
    
    signAndMarshalSignature(signature);

    List<Reference> references = signature.getReferences();
    assertTrue(references.size() == 2);

    Reference reference = references.get(0);
    assertNotNull(reference.getId());
    
    List<?> transforms = reference.getTransforms();
    assertTrue(transforms.size() == 2);
    
    Transform transform = (Transform) transforms.get(0);
    assertTrue(Transform.XPATH2.equals(transform.getAlgorithm()));
  
    List<XMLObject> objects = signature.getXMLObjects();
    assertNotNull(objects);
    assertTrue("Size " + objects.size() + " but should be 2.", objects.size() == 2);
    
    XMLObject object = objects.get(0);
    
    assertTrue(("#" + object.getId()).equals(reference.getURI()));
    
  }

  @Test
  public void testDataObject_LocRefContent_1() throws JAXBException, SLCommandException, XMLStreamException, SLRequestException, MarshalException, XMLSignatureException, SLViewerException {

    System.out.println("\n ****************** testDataObject_LocRefContent_1 \n");

    List<DataObjectInfoType> dataObjectInfos = unmarshalDataObjectInfo("DataObjectInfo_LocRefContent_1.xml");
    
    Signature signature = new Signature(urlDereferencer, new IdValueFactoryImpl(), new AlgorithmMethodFactoryImpl(), false);
    
    for (DataObjectInfoType dataObjectInfo : dataObjectInfos) {
      signature.addDataObject(dataObjectInfo);
    }
    
    signature.buildXMLSignature();

    signAndMarshalSignature(signature);

    List<Reference> references = signature.getReferences();
    assertTrue(references.size() == 2);

    Reference reference = references.get(0);
    assertNotNull(reference.getId());
    
    List<?> transforms = reference.getTransforms();
    assertTrue(transforms.size() == 2);
    
    Transform transform = (Transform) transforms.get(0);
    assertTrue(Transform.XPATH2.equals(transform.getAlgorithm()));
  
    List<XMLObject> objects = signature.getXMLObjects();
    assertNotNull(objects);
    assertTrue("Size " + objects.size() + " but should be 2.", objects.size() == 2);
    
    XMLObject object = objects.get(0);
    
    assertTrue(("#" + object.getId()).equals(reference.getURI()));
    
  }

  @Test
  public void testDataObject_LocRefContent_14_1() throws JAXBException, SLCommandException, XMLStreamException, SLRequestException, MarshalException, XMLSignatureException, SLViewerException {

    System.out.println("\n ****************** testDataObject_LocRefContent_1 \n");

    List<DataObjectInfoType> dataObjectInfos = unmarshalDataObjectInfo("DataObjectInfo_LocRefContent_1.xml");
    
    Signature signature = new Signature(urlDereferencer, new IdValueFactoryImpl(), new AlgorithmMethodFactoryImpl(), true);
    
    for (DataObjectInfoType dataObjectInfo : dataObjectInfos) {
      signature.addDataObject(dataObjectInfo);
    }
    
    signature.buildXMLSignature();

    signAndMarshalSignature(signature);

    List<Reference> references = signature.getReferences();
    assertTrue(references.size() == 2);

    Reference reference = references.get(0);
    assertNotNull(reference.getId());
    
    List<?> transforms = reference.getTransforms();
    assertTrue(transforms.size() == 2);
    
    Transform transform = (Transform) transforms.get(0);
    assertTrue(Transform.XPATH2.equals(transform.getAlgorithm()));
  
    List<XMLObject> objects = signature.getXMLObjects();
    assertNotNull(objects);
    assertTrue("Size " + objects.size() + " but should be 2.", objects.size() == 2);
    
    XMLObject object = objects.get(0);
    
    assertTrue(("#" + object.getId()).equals(reference.getURI()));
    
  }

  @Test
  public void testDataObject_LocRefContent_2() throws JAXBException, SLCommandException, XMLStreamException, SLRequestException, MarshalException, XMLSignatureException, SLViewerException {

    System.out.println("\n ****************** testDataObject_LocRefContent_2 \n");

    List<DataObjectInfoType> dataObjectInfos = unmarshalDataObjectInfo("DataObjectInfo_LocRefContent_2.xml");
    
    Signature signature = new Signature(urlDereferencer, new IdValueFactoryImpl(), new AlgorithmMethodFactoryImpl(), false);
    
    for (DataObjectInfoType dataObjectInfo : dataObjectInfos) {
      signature.addDataObject(dataObjectInfo);
    }
    
    signature.buildXMLSignature();
    
    signAndMarshalSignature(signature);
    
    List<Reference> references = signature.getReferences();
    assertTrue(references.size() == 2);

    Reference reference = references.get(0);
    assertNotNull(reference.getId());
    
    List<?> transforms = reference.getTransforms();
    assertTrue(transforms.size() == 1);
    
    Transform transform = (Transform) transforms.get(0);
    assertTrue(Transform.BASE64.equals(transform.getAlgorithm()));
  
    List<XMLObject> objects = signature.getXMLObjects();
    assertNotNull(objects);
    assertTrue("Size " + objects.size() + " but should be 2.", objects.size() == 2);
    
    XMLObject object = objects.get(0);
    
    assertTrue(("#" + object.getId()).equals(reference.getURI()));
    
  }

  @Test
  public void testDataObject_LocRefContent_14_2() throws JAXBException, SLCommandException, XMLStreamException, SLRequestException, MarshalException, XMLSignatureException, SLViewerException {

    System.out.println("\n ****************** testDataObject_LocRefContent_2 \n");

    List<DataObjectInfoType> dataObjectInfos = unmarshalDataObjectInfo("DataObjectInfo_LocRefContent_2.xml");
    
    Signature signature = new Signature(urlDereferencer, new IdValueFactoryImpl(), new AlgorithmMethodFactoryImpl(), true);
    
    for (DataObjectInfoType dataObjectInfo : dataObjectInfos) {
      signature.addDataObject(dataObjectInfo);
    }
    
    signature.buildXMLSignature();
    
    signAndMarshalSignature(signature);
    
    List<Reference> references = signature.getReferences();
    assertTrue(references.size() == 2);

    Reference reference = references.get(0);
    assertNotNull(reference.getId());
    
    List<?> transforms = reference.getTransforms();
    assertTrue(transforms.size() == 1);
    
    Transform transform = (Transform) transforms.get(0);
    assertTrue(Transform.BASE64.equals(transform.getAlgorithm()));
  
    List<XMLObject> objects = signature.getXMLObjects();
    assertNotNull(objects);
    assertTrue("Size " + objects.size() + " but should be 2.", objects.size() == 2);
    
    XMLObject object = objects.get(0);
    
    assertTrue(("#" + object.getId()).equals(reference.getURI()));
    
  }

  @Test
  public void testDataObject_Reference_1() throws JAXBException, SLCommandException, XMLStreamException, SLRequestException, MarshalException, XMLSignatureException, SLViewerException {

    System.out.println("\n ****************** testDataObject_Reference_1 \n");

    List<DataObjectInfoType> dataObjectInfos = unmarshalDataObjectInfo("DataObjectInfo_Reference_1.xml");
    
    Signature signature = new Signature(urlDereferencer, new IdValueFactoryImpl(), new AlgorithmMethodFactoryImpl(), false);
    
    for (DataObjectInfoType dataObjectInfo : dataObjectInfos) {
      signature.addDataObject(dataObjectInfo);
    }
    
    signature.buildXMLSignature();
    
    signAndMarshalSignature(signature);
    
    List<Reference> references = signature.getReferences();
    assertTrue(references.size() == 2);

    Reference reference = references.get(0);
    assertNotNull(reference.getId());
    
    List<?> transforms = reference.getTransforms();
    assertTrue(transforms.size() == 1);
    
    Transform transform = (Transform) transforms.get(0);
    assertTrue(Transform.BASE64.equals(transform.getAlgorithm()));
  
    List<XMLObject> objects = signature.getXMLObjects();
    assertNotNull(objects);
    assertTrue("Size " + objects.size() + " but should be 2.", objects.size() == 2);
    
    XMLObject object = objects.get(0);
    
    assertTrue(("#" + object.getId()).equals(reference.getURI()));
    
  }

  @Test
  public void testDataObject_Reference_14_1() throws JAXBException, SLCommandException, XMLStreamException, SLRequestException, MarshalException, XMLSignatureException, SLViewerException {

    System.out.println("\n ****************** testDataObject_Reference_1 \n");

    List<DataObjectInfoType> dataObjectInfos = unmarshalDataObjectInfo("DataObjectInfo_Reference_1.xml");
    
    Signature signature = new Signature(urlDereferencer, new IdValueFactoryImpl(), new AlgorithmMethodFactoryImpl(), true);
    
    for (DataObjectInfoType dataObjectInfo : dataObjectInfos) {
      signature.addDataObject(dataObjectInfo);
    }
    
    signature.buildXMLSignature();
    
    signAndMarshalSignature(signature);
    
    List<Reference> references = signature.getReferences();
    assertTrue(references.size() == 2);

    Reference reference = references.get(0);
    assertNotNull(reference.getId());
    
    List<?> transforms = reference.getTransforms();
    assertTrue(transforms.size() == 1);
    
    Transform transform = (Transform) transforms.get(0);
    assertTrue(Transform.BASE64.equals(transform.getAlgorithm()));
  
    List<XMLObject> objects = signature.getXMLObjects();
    assertNotNull(objects);
    assertTrue("Size " + objects.size() + " but should be 2.", objects.size() == 2);
    
    XMLObject object = objects.get(0);
    
    assertTrue(("#" + object.getId()).equals(reference.getURI()));
    
  }

  @Test
  public void testDataObject_Detached_1() throws JAXBException, SLCommandException, XMLStreamException, SLRequestException, MarshalException, XMLSignatureException, SLViewerException {

    System.out.println("\n ****************** testDataObject_Detached_1 \n");

    List<DataObjectInfoType> dataObjectInfos = unmarshalDataObjectInfo("DataObjectInfo_Detached_1.xml");
    
    Signature signature = new Signature(urlDereferencer, new IdValueFactoryImpl(), new AlgorithmMethodFactoryImpl(), false);
    
    for (DataObjectInfoType dataObjectInfo : dataObjectInfos) {
      signature.addDataObject(dataObjectInfo);
    }
    
    signature.buildXMLSignature();
    
    signAndMarshalSignature(signature);
    
    List<Reference> references = signature.getReferences();
    assertTrue(references.size() == 2);

    Reference reference = references.get(0);
    assertNotNull(reference.getId());
    
    List<?> transforms = reference.getTransforms();
    assertTrue(transforms.size() == 0);
    
    List<XMLObject> objects = signature.getXMLObjects();
    assertNotNull(objects);
    assertTrue(objects.size() == 1);
    
  }

  @Test
  public void testDataObject_Detached_14_1() throws JAXBException, SLCommandException, XMLStreamException, SLRequestException, MarshalException, XMLSignatureException, SLViewerException {

    System.out.println("\n ****************** testDataObject_Detached_1 \n");

    List<DataObjectInfoType> dataObjectInfos = unmarshalDataObjectInfo("DataObjectInfo_Detached_1.xml");
    
    Signature signature = new Signature(urlDereferencer, new IdValueFactoryImpl(), new AlgorithmMethodFactoryImpl(), true);
    
    for (DataObjectInfoType dataObjectInfo : dataObjectInfos) {
      signature.addDataObject(dataObjectInfo);
    }
    
    signature.buildXMLSignature();
    
    signAndMarshalSignature(signature);
    
    List<Reference> references = signature.getReferences();
    assertTrue(references.size() == 2);

    Reference reference = references.get(0);
    assertNotNull(reference.getId());
    
    List<?> transforms = reference.getTransforms();
    assertTrue(transforms.size() == 0);
    
    List<XMLObject> objects = signature.getXMLObjects();
    assertNotNull(objects);
    assertTrue(objects.size() == 1);
    
  }

  @Test
  public void testDataObject_Detached_Base64Content() throws JAXBException, SLCommandException, XMLStreamException, SLRequestException, MarshalException, XMLSignatureException, SLViewerException {

    System.out.println("\n ****************** testDataObject_Detached_Base64Content \n");

    List<DataObjectInfoType> dataObjectInfos = unmarshalDataObjectInfo("DataObjectInfo_Detached_Base64Content.xml");
    
    Signature signature = new Signature(urlDereferencer, new IdValueFactoryImpl(), new AlgorithmMethodFactoryImpl(), false);
    
    for (DataObjectInfoType dataObjectInfo : dataObjectInfos) {
      signature.addDataObject(dataObjectInfo);
    }
    
    signature.buildXMLSignature();
    
    signAndMarshalSignature(signature);
    
    List<?> references = signature.getReferences();
    assertTrue(references.size() == 2);

    Reference reference = (Reference) references.get(0);
    assertNotNull(reference.getId());
    
    List<?> transforms = reference.getTransforms();
    assertTrue(transforms.size() == 0);
    
    List<XMLObject> objects = signature.getXMLObjects();
    assertNotNull(objects);
    assertTrue(objects.size() == 1);
    
  }

  @Test
  public void testDataObject_Detached_Base64Content_14() throws JAXBException, SLCommandException, XMLStreamException, SLRequestException, MarshalException, XMLSignatureException, SLViewerException {

    System.out.println("\n ****************** testDataObject_Detached_Base64Content \n");

    List<DataObjectInfoType> dataObjectInfos = unmarshalDataObjectInfo("DataObjectInfo_Detached_Base64Content.xml");
    
    Signature signature = new Signature(urlDereferencer, new IdValueFactoryImpl(), new AlgorithmMethodFactoryImpl(), true);
    
    for (DataObjectInfoType dataObjectInfo : dataObjectInfos) {
      signature.addDataObject(dataObjectInfo);
    }
    
    signature.buildXMLSignature();
    
    signAndMarshalSignature(signature);
    
    List<?> references = signature.getReferences();
    assertTrue(references.size() == 2);

    Reference reference = (Reference) references.get(0);
    assertNotNull(reference.getId());
    
    List<?> transforms = reference.getTransforms();
    assertTrue(transforms.size() == 0);
    
    List<XMLObject> objects = signature.getXMLObjects();
    assertNotNull(objects);
    assertTrue(objects.size() == 1);
    
  }

  @Test
  public void testDataObject_Detached_LocRefContent() throws JAXBException, SLCommandException, XMLStreamException, SLRequestException, MarshalException, XMLSignatureException, SLViewerException {

    System.out.println("\n ****************** testDataObject_Detached_LocRefContent \n");

    List<DataObjectInfoType> dataObjectInfos = unmarshalDataObjectInfo("DataObjectInfo_Detached_LocRefContent.xml");

    Signature signature = new Signature(urlDereferencer, new IdValueFactoryImpl(), new AlgorithmMethodFactoryImpl(), false);

    for (DataObjectInfoType dataObjectInfo : dataObjectInfos) {
      signature.addDataObject(dataObjectInfo);
    }

    signature.buildXMLSignature();

    signAndMarshalSignature(signature);

    List<Reference> references = signature.getReferences();
    assertTrue(references.size() == 2);

    Reference reference = references.get(0);
    assertNotNull(reference.getId());

    List<?> transforms = reference.getTransforms();
    assertTrue(transforms.size() == 0);

    List<XMLObject> objects = signature.getXMLObjects();
    assertNotNull(objects);
    assertTrue(objects.size() == 1);

  }

  @Test
  public void testDataObject_Detached_LocRefContent_14() throws JAXBException, SLCommandException, XMLStreamException, SLRequestException, MarshalException, XMLSignatureException, SLViewerException {

    System.out.println("\n ****************** testDataObject_Detached_LocRefContent \n");

    List<DataObjectInfoType> dataObjectInfos = unmarshalDataObjectInfo("DataObjectInfo_Detached_LocRefContent.xml");

    Signature signature = new Signature(urlDereferencer, new IdValueFactoryImpl(), new AlgorithmMethodFactoryImpl(), true);

    for (DataObjectInfoType dataObjectInfo : dataObjectInfos) {
      signature.addDataObject(dataObjectInfo);
    }

    signature.buildXMLSignature();

    signAndMarshalSignature(signature);

    List<Reference> references = signature.getReferences();
    assertTrue(references.size() == 2);

    Reference reference = references.get(0);
    assertNotNull(reference.getId());

    List<?> transforms = reference.getTransforms();
    assertTrue(transforms.size() == 0);

    List<XMLObject> objects = signature.getXMLObjects();
    assertNotNull(objects);
    assertTrue(objects.size() == 1);

  }

  //
  //
  // TransformsInfo
  //
  //
  
  private CreateXMLSignatureRequestType unmarshalCreateXMLSignatureRequest(String file) throws JAXBException, XMLStreamException {

    Object object = unmarshal(file);

    Object requestType = ((JAXBElement<?>) object).getValue();
    
    assertTrue(requestType instanceof CreateXMLSignatureRequestType);
    
    return (CreateXMLSignatureRequestType) requestType;
    
  }

  
  @Test
  public void testTransformsInfo_1() throws JAXBException, SLCommandException, XMLStreamException, SLRequestException, MarshalException, XMLSignatureException, SLViewerException {

    CreateXMLSignatureRequestType requestType = unmarshalCreateXMLSignatureRequest("TransformsInfo_1.xml");
    
    Signature signature = new Signature(urlDereferencer, new IdValueFactoryImpl(), new AlgorithmMethodFactoryImpl(), false);


    signature.setSignatureInfo(requestType.getSignatureInfo());
    
    List<DataObjectInfoType> dataObjectInfos = requestType.getDataObjectInfo();
    
    for (DataObjectInfoType dataObjectInfo : dataObjectInfos) {
      signature.addDataObject(dataObjectInfo);
    }
    
    signature.setSignerCertificate(certificate);
    
    signature.buildXMLSignature();
    
    signAndMarshalSignature(signature);
    
    List<Reference> references = signature.getReferences();
    assertTrue(references.size() == 2);

    Reference reference = references.get(0);
    assertNotNull(reference.getId());
    
    List<?> transforms = reference.getTransforms();
    assertTrue("Size " + transforms.size() + "", transforms.size() == 3);
    
    Transform transform = (Transform) transforms.get(0);
    assertTrue(Transform.ENVELOPED.equals(transform.getAlgorithm()));
  
    List<XMLObject> objects = signature.getXMLObjects();
    assertNotNull(objects);
    assertTrue("Size " + objects.size() + " but should be 1.", objects.size() == 1);
    
  }

  @Test
  public void testTransformsInfo_14_1() throws JAXBException, SLCommandException, XMLStreamException, SLRequestException, MarshalException, XMLSignatureException, SLViewerException {

    CreateXMLSignatureRequestType requestType = unmarshalCreateXMLSignatureRequest("TransformsInfo_1.xml");
    
    Signature signature = new Signature(urlDereferencer, new IdValueFactoryImpl(), new AlgorithmMethodFactoryImpl(), true);


    signature.setSignatureInfo(requestType.getSignatureInfo());
    
    List<DataObjectInfoType> dataObjectInfos = requestType.getDataObjectInfo();
    
    for (DataObjectInfoType dataObjectInfo : dataObjectInfos) {
      signature.addDataObject(dataObjectInfo);
    }
    
    signature.setSignerCertificate(certificate);
    
    signature.buildXMLSignature();
    
    signAndMarshalSignature(signature);
    
    List<Reference> references = signature.getReferences();
    assertTrue(references.size() == 2);

    Reference reference = references.get(0);
    assertNotNull(reference.getId());
    
    List<?> transforms = reference.getTransforms();
    assertTrue("Size " + transforms.size() + "", transforms.size() == 3);
    
    Transform transform = (Transform) transforms.get(0);
    assertTrue(Transform.ENVELOPED.equals(transform.getAlgorithm()));
  
    List<XMLObject> objects = signature.getXMLObjects();
    assertNotNull(objects);
    assertTrue("Size " + objects.size() + " but should be 1.", objects.size() == 1);
    
  }

  @Test
  public void testTransformsInfo_2() throws JAXBException, SLCommandException, XMLStreamException, SLRequestException, MarshalException, XMLSignatureException, SLViewerException {

    CreateXMLSignatureRequestType requestType = unmarshalCreateXMLSignatureRequest("TransformsInfo_2.xml");

    Signature signature = new Signature(urlDereferencer, new IdValueFactoryImpl(), new AlgorithmMethodFactoryImpl(), false);


    signature.setSignatureInfo(requestType.getSignatureInfo());

    List<DataObjectInfoType> dataObjectInfos = requestType.getDataObjectInfo();

    for (DataObjectInfoType dataObjectInfo : dataObjectInfos) {
      signature.addDataObject(dataObjectInfo);
    }

    signature.setSignerCertificate(certificate);

    signature.buildXMLSignature();

    signAndMarshalSignature(signature);

    List<Reference> references = signature.getReferences();
    assertTrue(references.size() == 2);

    Reference reference = references.get(0);
    assertNotNull(reference.getId());

    List<?> transforms = reference.getTransforms();
    assertTrue("Size " + transforms.size() + "", transforms.size() == 2);

    Transform transform = (Transform) transforms.get(0);
    assertTrue(Transform.XSLT.equals(transform.getAlgorithm()));

    List<XMLObject> objects = signature.getXMLObjects();
    assertNotNull(objects);
    assertTrue("Size " + objects.size() + " but should be 1.", objects.size() == 1);

  }

  @Test
  public void testTransformsInfo_14_2() throws JAXBException, SLCommandException, XMLStreamException, SLRequestException, MarshalException, XMLSignatureException, SLViewerException {

    CreateXMLSignatureRequestType requestType = unmarshalCreateXMLSignatureRequest("TransformsInfo_2.xml");

    Signature signature = new Signature(urlDereferencer, new IdValueFactoryImpl(), new AlgorithmMethodFactoryImpl(), true);


    signature.setSignatureInfo(requestType.getSignatureInfo());

    List<DataObjectInfoType> dataObjectInfos = requestType.getDataObjectInfo();

    for (DataObjectInfoType dataObjectInfo : dataObjectInfos) {
      signature.addDataObject(dataObjectInfo);
    }

    signature.setSignerCertificate(certificate);

    signature.buildXMLSignature();

    signAndMarshalSignature(signature);

    List<Reference> references = signature.getReferences();
    assertTrue(references.size() == 2);

    Reference reference = references.get(0);
    assertNotNull(reference.getId());

    List<?> transforms = reference.getTransforms();
    assertTrue("Size " + transforms.size() + "", transforms.size() == 2);

    Transform transform = (Transform) transforms.get(0);
    assertTrue(Transform.XSLT.equals(transform.getAlgorithm()));

    List<XMLObject> objects = signature.getXMLObjects();
    assertNotNull(objects);
    assertTrue("Size " + objects.size() + " but should be 1.", objects.size() == 1);

  }
}
