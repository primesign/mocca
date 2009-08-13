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

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import javax.smartcardio.Card;

import org.junit.Test;

import at.gv.egiz.smcc.SignatureCard.KeyboxName;
import at.gv.egiz.smcc.acos.A04ApplDEC;

@SuppressWarnings("restriction")
public abstract class CardTest {

  public class TestPINProvider implements PINProvider {
    
    int provided = 0;
  
    char[] pin;
  
    public TestPINProvider(char[] pin) {
      super();
      this.pin = pin;
    }
  
    @Override
    public char[] providePIN(PINSpec spec, int retries)
        throws CancelledException, InterruptedException {
      provided++;
      return pin;
    }

    public int getProvided() {
      return provided;
    }
  
  }

  public class TestChangePINProvider extends TestPINProvider implements
      ChangePINProvider {
  
    char[] oldPin;
  
    public TestChangePINProvider(char[] oldPin, char[] pin) {
      super(pin);
      this.oldPin = oldPin;
    }
  
    @Override
    public char[] provideOldPIN(PINSpec spec, int retries)
        throws CancelledException, InterruptedException {
      return oldPin;
    }
  
  }

  public class TestInvalidPINProvider implements PINProvider {

    int provided = 0;
    int numWrongTries = 0;

    char[] pin;

    public TestInvalidPINProvider(char[] pin, int numWrongTries) {
      super();
      this.pin = pin;
      this.numWrongTries = numWrongTries;
    }

    @Override
    public char[] providePIN(PINSpec spec, int retries)
        throws CancelledException, InterruptedException {
      if (provided >= numWrongTries) {
        throw new CancelledException("Number of wrong tries reached: " + provided);
      } else {
        provided++;
        return pin;
      }
    }

    public int getProvided() {
      return provided;
    }
  }

  public class TestInvalidChangePINProvider implements ChangePINProvider {

    int provided = 0;
    int numWrongTries = 0;

    char[] pin;
    char[] oldPin;

    /** emulate ChangePinProvider */
    public TestInvalidChangePINProvider(char[] oldPin, char[] newPin, int numWrongTries) {
      super();
      this.pin = newPin;
      this.oldPin = oldPin;
      this.numWrongTries = numWrongTries;
    }

    @Override
    public char[] providePIN(PINSpec spec, int retries)
        throws CancelledException, InterruptedException {
      return pin;
    }

    public int getProvided() {
      return provided;
    }

    @Override
    public char[] provideOldPIN(PINSpec spec, int retries)
        throws CancelledException, InterruptedException {
      if (provided >= numWrongTries) {
        throw new CancelledException("Number of wrong tries reached: " + provided);
      } else {
        provided++;
        return oldPin;
      }
    }
  }

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
    
    TestPINProvider pinProvider = new TestPINProvider(pin);

    byte[] idlink = signatureCard.getInfobox("IdentityLink",
        pinProvider, null);
    assertNotNull(idlink);
    assertTrue(Arrays.equals(idlink, A04ApplDEC.IDLINK));
    assertEquals(1, pinProvider.provided);

  }

  @Test(expected = CancelledException.class)
  public void testSignSIGCancel() throws SignatureCardException,
      InterruptedException, CardNotSupportedException,
      NoSuchAlgorithmException, UnsupportedEncodingException {
      
        SignatureCard signatureCard = createSignatureCard();
      
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] hash = md.digest("MOCCA".getBytes("ASCII"));
      
        PINProvider pinProvider = new PINProvider() {
          @Override
          public char[] providePIN(PINSpec spec, int retries)
              throws CancelledException, InterruptedException {
            throw new CancelledException();
          }
        };
      
        signatureCard.createSignature(hash, KeyboxName.SECURE_SIGNATURE_KEYPAIR,
            pinProvider);
      
      }

  @Test(expected = CancelledException.class)
  public void testSignDECCancel() throws SignatureCardException,
      InterruptedException, CardNotSupportedException,
      NoSuchAlgorithmException, UnsupportedEncodingException {
      
        SignatureCard signatureCard = createSignatureCard();
      
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] hash = md.digest("MOCCA".getBytes("ASCII"));
      
        PINProvider pinProvider = new PINProvider() {
          @Override
          public char[] providePIN(PINSpec spec, int retries)
              throws CancelledException, InterruptedException {
            throw new CancelledException();
          }
        };
      
        signatureCard.createSignature(hash, KeyboxName.CERITIFIED_KEYPAIR,
            pinProvider);
      
      }

  @Test(expected = InterruptedException.class)
  public void testSignSIGInterrrupted() throws SignatureCardException,
      InterruptedException, CardNotSupportedException,
      NoSuchAlgorithmException, UnsupportedEncodingException {
      
        SignatureCard signatureCard = createSignatureCard();
      
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] hash = md.digest("MOCCA".getBytes("ASCII"));
      
        PINProvider pinProvider = new PINProvider() {
          @Override
          public char[] providePIN(PINSpec spec, int retries)
              throws CancelledException, InterruptedException {
            throw new InterruptedException();
          }
        };
      
        signatureCard.createSignature(hash, KeyboxName.SECURE_SIGNATURE_KEYPAIR,
            pinProvider);
      
      }

  @Test(expected = InterruptedException.class)
  public void testSignDECInterrrupted() throws SignatureCardException,
      InterruptedException, CardNotSupportedException,
      NoSuchAlgorithmException, UnsupportedEncodingException {
      
        SignatureCard signatureCard = createSignatureCard();
      
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] hash = md.digest("MOCCA".getBytes("ASCII"));
      
        PINProvider pinProvider = new PINProvider() {
          @Override
          public char[] providePIN(PINSpec spec, int retries)
              throws CancelledException, InterruptedException {
            throw new InterruptedException();
          }
        };
      
        signatureCard.createSignature(hash, KeyboxName.CERITIFIED_KEYPAIR,
            pinProvider);
      
      }

  @Test(expected = CancelledException.class)
  public void testSignSIGConcurrent() throws SignatureCardException,
      InterruptedException, CardNotSupportedException,
      NoSuchAlgorithmException, UnsupportedEncodingException {
      
        final SignatureCard signatureCard = createSignatureCard();
      
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] hash = md.digest("MOCCA".getBytes("ASCII"));
      
        PINProvider pinProvider = new PINProvider() {
          @Override
          public char[] providePIN(PINSpec spec, int retries)
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
      
        signatureCard.createSignature(hash, KeyboxName.SECURE_SIGNATURE_KEYPAIR,
            pinProvider);
      
      }

  @Test(expected = CancelledException.class)
  public void testSignDECConcurrent() throws SignatureCardException,
      InterruptedException, CardNotSupportedException,
      NoSuchAlgorithmException, UnsupportedEncodingException {
      
        final SignatureCard signatureCard = createSignatureCard();
      
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] hash = md.digest("MOCCA".getBytes("ASCII"));
      
        PINProvider pinProvider = new PINProvider() {
          @Override
          public char[] providePIN(PINSpec spec, int retries)
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
      
        signatureCard.createSignature(hash, KeyboxName.CERITIFIED_KEYPAIR,
            pinProvider);
      
      }

  @Test
  public void testGetPinSpecs() throws CardNotSupportedException {
  
    PINMgmtSignatureCard signatureCard = (PINMgmtSignatureCard) createSignatureCard();
  
    List<PINSpec> specs = signatureCard.getPINSpecs();
    assertNotNull(specs);
    assertTrue(specs.size() > 0);
  
  }

  @Test(expected = SignatureCardException.class)
  public void testActivatePin() throws SignatureCardException,
      InterruptedException, CardNotSupportedException,
      NoSuchAlgorithmException, UnsupportedEncodingException {
      
        PINMgmtSignatureCard signatureCard = (PINMgmtSignatureCard) createSignatureCard();
      
        PINProvider pinProvider = new PINProvider() {
          @Override
          public char[] providePIN(PINSpec spec, int retries)
              throws CancelledException, InterruptedException {
            throw new CancelledException();
          }
        };
      
        List<PINSpec> specs = signatureCard.getPINSpecs();
      
        signatureCard.activatePIN(specs.get(0), pinProvider);
      }

}