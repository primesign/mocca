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
    
    byte[] certificateSS = signatureCard.getCertificate(KeyboxName.SECURE_SIGNATURE_KEYPAIR, null);
    
    assertArrayEquals(certificateSSRef, certificateSS);
    
    byte[] certificateGSRef = (byte[]) applicationContext.getBean("certificateGS", byte[].class);
    
    byte[] certificateGS = signatureCard.getCertificate(KeyboxName.CERTIFIED_KEYPAIR, null);
    
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
        KeyboxName.CERTIFIED_KEYPAIR, new SMCCTestPINProvider(pin), null);

    assertNotNull(signature);
    
  }
  
  @Test(expected = LockedException.class)
  public void testSignDECWrongPIN() throws UnsupportedEncodingException, SignatureCardException, InterruptedException, IOException {
    
    char[] pin = "00000".toCharArray();

    byte[] signature = signatureCard.createSignature(new ByteArrayInputStream("MOCCA"
        .getBytes("ASCII")),
        KeyboxName.CERTIFIED_KEYPAIR, new SMCCTestPINProvider(pin), null);

    assertNotNull(signature);
    
  }
  
  @Test(expected = CancelledException.class)
  public void testSignDECCancel() throws SignatureCardException,
      InterruptedException, CardNotSupportedException,
      NoSuchAlgorithmException, IOException {

    PINGUI pinProvider = new CancelPINProvider();

    signatureCard.createSignature(new ByteArrayInputStream(MOCCA),
        KeyboxName.CERTIFIED_KEYPAIR, pinProvider, null);

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
        KeyboxName.CERTIFIED_KEYPAIR, pinProvider, null);

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
          signatureCard.getCertificate(KeyboxName.SECURE_SIGNATURE_KEYPAIR, null);
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
          signatureCard.getCertificate(KeyboxName.CERTIFIED_KEYPAIR, null);
          assertTrue(false);
          return null;
        } catch (SignatureCardException e) {
          // expected
          throw new CancelledException();
        }
      }
    };

    signatureCard.createSignature(new ByteArrayInputStream(MOCCA),
        KeyboxName.CERTIFIED_KEYPAIR, pinProvider, null);

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
