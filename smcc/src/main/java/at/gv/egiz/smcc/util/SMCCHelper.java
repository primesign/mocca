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


package at.gv.egiz.smcc.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.util.Locale;
import java.util.Map;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.smcc.CardNotSupportedException;
import at.gv.egiz.smcc.SignatureCard;
import at.gv.egiz.smcc.SignatureCardFactory;

public class SMCCHelper {

  public final static int NO_CARD = 0;
  public final static int PC_SC_NOT_SUPPORTED = 1;
  public final static int TERMINAL_NOT_PRESENT = 2;
  public final static int CARD_NOT_SUPPORTED = 3;
  public final static int CARD_FOUND = 4;

  private final Logger log = LoggerFactory.getLogger(SMCCHelper.class);

  protected SmartCardIO smartCardIO = new SmartCardIO();
  protected int resultCode = NO_CARD;
  protected SignatureCard signatureCard = null;
  protected static boolean useSWCard = false;

  public SMCCHelper() {	

    String osName = System.getProperty("os.name");
    if (osName.startsWith("Linux")) {
      File libFile;
      try {
        libFile = LinuxLibraryFinder.getLibraryPath("pcsclite", "1");
        System.setProperty("sun.security.smartcardio.library", libFile.getAbsolutePath());
      } catch (FileNotFoundException e) {
        log.error("PC/SC library not found", e);
      }
    }

    System.setProperty("sun.security.smartcardio.t0GetResponse", "false");
    update();
  }

  public synchronized void update() {
    update(-1);
  }

  public synchronized void update(int sleep) {
    SignatureCardFactory factory = SignatureCardFactory.getInstance();
    if (useSWCard) {
      log.info("Using SW Card");
      try {
        signatureCard = factory.createSignatureCard(null, null);
        resultCode = CARD_FOUND;
      } catch (CardNotSupportedException e) {
        resultCode = CARD_NOT_SUPPORTED;
        signatureCard = null;
      }
      return;
    }

    signatureCard = null;
    resultCode = NO_CARD;
    // find pcsc support
    if (smartCardIO.isPCSCSupported()) {
      // find supported card
      if (smartCardIO.isTerminalPresent()) {
        Map<CardTerminal, Card> newCards = null;
        if (sleep > 0) {
          smartCardIO.waitForInserted(sleep);

        }
        newCards = smartCardIO.getCards();
        for (CardTerminal cardTerminal : newCards.keySet()) {
          try {
            Card c = newCards.get(cardTerminal);
            if (c == null) {
              throw new CardNotSupportedException();
            }            
            signatureCard = factory.createSignatureCard(c, cardTerminal);
            if (log.isTraceEnabled()) {
              Object[] args = { signatureCard, cardTerminal.getName(),
                  toString(newCards.get(cardTerminal).getATR().getBytes()) };
              log.trace("Found supported card ({}) in terminal '{}', ATR = {}.", args);
            }
            resultCode = CARD_FOUND;
            break;

          } catch (CardNotSupportedException e) {
            Card c = newCards.get(cardTerminal);
            if (c != null) {
              Object[] args = { cardTerminal.getName(),
                  toString(c.getATR().getBytes()) };
              log.info("Found unsupported card in terminal '{}', ATR = {}.",
                  args);
            } else {
              log.info("Found unsupported card in terminal '{}' without ATR.",
                  cardTerminal.getName());
            }
            resultCode = CARD_NOT_SUPPORTED;
          }
        }
      } else {
        resultCode = TERMINAL_NOT_PRESENT;
      }
    } else {
      resultCode = PC_SC_NOT_SUPPORTED;
    }
  }

  public synchronized SignatureCard getSignatureCard(Locale locale) {
    if (signatureCard != null) {
      signatureCard.setLocale(locale);
    }
    return signatureCard;
  }

  public int getResultCode() {
    return resultCode;
  }

  public static String toString(byte[] b) {
    StringBuffer sb = new StringBuffer();
    sb.append('[');
    if (b != null && b.length > 0) {
      sb.append(Integer.toHexString((b[0] & 240) >> 4));
      sb.append(Integer.toHexString(b[0] & 15));
      for (int i = 1; i < b.length; i++) {
        sb.append((i % 32 == 0) ? '\n' : ':');
        sb.append(Integer.toHexString((b[i] & 240) >> 4));
        sb.append(Integer.toHexString(b[i] & 15));
      }
    }
    sb.append(']');
    return sb.toString();
  }

	public static byte[] toByteArray(int val) throws CardException {

		String hexString = Integer.toHexString(val);

		if (hexString.length() > 4) {
			throw new CardException(
					"Unexpected input length to toByteArray() utility method: "
							+ hexString.length());
		}

		byte high = 0x00;
		byte low = 0x00;

		if (hexString.length() <= 2) {

			low = (byte) Integer.parseInt(hexString, 16);
		} else {

			low = (byte) Integer.parseInt(hexString.substring(hexString
					.length() - 2), 16);
			high = (byte) Integer.parseInt(hexString.substring(0, hexString
					.length() - 2), 16);
		}

		return new byte[] { high, low };
	}  
  
	public static BigInteger createUnsignedBigInteger(byte[] data) {

		byte[] unsigned = new byte[data.length + 1];
		unsigned[0] = (byte) 0x00;
		System.arraycopy(data, 0, unsigned, 1, data.length);

		return new BigInteger(unsigned);
	}	
	
  public static boolean isUseSWCard() {
    return useSWCard;
  }

  public static void setUseSWCard(boolean useSWCard) {
    SMCCHelper.useSWCard = useSWCard;
  }
}
