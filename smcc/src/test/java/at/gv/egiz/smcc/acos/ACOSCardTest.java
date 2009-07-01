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
package at.gv.egiz.smcc.acos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.junit.Test;

import at.gv.egiz.smcc.ACOSCard;
import at.gv.egiz.smcc.CardEmul;
import at.gv.egiz.smcc.CardNotSupportedException;
import at.gv.egiz.smcc.CardTest;
import at.gv.egiz.smcc.LockedException;
import at.gv.egiz.smcc.NotActivatedException;
import at.gv.egiz.smcc.SignatureCard;
import at.gv.egiz.smcc.SignatureCardException;
import at.gv.egiz.smcc.SignatureCard.KeyboxName;

public abstract class ACOSCardTest extends CardTest {

  public ACOSCardTest() {
    super();
  }

  protected abstract int getVersion();

  @Test
  public void testGetInfoboxIdentityLinkEmpty() throws SignatureCardException,
      InterruptedException, CardNotSupportedException {

    char[] pin = "0000".toCharArray();
    
    SignatureCard signatureCard = createSignatureCard();
    CardEmul card = (CardEmul) signatureCard.getCard();
    ACOSApplDEC appl = (ACOSApplDEC) card.getApplication(ACOSAppl.AID_DEC);
    appl.clearInfobox();

    byte[] idlink = signatureCard.getInfobox("IdentityLink",
        new TestPINProvider(pin), null);
    assertNull(idlink);

  }
  
  @Test(expected = SignatureCardException.class)
  public void testGetInfoboxIdentityInvalid() throws SignatureCardException,
      InterruptedException, CardNotSupportedException {

    char[] pin = "0000".toCharArray();
    
    SignatureCard signatureCard = createSignatureCard();
    CardEmul card = (CardEmul) signatureCard.getCard();
    ACOSApplDEC appl = (ACOSApplDEC) card.getApplication(ACOSAppl.AID_DEC);
    appl.setInfoboxHeader((byte) 0xFF);

    signatureCard.getInfobox("IdentityLink", new TestPINProvider(pin), null);

  }

  @Test
  public void testGetCerts() throws SignatureCardException,
      InterruptedException, CardNotSupportedException {

    SignatureCard signatureCard = createSignatureCard();

    byte[] cert;

    cert = signatureCard.getCertificate(KeyboxName.SECURE_SIGNATURE_KEYPAIR);
    assertNotNull(cert);
    assertTrue(Arrays.equals(cert, A04ApplSIG.C_CH_DS));

    cert = signatureCard.getCertificate(KeyboxName.CERITIFIED_KEYPAIR);
    assertNotNull(cert);
    assertTrue(Arrays.equals(cert, A04ApplDEC.C_CH_EKEY));

  }

  @Test(expected = NotActivatedException.class)
  public void testGetSIGCertEmpty() throws SignatureCardException,
      InterruptedException, CardNotSupportedException {

    SignatureCard signatureCard = createSignatureCard();
    CardEmul card = (CardEmul) signatureCard.getCard();
    ACOSApplSIG appl = (ACOSApplSIG) card.getApplication(ACOSAppl.AID_SIG);
    appl.clearCert();

    signatureCard.getCertificate(KeyboxName.SECURE_SIGNATURE_KEYPAIR);

  }

  @Test(expected = NotActivatedException.class)
  public void testGetDECCertEmpty() throws SignatureCardException,
      InterruptedException, CardNotSupportedException {

    SignatureCard signatureCard = createSignatureCard();
    CardEmul card = (CardEmul) signatureCard.getCard();
    ACOSApplDEC appl = (ACOSApplDEC) card.getApplication(ACOSAppl.AID_DEC);
    appl.clearCert();

    signatureCard.getCertificate(KeyboxName.CERITIFIED_KEYPAIR);

  }

  @Test
  public void testSignSIG() throws SignatureCardException,
      InterruptedException, CardNotSupportedException,
      NoSuchAlgorithmException, UnsupportedEncodingException {

    char[] pin = "123456".toCharArray();

    SignatureCard signatureCard = createSignatureCard();
    CardEmul card = (CardEmul) signatureCard.getCard();
    ACOSApplSIG appl = (ACOSApplSIG) card.getApplication(ACOSAppl.AID_SIG);
    appl.setPin(ACOSApplSIG.KID_PIN_SIG, pin);

    MessageDigest md = MessageDigest.getInstance("SHA-1");
    byte[] hash = md.digest("MOCCA".getBytes("ASCII"));

    byte[] signature = signatureCard.createSignature(hash,
        KeyboxName.SECURE_SIGNATURE_KEYPAIR, new TestPINProvider(pin));

    assertNotNull(signature);

  }

  @Test
  public void testSignDEC() throws SignatureCardException,
      InterruptedException, CardNotSupportedException,
      NoSuchAlgorithmException, UnsupportedEncodingException {

    char[] pin = "1234".toCharArray();

    SignatureCard signatureCard = createSignatureCard();
    CardEmul card = (CardEmul) signatureCard.getCard();
    ACOSApplDEC appl = (ACOSApplDEC) card.getApplication(ACOSAppl.AID_DEC);
    appl.setPin(ACOSApplDEC.KID_PIN_DEC, pin);

    MessageDigest md = MessageDigest.getInstance("SHA-1");
    byte[] hash = md.digest("MOCCA".getBytes("ASCII"));

    byte[] signature = signatureCard.createSignature(hash,
        KeyboxName.CERITIFIED_KEYPAIR, new TestPINProvider(pin));

    assertNotNull(signature);

  }

  @Test(expected = LockedException.class)
  public void testSignSIGInvalidPin() throws SignatureCardException,
      InterruptedException, CardNotSupportedException,
      NoSuchAlgorithmException, UnsupportedEncodingException {

    SignatureCard signatureCard = createSignatureCard();

    MessageDigest md = MessageDigest.getInstance("SHA-1");
    byte[] hash = md.digest("MOCCA".getBytes("ASCII"));

    TestPINProvider pinProvider = new TestPINProvider("000000".toCharArray());

    signatureCard.createSignature(hash, KeyboxName.SECURE_SIGNATURE_KEYPAIR,
        pinProvider);

  }

  @Test(expected = LockedException.class)
  public void testSignDECInvalidPin() throws SignatureCardException,
      InterruptedException, CardNotSupportedException,
      NoSuchAlgorithmException, UnsupportedEncodingException {

    SignatureCard signatureCard = createSignatureCard();

    MessageDigest md = MessageDigest.getInstance("SHA-1");
    byte[] hash = md.digest("MOCCA".getBytes("ASCII"));

    TestPINProvider pinProvider = new TestPINProvider("0000".toCharArray());

    signatureCard.createSignature(hash, KeyboxName.CERITIFIED_KEYPAIR,
        pinProvider);

  }

  @Test(expected = LockedException.class)
  public void testSignSIGBlockedPin() throws SignatureCardException,
      InterruptedException, CardNotSupportedException,
      NoSuchAlgorithmException, UnsupportedEncodingException {

    SignatureCard signatureCard = createSignatureCard();
    CardEmul card = (CardEmul) signatureCard.getCard();
    ACOSApplSIG appl = (ACOSApplSIG) card.getApplication(ACOSAppl.AID_SIG);
    appl.setPin(ACOSApplSIG.KID_PIN_SIG, null);

    MessageDigest md = MessageDigest.getInstance("SHA-1");
    byte[] hash = md.digest("MOCCA".getBytes("ASCII"));

    TestPINProvider pinProvider = new TestPINProvider("000000".toCharArray());

    signatureCard.createSignature(hash, KeyboxName.SECURE_SIGNATURE_KEYPAIR,
        pinProvider);

  }

  @Test(expected = LockedException.class)
  public void testSignDECBlockedPin() throws SignatureCardException,
      InterruptedException, CardNotSupportedException,
      NoSuchAlgorithmException, UnsupportedEncodingException {

    SignatureCard signatureCard = createSignatureCard();
    CardEmul card = (CardEmul) signatureCard.getCard();
    ACOSApplDEC appl = (ACOSApplDEC) card.getApplication(ACOSAppl.AID_DEC);
    appl.setPin(ACOSApplDEC.KID_PIN_DEC, null);

    MessageDigest md = MessageDigest.getInstance("SHA-1");
    byte[] hash = md.digest("MOCCA".getBytes("ASCII"));

    TestPINProvider pinProvider = new TestPINProvider("0000".toCharArray());

    signatureCard.createSignature(hash, KeyboxName.CERITIFIED_KEYPAIR,
        pinProvider);

  }

}