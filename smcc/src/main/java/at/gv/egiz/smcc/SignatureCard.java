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



package at.gv.egiz.smcc;

import at.gv.egiz.smcc.pin.gui.PINGUI;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import javax.smartcardio.Card;
import javax.smartcardio.CardTerminal;

public interface SignatureCard {

  public static class KeyboxName {

    public static KeyboxName SECURE_SIGNATURE_KEYPAIR = new KeyboxName(
        "SecureSignatureKeypair");
    public static KeyboxName CERTIFIED_KEYPAIR = new KeyboxName(
        "CertifiedKeypair");

    private String keyboxName_;

    private KeyboxName(String keyboxName_) {
      this.keyboxName_ = keyboxName_;
    }

    public static KeyboxName getKeyboxName(String keyBox) {
      if (SECURE_SIGNATURE_KEYPAIR.equals(keyBox)) {
        return SECURE_SIGNATURE_KEYPAIR;
      } else if (CERTIFIED_KEYPAIR.equals(keyBox)) {
        return CERTIFIED_KEYPAIR;
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

  public String getTerminalName();
  
  public byte[] getCertificate(KeyboxName keyboxName, PINGUI pinGUI)
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
   * @param pinGUI
   * @param domainId may be null.
   * @return
   * @throws SignatureCardException
   * @throws InterruptedException if applet is destroyed while in pin dialog
   */
  public byte[] getInfobox(String infobox, PINGUI pinGUI, String domainId)
      throws SignatureCardException, InterruptedException;

  /**
   * 
   * @param input
   * @param keyboxName
   * @param pinGUI
   * @param alg TODO
   * @return
   * @throws at.gv.egiz.smcc.SignatureCardException
   * @throws java.lang.InterruptedException if applet is destroyed while in pin dialog
   * @throws IOException 
   */
  public byte[] createSignature(InputStream input, KeyboxName keyboxName,
      PINGUI pinGUI, String alg) throws SignatureCardException, InterruptedException, IOException;

  /**
   * Sets the local for evtl. required callbacks (e.g. PINSpec)
   * @param locale must not be null;
   */
  public void setLocale(Locale locale);


}
