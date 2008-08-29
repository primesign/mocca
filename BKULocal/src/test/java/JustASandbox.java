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
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.junit.Ignore;

import at.gv.egiz.bku.local.conf.Configurator;
import at.gv.egiz.bku.local.stal.SMCCSTALFactory;
import at.gv.egiz.bku.utils.StreamUtil;
import at.gv.egiz.stal.InfoboxReadRequest;
import at.gv.egiz.stal.STAL;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.SignRequest;
import at.gv.egiz.stal.SignResponse;

@Ignore
public class JustASandbox {

  /**
   * @param args
   * @throws IOException 
   */
  public static void main(String[] args) throws IOException {
 
    Configurator cfg = new Configurator();
    URL url = new URL("https://demo.egiz.gv.at");
    HttpsURLConnection uc = (HttpsURLConnection) url.openConnection();
    uc.connect();
    System.exit(-1);
    
    InfoboxReadRequest req = new InfoboxReadRequest();
    req.setInfoboxIdentifier("SecureSignatureKeypair");
    
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    InputStream is = JustASandbox.class.getClassLoader().getResourceAsStream("at/gv/egiz/bku/local/stal/sigInfo.xml");
    StreamUtil.copyStream(is, os);
    SignRequest sr = new SignRequest();
    sr.setSignedInfo(os.toByteArray());
    sr.setKeyIdentifier("SecureSignatureKeypair"); //os.toByteArray(), "SecureSignatureKeypair", null);
    STAL stal = (new SMCCSTALFactory()).createSTAL();
    
    List<STALRequest> reqList = new ArrayList<STALRequest>(2);
    reqList.add(req);
    reqList.add(sr);
    
    List<STALResponse> resp = stal.handleRequest(reqList);
    System.out.println(resp.get(0));
    System.out.println(resp.get(1));
    FileOutputStream fos = new FileOutputStream("c:/tmp/seq_now.der");
    SignResponse sir = (SignResponse) resp.get(1);
    fos.write(sir.getSignatureValue());
    fos.close();
  }

}
