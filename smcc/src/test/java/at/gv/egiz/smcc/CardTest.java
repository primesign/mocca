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

import at.gv.egiz.smcc.pin.gui.CancelPINProvider;
import at.gv.egiz.smcc.pin.gui.InterruptPINProvider;
import at.gv.egiz.smcc.pin.gui.CancelChangePINProvider;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.smartcardio.Card;

import org.junit.Test;

import at.gv.egiz.smcc.SignatureCard.KeyboxName;
import at.gv.egiz.smcc.acos.A04ApplDEC;
import at.gv.egiz.smcc.pin.gui.DummyPINGUI;
import at.gv.egiz.smcc.pin.gui.ModifyPINGUI;
import at.gv.egiz.smcc.pin.gui.PINGUI;
import at.gv.egiz.smcc.pin.gui.SMCCTestPINProvider;

@SuppressWarnings("restriction")
public abstract class CardTest {

  public CardTest() {
    super();
  }

  protected abstract SignatureCard createSignatureCard()
      throws CardNotSupportedException;

  @Test
  public void testGetCard() throws CardNotSupportedException {
    SignatureCard signatureCard = createSignatureCard();
    Card card = signatureCard.getCard();
    assertNotNull(card);
  }

  @Test
  public void testGetInfoboxIdentityLink() throws SignatureCardException,
      InterruptedException, CardNotSupportedException {

    final char[] pin = "0000".toCharArray();
    
    SignatureCard signatureCard = createSignatureCard();
    
    SMCCTestPINProvider pinProvider = new SMCCTestPINProvider(pin);

    byte[] idlink = signatureCard.getInfobox("IdentityLink",
        pinProvider, null);
    assertNotNull(idlink);
    assertTrue(Arrays.equals(idlink, A04ApplDEC.IDLINK));
    assertEquals(1, pinProvider.provided);

  }

  @Test(expected = CancelledException.class)
  public void testSignSIGCancel() throws SignatureCardException,
      InterruptedException, CardNotSupportedException,
      NoSuchAlgorithmException, IOException {
      
        SignatureCard signatureCard = createSignatureCard();
      
        PINGUI pinProvider = new CancelPINProvider();
      
    signatureCard.createSignature(new ByteArrayInputStream("MOCCA"
        .getBytes("ASCII")), KeyboxName.SECURE_SIGNATURE_KEYPAIR, pinProvider,
        null);
      
      }

  @Test(expected = CancelledException.class)
  public void testSignDECCancel() throws SignatureCardException,
      InterruptedException, CardNotSupportedException,
      NoSuchAlgorithmException, IOException {
      
        SignatureCard signatureCard = createSignatureCard();
      
        PINGUI pinProvider = new CancelPINProvider();
      
        signatureCard.createSignature(new ByteArrayInputStream("MOCCA"
            .getBytes("ASCII")), KeyboxName.CERITIFIED_KEYPAIR,
            pinProvider, null);
      
      }

  @Test(expected = InterruptedException.class)
  public void testSignSIGInterrrupted() throws SignatureCardException,
      InterruptedException, CardNotSupportedException,
      NoSuchAlgorithmException, IOException {
      
        SignatureCard signatureCard = createSignatureCard();
      
        PINGUI pinProvider = new InterruptPINProvider();
      
        signatureCard.createSignature(new ByteArrayInputStream("MOCCA"
            .getBytes("ASCII")), KeyboxName.SECURE_SIGNATURE_KEYPAIR,
            pinProvider, null);
      
      }

  @Test(expected = InterruptedException.class)
  public void testSignDECInterrrupted() throws SignatureCardException,
      InterruptedException, CardNotSupportedException,
      NoSuchAlgorithmException, IOException {
      
        SignatureCard signatureCard = createSignatureCard();
      
        PINGUI pinProvider = new InterruptPINProvider();
      
        signatureCard.createSignature(new ByteArrayInputStream("MOCCA"
            .getBytes("ASCII")), KeyboxName.CERITIFIED_KEYPAIR,
            pinProvider, null);
      
      }

  @Test(expected = CancelledException.class)
  public void testSignSIGConcurrent() throws SignatureCardException,
      InterruptedException, CardNotSupportedException,
      NoSuchAlgorithmException, IOException {
      
        final SignatureCard signatureCard = createSignatureCard();
      
        PINGUI pinProvider = new DummyPINGUI() {
          @Override
          public char[] providePIN(PinInfo spec, int retries)
              throws CancelledException, InterruptedException {

            try {
              signatureCard.getCertificate(KeyboxName.SECURE_SIGNATURE_KEYPAIR);
              assertTrue(false);
              return null;
            } catch (SignatureCardException e) {
              // expected
              throw new CancelledException();
            }

          }
        };

        signatureCard.createSignature(new ByteArrayInputStream("MOCCA"
            .getBytes("ASCII")), KeyboxName.SECURE_SIGNATURE_KEYPAIR,
            pinProvider, null);
      
      }

  @Test(expected = CancelledException.class)
  public void testSignDECConcurrent() throws SignatureCardException,
      InterruptedException, CardNotSupportedException,
      NoSuchAlgorithmException, IOException {
      
        final SignatureCard signatureCard = createSignatureCard();
      
        PINGUI pinProvider = new DummyPINGUI() {
          @Override
          public char[] providePIN(PinInfo spec, int retries)
              throws CancelledException, InterruptedException {
      
            try {
              signatureCard.getCertificate(KeyboxName.CERITIFIED_KEYPAIR);
              assertTrue(false);
              return null;
            } catch (SignatureCardException e) {
              // expected
              throw new CancelledException();
            }
          }
        };
      
        signatureCard.createSignature(new ByteArrayInputStream("MOCCA"
            .getBytes("ASCII")), KeyboxName.CERITIFIED_KEYPAIR,
            pinProvider, null);
      
      }

  @Test
  public void testGetPinSpecs() throws CardNotSupportedException, SignatureCardException {
  
    PINMgmtSignatureCard signatureCard = (PINMgmtSignatureCard) createSignatureCard();
  
    PinInfo[] specs = signatureCard.getPinInfos();
    assertNotNull(specs);
    assertTrue(specs.length > 0);
  
  }

  @Test(expected = SignatureCardException.class)
  public void testActivatePin() throws SignatureCardException,
      InterruptedException, CardNotSupportedException,
      NoSuchAlgorithmException, UnsupportedEncodingException {
      
        PINMgmtSignatureCard signatureCard = (PINMgmtSignatureCard) createSignatureCard();
      
        ModifyPINGUI pinProvider = new CancelChangePINProvider();
      
        PinInfo[] specs = signatureCard.getPinInfos();
      
        signatureCard.activatePIN(specs[0], pinProvider);
      }

}