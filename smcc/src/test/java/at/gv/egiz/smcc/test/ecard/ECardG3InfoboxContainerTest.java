/*
* Copyright 2009 Federal Chancellery Austria and
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

package at.gv.egiz.smcc.test.ecard;

import static org.junit.Assert.*;

import org.junit.Test;

import at.gv.egiz.smcc.CancelledException;
import at.gv.egiz.smcc.PinInfo;
import at.gv.egiz.smcc.SignatureCardException;
import at.gv.egiz.smcc.pin.gui.DummyPINGUI;
import at.gv.egiz.smcc.pin.gui.PINGUI;
import at.gv.egiz.smcc.test.AbstractCardTestBase;

public class ECardG3InfoboxContainerTest extends AbstractCardTestBase {

  @Test
  public void testGetInfoboxIdentityLink() throws SignatureCardException, InterruptedException {

    PINGUI pinProvider = new DummyPINGUI() {
      @Override
      public char[] providePIN(PinInfo pinSpec, int retries)
          throws CancelledException, InterruptedException {
        // must not require a PIN!
        fail();
        return null;
      }
    };
    
    byte[] idlinkRef = (byte[]) applicationContext.getBean("identityLink", byte[].class);

    byte[] idlink = signatureCard.getInfobox("IdentityLink", pinProvider, null);
    
    assertArrayEquals(idlinkRef, idlink);
    
  }

  
}
