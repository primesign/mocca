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


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.gv.egiz.slbinding;

import at.buergerkarte.namespaces.securitylayer._1_2_3.Base64XMLLocRefOptRefContentType;
import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;

import at.buergerkarte.namespaces.securitylayer._1_2_3.CreateXMLSignatureRequestType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.DataObjectAssociationType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.DataObjectInfoType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.MetaInfoType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.SignatureInfoCreationType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.TransformsInfoType;
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
            String slPkg = at.buergerkarte.namespaces.securitylayer._1_2_3.ObjectFactory.class.getPackage().getName();
            String dsigPkg = org.w3._2000._09.xmldsig_.ObjectFactory.class.getPackage().getName();

            JAXBContext jaxbContext = JAXBContext.newInstance(slPkg + ":" + dsigPkg);
            Unmarshaller um = jaxbContext.createUnmarshaller();

            FileInputStream fis = new FileInputStream(FILENAME_REQ);
            InputStream is = new BufferedInputStream(fis);

            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLEventReader reader = inputFactory.createXMLEventReader(is);
            final RedirectEventFilter contentFilter = new RedirectEventFilter();
            XMLEventReader filteredReader = inputFactory.createFilteredReader(reader, contentFilter);

            um.setListener(new RedirectUnmarshallerListener(contentFilter));

            JAXBElement<?> req = (JAXBElement<?>) um.unmarshal(filteredReader);
            is.close();

            FileOutputStream fos = new FileOutputStream(FILENAME_REQ + "_redirect.txt");
            OutputStream os = new BufferedOutputStream(fos);

            CreateXMLSignatureRequestType request = (CreateXMLSignatureRequestType) req.getValue();
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
                    assertNotNull(ti);
                    System.out.println("found sl:TransformsInfo: " + ti.getClass().getName()); //at.gv.egiz.slbinding.impl.TransformsInfoType TransformsInfo");

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
