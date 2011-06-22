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