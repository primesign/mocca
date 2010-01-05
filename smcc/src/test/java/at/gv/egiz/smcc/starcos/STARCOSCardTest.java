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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;


import org.junit.Test;

import at.gv.egiz.smcc.CancelledException;
import at.gv.egiz.smcc.CardEmul;
import at.gv.egiz.smcc.CardNotSupportedException;
import at.gv.egiz.smcc.CardTerminalEmul;
import at.gv.egiz.smcc.CardTest;
import at.gv.egiz.smcc.pin.gui.ChangePINProvider;
import at.gv.egiz.smcc.pin.gui.InvalidChangePINProvider;
import at.gv.egiz.smcc.pin.gui.InvalidPINProvider;
import at.gv.egiz.smcc.LockedException;
import at.gv.egiz.smcc.NotActivatedException;
import at.gv.egiz.smcc.PIN;
import at.gv.egiz.smcc.PINFormatException;
import at.gv.egiz.smcc.PINMgmtSignatureCard;
import at.gv.egiz.smcc.PINSpec;
import at.gv.egiz.smcc.SignatureCard;
import at.gv.egiz.smcc.SignatureCardException;
import at.gv.egiz.smcc.SignatureCardFactory;
import at.gv.egiz.smcc.pin.gui.SMCCTestPINProvider;
import at.gv.egiz.smcc.SignatureCard.KeyboxName;
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

  protected SignatureCard createSignatureCard(byte[] SS_PIN, byte[] Glob_PIN, int pinState)
      throws CardNotSupportedException {
    SignatureCardFactory factory = SignatureCardFactory.getInstance();
    STARCOSCardEmul card = new STARCOSCardEmul(SS_PIN, Glob_PIN, pinState);
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
        new SMCCTestPINProvider(pin), null);
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

    signatureCard.getInfobox("IdentityLink", new SMCCTestPINProvider(pin), null);

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
      NoSuchAlgorithmException, IOException {

    char[] pin = "123456".toCharArray();

    SignatureCard signatureCard = createSignatureCard();
    CardEmul card = (CardEmul) signatureCard.getCard();
    STARCOSApplSichereSignatur appl = (STARCOSApplSichereSignatur) card.getApplication(STARCOSApplSichereSignatur.AID_SichereSignatur);
    appl.setPin(STARCOSApplSichereSignatur.KID_PIN_SS, pin);

    byte[] signature = signatureCard.createSignature(new ByteArrayInputStream("MOCCA"
        .getBytes("ASCII")),
        KeyboxName.SECURE_SIGNATURE_KEYPAIR, new SMCCTestPINProvider(pin), null);

    assertNotNull(signature);

  }

  @Test
  public void testSignGewoehnlicheSignatur() throws SignatureCardException,
      InterruptedException, CardNotSupportedException,
      NoSuchAlgorithmException, IOException {

    char[] pin = "1234".toCharArray();

    SignatureCard signatureCard = createSignatureCard();
    CardEmul card = (CardEmul) signatureCard.getCard();
    STARCOSCardChannelEmul channel = (STARCOSCardChannelEmul) card.getBasicChannel();
    channel.setPin(STARCOSCardChannelEmul.KID_PIN_Glob, pin);

    byte[] signature = signatureCard.createSignature(new ByteArrayInputStream("MOCCA"
        .getBytes("ASCII")),
        KeyboxName.CERITIFIED_KEYPAIR, new SMCCTestPINProvider(pin), null);

    assertNotNull(signature);

  }

  @Test(expected = LockedException.class)
  public void testSignSichereSignaturInvalidPin() throws SignatureCardException,
      InterruptedException, CardNotSupportedException,
      NoSuchAlgorithmException, IOException {

    SignatureCard signatureCard = createSignatureCard();

    SMCCTestPINProvider pinProvider = new SMCCTestPINProvider("000000".toCharArray());

    signatureCard.createSignature(new ByteArrayInputStream("MOCCA"
        .getBytes("ASCII")), KeyboxName.SECURE_SIGNATURE_KEYPAIR,
        pinProvider, null);

  }

  @Test(expected = LockedException.class)
  public void testSignGewoehnlicheSignaturInvalidPin() throws SignatureCardException,
      InterruptedException, CardNotSupportedException,
      NoSuchAlgorithmException, IOException {

    SignatureCard signatureCard = createSignatureCard();

    SMCCTestPINProvider pinProvider = new SMCCTestPINProvider("1234".toCharArray());

    signatureCard.createSignature(new ByteArrayInputStream("MOCCA"
        .getBytes("ASCII")), KeyboxName.CERITIFIED_KEYPAIR,
        pinProvider, null);

  }

  @Test(expected = LockedException.class)
  public void testSignSichereSignaturBlockedPin() throws SignatureCardException,
      InterruptedException, CardNotSupportedException,
      NoSuchAlgorithmException, IOException {

    SignatureCard signatureCard = createSignatureCard(null, null, PIN.STATE_PIN_BLOCKED);

    SMCCTestPINProvider pinProvider = new SMCCTestPINProvider("000000".toCharArray());
    assertTrue(pinProvider.getProvided() <= 0);

    signatureCard.createSignature(new ByteArrayInputStream("MOCCA"
        .getBytes("ASCII")), KeyboxName.SECURE_SIGNATURE_KEYPAIR,
        pinProvider, null);

  }

  @Test(expected = LockedException.class)
  public void testSignGewoehnlicheSignaturBlockedPin() throws SignatureCardException,
      InterruptedException, CardNotSupportedException,
      NoSuchAlgorithmException, IOException {

    SignatureCard signatureCard = createSignatureCard(null, null, PIN.STATE_PIN_BLOCKED);
    
    SMCCTestPINProvider pinProvider = new SMCCTestPINProvider("0000".toCharArray());

    signatureCard.createSignature(new ByteArrayInputStream("MOCCA"
        .getBytes("ASCII")), KeyboxName.CERITIFIED_KEYPAIR,
        pinProvider, null);

  }

  @Test
  public void testChangePin() throws CardNotSupportedException,
      LockedException, NotActivatedException, CancelledException,
      PINFormatException, SignatureCardException, InterruptedException {

    // set all initial pins to DEFAULT_SS_PIN (123456)
    PINMgmtSignatureCard signatureCard = (PINMgmtSignatureCard) createSignatureCard(
            STARCOSCardEmul.DEFAULT_SS_PIN, STARCOSCardEmul.DEFAULT_SS_PIN, PIN.STATE_RESET);
    
    for (PINSpec pinSpec : signatureCard.getPINSpecs()) {

      char[] pin = "123456".toCharArray();

      for (int i = pinSpec.getMinLength(); i <= pinSpec.getMaxLength(); i++) {
        signatureCard.verifyPIN(pinSpec, new SMCCTestPINProvider(pin));
        char[] newPin = new char[i];
        Arrays.fill(newPin, '0');
        signatureCard
            .changePIN(pinSpec, new ChangePINProvider(pin, newPin));
        signatureCard.verifyPIN(pinSpec, new SMCCTestPINProvider(newPin));
        pin = newPin;
      }
    }
  }

  @Test
  @Override
  public void testActivatePin() throws CardNotSupportedException,
      LockedException, NotActivatedException, CancelledException,
      PINFormatException, SignatureCardException, InterruptedException {

    PINMgmtSignatureCard signatureCard = (PINMgmtSignatureCard) createSignatureCard(
            null, null, PIN.STATE_PIN_NOTACTIVE);

    for (PINSpec pinSpec : signatureCard.getPINSpecs()) {

      char[] pin = "1234567890".substring(0, pinSpec.getMinLength()).toCharArray();

      boolean notActive = false;
      try {
        signatureCard.verifyPIN(pinSpec, new SMCCTestPINProvider(pin));
      } catch (NotActivatedException ex) {
        notActive = true;
      }
      assertTrue(notActive);

      signatureCard.activatePIN(pinSpec, new ChangePINProvider(null, pin));
      signatureCard.verifyPIN(pinSpec, new SMCCTestPINProvider(pin));
    }
  }

  @Test
  public void testVerifyInvalidPin() throws CardNotSupportedException,
      LockedException, NotActivatedException, CancelledException,
      PINFormatException, SignatureCardException, InterruptedException {

    PINMgmtSignatureCard signatureCard = (PINMgmtSignatureCard) createSignatureCard();

    for (PINSpec pinSpec : signatureCard.getPINSpecs()) {

      char[] invalidPin = "999999".toCharArray();
      int numInvalidTries = 2;
      InvalidPINProvider invalidPinProvider = new InvalidPINProvider(invalidPin, numInvalidTries);
      try {
        signatureCard.verifyPIN(pinSpec, invalidPinProvider);
      } catch (CancelledException ex) {
      } finally {
        assertTrue(invalidPinProvider.getProvided() == numInvalidTries);
      }
    }
  }

  @Test
  public void testChangeInvalidPin() throws CardNotSupportedException,
      LockedException, NotActivatedException, CancelledException,
      PINFormatException, SignatureCardException, InterruptedException {

    PINMgmtSignatureCard signatureCard = (PINMgmtSignatureCard) createSignatureCard();
    
    for (PINSpec pinSpec : signatureCard.getPINSpecs()) {

      char[] invalidPin = "999999".toCharArray();
      int numInvalidTries = 2;
      InvalidChangePINProvider invalidPinProvider =
              new InvalidChangePINProvider(invalidPin, invalidPin, numInvalidTries);

      try {
        signatureCard.changePIN(pinSpec, invalidPinProvider);
      } catch (CancelledException ex) {
      } finally {
        assertTrue(invalidPinProvider.getProvided() == numInvalidTries);
      }
    }
  }
}
