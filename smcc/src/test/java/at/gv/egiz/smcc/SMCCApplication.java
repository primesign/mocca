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
package at.gv.egiz.smcc;

import java.util.Locale;

import at.gv.egiz.smcc.util.SMCCHelper;

public class SMCCApplication {

  /**
   * @param args
   */
  public static void main(String[] args) {
    
    SignatureCard sc = null;
    SMCCHelper smccHelper = new SMCCHelper();
    while (smccHelper.getResultCode() != SMCCHelper.CARD_FOUND) {
      System.out.println("Did not get a signature card ... "+smccHelper.getResultCode());
      smccHelper.update();
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    sc = smccHelper.getSignatureCard(Locale.getDefault());
    System.out.println("Found supported siganture card: "+sc);
  }

}
