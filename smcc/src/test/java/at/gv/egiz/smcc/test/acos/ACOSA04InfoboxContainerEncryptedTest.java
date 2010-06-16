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

package at.gv.egiz.smcc.test.acos;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

import at.gv.egiz.smcc.SignatureCardException;
import at.gv.egiz.smcc.pin.gui.PINGUI;
import at.gv.egiz.smcc.pin.gui.SMCCTestPINProvider;
import at.gv.egiz.smcc.test.AbstractCardTestBase;

public class ACOSA04InfoboxContainerEncryptedTest extends AbstractCardTestBase {

  @Test
  public void testGetInfoboxIdentityLink() throws SignatureCardException, InterruptedException {

    PINGUI pinProvider = new SMCCTestPINProvider("1234".toCharArray());
    
    byte[] idlinkRef = (byte[]) applicationContext.getBean("identityLink", byte[].class);

    byte[] idlink = signatureCard.getInfobox("IdentityLink", pinProvider, null);
    
    assertArrayEquals(idlinkRef, idlink);
    
  }

}
