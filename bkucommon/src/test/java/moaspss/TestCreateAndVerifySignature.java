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


package moaspss;

import static junit.framework.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Iterator;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import moaspss.generated.KeyInfoType;
import moaspss.generated.VerifyXMLSignatureResponseType;
import moaspss.generated.X509DataType;

import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import at.buergerkarte.namespaces.securitylayer._1_2_3.ErrorResponseType;

@Ignore
public class TestCreateAndVerifySignature {

  protected Element parseCreateXMLSignatureRequest(InputStream is)
      throws ParserConfigurationException, SAXException, IOException {

    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    Document doc;
    DocumentBuilder db = dbf.newDocumentBuilder();
    doc = db.parse(is);

    Element docElem = doc.getDocumentElement();
    if ("http://www.buergerkarte.at/namespaces/securitylayer/1.2#".equals(docElem.getNamespaceURI())
        && "CreateXMLSignatureRequest".equals(docElem.getLocalName())) {
      return docElem;
    } else {
      return null;
    }

  }
  
  protected DocumentFragment getXMLSignatureFromResponse(String xpath, Object response) throws SLException, JAXBException {

    if (response instanceof Element) {
      
      Element respElem = (Element) response;
      if ("http://www.buergerkarte.at/namespaces/securitylayer/1.2#".equals(respElem.getNamespaceURI())
          && "CreateXMLSignatureResponse".equals(respElem.getLocalName())) {

        NodeList childNodes = respElem.getChildNodes();
        
        Document doc = respElem.getOwnerDocument();
        DocumentFragment fragment = doc.createDocumentFragment();
        
        for (int i = 0; i < childNodes.getLength(); i++) {
          fragment.appendChild(childNodes.item(i));
        }
        
        return fragment;
        
      } else {
        Unmarshaller unmarshaller = SLClient.getJAXBContext().createUnmarshaller();
        Object obj = unmarshaller.unmarshal(respElem);
        
        if (obj instanceof JAXBElement<?>) {
            JAXBElement<?> element = (JAXBElement<?>) obj;
            if (element.getValue() instanceof ErrorResponseType) {
                ErrorResponseType error = (ErrorResponseType) element.getValue();
                throw new SLException(error.getErrorCode(), error.getInfo());
            }
        }
      }
    }
    
    return null;
    
  }

  public X509Certificate getCertFromKeyInfo(KeyInfoType keyInfo) throws CertificateException {

    Iterator<Object> keyInfos = keyInfo.getContent().iterator();
    while (keyInfos.hasNext()) {
      Object ki = keyInfos.next();
      if (ki instanceof JAXBElement<?>
          && X509DataType.class.isAssignableFrom(((JAXBElement<?>) ki)
              .getDeclaredType())) {
        X509DataType x509data = (X509DataType) ((JAXBElement<?>) ki).getValue();
        Iterator<Object> contents = x509data
            .getX509IssuerSerialOrX509SKIOrX509SubjectName().iterator();
        while (contents.hasNext()) {
          Object content = (Object) contents.next();
          if (byte[].class.isAssignableFrom(((JAXBElement<?>) content)
              .getDeclaredType())) {
            CertificateFactory certFactory = CertificateFactory.getInstance("X509");
            byte[] b = (byte[]) ((JAXBElement<?>) content).getValue();
            return (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(b));
          }
        }

      }
    }
    
    return null;

  }
  
  @Test
  public void testCreateAndVerifyXMLSignature()
      throws ParserConfigurationException, SAXException, IOException,
      TransformerException, JAXBException, ClassCastException,
      ClassNotFoundException, InstantiationException, IllegalAccessException, CertificateException {
    
    ClassLoader cl = TestCreateAndVerifySignature.class.getClassLoader();
    InputStream is = cl.getResourceAsStream("moaspss/CreateXMLSignatureRequest.xml");
    Element cxsReq = parseCreateXMLSignatureRequest(is);
    
    Node cxsResp;
    try {
      SLClient slClient = new SLClient();
      Object response = slClient.submitRequest(cxsReq, Element.class);
      cxsResp = getXMLSignatureFromResponse(".", response);
    } catch (SLException e) {
      fail(e.getMessage());
      return;
    }

    MOASPClient spClient = new MOASPClient();
    JAXBElement<VerifyXMLSignatureResponseType> verifySignature = spClient.verifySignature(cxsResp, ".", "qualifiedSignature");
    VerifyXMLSignatureResponseType vxsResp = verifySignature.getValue();
    int signatureCheck = vxsResp.getSignatureCheck().getCode().intValue();
    if (signatureCheck != 0) {
      fail("SignatureCheck = " + signatureCheck);
    }
    int certificateCheck = vxsResp.getCertificateCheck().getCode().intValue();
    if (certificateCheck != 0) {
      
      X509Certificate certificate = getCertFromKeyInfo(vxsResp.getSignerInfo());
      if (certificate != null) {
        System.out.println(certificate);
      }
      
      fail("CertificateCheck = " + certificateCheck);
    }

  }
  
}
