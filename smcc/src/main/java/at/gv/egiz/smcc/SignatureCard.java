//Copyright (C) 2002 IAIK
//http://jce.iaik.at
//
//Copyright (C) 2003 Stiftung Secure Information and 
//                 Communication Technologies SIC
//http://www.sic.st
//
//All rights reserved.
//
//This source is provided for inspection purposes and recompilation only,
//unless specified differently in a contract with IAIK. This source has to
//be kept in strict confidence and must not be disclosed to any third party
//under any circumstances. Redistribution in source and binary forms, with
//or without modification, are <not> permitted in any case!
//
//THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
//ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
//IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
//ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
//FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
//DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
//OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
//HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
//LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
//OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
//SUCH DAMAGE.
//
//
package at.gv.egiz.smcc;

import java.util.List;
import java.util.Locale;

import java.util.Map;
import javax.smartcardio.Card;
import javax.smartcardio.CardTerminal;

public interface SignatureCard {

  public static class KeyboxName {

    public static KeyboxName SECURE_SIGNATURE_KEYPAIR = new KeyboxName(
        "SecureSignatureKeypair");
    public static KeyboxName CERITIFIED_KEYPAIR = new KeyboxName(
        "CertifiedKeypair");

    private String keyboxName_;

    private KeyboxName(String keyboxName_) {
      this.keyboxName_ = keyboxName_;
    }

    public static KeyboxName getKeyboxName(String keyBox) {
      if (SECURE_SIGNATURE_KEYPAIR.equals(keyBox)) {
        return SECURE_SIGNATURE_KEYPAIR;
      } else if (CERITIFIED_KEYPAIR.equals(keyBox)) {
        return CERITIFIED_KEYPAIR;
      } else {
        return new KeyboxName(keyBox);
      }
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof String) {
        return obj.equals(keyboxName_);
      }
      if (obj instanceof KeyboxName) {
        return ((KeyboxName) obj).keyboxName_.equals(keyboxName_);
      } else {
        return super.equals(obj);
      }
    }

    public String getKeyboxName() {
      return keyboxName_;
    }

  }

  public void init(Card card, CardTerminal cardTerminal);
  
  public Card getCard();

  public byte[] getCertificate(KeyboxName keyboxName)
      throws SignatureCardException, InterruptedException;
  
  public void disconnect(boolean reset);
  
  /**
   * Performs a reset of the card.
   * 
   * @throws SignatureCardException if reset fails.
   */
  public void reset() throws SignatureCardException;

  /**
   * 
   * @param infobox
   * @param provider
   * @param domainId may be null.
   * @return
   * @throws SignatureCardException
   * @throws InterruptedException if applet is destroyed while in pin dialog
   */
  public byte[] getInfobox(String infobox, PINProvider provider, String domainId)
      throws SignatureCardException, InterruptedException;

  /**
   * 
   * @param hash
   * @param keyboxName
   * @param provider
   * @return
   * @throws at.gv.egiz.smcc.SignatureCardException
   * @throws java.lang.InterruptedException if applet is destroyed while in pin dialog
   */
  public byte[] createSignature(byte[] hash, KeyboxName keyboxName,
      PINProvider provider) throws SignatureCardException, InterruptedException;

  /**
   * Get the KIDs for all available PINs and the corresponding PINSpecs
   * @return array of KIDs
   */
  public List<PINSpec> getPINSpecs();

  /**
   *
   * @param pin may be null to test the PIN status
   * @param kid
   * @return the number of remaining retries or -1
   * @throws at.gv.egiz.smcc.LockedException
   * @throws at.gv.egiz.smcc.NotActivatedException
   * @throws at.gv.egiz.smcc.SignatureCardException
   */
  public int verifyPIN(String pin, byte kid) throws LockedException, NotActivatedException, SignatureCardException;

  /**
   * Sets the local for evtl. required callbacks (e.g. PINSpec)
   * @param locale must not be null;
   */
  public void setLocale(Locale locale);
  

}
