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
package at.gv.egiz.smcc.starcos;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.smartcardio.CardChannel;

import org.junit.Test;

import at.gv.egiz.smcc.CancelledException;
import at.gv.egiz.smcc.CardEmul;
import at.gv.egiz.smcc.CardNotSupportedException;
import at.gv.egiz.smcc.CardTerminalEmul;
import at.gv.egiz.smcc.CardTest;
import at.gv.egiz.smcc.LockedException;
import at.gv.egiz.smcc.NotActivatedException;
import at.gv.egiz.smcc.PINFormatException;
import at.gv.egiz.smcc.PINMgmtSignatureCard;
import at.gv.egiz.smcc.PINSpec;
import at.gv.egiz.smcc.STARCOSCard;
import at.gv.egiz.smcc.SignatureCard;
import at.gv.egiz.smcc.SignatureCardException;
import at.gv.egiz.smcc.SignatureCardFactory;
import at.gv.egiz.smcc.CardTest.TestChangePINProvider;
import at.gv.egiz.smcc.CardTest.TestPINProvider;
import at.gv.egiz.smcc.PINProvider;
import at.gv.egiz.smcc.SignatureCard.KeyboxName;
import at.gv.egiz.smcc.acos.A03ApplDEC;
import at.gv.egiz.smcc.acos.A04ApplDEC;
import at.gv.egiz.smcc.acos.A04ApplSIG;
import at.gv.egiz.smcc.acos.ACOSAppl;
import at.gv.egiz.smcc.acos.ACOSApplDEC;
import at.gv.egiz.smcc.acos.ACOSApplSIG;
import org.junit.Ignore;

public class STARCOSCardTest extends CardTest {

  @Override
  protected SignatureCard createSignatureCard()
      throws CardNotSupportedException {
    SignatureCardFactory factory = SignatureCardFactory.getInstance();
    STARCOSCardEmul card = new STARCOSCardEmul();
    SignatureCard signatureCard = factory.createSignatureCard(card,
        new CardTerminalEmul(card));
    assertTrue(signatureCard instanceof PINMgmtSignatureCard);
    return signatureCard;
  }
  
  @Test
  public void testGetInfoboxIdentityLinkEmpty() throws SignatureCardException,
      InterruptedException, CardNotSupportedException {

    char[] pin = "0000".toCharArray();
    
    SignatureCard signatureCard = createSignatureCard();
    CardEmul card = (CardEmul) signatureCard.getCard();
    STARCOSApplInfobox appl = (STARCOSApplInfobox) card.getApplication(STARCOSAppl.AID_Infobox);
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
    STARCOSApplInfobox appl = (STARCOSApplInfobox) card.getApplication(STARCOSAppl.AID_Infobox);
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
    assertTrue(Arrays.equals(cert, STARCOSApplSichereSignatur.C_X509_CH_DS));

    cert = signatureCard.getCertificate(KeyboxName.CERITIFIED_KEYPAIR);
    assertNotNull(cert);
    assertTrue(Arrays.equals(cert, STARCOSApplGewoehnlicheSignatur.C_X509_CH_AUT));

  }

  @Test(expected = NotActivatedException.class)
  public void testGetDSCertEmpty() throws SignatureCardException,
      InterruptedException, CardNotSupportedException {

    SignatureCard signatureCard = createSignatureCard();
    CardEmul card = (CardEmul) signatureCard.getCard();
    STARCOSApplSichereSignatur appl = (STARCOSApplSichereSignatur) card.getApplication(STARCOSApplSichereSignatur.AID_SichereSignatur);
    appl.clearCert();

    signatureCard.getCertificate(KeyboxName.SECURE_SIGNATURE_KEYPAIR);

  }

  @Test(expected = NotActivatedException.class)
  public void testGetAUTCertEmpty() throws SignatureCardException,
      InterruptedException, CardNotSupportedException {

    SignatureCard signatureCard = createSignatureCard();
    CardEmul card = (CardEmul) signatureCard.getCard();
    STARCOSApplGewoehnlicheSignatur appl = (STARCOSApplGewoehnlicheSignatur) card.getApplication(STARCOSApplGewoehnlicheSignatur.AID_GewoehnlicheSignatur);
    appl.clearCert();

    signatureCard.getCertificate(KeyboxName.CERITIFIED_KEYPAIR);

  }
  
  @Test
  public void testSignSichereSignatur() throws SignatureCardException,
      InterruptedException, CardNotSupportedException,
      NoSuchAlgorithmException, UnsupportedEncodingException {

    char[] pin = "123456".toCharArray();

    SignatureCard signatureCard = createSignatureCard();
    CardEmul card = (CardEmul) signatureCard.getCard();
    STARCOSApplSichereSignatur appl = (STARCOSApplSichereSignatur) card.getApplication(STARCOSApplSichereSignatur.AID_SichereSignatur);
    appl.setPin(STARCOSApplSichereSignatur.KID_PIN_SS, pin);

    MessageDigest md = MessageDigest.getInstance("SHA-1");
    byte[] hash = md.digest("MOCCA".getBytes("ASCII"));

    byte[] signature = signatureCard.createSignature(hash,
        KeyboxName.SECURE_SIGNATURE_KEYPAIR, new TestPINProvider(pin));

    assertNotNull(signature);

  }

  @Test
  public void testSignGewoehnlicheSignatur() throws SignatureCardException,
      InterruptedException, CardNotSupportedException,
      NoSuchAlgorithmException, UnsupportedEncodingException {

    char[] pin = "1234".toCharArray();

    SignatureCard signatureCard = createSignatureCard();
    CardEmul card = (CardEmul) signatureCard.getCard();
    STARCOSCardChannelEmul channel = (STARCOSCardChannelEmul) card.getBasicChannel();
    channel.setPin(STARCOSCardChannelEmul.KID_PIN_Glob, pin);

    MessageDigest md = MessageDigest.getInstance("SHA-1");
    byte[] hash = md.digest("MOCCA".getBytes("ASCII"));

    byte[] signature = signatureCard.createSignature(hash,
        KeyboxName.CERITIFIED_KEYPAIR, new TestPINProvider(pin));

    assertNotNull(signature);

  }
  
  @Test(expected = LockedException.class)
  public void testSignSichereSignaturInvalidPin() throws SignatureCardException,
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
  public void testSignGewoehnlicheSignaturInvalidPin() throws SignatureCardException,
      InterruptedException, CardNotSupportedException,
      NoSuchAlgorithmException, UnsupportedEncodingException {

    SignatureCard signatureCard = createSignatureCard();

    MessageDigest md = MessageDigest.getInstance("SHA-1");
    byte[] hash = md.digest("MOCCA".getBytes("ASCII"));

    TestPINProvider pinProvider = new TestPINProvider("1234".toCharArray());

    signatureCard.createSignature(hash, KeyboxName.CERITIFIED_KEYPAIR,
        pinProvider);

  }

  @Test(expected = LockedException.class)
  public void testSignSichereSignaturBlockedPin() throws SignatureCardException,
      InterruptedException, CardNotSupportedException,
      NoSuchAlgorithmException, UnsupportedEncodingException {

    SignatureCard signatureCard = createSignatureCard();
    CardEmul card = (CardEmul) signatureCard.getCard();
    STARCOSApplSichereSignatur appl = (STARCOSApplSichereSignatur) card.getApplication(STARCOSApplSichereSignatur.AID_SichereSignatur);
    appl.setPin(STARCOSApplSichereSignatur.KID_PIN_SS, null);

    MessageDigest md = MessageDigest.getInstance("SHA-1");
    byte[] hash = md.digest("MOCCA".getBytes("ASCII"));

    TestPINProvider pinProvider = new TestPINProvider("000000".toCharArray());
    assertTrue(pinProvider.getProvided() <= 0);

    signatureCard.createSignature(hash, KeyboxName.SECURE_SIGNATURE_KEYPAIR,
        pinProvider);

  }

  @Test(expected = LockedException.class)
  public void testSignGewoehnlicheSignaturBlockedPin() throws SignatureCardException,
      InterruptedException, CardNotSupportedException,
      NoSuchAlgorithmException, UnsupportedEncodingException {

    SignatureCard signatureCard = createSignatureCard();
    CardEmul card = (CardEmul) signatureCard.getCard();
    STARCOSCardChannelEmul channel = (STARCOSCardChannelEmul) card.getBasicChannel();
    channel.setPin(STARCOSCardChannelEmul.KID_PIN_Glob, null);

    MessageDigest md = MessageDigest.getInstance("SHA-1");
    byte[] hash = md.digest("MOCCA".getBytes("ASCII"));

    TestPINProvider pinProvider = new TestPINProvider("0000".toCharArray());

    signatureCard.createSignature(hash, KeyboxName.CERITIFIED_KEYPAIR,
        pinProvider);

  }
  
  @Test
  public void testChangePin() throws CardNotSupportedException,
      LockedException, NotActivatedException, CancelledException,
      PINFormatException, SignatureCardException, InterruptedException {

    char[] defaultPin = "123456".toCharArray();

    PINMgmtSignatureCard signatureCard = (PINMgmtSignatureCard) createSignatureCard();
    CardEmul card = (CardEmul) signatureCard.getCard();
    STARCOSCardChannelEmul channel = (STARCOSCardChannelEmul) card.getBasicChannel();
    channel.setPin(STARCOSCardChannelEmul.KID_PIN_Glob, defaultPin);
    STARCOSApplSichereSignatur appl = (STARCOSApplSichereSignatur) card.getApplication(STARCOSApplSichereSignatur.AID_SichereSignatur);
    appl.setPin(STARCOSApplSichereSignatur.KID_PIN_SS, defaultPin);
    
    for (PINSpec pinSpec : signatureCard.getPINSpecs()) {

      char[] pin = defaultPin;

      for (int i = pinSpec.getMinLength(); i <= pinSpec.getMaxLength(); i++) {
        signatureCard.verifyPIN(pinSpec, new TestPINProvider(pin));
        char[] newPin = new char[i];
        Arrays.fill(newPin, '0');
        signatureCard
            .changePIN(pinSpec, new TestChangePINProvider(pin, newPin));
        signatureCard.verifyPIN(pinSpec, new TestPINProvider(newPin));
        pin = newPin;
      }
    }
  }

  @Test
  public void testVerifyWrongPin() throws CardNotSupportedException,
      LockedException, NotActivatedException, CancelledException,
      PINFormatException, SignatureCardException, InterruptedException {

    char[] defaultPin = "123456".toCharArray();

    PINMgmtSignatureCard signatureCard = (PINMgmtSignatureCard) createSignatureCard();
    CardEmul card = (CardEmul) signatureCard.getCard();
    STARCOSCardChannelEmul channel = (STARCOSCardChannelEmul) card.getBasicChannel();
    channel.setPin(STARCOSCardChannelEmul.KID_PIN_Glob, defaultPin);
    STARCOSApplSichereSignatur appl = (STARCOSApplSichereSignatur) card.getApplication(STARCOSApplSichereSignatur.AID_SichereSignatur);
    appl.setPin(STARCOSApplSichereSignatur.KID_PIN_SS, defaultPin);

    for (PINSpec pinSpec : signatureCard.getPINSpecs()) {

      char[] wrongPin = "999999".toCharArray();
      int numWrongTries = 2;
      TestWrongPINProvider wrongPinProvider = new TestWrongPINProvider(wrongPin, numWrongTries);
      try {
        signatureCard.verifyPIN(pinSpec, wrongPinProvider);
      } catch (CancelledException ex) {
      } finally {
        assertTrue(wrongPinProvider.getProvided() == numWrongTries);
      }
    }
  }

  @Test
  public void testChangeWrongPin() throws CardNotSupportedException,
      LockedException, NotActivatedException, CancelledException,
      PINFormatException, SignatureCardException, InterruptedException {
    char[] defaultPin = "123456".toCharArray();

    PINMgmtSignatureCard signatureCard = (PINMgmtSignatureCard) createSignatureCard();
    CardEmul card = (CardEmul) signatureCard.getCard();
    STARCOSCardChannelEmul channel = (STARCOSCardChannelEmul) card.getBasicChannel();
    channel.setPin(STARCOSCardChannelEmul.KID_PIN_Glob, defaultPin);
    STARCOSApplSichereSignatur appl = (STARCOSApplSichereSignatur) card.getApplication(STARCOSApplSichereSignatur.AID_SichereSignatur);
    appl.setPin(STARCOSApplSichereSignatur.KID_PIN_SS, defaultPin);

    for (PINSpec pinSpec : signatureCard.getPINSpecs()) {

      char[] wrongPin = "999999".toCharArray();
      int numWrongTries = 2;
      TestWrongChangePINProvider wrongPinProvider =
              new TestWrongChangePINProvider(wrongPin, defaultPin, numWrongTries);

      try {
        signatureCard.changePIN(pinSpec, wrongPinProvider);
      } catch (CancelledException ex) {
      } finally {
        assertTrue(wrongPinProvider.getProvided() == numWrongTries);
      }
    }
  }
}
