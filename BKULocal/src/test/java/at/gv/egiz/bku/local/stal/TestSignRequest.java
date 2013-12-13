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


package at.gv.egiz.bku.local.stal;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;

import at.gv.egiz.bku.utils.StreamUtil;
import at.gv.egiz.smcc.SignatureCard;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.SignRequest;
import at.gv.egiz.stal.SignRequest.SignedInfo;

@Ignore
public class TestSignRequest {

  public void test() throws Exception {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    StreamUtil.copyStream(getClass().getClassLoader().getResourceAsStream("at/gv/egiz/bku/local/stal/sigInfo.xml"), os);
    byte[] signedInfo = os.toByteArray();
    SignRequest sr = new SignRequest();
    SignedInfo si = new SignedInfo();
    si.setValue(signedInfo);
    sr.setSignedInfo(si);
    sr.setKeyIdentifier(SignatureCard.KeyboxName.SECURE_SIGNATURE_KEYPAIR.getKeyboxName());
    List<STALRequest> reqList = new ArrayList<STALRequest>(1);
    reqList.add(sr);
    System.out.println((new LocalSTALFactory()).createSTAL().handleRequest(reqList));
  }
  
  public static void main(String[] args) {
    TestSignRequest tsr = new TestSignRequest();
    try {
      tsr.test();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
