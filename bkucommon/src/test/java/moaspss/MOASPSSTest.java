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
package moaspss;

import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import moaspss.generated.ContentOptionalRefType;
import moaspss.generated.InputDataType;
import moaspss.generated.MOAFault;
import moaspss.generated.ObjectFactory;
import moaspss.generated.SignatureVerificationPortType;
import moaspss.generated.SignatureVerificationService;
import moaspss.generated.VerifyXMLSignatureRequestType;
import moaspss.generated.VerifyXMLSignatureResponseType;
import moaspss.generated.VerifyXMLSignatureRequestType.VerifySignatureInfo;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class MOASPSSTest {

  public static final String REQ_FILE = "TODO.xml";
  private static JAXBContext ctx;
  private static SignatureVerificationPortType port;
  
  @BeforeClass
  public static void setUp() throws JAXBException, MalformedURLException {
    QName serviceName = new QName("http://reference.e-government.gv.at/namespace/moa/wsdl/20020822#", "SignatureVerificationService");
    
    URL wsdlURL = MOASPSSTest.class.getClassLoader().getResource("MOA-SPSS-1.3.wsdl");
    
    assertNotNull(wsdlURL);
    
    SignatureVerificationService service = new SignatureVerificationService(wsdlURL, serviceName);
    
    port = service.getSignatureVerificationPort();
    assertNotNull(port);
    ctx = JAXBContext.newInstance(VerifyXMLSignatureRequestType.class.getPackage().getName());
  }
  
  @Test
  public void verifySignature() throws JAXBException, IOException {
    
    InputStream stream = MOASPSSTest.class.getClassLoader().getResourceAsStream("Untitled1.xml");
    assertNotNull(stream);
    
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    int b;
    while ((b = stream.read()) != -1) {
      bos.write(b);
    }
    stream.close();
    
    ObjectFactory factory = new ObjectFactory();

    ContentOptionalRefType contentOptionalRefType = factory.createContentOptionalRefType();
    contentOptionalRefType.setBase64Content(bos.toByteArray());

    VerifySignatureInfo verifySignatureInfo = factory.createVerifyXMLSignatureRequestTypeVerifySignatureInfo();
    verifySignatureInfo.setVerifySignatureEnvironment(contentOptionalRefType);
    verifySignatureInfo.setVerifySignatureLocation("/child::*[1]/child::*[2]");
    
    VerifyXMLSignatureRequestType verifyXMLSignatureRequestType = factory.createVerifyXMLSignatureRequestType();
    verifyXMLSignatureRequestType.setVerifySignatureInfo(verifySignatureInfo);
    verifyXMLSignatureRequestType.setTrustProfileID("IdentityLink");
    verifyXMLSignatureRequestType.setReturnHashInputData(Boolean.TRUE);
    
    VerifyXMLSignatureResponseType resp = null;
    try {
      resp = port.verifyXMLSignature(verifyXMLSignatureRequestType);
    } catch (MOAFault e) {
      e.printStackTrace();
    }
    
    JAXBElement<VerifyXMLSignatureResponseType> verifyXMLSignatureResponse = factory.createVerifyXMLSignatureResponse(resp);
    
    Marshaller marshaller = ctx.createMarshaller();
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    marshaller.marshal(verifyXMLSignatureResponse, System.out);
    
    List<InputDataType> hashInputData = resp.getHashInputData();
    for (InputDataType inputDataType : hashInputData) {
      System.out.println("------------------------------------------");
      System.out.println("HashInputData: " + inputDataType.getPartOf() + " " + inputDataType.getReferringSigReference());
      System.out.println("------------------------------------------");
      System.out.write(inputDataType.getBase64Content());
      System.out.println();
    }
  }
}
