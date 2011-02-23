/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.gv.egiz.stal.service.translator;

import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.SignRequest;
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
    request.setSignedInfo("signedinfo".getBytes());
    STALTranslator instance = new STALTranslator();
    JAXBElement<? extends RequestType> result = instance.translate(request);
    assertEquals(SignRequestType.class, result.getValue().getClass());
    SignRequestType resultT = (SignRequestType) result.getValue();
    assertEquals(request.getKeyIdentifier(), resultT.getKeyIdentifier());
    assertEquals(request.getSignedInfo(), resultT.getSignedInfo());
  }

  /**
   * Test of translate method, of class STALTranslator.
   */
  @Test
  public void testTranslate_1args_1() throws Exception {
    System.out.println("translate");
    SignRequestType req = of.createSignRequestType();
    req.setKeyIdentifier("kid");
    req.setSignedInfo("signedinfo".getBytes());
    JAXBElement<? extends RequestType> request = of.createGetNextRequestResponseTypeSignRequest(req);
    STALTranslator instance = new STALTranslator();
    STALRequest result = instance.translateWSRequest(request);
    assertEquals(SignRequest.class, result.getClass());
    assertEquals(req.getKeyIdentifier(), ((SignRequest) result).getKeyIdentifier());
    assertEquals(req.getSignedInfo(), ((SignRequest) result).getSignedInfo());
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