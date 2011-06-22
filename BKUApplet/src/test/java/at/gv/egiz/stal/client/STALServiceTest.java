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

package at.gv.egiz.stal.client;

import static org.junit.Assert.assertNotNull;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.junit.Test;

import at.gv.egiz.stal.service.GetHashDataInputFault;
import at.gv.egiz.stal.service.STALPortType;
import at.gv.egiz.stal.service.STALService;
import at.gv.egiz.stal.service.types.GetHashDataInputResponseType;
import at.gv.egiz.stal.service.types.GetHashDataInputType;
import at.gv.egiz.stal.service.types.GetNextRequestResponseType;
import at.gv.egiz.stal.service.types.GetNextRequestType;
import at.gv.egiz.stal.service.types.InfoboxReadRequestType;
import at.gv.egiz.stal.service.types.RequestType;

/**
 *
 * @author clemens
 */
public class STALServiceTest {
    
//    @Test
    public void callSTAL() {
        try {
            URL endpointURL = new URL("http://localhost:3495/bkuonline/stal?wsdl");
            QName endpointName = new QName("http://www.egiz.gv.at/wsdl/stal", "STALService");
            STALService stal = new STALService(endpointURL, endpointName);
//            stal = new STALService();
            STALPortType port = stal.getSTALPort();

            GetNextRequestType nrReq = new GetNextRequestType();
            nrReq.setSessionId("TestSession"); //STALServiceImpl.TEST_SESSION_ID);
//            req.getResponse().add(new ErrorResponse(1234));
            GetNextRequestResponseType nrResp = port.getNextRequest(nrReq);
            assertNotNull(nrResp);
            System.out.println("got response: " + nrResp.getInfoboxReadRequestOrSignRequestOrQuitRequest().size());
            for (JAXBElement<? extends RequestType> stalReqElt : nrResp.getInfoboxReadRequestOrSignRequestOrQuitRequest()) {
              RequestType stalReq = stalReqElt.getValue();
                if (stalReq instanceof InfoboxReadRequestType) {
                   String ibid = ((InfoboxReadRequestType) stalReq).getInfoboxIdentifier(); 
                   String did = ((InfoboxReadRequestType) stalReq).getDomainIdentifier();
                    System.out.println(" received InfoboxReadRequest for " + ibid + ", " + did);
                } else {
                    System.out.println(" received STAL request " + stalReq.getClass().getName());
                }
            }
            
            GetHashDataInputType hdReq = new GetHashDataInputType();
            hdReq.setSessionId("TestSession"); //STALServiceImpl.TEST_SESSION_ID);
            GetHashDataInputType.Reference ref = new GetHashDataInputType.Reference();
            ref.setID("refId");
            hdReq.getReference().add(ref);
            GetHashDataInputResponseType hdResp = port.getHashDataInput(hdReq);
            GetHashDataInputResponseType.Reference hdRef = hdResp.getReference().get(0);
            System.out.println("got HashDataInput " + new String(hdRef.getValue()));
            
            
        } catch (GetHashDataInputFault ex) {
            ex.printStackTrace();
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
    }
    
    @Test
    public void testSTAL() {
        //TODO
    }

}
