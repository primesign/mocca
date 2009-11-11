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

import at.gv.egiz.smcc.ccid.CCID;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

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

    @Override
    public String toString() {
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
   * @param input
   * @param keyboxName
   * @param provider
   * @param alg TODO
   * @return
   * @throws at.gv.egiz.smcc.SignatureCardException
   * @throws java.lang.InterruptedException if applet is destroyed while in pin dialog
   * @throws IOException 
   */
  public byte[] createSignature(InputStream input, KeyboxName keyboxName,
      PINProvider provider, String alg) throws SignatureCardException, InterruptedException, IOException;

  public CCID getReader();

  /**
   * Sets the local for evtl. required callbacks (e.g. PINSpec)
   * @param locale must not be null;
   */
  public void setLocale(Locale locale);


}
