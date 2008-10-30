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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.smartcardio.ATR;
import javax.smartcardio.Card;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A factory for creating {@link SignatureCard}s from {@link Card}s. 
 */
public class SignatureCardFactory {
  
  /**
   * This class represents a supported smart card. 
   */
  private class SupportedCard {
    
    /**
     * The ATR pattern. 
     */
    private byte[] atrPattern;
    
    /**
     * The ATR mask.
     */
    private byte[] atrMask;
    
    /**
     * The implementation class.
     */
    private String impl;

    /**
     * Creates a new SupportedCard instance with the given ATR pattern and mask
     * und the corresponding implementation class.
     * 
     * @param atrPattern
     *          the ATR pattern
     * @param atrMask
     *          the ATR mask
     * @param implementationClass
     *          the name of the implementation class
     * 
     * @throws NullPointerException
     *           if <code>atrPattern</code> or <code>atrMask</code> is
     *           <code>null</code>.
     * @throws IllegalArgumentException
     *           if the lengths of <code>atrPattern</code> and
     *           <code>atrMask</code> of not equal.
     */
    public SupportedCard(byte[] atrPattern, byte[] atrMask, String implementationClass) {
      if (atrPattern.length != atrMask.length) {
        throw new IllegalArgumentException("Length of 'atr' and 'mask' must be equal.");
      }
      this.atrPattern = atrPattern;
      this.atrMask = atrMask;
      this.impl = implementationClass;
    }

    /**
     * Returns true if the given ATR matches the ATR pattern and mask this
     * SupportedCard object.
     * 
     * @param atr
     *          the ATR
     * 
     * @return <code>true</code> if the given ATR matches the ATR pattern and
     *         mask of this SupportedCard object, or <code>false</code>
     *         otherwise.
     */
    public boolean matches(ATR atr) {

      byte[] bytes = atr.getBytes();
      if (bytes == null) {
        return false;
      }
      if (bytes.length < atrMask.length) {
        // we cannot test for equal length here, as we get ATRs with 
        // additional bytes on systems using PCSClite (e.g. linux and OS X) sometimes
        return false;
      }

      int l = Math.min(atrMask.length, bytes.length);
      for (int i = 0; i < l; i++) {
        if ((bytes[i] & atrMask[i]) != atrPattern[i]) {
          return false;
        }
      }
      return true;
      
    }

    /**
     * @return the corresponding implementation class.
     */
    public String getImplementationClassName() {
      return impl;
    }
    
  }

  /**
   * Logging facility.
   */
  private static Log log = LogFactory.getLog(SignatureCardFactory.class);
  
  /**
   * The instance to be returned by {@link #getInstance()}.
   */
  private static SignatureCardFactory instance;
  
  /**
   * The list of supported smart cards.
   */
  private List<SupportedCard> supportedCards;
  
  /**
   * @return an instance of this SignatureCardFactory.
   */
  public static synchronized SignatureCardFactory getInstance() {
    if (instance == null) {
      instance = new SignatureCardFactory();
    }
    return instance;
  }

  /**
   * Private constructor.
   */
  private SignatureCardFactory() {
    
    supportedCards = new ArrayList<SupportedCard>();

    // e-card
    supportedCards.add(new SupportedCard(
        // ATR  (3b:bd:18:00:81:31:fe:45:80:51:02:00:00:00:00:00:00:00:00:00:00:00)
        new byte[] {
            (byte) 0x3b, (byte) 0xbd, (byte) 0x18, (byte) 0x00, (byte) 0x81, (byte) 0x31, (byte) 0xfe, (byte) 0x45, 
            (byte) 0x80, (byte) 0x51, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 
        },
        // mask (ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:00:00:00:00:00:00:00:00:00:00:00)
        new byte[] {
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 
        },
        "at.gv.egiz.smcc.STARCOSCard"));

    // a-sign premium
    supportedCards.add(new SupportedCard(
        // ATR  (3b:bf:11:00:81:31:fe:45:45:50:41:00:00:00:00:00:00:00:00:00:00:00:00:00)
        new byte[] {
            (byte) 0x3b, (byte) 0xbf, (byte) 0x11, (byte) 0x00, (byte) 0x81, (byte) 0x31, (byte) 0xfe, (byte) 0x45, 
            (byte) 0x45, (byte) 0x50, (byte) 0x41, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 
        },
        // mask (ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:00:00:00:00:00:00:00:00:00:00:00:00:00)
        new byte[] {
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
        },
        "at.gv.egiz.smcc.ACOSCard"));
    
  }

  /**
   * Creates a SignatureCard instance with the given smart card.
   * 
   * @param card
   *          the smart card, or <code>null</code> if a software card should be
   *          created
   * 
   * @return a SignatureCard instance
   * 
   * @throws CardNotSupportedException
   *           if no implementation of the given <code>card</code> could be
   *           found
   */
  public SignatureCard createSignatureCard(Card card)
      throws CardNotSupportedException {
    
    if(card == null) {
      SignatureCard sCard = new SWCard();
      sCard.init(card);
      return sCard;
    }

    ATR atr = card.getATR();
    Iterator<SupportedCard> cards = supportedCards.iterator();
    while (cards.hasNext()) {
      SupportedCard supportedCard = cards.next();
      if(supportedCard.matches(atr)) {
        
        ClassLoader cl = SignatureCardFactory.class.getClassLoader();
        SignatureCard sc;
        try {
          Class<?> scClass = cl.loadClass(supportedCard.getImplementationClassName());
          sc = (SignatureCard) scClass.newInstance();
          sc.init(card);
          return sc;

        } catch (ClassNotFoundException e) {
          log.warn("Cannot find signature card implementation class.", e);
          throw new CardNotSupportedException("Cannot find signature card implementation class.", e);
        } catch (InstantiationException e) {
          log.warn("Failed to instantiate signature card implementation.", e);
          throw new CardNotSupportedException("Failed to instantiate signature card implementation.", e);
        } catch (IllegalAccessException e) {
          log.warn("Failed to instantiate signature card implementation.", e);
          throw new CardNotSupportedException("Failed to instantiate signature card implementation.", e);
        }
        
      }
    }
    
    throw new CardNotSupportedException("Card not supported: ATR=" + toString(atr.getBytes()));
    
  }
  
  public static String toString(byte[] b) {
    StringBuffer sb = new StringBuffer();
    if (b != null && b.length > 0) {
      sb.append(Integer.toHexString((b[0] & 240) >> 4));
      sb.append(Integer.toHexString(b[0] & 15));
    }
    for(int i = 1; i < b.length; i++) {
      sb.append(':');
      sb.append(Integer.toHexString((b[i] & 240) >> 4));
      sb.append(Integer.toHexString(b[i] & 15));
    }
    return sb.toString();
  }


}
