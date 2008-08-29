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
package at.gv.egiz.bku.local.stal;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;

import at.gv.egiz.bku.utils.StreamUtil;
import at.gv.egiz.smcc.SignatureCard;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.SignRequest;

@Ignore
public class TestSignRequest {
  
  public void test() throws Exception {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    StreamUtil.copyStream(getClass().getClassLoader().getResourceAsStream("at/gv/egiz/bku/local/stal/sigInfo.xml"), os);
    byte[] signedInfo = os.toByteArray();
    SignRequest sr = new SignRequest();
    sr.setSignedInfo(signedInfo);
    sr.setKeyIdentifier(SignatureCard.KeyboxName.SECURE_SIGNATURE_KEYPAIR.getKeyboxName());
    List<STALRequest> reqList = new ArrayList<STALRequest>(1);
    reqList.add(sr);
    System.out.println((new SMCCSTALFactory()).createSTAL().handleRequest(reqList));
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
