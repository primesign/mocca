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

package at.gv.egiz.smcc.card;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Formatter;
import java.util.Locale;

import at.gv.egiz.smcc.CancelledException;
import at.gv.egiz.smcc.PinInfo;
import at.gv.egiz.smcc.SignatureCard;
import at.gv.egiz.smcc.SignatureCardException;
import at.gv.egiz.smcc.SignatureCard.KeyboxName;
import at.gv.egiz.smcc.pin.gui.PINGUI;
import at.gv.egiz.smcc.util.SMCCHelper;

public class CreateSignature {

  /**
   * @param args
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception {

    SMCCHelper helper = new SMCCHelper();

    SignatureCard signatureCard = helper.getSignatureCard(Locale.getDefault());
    
    if (signatureCard == null) {
      return;
    }
    
    InputStream data = new ByteArrayInputStream("just a test".getBytes(Charset.forName("UTF-8")));
    byte[] signature = createSignature(signatureCard, KeyboxName.SECURE_SIGNATURE_KEYPAIR, data);
    Formatter printf = new Formatter(System.out); 
    printf.format("Signature: %1$x", new BigInteger(signature));

  }

  public static byte[] createSignature(SignatureCard signatureCard, KeyboxName keyboxName, InputStream data) throws SignatureCardException, InterruptedException, IOException {
    return signatureCard.createSignature(data, keyboxName, new ConsolePINGUI(), "http://www.w3.org/2000/09/xmldsig#rsa-sha1");
  }
  
  public static class ConsolePINGUI implements PINGUI {

    @Override
    public void allKeysCleared() {
    }

    @Override
    public void correctionButtonPressed() {
    }

    @Override
    public void enterPIN(PinInfo spec, int retries) throws CancelledException,
        InterruptedException {
    }

    @Override
    public void enterPINDirect(PinInfo spec, int retries)
        throws CancelledException, InterruptedException {
    }

    @Override
    public void validKeyPressed() {
    }

    @Override
    public char[] providePIN(PinInfo pinSpec, int retries)
        throws CancelledException, InterruptedException {
      System.out.print("Enter " + pinSpec.getLocalizedName() + ": ");
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
      String pin;
      try {
        pin = in.readLine();
      } catch (IOException e) {
        throw new CancelledException(e);
      }
      if (pin == null || pin.length() == 0) {
        throw new CancelledException();
      }
      return pin.toCharArray();
    }
    
  }
  
}
