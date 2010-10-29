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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardTerminal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A factory for creating {@link SignatureCard}s from {@link Card}s. 
 */
public class SignatureCardFactory {

  public static boolean ENFORCE_RECOMMENDED_PIN_LENGTH = false;
  
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
  private final Logger log = LoggerFactory.getLogger(SignatureCardFactory.class);
  
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
    
    // e-card G3
    supportedCards.add(new SupportedCard(
        // ATR  (3b:dd:96:ff:81:b1:fe:45:1f:03:80:31:b0:52:02:03:64:04:1b:b4:22:81:05:18)
        new byte[] {
            (byte) 0x3b, (byte) 0xdd, (byte) 0x96, (byte) 0xff, (byte) 0x81, (byte) 0xb1, (byte) 0xfe, (byte) 0x45, 
            (byte) 0x1f, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
        },
        // mask (
        new byte[] {
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 
            (byte) 0xff, (byte) 0xff, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 
        },
        "at.gv.egiz.smcc.STARCOSCard"));

    // a-sign premium (EPA)
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

    // a-sign premium (MCA)
    supportedCards.add(new SupportedCard(
        // ATR  (3b:bf:11:00:81:31:fe:45:45:50:41:00:00:00:00:00:00:00:00:00:00:00:00:00)
        new byte[] {
            (byte) 0x3b, (byte) 0xbf, (byte) 0x11, (byte) 0x00, (byte) 0x81, (byte) 0x31, (byte) 0xfe, (byte) 0x45, 
            (byte) 0x4D, (byte) 0x43, (byte) 0x41, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 
        },
        // mask (ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:00:00:00:00:00:00:00:00:00:00:00:00:00)
        new byte[] {
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
        },
        "at.gv.egiz.smcc.ACOSCard"));

    // BELPIC
    supportedCards.add(new SupportedCard(
            // ATR (3b:98:13:40:0A:A5:03:01:01:01:AD:13:11)
            new byte[] { (byte) 0x3b, (byte) 0x98, (byte) 0x13,
                    (byte) 0x40, (byte) 0x0a, (byte) 0xa5, (byte) 0x03,
                    (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0xad,
                    (byte) 0x13, (byte) 0x11 },
            // mask (ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff)
            new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff },
            "at.gv.egiz.smcc.BELPICCard"));
    supportedCards.add(new SupportedCard(
            // ATR [3b:98:_94_:40:_ff_:a5:03:01:01:01:ad:13:_10_]
            new byte[] { (byte) 0x3b, (byte) 0x98, (byte) 0x94,
                    (byte) 0x40, (byte) 0xff, (byte) 0xa5, (byte) 0x03,
                    (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0xad,
                    (byte) 0x13, (byte) 0x10 },
            // mask (ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff)
            new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff },
            "at.gv.egiz.smcc.BELPICCard"));
    supportedCards.add(new SupportedCard(
            // ATR [3b:98:_94_:40:0a:a5:03:01:01:01:ad:13:_10_]
            new byte[] { (byte) 0x3b, (byte) 0x98, (byte) 0x94,
                    (byte) 0x40, (byte) 0x0a, (byte) 0xa5, (byte) 0x03,
                    (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0xad,
                    (byte) 0x13, (byte) 0x10 },
            // mask (ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff)
            new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff },
            "at.gv.egiz.smcc.BELPICCard"));
    supportedCards.add(new SupportedCard(
            // ATR [3b:98:_95_:40:0a:a5:_07_:01:01:01:ad:13:_20_]
            new byte[] { (byte) 0x3b, (byte) 0x98, (byte) 0x95,
                    (byte) 0x40, (byte) 0x0a, (byte) 0xa5, (byte) 0x07,
                    (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0xad,
                    (byte) 0x13, (byte) 0x20 },
            // mask (ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff)
            new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff },
            "at.gv.egiz.smcc.BELPICCard"));

    // DNIe
    supportedCards.add(new SupportedCard(
            // ATR [3b:7f:38:00:00:00:6a:44:4e:49:65:20:02:4c:34:01:13:03:90:00]
            new byte[] { (byte) 0x3b, (byte) 0x7F, (byte) 0x38,
                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x6A,
                    (byte) 0x44, (byte) 0x4E, (byte) 0x49, (byte) 0x65,
                    (byte) 0x20, (byte) 0x02, (byte) 0x4C, (byte) 0x34, (byte) 0x01, (byte) 0x13, (byte) 0x03, (byte) 0x90, (byte) 0x00 },
            // mask (ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff)
            new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff },
            "at.gv.egiz.smcc.DNIeCard"));
    
    // ITCards
    supportedCards.add(new SupportedCard(
    // ATR =
    // [3b:ff:18:00:ff:81:31:fe:55:00:6b:02:09:02:00:01:11:01:43:4e:53:11:31:80:8e]
            new byte[] { (byte) 0x3b, (byte) 0xff, (byte) 0x18,
                    (byte) 0x00, (byte) 0xff, (byte) 0x81, (byte) 0x31,
                    (byte) 0xfe, (byte) 0x55, (byte) 0x00, (byte) 0x6b,
                    (byte) 0x02, (byte) 0x09 /*
                                             * , (byte) 0x02, (byte) 0x00,
                                             * (byte) 0x01, (byte) 0x11,
                                             * (byte) 0x01, (byte) 0x43,
                                             * (byte) 0x4e, (byte) 0x53,
                                             * (byte) 0x11, (byte) 0x31,
                                             * (byte) 0x80, (byte) 0x8e
                                             */
            },
            // mask (ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff)
            new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff /*
                                             * , (byte) 0xff, (byte) 0xff,
                                             * (byte) 0xff, (byte) 0xff,
                                             * (byte) 0xff, (byte) 0xff,
                                             * (byte) 0xff, (byte) 0xff,
                                             * (byte) 0xff, (byte) 0xff,
                                             * (byte) 0xff, (byte) 0xff
                                             */
            }, "at.gv.egiz.smcc.ITCard"));
    supportedCards.add(new SupportedCard(
        // ATR
        // (3B:FF:18:00:FF:C1:0A:31:FE:55:00:6B:05:08:C8:05:01:01:01:43:4E:53:10:31:80:1C)
        new byte[] { (byte) 0x3b, (byte) 0xff, (byte) 0x18,
                (byte) 0x00, (byte) 0xFF, (byte) 0xC1, (byte) 0x0a,
                (byte) 0x31, (byte) 0xfe, (byte) 0x55, (byte) 0x00,
                (byte) 0x6B, (byte) 0x05, (byte) 0x08, (byte) 0xC8,
                (byte) 0x05, (byte) 0x01, (byte) 0x01, (byte) 0x01,
                (byte) 0x43, (byte) 0x4E, (byte) 0x53, (byte) 0x10,
                (byte) 0x31, (byte) 0x80, (byte) 0x1C },
        // mask
        // (ff:ff:ff:00:00:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:00:00:00:00)
        new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0xff, (byte) 0xff, (byte) 0xff },
        "at.gv.egiz.smcc.ITCard"));

    // EstEID cards return different ATRs depending on the reader device
    supportedCards.add(new SupportedCard(
            // ATR
            // (3B:5E:11:FF:45:73:74:45:49:44:20:76:65:72:20:31:2E:30)
            new byte[] { (byte) 0x3b, (byte) 0x00, (byte) 0x00,
                    (byte) 0xff, (byte) 0x45, (byte) 0x73, (byte) 0x74,
                    (byte) 0x45, (byte) 0x49, (byte) 0x44, (byte) 0x20,
                    (byte) 0x76, (byte) 0x65, (byte) 0x72, (byte) 0x20,
                    (byte) 0x31, (byte) 0x2e, (byte) 0x30 },
            // mask
            // (ff:00:00:00:00:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff)
            new byte[] { (byte) 0xff, (byte) 0x00, (byte) 0x00,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff },
            "at.gv.egiz.smcc.EstEIDCard"));

    // EstEID cards return different ATRs depending on the reader device
    supportedCards.add(new SupportedCard(
            // ATR
            // (3B:DE:18:FF:C0:80:B1:FE:45:1F:03:45:73:74:45:49:44:20:76:65:72:20:31:2E:30:2B)
            new byte[] { (byte) 0x3b, (byte) 0xde, (byte) 0x18,
                    (byte) 0xff, (byte) 0xc0, (byte) 0x80, (byte) 0xb1,
                    (byte) 0xfe, (byte) 0x45, (byte) 0x1f, (byte) 0x03,
                    (byte) 0x45, (byte) 0x73, (byte) 0x74, (byte) 0x45,
                    (byte) 0x49, (byte) 0x44, (byte) 0x20, (byte) 0x76,
                    (byte) 0x65, (byte) 0x72, (byte) 0x20, (byte) 0x31,
                    (byte) 0x2e, (byte) 0x30, (byte) 0x2b },
            // mask
            // (ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:00:00:00:00)
            new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff },
            "at.gv.egiz.smcc.EstEIDCard"));
    
    supportedCards.add(new SupportedCard(
        // ATR (3B:7D:95:00:00:80:31:80:65:B0:83:11:C0:A9:83:00:90:00 -
        // 00:00:00:00)
        new byte[] { (byte) 0x3b, (byte) 0x7d, (byte) 0x95,
                (byte) 0x00, (byte) 0x00, (byte) 0x80, (byte) 0x31,
                (byte) 0x80, (byte) 0x65, (byte) 0xb0, (byte) 0x83,
                (byte) 0x11, (byte) 0xc0, (byte) 0xa9, (byte) 0x83,
                (byte) 0x00, (byte) 0x90, (byte) 0x00 },
        // mask
        // (ff:ff:ff:00:00:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:00:00:00:00)
        new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0x00, (byte) 0x00, (byte) 0xff, (byte) 0xff,
                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0x00, (byte) 0xff, (byte) 0x00 },
        "at.gv.egiz.smcc.PtEidCard"));
    
    supportedCards.add(new SupportedCard(
        // SwissSign ATR 3b:fa:18:00:02:c1:0a:31:fe:58:4b:53:77:69:73:73:53:69:67:6e:89
        new byte[] { (byte) 0x3b, (byte) 0xfa, (byte) 0x18,
                (byte) 0x00, (byte) 0x02, (byte) 0xc1, (byte) 0x0a,
                (byte) 0x31, (byte) 0xfe, (byte) 0x58, (byte) 0x4b,
                'S', 'w', 'i', 's', 's', 'S', 'i', 'g', 'n',
                (byte) 0x89},
        // mask
        new byte[] { 
                (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0xff, (byte) 0xff, (byte) 0xff},
        "at.gv.egiz.smcc.SuisseIDCard"));
    
    supportedCards.add(new SupportedCard(
        // QuoVadis ATR 3b:f2:18:00:02:c1:0a:31:fe:58:c8:08:74
        new byte[] { (byte) 0x3b, (byte) 0xf2, (byte) 0x18,
                (byte) 0x00, (byte) 0x02, (byte) 0xc1, (byte) 0x0a,
                (byte) 0x31, (byte) 0xfe, (byte) 0x58, (byte) 0xc8,
                (byte) 0x08, (byte) 0x74},
        // mask
        new byte[] {
                (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0xff},
        "at.gv.egiz.smcc.SuisseIDCard"));

  }

  /**
   * Creates a SignatureCard instance with the given smart card.
   * 
   * @param card
   *          the smart card, or <code>null</code> if a software card should be
   *          created
   * @param cardTerminal TODO
   * 
   * @return a SignatureCard instance
   * 
   * @throws CardNotSupportedException
   *           if no implementation of the given <code>card</code> could be
   *           found
   */
  public SignatureCard createSignatureCard(Card card, CardTerminal cardTerminal)
      throws CardNotSupportedException {
    
    if(card == null) {
      SignatureCard sCard = new SWCard();
      sCard.init(card, cardTerminal);
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
          
          sc = ExclSignatureCardProxy.newInstance(sc);
          
          sc.init(card, cardTerminal);

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
