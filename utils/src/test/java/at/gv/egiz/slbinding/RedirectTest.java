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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.gv.egiz.slbinding;

import at.buergerkarte.namespaces.securitylayer._1.Base64XMLLocRefOptRefContentType;
import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;

import at.buergerkarte.namespaces.securitylayer._1.CreateXMLSignatureRequestType;
import at.buergerkarte.namespaces.securitylayer._1.DataObjectAssociationType;
import at.buergerkarte.namespaces.securitylayer._1.DataObjectInfoType;
import at.buergerkarte.namespaces.securitylayer._1.MetaInfoType;
import at.buergerkarte.namespaces.securitylayer._1.SignatureInfoCreationType;
import at.buergerkarte.namespaces.securitylayer._1.TransformsInfoType;
import at.gv.egiz.slbinding.impl.SignatureLocationType;
import at.gv.egiz.slbinding.impl.XMLContentType;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;

import static org.junit.Assert.*;
import org.w3._2000._09.xmldsig_.TransformType;
import org.w3._2000._09.xmldsig_.TransformsType;

/**
 *
 * @author clemens
 */
public class RedirectTest {

    public static final String FILENAME_REQ = "src/test/requests/CreateXMLSignatureRequest02.xml";
    public static final String FILENAME_REQ_SCHEMA = "src/main/schema/Core-1.2.xsd";

    /**
     * Context path for unmarshaller (colon separated list of generated packages)
     */
    @Before
    public void setUp() throws JAXBException {
    }

    @Test
    public void testRedirect() {
        try {
            String slPkg = at.buergerkarte.namespaces.securitylayer._1.ObjectFactory.class.getPackage().getName();
            String dsigPkg = org.w3._2000._09.xmldsig_.ObjectFactory.class.getPackage().getName();

            JAXBContext jaxbContext = JAXBContext.newInstance(slPkg + ":" + dsigPkg);
            Unmarshaller um = jaxbContext.createUnmarshaller();

//            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
//            File schemaFile = new File(FILENAME_REQ_SCHEMA);
//            Schema TestRequestLaxSchema = schemaFactory.newSchema(schemaFile);
//            // validate request
//            um.setSchema(TestRequestLaxSchema);


            FileInputStream fis = new FileInputStream(FILENAME_REQ);
            InputStream is = new BufferedInputStream(fis);

            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLEventReader reader = inputFactory.createXMLEventReader(is);
            final RedirectEventFilter contentFilter = new RedirectEventFilter();
            XMLEventReader filteredReader = inputFactory.createFilteredReader(reader, contentFilter);

            um.setListener(new RedirectUnmarshallerListener(contentFilter));

//            List<Class> redirectTriggers = Arrays.asList(new Class[]{XMLContentType.class, TransformsType.class});
//            Set<Class<? extends RedirectCallback>> redirectTriggers = new HashSet<Class<? extends RedirectCallback>>(); //{XMLContentType.class, TransformsType.class
//            redirectTriggers.add(XMLContentType.class);
//            redirectTriggers.add(TransformsType.class);
//            ByteArrayRedirectCallback.registerRedirectTriggers(redirectTriggers);
//
//            Set<Class<? extends RedirectCallback>> preserveNSContextTriggers = new HashSet<Class<? extends RedirectCallback>>();
////            preserveNSContextTriggers.add(TransformsType.class);
//            preserveNSContextTriggers.add(SignatureInfoCreationType.SignatureLocation.class);
//            ByteArrayRedirectCallback.registerPreserveContextTriggers(preserveNSContextTriggers);

            JAXBElement<CreateXMLSignatureRequestType> req = (JAXBElement<CreateXMLSignatureRequestType>) um.unmarshal(filteredReader);
            is.close();

            FileOutputStream fos = new FileOutputStream(FILENAME_REQ + "_redirect.txt");
            OutputStream os = new BufferedOutputStream(fos);

            CreateXMLSignatureRequestType request = req.getValue();
            List<DataObjectInfoType> dataObjectInfos = request.getDataObjectInfo();
            Iterator<DataObjectInfoType> doiIt = dataObjectInfos.iterator();
            while (doiIt.hasNext()) {
                DataObjectInfoType doi = doiIt.next();
                Base64XMLLocRefOptRefContentType dataObj = doi.getDataObject();
                XMLContentType dataObjXML = (XMLContentType) dataObj.getXMLContent();
                if (dataObjXML != null) {
                    System.out.println("found at.gv.egiz.slbinding.impl.XMLContentType DataObject");
                    ByteArrayOutputStream xmlContent = dataObjXML.getRedirectedStream();
                    assertNotNull(xmlContent);
                    os.write(xmlContent.toByteArray());
                    os.write("\n\n\n".getBytes());
                }

                List<TransformsInfoType> transformsInfos = doi.getTransformsInfo();
                Iterator<TransformsInfoType> tiIt = transformsInfos.iterator();
                while (tiIt.hasNext()) {
                    at.gv.egiz.slbinding.impl.TransformsInfoType ti = (at.gv.egiz.slbinding.impl.TransformsInfoType) tiIt.next();
//                    TransformsInfoType ti = tiIt.next();
                    assertNotNull(ti);
                    System.out.println("found sl:TransformsInfo: " + ti.getClass().getName()); //at.gv.egiz.slbinding.impl.TransformsInfoType TransformsInfo");
//                    TransformsType ts = ti.getTransforms();
//                    assertNotNull(ts);
//                    System.out.println("found dsig:Transforms " + ts.getClass().getName()); //org.w3._2000._09.xmldsig_.TransformsType dsig:Transforms");
//                    List<TransformType> tL = ts.getTransform();
//                    assertNotNull(tL);
//                    System.out.println("found " + tL.size() + " org.w3._2000._09.xmldsig_.TransformType dsig:Transform");
//                    for (TransformType t : tL) {
//                      if (t instanceof at.gv.egiz.slbinding.impl.TransformType) {
//                        System.out.println("found at.gv.egiz.slbinding.impl.TransformType");
//                        byte[] redirectedBytes = ((at.gv.egiz.slbinding.impl.TransformType) t).getRedirectedStream().toByteArray();
//                        if (redirectedBytes != null && redirectedBytes.length > 0) {
//                          System.out.println("reading redirected stream...");
//                          os.write("--- redirected Transform ---".getBytes());
//                          os.write(redirectedBytes);
//                          os.write("\n".getBytes());
//                        } else {
//                          System.out.println("no redirected stream");
//                        }
//                      }
//                    }

                    ByteArrayOutputStream dsigTransforms = ti.getRedirectedStream();
                    os.write("--- redirected TransformsInfo content ---".getBytes());
                    os.write(dsigTransforms.toByteArray());
                    os.write("\n---".getBytes());

                    MetaInfoType mi = ti.getFinalDataMetaInfo();
                    assertNotNull(mi);
                    assertNull(ti.getTransforms());
                    
                }
                List<DataObjectAssociationType> supplements = doi.getSupplement();
                if (supplements != null) {
                    Iterator<DataObjectAssociationType> doaIt = supplements.iterator();
                    while (doaIt.hasNext()) {
                        System.out.println("found Supplement");
                    }
                }
            }
            SignatureInfoCreationType si = request.getSignatureInfo();
            if (si != null) {
//                Base64XMLOptRefContentType sigEnv = si.getSignatureEnvironment();
//                XMLContentType sigEnvXML = sigEnv.getXMLContent();
//                if (sigEnvXML != null) {
//                    System.out.println("found SignatureEnvironment XMLContent");
//                    ByteArrayOutputStream xmlContent = sigEnvXML.getRedirectedStream();
//                    os.write(xmlContent.toByteArray());
//                    os.write("\n".getBytes());
//                }
//                
//                SignatureInfoCreationType.SignatureLocation sigLocation = si.getSignatureLocation();
                SignatureLocationType sigLocation = (SignatureLocationType) si.getSignatureLocation();
                assertNotNull(sigLocation);
                System.out.println("found at.gv.egiz.slbinding.impl.SignatureLocationType SignatureLocation");

                NamespaceContext ctx = sigLocation.getNamespaceContext();
                assertNotNull(ctx);
                String samlNS = ctx.getNamespaceURI("saml");
                assertEquals(samlNS, "urn:oasis:names:tc:SAML:2.0:assertion");
                System.out.println("found preserved namespace xmlns:saml " + samlNS);

            }
            os.flush();
            os.close();

            fos = new FileOutputStream(FILENAME_REQ + "_bound.xml");
            os = new BufferedOutputStream(fos);

            Marshaller m = jaxbContext.createMarshaller();
            m.marshal(req, os);

            os.flush();
            os.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }
}
