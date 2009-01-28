/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.gv.egiz.stalx.service;

import at.buergerkarte.namespaces.cardchannel.service.CommandAPDUType;
import at.buergerkarte.namespaces.cardchannel.service.ScriptType;
import at.gv.egiz.stal.service.types.GetNextRequestResponseType;
import at.gv.egiz.stal.service.types.RequestType;
import java.net.URL;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author clemens
 */
@Ignore
public class STALServiceTest {

  static at.gv.egiz.stal.service.STALPortType port;

  public STALServiceTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
    //      UsesJAXBContextFeature feature = new UsesJAXBContextFeature(ClientJAXBContextFactory.class);

    URL wsdlLocation = new URL("http://localhost:3495/stal?wsdl");
    QName serviceName = new QName("http://www.egiz.gv.at/wsdl/stal", "STALService");
    STALService service = new STALService(wsdlLocation, serviceName);
    port = service.getSTALPort();
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

//  /**
//   * Test of getSTALPort method, of class STALService.
//   */
//  @Test
//  public void testGetSTALPort() {
//    System.out.println("getSTALPort");
//    STALService instance = null;
//    STALPortType expResult = null;
//    STALPortType result = instance.getSTALPort();
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
  @Test
  public void testConnect() {
    System.out.println("connecting to STAL WS [TestSession] ...");
    GetNextRequestResponseType wsResponse = port.connect("TestSession");

    List<JAXBElement<? extends RequestType>> stalRequests = wsResponse.getInfoboxReadRequestOrSignRequestOrQuitRequest();
    System.out.println("Received " + wsResponse.getClass() + " containing " + stalRequests.size() + " requests");
    for (JAXBElement<? extends RequestType> jAXBElement : stalRequests) {
      System.out.println(" STALRequest " + jAXBElement.getValue().getClass());
      RequestType request = jAXBElement.getValue();
      if (request instanceof ScriptType) {
        List<Object> apdus = ((ScriptType) request).getResetOrCommandAPDUOrVerifyAPDU();
        for (Object object : apdus) {
          System.out.println("   - APDU: " + new String(((CommandAPDUType) object).getValue()));
//            System.out.println("  APDU: " + object.getClass());
        }
      }
    }
  }


//      GetHashDataInputType hdi = stalOF.createGetHashDataInputType();
//      hdi.setSessionId("TestSession");
//      hdi.getReference().add(new GetHashDataInputType.Reference());
//      GetHashDataInputResponseType hdiResponse = port.getHashDataInput(hdi);
//      List<GetHashDataInputResponseType.Reference> l = hdiResponse.getReference();
//      System.out.println("HDI references: " + l.size());
//      for (GetHashDataInputResponseType.Reference reference : l) {
//        System.out.println(" Reference " + reference.getID());
//      }


//      ScriptType part2 = ccOF.createScriptType();
//      ResponseType scriptResp = ccOF.createResponseType();
//      GetNextRequestResponseType stalReqResp =
//              port.nextRequest();
//      // TODO process result here
//      generated.ResponseType result2 = port.runAPDUScript(part2);
//      System.out.println("Result = " + result2);




}