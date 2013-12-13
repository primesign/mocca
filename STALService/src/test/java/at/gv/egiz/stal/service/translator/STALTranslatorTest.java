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



package at.gv.egiz.stal.service.translator;

import java.math.BigInteger;

import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.SignRequest;
import at.gv.egiz.stal.SignRequest.SignedInfo;
import at.gv.egiz.stal.service.translator.STALTranslator.TranslationHandler;
import at.gv.egiz.stal.service.types.ObjectFactory;
import at.gv.egiz.stal.service.types.RequestType;
import at.gv.egiz.stal.service.types.ResponseType;
import at.gv.egiz.stal.service.types.SignRequestType;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author clemens
 */
public class STALTranslatorTest {

  static ObjectFactory of;

    public STALTranslatorTest() {
    }

  @BeforeClass
  public static void setUpClass() throws Exception {
    of = new ObjectFactory();
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

  /**
   * Test of registerTranslationHandler method, of class STALTranslator.
   */
  @Test
  @Ignore
  public void testRegisterTranslationHandler() {
    System.out.println("registerTranslationHandler");
    TranslationHandler handler = null;
    STALTranslator instance = new STALTranslator();
    instance.registerTranslationHandler(handler);

    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of translate method, of class STALTranslator.
   */
  @Test
  public void testTranslate_STALRequest() throws Exception {
    System.out.println("translate");
    SignRequest request = new SignRequest();
    request.setKeyIdentifier("kid");
    SignedInfo signedInfo = new SignedInfo();
    signedInfo.setValue("signedinfo".getBytes());
    request.setSignedInfo(signedInfo);
    STALTranslator instance = new STALTranslator();
    JAXBElement<? extends RequestType> result = instance.translate(request);
    assertEquals(SignRequestType.class, result.getValue().getClass());
    SignRequestType resultT = (SignRequestType) result.getValue();
    assertEquals(request.getKeyIdentifier(), resultT.getKeyIdentifier());
    assertEquals(request.getSignedInfo().getValue(), resultT.getSignedInfo().getValue());
    assertEquals(request.getSignedInfo().isIsCMSSignedAttributes(), resultT.getSignedInfo().isIsCMSSignedAttributes());
    assertEquals(request.getSignatureMethod(), resultT.getSignatureMethod());
    assertEquals(request.getDigestMethod(), resultT.getDigestMethod());
    if (request.getExcludedByteRange() == null)
      assertNull(resultT.getExcludedByteRange());
    else {
      assertEquals(request.getExcludedByteRange().getFrom(), resultT.getExcludedByteRange().getFrom());
      assertEquals(request.getExcludedByteRange().getTo(), resultT.getExcludedByteRange().getTo());
    }
  }

  /**
   * Test of translate method, of class STALTranslator.
   */
  @Test
  public void testTranslate_1args_1() throws Exception {
    System.out.println("translate");
    SignRequestType req = of.createSignRequestType();
    req.setKeyIdentifier("kid");
    SignRequestType.SignedInfo signedInfo = of.createSignRequestTypeSignedInfo();
    signedInfo.setValue("signedinfo".getBytes());
    req.setSignedInfo(signedInfo);
    req.setSignatureMethod("signatureMethod");
    req.setDigestMethod("digestMethod");
    SignRequestType.ExcludedByteRange excludedByteRange = of.createSignRequestTypeExcludedByteRange();
    excludedByteRange.setFrom(BigInteger.ZERO);
    excludedByteRange.setTo(BigInteger.ONE);
    req.setExcludedByteRange(excludedByteRange);
    JAXBElement<? extends RequestType> request = of.createGetNextRequestResponseTypeSignRequest(req);
    STALTranslator instance = new STALTranslator();
    STALRequest result = instance.translateWSRequest(request);
    assertEquals(SignRequest.class, result.getClass());
    assertEquals(req.getKeyIdentifier(), ((SignRequest) result).getKeyIdentifier());
    assertEquals(req.getSignedInfo().getValue(), ((SignRequest) result).getSignedInfo().getValue());
    assertEquals(req.getSignedInfo().isIsCMSSignedAttributes(), ((SignRequest) result).getSignedInfo().isIsCMSSignedAttributes());
    assertEquals(req.getSignatureMethod(), ((SignRequest) result).getSignatureMethod());
    assertEquals(req.getDigestMethod(), ((SignRequest) result).getDigestMethod());
    if (req.getExcludedByteRange() == null)
      assertNull(((SignRequest) result).getExcludedByteRange());
    else {
      assertEquals(req.getExcludedByteRange().getFrom(), ((SignRequest) result).getExcludedByteRange().getFrom());
      assertEquals(req.getExcludedByteRange().getTo(), ((SignRequest) result).getExcludedByteRange().getTo());
    }
  }

  @Test(expected=RuntimeException.class)
  public void testTranslate_invalidInput() throws Exception {
    System.out.println("translate");
    QName n =  new QName("http://www.egiz.gv.at/stal", "SignRequest");
    JAXBElement<? extends RequestType> request = new JAXBElement<SignRequestType>(n, SignRequestType.class, null);
    STALTranslator instance = new STALTranslator();
    STALRequest result = instance.translateWSRequest(request);
    assertEquals(SignRequest.class, result.getClass());
  }


  /**
   * Test of translate method, of class STALTranslator.
   */
  @Test
  @Ignore
  public void testTranslate_STALResponse() throws Exception {
    System.out.println("translate");
    STALResponse response = null;
    STALTranslator instance = new STALTranslator();
    JAXBElement<? extends ResponseType> expResult = null;
    JAXBElement<? extends ResponseType> result = instance.translate(response);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of translate method, of class STALTranslator.
   */
  @Test
  @Ignore
  public void testTranslate_1args_2() throws Exception {
    System.out.println("translate");
    JAXBElement<? extends ResponseType> response = null;
    STALTranslator instance = new STALTranslator();
    STALResponse expResult = null;
    STALResponse result = instance.translateWSResponse(response);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

}