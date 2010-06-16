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

package at.gv.egiz.smcc.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.junit.Test;

import at.gv.egiz.smcc.CancelledException;
import at.gv.egiz.smcc.CardNotSupportedException;
import at.gv.egiz.smcc.LockedException;
import at.gv.egiz.smcc.PINMgmtSignatureCard;
import at.gv.egiz.smcc.PinInfo;
import at.gv.egiz.smcc.SignatureCardException;
import at.gv.egiz.smcc.SignatureCard.KeyboxName;
import at.gv.egiz.smcc.pin.gui.CancelPINProvider;
import at.gv.egiz.smcc.pin.gui.DummyPINGUI;
import at.gv.egiz.smcc.pin.gui.InterruptPINProvider;
import at.gv.egiz.smcc.pin.gui.PINGUI;
import at.gv.egiz.smcc.pin.gui.SMCCTestPINProvider;

public abstract class AbstractCardTest extends AbstractCardTestBase {
  
  @Test
  public void testGetCertificates() throws SignatureCardException, InterruptedException {
    
    byte[] certificateSSRef = (byte[]) applicationContext.getBean("certificateSS", byte[].class);
    
    byte[] certificateSS = signatureCard.getCertificate(KeyboxName.SECURE_SIGNATURE_KEYPAIR);
    
    assertArrayEquals(certificateSSRef, certificateSS);
    
    byte[] certificateGSRef = (byte[]) applicationContext.getBean("certificateGS", byte[].class);
    
    byte[] certificateGS = signatureCard.getCertificate(KeyboxName.CERITIFIED_KEYPAIR);
    
    assertArrayEquals(certificateGSRef, certificateGS);
    
  }
  
  @Test
  public void testSignSIG() throws UnsupportedEncodingException, SignatureCardException, InterruptedException, IOException {
    
    char[] pin = "123456".toCharArray();

    byte[] signature = signatureCard.createSignature(new ByteArrayInputStream("MOCCA"
        .getBytes("ASCII")),
        KeyboxName.SECURE_SIGNATURE_KEYPAIR, new SMCCTestPINProvider(pin), null);

    assertNotNull(signature);
    
  }

  @Test(expected = LockedException.class)
  public void testSignSIGWrongPIN() throws UnsupportedEncodingException, SignatureCardException, InterruptedException, IOException {
    
    char[] pin = "00000".toCharArray();

    byte[] signature = signatureCard.createSignature(new ByteArrayInputStream("MOCCA"
        .getBytes("ASCII")),
        KeyboxName.SECURE_SIGNATURE_KEYPAIR, new SMCCTestPINProvider(pin), null);

    assertNotNull(signature);
    
  }
  
  @Test(expected = CancelledException.class)
  public void testSignSIGCancel() throws SignatureCardException,
      InterruptedException, CardNotSupportedException,
      NoSuchAlgorithmException, IOException {

    PINGUI pinProvider = new CancelPINProvider();

    signatureCard.createSignature(new ByteArrayInputStream(MOCCA),
        KeyboxName.SECURE_SIGNATURE_KEYPAIR, pinProvider, null);

  }

  @Test
  public void testSignDEC() throws UnsupportedEncodingException, SignatureCardException, InterruptedException, IOException {
    
    char[] pin = "1234".toCharArray();

    byte[] signature = signatureCard.createSignature(new ByteArrayInputStream("MOCCA"
        .getBytes("ASCII")),
        KeyboxName.CERITIFIED_KEYPAIR, new SMCCTestPINProvider(pin), null);

    assertNotNull(signature);
    
  }
  
  @Test(expected = LockedException.class)
  public void testSignDECWrongPIN() throws UnsupportedEncodingException, SignatureCardException, InterruptedException, IOException {
    
    char[] pin = "00000".toCharArray();

    byte[] signature = signatureCard.createSignature(new ByteArrayInputStream("MOCCA"
        .getBytes("ASCII")),
        KeyboxName.CERITIFIED_KEYPAIR, new SMCCTestPINProvider(pin), null);

    assertNotNull(signature);
    
  }
  
  @Test(expected = CancelledException.class)
  public void testSignDECCancel() throws SignatureCardException,
      InterruptedException, CardNotSupportedException,
      NoSuchAlgorithmException, IOException {

    PINGUI pinProvider = new CancelPINProvider();

    signatureCard.createSignature(new ByteArrayInputStream(MOCCA),
        KeyboxName.CERITIFIED_KEYPAIR, pinProvider, null);

  }
  
  @Test(expected = InterruptedException.class)
  public void testSignSIGInterrrupted() throws SignatureCardException,
      InterruptedException, CardNotSupportedException,
      NoSuchAlgorithmException, IOException {

    PINGUI pinProvider = new InterruptPINProvider();

    signatureCard.createSignature(new ByteArrayInputStream(MOCCA),
        KeyboxName.SECURE_SIGNATURE_KEYPAIR, pinProvider, null);

  }

  @Test(expected = InterruptedException.class)
  public void testSignDECInterrrupted() throws SignatureCardException,
      InterruptedException, CardNotSupportedException,
      NoSuchAlgorithmException, IOException {

    PINGUI pinProvider = new InterruptPINProvider();

    signatureCard.createSignature(new ByteArrayInputStream(MOCCA),
        KeyboxName.CERITIFIED_KEYPAIR, pinProvider, null);

  }
  
  @Test(expected = CancelledException.class)
  public void testSignSIGConcurrent() throws SignatureCardException,
      InterruptedException, CardNotSupportedException,
      NoSuchAlgorithmException, IOException {

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

    signatureCard.createSignature(new ByteArrayInputStream(MOCCA),
        KeyboxName.SECURE_SIGNATURE_KEYPAIR, pinProvider, null);

  }

  @Test(expected = CancelledException.class)
  public void testSignDECConcurrent() throws SignatureCardException,
      InterruptedException, CardNotSupportedException,
      NoSuchAlgorithmException, IOException {

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

    signatureCard.createSignature(new ByteArrayInputStream(MOCCA),
        KeyboxName.CERITIFIED_KEYPAIR, pinProvider, null);

  }
  
  @Test
  public void testGetPinSpecs() throws CardNotSupportedException,
      SignatureCardException {

    assertTrue(signatureCard instanceof PINMgmtSignatureCard);

    PinInfo[] specs = ((PINMgmtSignatureCard) signatureCard).getPinInfos();
    assertNotNull(specs);
    assertTrue(specs.length > 0);

  }

}
