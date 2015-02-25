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
		 * The historical byte pattern.
		 */
		private byte[] historicalBytesPattern;

		/**
		 * The implementation class.
		 */
		private String impl;

		/**
		 * Creates a new SupportedCard instance with the given ATR pattern and
		 * mask und the corresponding implementation class.
		 * 
		 * @param atrPattern
		 *            the ATR pattern
		 * @param atrMask
		 *            the ATR mask
		 * @param implementationClass
		 *            the name of the implementation class
		 * 
		 * @throws NullPointerException
		 *             if <code>atrPattern</code> or <code>atrMask</code> is
		 *             <code>null</code>.
		 * @throws IllegalArgumentException
		 *             if the lengths of <code>atrPattern</code> and
		 *             <code>atrMask</code> of not equal.
		 */
		public SupportedCard(byte[] atrPattern, byte[] atrMask,
				String implementationClass) {
			if (atrPattern.length != atrMask.length) {
				throw new IllegalArgumentException(
						"Length of 'atr' and 'mask' must be equal.");
			}
			this.atrPattern = atrPattern;
			this.atrMask = atrMask;
			this.impl = implementationClass;
		}

		/**
		 * Creates a new SupportedCard instance with the given ATR pattern and
		 * mask and the corresponding implementation class.
		 * 
		 * @param atrPattern
		 *            the ATR pattern
		 * @param atrSubPattern
		 *            the ATR sub pattern *
		 * @param atrMask
		 *            the ATR mask
		 * @param implementationClass
		 *            the name of the implementation class
		 * 
		 * @throws NullPointerException
		 *             if <code>atrPattern</code> or <code>atrMask</code> is
		 *             <code>null</code>.
		 * @throws IllegalArgumentException
		 *             if the lengths of <code>atrPattern</code> and
		 *             <code>atrMask</code> of not equal.
		 */
		public SupportedCard(byte[] atrPattern, byte[] historicalBytesPattern,
				byte[] atrMask, String implementationClass) {

			this(atrPattern, atrMask, implementationClass);
			this.historicalBytesPattern = historicalBytesPattern;
		}

		/**
		 * Returns true if the given ATR matches the ATR pattern and mask this
		 * SupportedCard object.
		 * 
		 * @param atr
		 *            the ATR
		 * 
		 * @return <code>true</code> if the given ATR matches the ATR pattern
		 *         and mask of this SupportedCard object, or <code>false</code>
		 *         otherwise.
		 */
		public boolean matches(ATR atr) {

			byte[] bytes = atr.getBytes();
			if (bytes == null) {
				return false;
			}
			if (bytes.length < atrMask.length) {
				// we cannot test for equal length here, as we get ATRs with
				// additional bytes on systems using PCSClite (e.g. linux and OS
				// X) sometimes
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
		 * Returns true if the historical bytes of the given ATR contain the
		 * historical bytes pattern of this SupportedCard object.
		 * 
		 * @param atr
		 *            the ATR
		 * 
		 * @return <code>true</code> if the historical bytes of the given ATR
		 *         contain the historical bytes pattern of this SupportedCard
		 *         object, or <code>false</code> otherwise.
		 */
		public boolean matchesHistoricalBytesPattern(ATR atr) {

			byte[] historicalBytes = atr.getHistoricalBytes();
			if (historicalBytes == null
					|| this.historicalBytesPattern == null
					|| this.historicalBytesPattern.length > historicalBytes.length) {

				return false;
			}

			int[] failure = computeFailure(this.historicalBytesPattern);

			int j = 0;

			for (int i = 0; i < historicalBytes.length; i++) {
				while (j > 0
						&& this.historicalBytesPattern[j] != historicalBytes[i]) {
					j = failure[j - 1];
				}
				if (this.historicalBytesPattern[j] == historicalBytes[i]) {
					j++;
				}
				if (j == this.historicalBytesPattern.length) {
					return true;
				}
			}
			return false;
		}

		private int[] computeFailure(byte[] pattern) {
			int[] failure = new int[pattern.length];

			int j = 0;
			for (int i = 1; i < pattern.length; i++) {
				while (j > 0 && pattern[j] != pattern[i]) {
					j = failure[j - 1];
				}
				if (pattern[j] == pattern[i]) {
					j++;
				}
				failure[i] = j;
			}

			return failure;
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
	private final Logger log = LoggerFactory
			.getLogger(SignatureCardFactory.class);

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
				// ATR
				// (3b:bd:18:00:81:31:fe:45:80:51:02:00:00:00:00:00:00:00:00:00:00:00)
				new byte[] { (byte) 0x3b, (byte) 0xbd, (byte) 0x18,
						(byte) 0x00, (byte) 0x81, (byte) 0x31, (byte) 0xfe,
						(byte) 0x45, (byte) 0x80, (byte) 0x51, (byte) 0x02,
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
						(byte) 0x00, (byte) 0x00, (byte) 0x00 },
				// mask
				// (ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:00:00:00:00:00:00:00:00:00:00:00)
				new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
						(byte) 0x00, (byte) 0x00, (byte) 0x00 },
				"at.gv.egiz.smcc.STARCOSCard"));

		// e-card G3
		supportedCards.add(new SupportedCard(
				// ATR
				// (3b:dd:96:ff:81:b1:fe:45:1f:03:80:31:b0:52:02:03:64:04:1b:b4:22:81:05:18)
				new byte[] { (byte) 0x3b, (byte) 0xdd, (byte) 0x96,
						(byte) 0xff, (byte) 0x81, (byte) 0xb1, (byte) 0xfe,
						(byte) 0x45, (byte) 0x1f, (byte) 0x03, (byte) 0x00,
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
						(byte) 0x00 },
				// mask
				// (ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:00:00:00:00:00:00:00:00:00:00:00:00:00:00)
				new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0x00,
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
						(byte) 0x00 }, "at.gv.egiz.smcc.STARCOSCard"));

		// e-card G4
		supportedCards.add(new SupportedCard(
				// ATR
				// (3b:df:18:00:81:31:fe:58:80:31:b0:52:02:04:64:05:c9:03:ac:73:b7:b1:d4:22)
				new byte[] { (byte) 0x3b, (byte) 0xdf, (byte) 0x18,
						(byte) 0x00, (byte) 0x81, (byte) 0x31, (byte) 0xfe,
						(byte) 0x58, (byte) 0x00, (byte) 0x00, (byte) 0x00,
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
						(byte) 0x00 },
				// mask
				// (ff:ff:ff:ff:ff:ff:ff:ff:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00)
				new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0x00, (byte) 0x00, (byte) 0x00,
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
						(byte) 0x00 }, "at.gv.egiz.smcc.STARCOSCard"));

		// a-sign premium (EPA)
		supportedCards.add(new SupportedCard(
		// ATR
		// (3b:bf:11:00:81:31:fe:45:45:50:41:00:00:00:00:00:00:00:00:00:00:00:00:00)
				new byte[] { (byte) 0x3b, (byte) 0xbf, (byte) 0x11,
						(byte) 0x00, (byte) 0x81, (byte) 0x31, (byte) 0xfe,
						(byte) 0x45, (byte) 0x45, (byte) 0x50, (byte) 0x41,
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
						(byte) 0x00 },
				// mask
				// (ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:00:00:00:00:00:00:00:00:00:00:00:00:00)
				new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
						(byte) 0x00 }, "at.gv.egiz.smcc.ACOSCard"));

		// a-sign premium (MCA)
		supportedCards.add(new SupportedCard(
		// ATR
		// (3b:bf:11:00:81:31:fe:45:45:50:41:00:00:00:00:00:00:00:00:00:00:00:00:00)
				new byte[] { (byte) 0x3b, (byte) 0xbf, (byte) 0x11,
						(byte) 0x00, (byte) 0x81, (byte) 0x31, (byte) 0xfe,
						(byte) 0x45, (byte) 0x4D, (byte) 0x43, (byte) 0x41,
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
						(byte) 0x00 },
				// mask
				// (ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:00:00:00:00:00:00:00:00:00:00:00:00:00)
				new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
						(byte) 0x00 }, "at.gv.egiz.smcc.ACOSCard"));

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

		// Cypriotic EID
		supportedCards.add(new SupportedCard(
				// ATR [3B:DD:18:00:81:31:FE.45:80:F9:A0:00:00:00:77:01:08:00:07::90:00:FE]
				new byte[] { (byte) 0x3b, (byte) 0xdd, (byte) 0x18, 
						(byte) 0x00, (byte) 0x81, (byte) 0x31, (byte) 0xfe, 
						(byte) 0x45, (byte) 0x80, (byte) 0xf9, (byte) 0xa0,
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x77,
						(byte) 0x01, (byte) 0x08, (byte) 0x00, (byte) 0x07,
						(byte) 0x90, (byte) 0x00, (byte) 0xfe},
				// mas (ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff)
				new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff, 
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff},
				"at.gv.egiz.smcc.CypriotEID"));	
		
		// Gemalto .NET V2.0
		supportedCards.add(new SupportedCard(
				// ATR [3B:16:96:41:73:74:72:69:64]
				new byte[] { (byte) 0x3b, (byte) 0x16, (byte) 0x96, 
						(byte) 0x41, (byte) 0x73, (byte) 0x74, (byte) 0x72, 
						(byte) 0x69, (byte) 0x64},
				// mas (ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff)
				new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff, 
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 
						(byte) 0xff, (byte) 0xff},
				"at.gv.egiz.smcc.GemaltoNetV2_0Card"));	
		
		// ES DNIe
		supportedCards.add(new SupportedCard(
		// ATR [3b:7f:38:00:00:00:6a:44:4e:49:65:20:02:4c:34:01:13:03:90:00]
				new byte[] { (byte) 0x3b, (byte) 0x7F, (byte) 0x38,
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x6A,
						(byte) 0x44, (byte) 0x4E, (byte) 0x49, (byte) 0x65,
						(byte) 0x00, (byte) 0x02, (byte) 0x4C, (byte) 0x34,
						(byte) 0x01, (byte) 0x13, (byte) 0x00, (byte) 0x90,
						(byte) 0x00 },
				// mask (ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff)
				new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0x00, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0x00, (byte) 0xff,
						(byte) 0xff }, "at.gv.egiz.smcc.ESDNIeCard"));

		// FMNT card - ATR is correct, but implementation is NOT equal to DNIe
		// supportedCards.add(new SupportedCard(
		// // ATR
		// // [3b:ef:00:00:40:14:80:25:43:45:52:45:53:57:01:16:01:01:03:90:00]
		// new byte[] { (byte) 0x3b, (byte) 0xEF, (byte) 0x00,
		// (byte) 0x00, (byte) 0x40, (byte) 0x14, (byte) 0x80,
		// (byte) 0x25, (byte) 0x43, (byte) 0x45, (byte) 0x52,
		// (byte) 0x45, (byte) 0x53, (byte) 0x57, (byte) 0x01,
		// (byte) 0x16, (byte) 0x01, (byte) 0x01, (byte) 0x03,
		// (byte) 0x90, (byte) 0x00 },
		// // mask
		// (ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff)
		// new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff,
		// (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
		// (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
		// (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
		// (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
		// (byte) 0xff, (byte) 0xff },
		// "at.gv.egiz.smcc.ESDNIeCard"));

		// FIN eID
		supportedCards.add(new SupportedCard(
		// ATR [3b:7B:94:00:00:80:62:12:51:56:46:69:6E:45:49:44]
				new byte[] { (byte) 0x3b, (byte) 0x7B, (byte) 0x94,
						(byte) 0x00, (byte) 0x00, (byte) 0x80, (byte) 0x62,
						(byte) 0x00, (byte) 0x51, (byte) 0x56, (byte) 0x46,
						(byte) 0x69, (byte) 0x6E, (byte) 0x45, (byte) 0x49,
						(byte) 0x44 },
				// historical bytes pattern
				new byte[] { 'F', 'i', 'n', 'E', 'I', 'D' },
				// mask (ff:ff:ff:ff:ff:ff:ff:00:ff:ff:ff:ff:ff:ff:ff:ff) -
				// ignore card OS minor version
				new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0x00, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff }, "at.gv.egiz.smcc.FINEIDCard"));

		// LT eID
		supportedCards.add(new SupportedCard(
				// ATR [3b:7D:94:00:00:80:31:80:65:B0:83:11:C0:A9:83:00:90:00]
				new byte[] { (byte) 0x3b, (byte) 0x7D, (byte) 0x94,
						(byte) 0x00, (byte) 0x00, (byte) 0x80, (byte) 0x31,
						(byte) 0x80, (byte) 0x65, (byte) 0xB0, (byte) 0x83,
						(byte) 0x11, (byte) 0xC0, (byte) 0xA9, (byte) 0x83,
						(byte) 0x00, (byte) 0x90, (byte) 0x00 },
				// mask (ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff)
				// -
				// ignore card OS minor version
				new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff },
				"at.gv.egiz.smcc.LtEIDCard"));

		// SE eID
		supportedCards.add(new SupportedCard(
				// ATR
				// [3B:9F:94:80:1F:C3:00:68:10:44:05:01:46:49:53:45:31:C8:07:90:00:18]
				new byte[] { (byte) 0x3b, (byte) 0x9F, (byte) 0x90,
						(byte) 0x80, (byte) 0x1F, (byte) 0xC0, (byte) 0x00,
						(byte) 0x68, (byte) 0x00, (byte) 0x00, (byte) 0x05,
						(byte) 0x00, (byte) 0x46, (byte) 0x49, (byte) 0x53,
						(byte) 0x45, (byte) 0x31, (byte) 0xC8, (byte) 0x00,
						(byte) 0x00, (byte) 0x00, (byte) 0x00 },
				// mask
				// (ff:ff:f0:ff:ff:f0:ff:ff:00:00:ff:f0:ff:ff:ff:ff:ff:ff:F0:00:00:00)
				new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xf0,
						(byte) 0xff, (byte) 0xff, (byte) 0xf0, (byte) 0xff,
						(byte) 0xff, (byte) 0x00, (byte) 0x00, (byte) 0xff,
						(byte) 0xf0, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xf0,
						(byte) 0x00, (byte) 0x00, (byte) 0x00 },
				"at.gv.egiz.smcc.SEIdentityCard"));

		// IS VISA electron
		supportedCards.add(new SupportedCard(
		// ATR
		// [3B:68:00:00:00:73:C8:40:10:00:90:00]
				new byte[] { (byte) 0x3b, (byte) 0x68, (byte) 0x00,
						(byte) 0x00, (byte) 0x00, (byte) 0x73, (byte) 0xC8,
						(byte) 0x40, (byte) 0x10, (byte) 0x00, (byte) 0x90,
						(byte) 0x00 },
				// mask
				// (ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff)
				new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff }, "at.gv.egiz.smcc.ISVISAElectronCard"));

		// IS Maestro
		supportedCards.add(new SupportedCard(
				// ATR
				// [3B:6F:00:00:80:31:E0:6B:04:20:05:02:58:55:55:55:55:55:55]
				new byte[] { (byte) 0x3b, (byte) 0x6F, (byte) 0x00,
						(byte) 0x00, (byte) 0x80, (byte) 0x31, (byte) 0xE0,
						(byte) 0x6B, (byte) 0x04, (byte) 0x20, (byte) 0x05,
						(byte) 0x02, (byte) 0x58, (byte) 0x55, (byte) 0x55,
						(byte) 0x55, (byte) 0x55, (byte) 0x55, (byte) 0x55 },
				// mask
				// (ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff)
				new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff },
				"at.gv.egiz.smcc.ISMAESTROCard"));

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

		supportedCards.add(new SupportedCard(
				// ATR
				// (3B:DF:18:00:81:31:FE:7D:00:6B:15:0C:01:81:01:01:01:43:4E:53:10:31:80:F8)
				new byte[] { (byte) 0x3b, (byte) 0xdf, (byte) 0x18,
						(byte) 0x00, (byte) 0x81, (byte) 0x31, (byte) 0xfe,
						(byte) 0x7d, (byte) 0x00, (byte) 0x6b, (byte) 0x15,
						(byte) 0x0c, (byte) 0x01, (byte) 0x81, (byte) 0x01,
						(byte) 0x01, (byte) 0x01, (byte) 0x43, (byte) 0x4e,
						(byte) 0x53, (byte) 0x10, (byte) 0x31, (byte) 0x80,
						(byte) 0xf8},
				// mask
				// (ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff)
				new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff},
				"at.gv.egiz.smcc.ITCard"));

		supportedCards.add(new SupportedCard(
				// ATR = 3B xF x1 31 xx xx 00 6B xx 0xxxxxxx xx xx 01 xx 0x 43 4E 53 10 31
				// 80 xx
				// 0-0 variant
				new byte[] { (byte) 0x3B, (byte) 0x8F, (byte) 0x81, (byte) 0x31,
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x6B, (byte) 0x00,
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00,
						(byte) 0x00, (byte) 0x43, (byte) 0x4E, (byte) 0x53, (byte) 0x10,
						(byte) 0x31, (byte) 0x80, (byte) 0x00 }, new byte[] { (byte) 0xFF,
						(byte) 0x8F, (byte) 0x8F, (byte) 0xFF, (byte) 0x00, (byte) 0x00,
						(byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0x80, (byte) 0x00,
						(byte) 0x00, (byte) 0xFF, (byte) 0x00, (byte) 0xF0, (byte) 0xFF,
						(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
						(byte) 0x00 },
				"at.gv.egiz.smcc.ITCard"));

		supportedCards.add(new SupportedCard(
				// ATR = 3B xF x1 xx 31 xx xx 00 6B xx 0xxxxxxx xx xx 01 xx 0x 43 4E 53 10
				// 31 80 xx
				// 0-1 variant
				new byte[] { (byte) 0x3B, (byte) 0x8F, (byte) 0x81, (byte) 0x00,
						(byte) 0x31, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x6B,
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01,
						(byte) 0x00, (byte) 0x00, (byte) 0x43, (byte) 0x4E, (byte) 0x53,
						(byte) 0x10, (byte) 0x31, (byte) 0x80, (byte) 0x00 }, new byte[] {
						(byte) 0xFF, (byte) 0x8F, (byte) 0x8F, (byte) 0x00, (byte) 0xFF,
						(byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0x00,
						(byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x00,
						(byte) 0xF0, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
						(byte) 0xFF, (byte) 0xFF, (byte) 0x00 },
				"at.gv.egiz.smcc.ITCard"));

		supportedCards.add(new SupportedCard(
				// ATR = 3B xF x1 xx xx 31 xx xx 00 6B xx 0xxxxxxx xx xx 01 xx 0x 43 4E
				// 53 10 31 80 xx
				// 0-2 variant
				new byte[] { (byte) 0x3B, (byte) 0x8F, (byte) 0x81, (byte) 0x00,
						(byte) 0x00, (byte) 0x31, (byte) 0x00, (byte) 0x00, (byte) 0x00,
						(byte) 0x6B, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
						(byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x43, (byte) 0x4E,
						(byte) 0x53, (byte) 0x10, (byte) 0x31, (byte) 0x80, (byte) 0x00 },
				new byte[] { (byte) 0xFF, (byte) 0x8F, (byte) 0x8F, (byte) 0x00,
						(byte) 0x00, (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0xFF,
						(byte) 0xFF, (byte) 0x00, (byte) 0x80, (byte) 0x00, (byte) 0x00,
						(byte) 0xFF, (byte) 0x00, (byte) 0xF0, (byte) 0xFF, (byte) 0xFF,
						(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x00 },
				"at.gv.egiz.smcc.ITCard"));

		supportedCards.add(new SupportedCard(
				// ATR = 3B xF x1 xx xx xx 31 xx xx 00 6B xx 0xxxxxxx xx xx 01 xx 0x 43 4E
				// 53 10 31 80 xx
				// 0-3 variant
				new byte[] { (byte) 0x3B, (byte) 0x8F, (byte) 0xF1, (byte) 0x00,
						(byte) 0x00, (byte) 0x00, (byte) 0x31, (byte) 0x00, (byte) 0x00,
						(byte) 0x00, (byte) 0x6B, (byte) 0x00, (byte) 0x00, (byte) 0x00,
						(byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x43,
						(byte) 0x4E, (byte) 0x53, (byte) 0x10, (byte) 0x31, (byte) 0x80,
						(byte) 0x00 }, new byte[] { (byte) 0xFF, (byte) 0x8F, (byte) 0xFF,
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x00,
						(byte) 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0x80,
						(byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x00, (byte) 0xF0,
						(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
						(byte) 0xFF, (byte) 0x00 },
				"at.gv.egiz.smcc.ITCard"));

		supportedCards.add(new SupportedCard(
				// ATR = 3B xF xx x1 31 xx xx 00 6B xx 0xxxxxxx xx xx 01 xx 0x 43 4E 53 10
				// 31 80 xx
				// 1-0 variant
				new byte[] { (byte) 0x3B, (byte) 0x8F, (byte) 0x00, (byte) 0x81,
						(byte) 0x31, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x6B,
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01,
						(byte) 0x00, (byte) 0x00, (byte) 0x43, (byte) 0x4E, (byte) 0x53,
						(byte) 0x10, (byte) 0x31, (byte) 0x80, (byte) 0x00 }, new byte[] {
						(byte) 0xFF, (byte) 0x8F, (byte) 0x00, (byte) 0x8F, (byte) 0xFF,
						(byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0x00,
						(byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x00,
						(byte) 0xF0, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
						(byte) 0xFF, (byte) 0xFF, (byte) 0x00 },
				"at.gv.egiz.smcc.ITCard"));

		supportedCards.add(new SupportedCard(
				// ATR = 3B xF xx x1 xx 31 xx xx 00 6B xx 0xxxxxxx xx xx 01 xx 0x 43 4E
				// 53 10 31 80 xx
				// 1-1 variant
				new byte[] { (byte) 0x3B, (byte) 0x8F, (byte) 0x00, (byte) 0x81,
						(byte) 0x00, (byte) 0x31, (byte) 0x00, (byte) 0x00, (byte) 0x00,
						(byte) 0x6B, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
						(byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x43, (byte) 0x4E,
						(byte) 0x53, (byte) 0x10, (byte) 0x31, (byte) 0x80, (byte) 0x00 },
				new byte[] { (byte) 0xFF, (byte) 0x8F, (byte) 0x00, (byte) 0x8F,
						(byte) 0x00, (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0xFF,
						(byte) 0xFF, (byte) 0x00, (byte) 0x80, (byte) 0x00, (byte) 0x00,
						(byte) 0xFF, (byte) 0x00, (byte) 0xF0, (byte) 0xFF, (byte) 0xFF,
						(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x00 },
				"at.gv.egiz.smcc.ITCard"));

		supportedCards.add(new SupportedCard(
				// ATR = 3B xF xx x1 xx xx 31 xx xx 00 6B xx 0xxxxxxx xx xx 01 xx 0x 43 4E
				// 53 10 31 80 xx
				// 1-2 variant
				new byte[] { (byte) 0x3B, (byte) 0x8F, (byte) 0x00, (byte) 0x81,
						(byte) 0x00, (byte) 0x00, (byte) 0x31, (byte) 0x00, (byte) 0x00,
						(byte) 0x00, (byte) 0x6B, (byte) 0x00, (byte) 0x00, (byte) 0x00,
						(byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x43,
						(byte) 0x4E, (byte) 0x53, (byte) 0x10, (byte) 0x31, (byte) 0x80,
						(byte) 0x00 }, new byte[] { (byte) 0xFF, (byte) 0x8F, (byte) 0x00,
						(byte) 0x8F, (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x00,
						(byte) 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0x80,
						(byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x00, (byte) 0xF0,
						(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
						(byte) 0xFF, (byte) 0x00 },
				"at.gv.egiz.smcc.ITCard"));

		supportedCards.add(new SupportedCard(
				// ATR = 3B xF xx x1 xx xx xx 31 xx xx 00 6B xx 0xxxxxxx xx xx 01 xx 0x
				// 43 4E 53 10 31 80 xx
				// 1-3 variant
				new byte[] { (byte) 0x3B, (byte) 0x8F, (byte) 0x00, (byte) 0xF1,
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x31, (byte) 0x00,
						(byte) 0x00, (byte) 0x00, (byte) 0x6B, (byte) 0x00, (byte) 0x00,
						(byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00,
						(byte) 0x43, (byte) 0x4E, (byte) 0x53, (byte) 0x10, (byte) 0x31,
						(byte) 0x80, (byte) 0x00 }, new byte[] { (byte) 0xFF, (byte) 0x8F,
						(byte) 0x00, (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00,
						(byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0xFF,
						(byte) 0x00, (byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0xFF,
						(byte) 0x00, (byte) 0xF0, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
						(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x00 },
				"at.gv.egiz.smcc.ITCard"));

		supportedCards.add(new SupportedCard(
				// ATR = 3B xF xx xx x1 31 xx xx 00 6B xx 0xxxxxxx xx xx 01 xx 0x 43 4E
				// 53 10 31 80 xx
				// 2-0 variant
				new byte[] { (byte) 0x3B, (byte) 0x8F, (byte) 0x00, (byte) 0x00,
						(byte) 0x81, (byte) 0x31, (byte) 0x00, (byte) 0x00, (byte) 0x00,
						(byte) 0x6B, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
						(byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x43, (byte) 0x4E,
						(byte) 0x53, (byte) 0x10, (byte) 0x31, (byte) 0x80, (byte) 0x00 },
				new byte[] { (byte) 0xFF, (byte) 0x8F, (byte) 0x00, (byte) 0x00,
						(byte) 0x8F, (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0xFF,
						(byte) 0xFF, (byte) 0x00, (byte) 0x80, (byte) 0x00, (byte) 0x00,
						(byte) 0xFF, (byte) 0x00, (byte) 0xF0, (byte) 0xFF, (byte) 0xFF,
						(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x00 },
				"at.gv.egiz.smcc.ITCard"));

		supportedCards.add(new SupportedCard(
				// ATR = 3B xF xx xx x1 xx 31 xx xx 00 6B xx 0xxxxxxx xx xx 01 xx 0x 43 4E
				// 53 10 31 80 xx
				// 2-1 variant
				new byte[] { (byte) 0x3B, (byte) 0x8F, (byte) 0x00, (byte) 0x00,
						(byte) 0x81, (byte) 0x00, (byte) 0x31, (byte) 0x00, (byte) 0x00,
						(byte) 0x00, (byte) 0x6B, (byte) 0x00, (byte) 0x00, (byte) 0x00,
						(byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x43,
						(byte) 0x4E, (byte) 0x53, (byte) 0x10, (byte) 0x31, (byte) 0x80,
						(byte) 0x00 }, new byte[] { (byte) 0xFF, (byte) 0x8F, (byte) 0x00,
						(byte) 0x00, (byte) 0x8F, (byte) 0x00, (byte) 0xFF, (byte) 0x00,
						(byte) 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0x80,
						(byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x00, (byte) 0xF0,
						(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
						(byte) 0xFF, (byte) 0x00 },
				"at.gv.egiz.smcc.ITCard"));

		supportedCards.add(new SupportedCard(
				// ATR = 3B xF xx xx x1 xx xx 31 xx xx 00 6B xx 0xxxxxxx xx xx 01 xx 0x
				// 43 4E 53 10 31 80 xx
				// 2-2 variant
				new byte[] { (byte) 0x3B, (byte) 0x8F, (byte) 0x00, (byte) 0x00,
						(byte) 0x81, (byte) 0x00, (byte) 0x00, (byte) 0x31, (byte) 0x00,
						(byte) 0x00, (byte) 0x00, (byte) 0x6B, (byte) 0x00, (byte) 0x00,
						(byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00,
						(byte) 0x43, (byte) 0x4E, (byte) 0x53, (byte) 0x10, (byte) 0x31,
						(byte) 0x80, (byte) 0x00 }, new byte[] { (byte) 0xFF, (byte) 0x8F,
						(byte) 0x00, (byte) 0x00, (byte) 0x8F, (byte) 0x00, (byte) 0x00,
						(byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0xFF,
						(byte) 0x00, (byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0xFF,
						(byte) 0x00, (byte) 0xF0, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
						(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x00 },
				"at.gv.egiz.smcc.ITCard"));

		supportedCards.add(new SupportedCard(
				// ATR = 3B xF xx xx x1 xx xx xx 31 xx xx 00 6B xx 0xxxxxxx xx xx 01 xx 0x
				// 43 4E 53 10 31 80 xx
				// 2-3 variant
				new byte[] { (byte) 0x3B, (byte) 0x8F, (byte) 0x00, (byte) 0x00,
						(byte) 0xF1, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x31,
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x6B, (byte) 0x00,
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00,
						(byte) 0x00, (byte) 0x43, (byte) 0x4E, (byte) 0x53, (byte) 0x10,
						(byte) 0x31, (byte) 0x80, (byte) 0x00 }, new byte[] { (byte) 0xFF,
						(byte) 0x8F, (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x00,
						(byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x00, (byte) 0x00,
						(byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0x80, (byte) 0x00,
						(byte) 0x00, (byte) 0xFF, (byte) 0x00, (byte) 0xF0, (byte) 0xFF,
						(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
						(byte) 0x00 },
				"at.gv.egiz.smcc.ITCard"));

		supportedCards.add(new SupportedCard(
				// ATR = 3B xF xx xx xx x1 31 xx xx 00 6B xx 0xxxxxxx xx xx 01 xx 0x 43 4E
				// 53 10 31 80 xx
				// 3-0 variant
				new byte[] { (byte) 0x3B, (byte) 0xFF, (byte) 0x00, (byte) 0x00,
						(byte) 0x00, (byte) 0x81, (byte) 0x31, (byte) 0x00, (byte) 0x00,
						(byte) 0x00, (byte) 0x6B, (byte) 0x00, (byte) 0x00, (byte) 0x00,
						(byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x43,
						(byte) 0x4E, (byte) 0x53, (byte) 0x10, (byte) 0x31, (byte) 0x80,
						(byte) 0x00 }, new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0x00,
						(byte) 0x00, (byte) 0x00, (byte) 0x8F, (byte) 0xFF, (byte) 0x00,
						(byte) 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0x80,
						(byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x00, (byte) 0xF0,
						(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
						(byte) 0xFF, (byte) 0x00 },
				"at.gv.egiz.smcc.ITCard"));

		supportedCards.add(new SupportedCard(
				// ATR = 3B xF xx xx xx x1 xx 31 xx xx 00 6B xx 0xxxxxxx xx xx 01 xx 0x
				// 43 4E 53 10 31 80 xx
				// 3-1 variant
				new byte[] { (byte) 0x3B, (byte) 0xFF, (byte) 0x00, (byte) 0x00,
						(byte) 0x00, (byte) 0x81, (byte) 0x00, (byte) 0x31, (byte) 0x00,
						(byte) 0x00, (byte) 0x00, (byte) 0x6B, (byte) 0x00, (byte) 0x00,
						(byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00,
						(byte) 0x43, (byte) 0x4E, (byte) 0x53, (byte) 0x10, (byte) 0x31,
						(byte) 0x80, (byte) 0x00 }, new byte[] { (byte) 0xFF, (byte) 0xFF,
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x8F, (byte) 0x00,
						(byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0xFF,
						(byte) 0x00, (byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0xFF,
						(byte) 0x00, (byte) 0xF0, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
						(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x00 },
				"at.gv.egiz.smcc.ITCard"));

		supportedCards.add(new SupportedCard(
				// ATR = 3B xF xx xx xx x1 xx xx 31 xx xx 00 6B xx 0xxxxxxx xx xx 01 xx 0x
				// 43 4E 53 10 31 80 xx
				// 3-2 variant
				new byte[] { (byte) 0x3B, (byte) 0xFF, (byte) 0x00, (byte) 0x00,
						(byte) 0x00, (byte) 0x81, (byte) 0x00, (byte) 0x00, (byte) 0x31,
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x6B, (byte) 0x00,
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00,
						(byte) 0x00, (byte) 0x43, (byte) 0x4E, (byte) 0x53, (byte) 0x10,
						(byte) 0x31, (byte) 0x80, (byte) 0x00 }, new byte[] { (byte) 0xFF,
						(byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x8F,
						(byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x00, (byte) 0x00,
						(byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0x80, (byte) 0x00,
						(byte) 0x00, (byte) 0xFF, (byte) 0x00, (byte) 0xF0, (byte) 0xFF,
						(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
						(byte) 0x00 },
				"at.gv.egiz.smcc.ITCard"));

		supportedCards.add(new SupportedCard(
				// ATR = 3B xF xx xx xx x1 xx xx xx 31 xx xx 00 6B xx 0xxxxxxx xx xx 01 xx
				// 0x 43 4E 53 10 31 80 xx
				// 3-3 variant
				new byte[] { (byte) 0x3B, (byte) 0xFF, (byte) 0x00, (byte) 0x00,
						(byte) 0x00, (byte) 0xF1, (byte) 0x00, (byte) 0x00, (byte) 0x00,
						(byte) 0x31, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x6B,
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01,
						(byte) 0x00, (byte) 0x00, (byte) 0x43, (byte) 0x4E, (byte) 0x53,
						(byte) 0x10, (byte) 0x31, (byte) 0x80, (byte) 0x00 }, new byte[] {
						(byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00,
						(byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFF,
						(byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0x00,
						(byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x00,
						(byte) 0xF0, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
						(byte) 0xFF, (byte) 0xFF, (byte) 0x00 },
				"at.gv.egiz.smcc.ITCard"));

		// ATR for EstEID v.1.0 realised on Micardo Public 2.1 - Warm ATR
		// ATR for EstEID v.1.0 realised on Micardo Public 3.0 - Warm ATR
		// ATR for EstEID v.1.1 for DigiID realised on MultoOS by KeyCorp on IE4 - Cold ATR
		supportedCards.add(new SupportedCard(
				// ATR
				// (3B:XX:XX:XX:45:73:74:45:49:44:20:76:65:72:20:31:2E:30)
				new byte[] { (byte) 0x3b, (byte) 0x00, (byte) 0x00,
						(byte) 0x00, 'E', 's', 't', 'E', 'I', 'D', ' ', 'v',
						'e', 'r', ' ', '1', '.', '0' },
				// historical bytes pattern
				new byte[] { 'E', 's', 't', 'E', 'I', 'D', ' ', 'v', 'e', 'r',
						' ', '1', '.', '0' },
				// mask
				// (ff:00:00:00:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff)
				new byte[] { (byte) 0xff, (byte) 0x00, (byte) 0x00,
						(byte) 0x00, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff },						
				"at.gv.egiz.smcc.EstEIDCard"));

		// ATR for EstEID v.1.0 realised on Micardo Public 3.0 - Cold ATR
		supportedCards.add(new SupportedCard(
				// ATR
				// (3B:DE:18:FF:C0:80:B1:FE:45:1F:03:45:73:74:45:49:44:20:76:65:72:20:31:2E:30:2B)
				new byte[] { (byte) 0x3b, (byte) 0xde, (byte) 0x18,
						(byte) 0xff, (byte) 0xc0, (byte) 0x80, (byte) 0xb1,
						(byte) 0xfe, (byte) 0x45, (byte) 0x1f, (byte) 0x03,
						'E', 's', 't', 'E', 'I', 'D', ' ', 'v', 'e', 'r', ' ',
						'1', '.', '0', (byte) 0x2b },
				// historical bytes pattern
				new byte[] { 'E', 's', 't', 'E', 'I', 'D', ' ', 'v', 'e', 'r',
						' ', '1', '.', '0' },
				// mask
				// (ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff)
				new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff },
				"at.gv.egiz.smcc.EstEIDCard"));
		
		// ATR for EstEID v.1.0 realised on Micardo Public 2.1 - Cold ATR
		// ATR for EstEID v.1.1 for DigiID realised on MultoOS by KeyCorp on IE4 - Warm ATR
		supportedCards.add(new SupportedCard(				
				// (3b:fe:94:00:ff:80:b1:fa:45:1f:03:45:73:74:45:49:44:20:76:65:72:20:31:2e:30:43)
				new byte[] { (byte) 0x3b, (byte) 0xfe, (byte) 0x94,
						(byte) 0x00, (byte) 0xff, (byte) 0x80, (byte) 0xb1,
						(byte) 0xfa, (byte) 0x45, (byte) 0x1f, (byte) 0x03,
						'E', 's', 't', 'E', 'I', 'D', ' ', 'v', 'e', 'r', ' ',
						'1', '.', '0', (byte) 0x43 },
				// historical bytes pattern
				new byte[] { 'E', 's', 't', 'E', 'I', 'D', ' ', 'v', 'e', 'r',
						' ', '1', '.', '0' },
				// mask
				// (ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff)
				new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff },
				"at.gv.egiz.smcc.EstEIDCard"));

		supportedCards.add(new SupportedCard(
		// EstEID v3.0 (recalled 46 cards) and v3.0 and v3.4 - Cold ATR
		// (3B:FE:18:00:00:80:31:FE:45:45:73:74:45:49:44:20:76:65:72:20:31:2E:30:A8)
				new byte[] { (byte) 0x3b, (byte) 0xfe, (byte) 0x18,
						(byte) 0x00, (byte) 0x00, (byte) 0x80, (byte) 0x31,
						(byte) 0xfe, (byte) 0x45, 'E', 's', 't', 'E', 'I', 'D',
						' ', 'v', 'e', 'r', ' ', '1', '.', '0', (byte) 0xA8 },
				// historical bytes pattern
				new byte[] { 'E', 's', 't', 'E', 'I', 'D', ' ', 'v', 'e', 'r',
						' ', '1', '.', '0' },
				// mask
				// (ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff)
				new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff }, "at.gv.egiz.smcc.EstEIDCard"));

		supportedCards.add(new SupportedCard(
		// EstEID v3.0 (recalled 46 cards) - Warm ATR
		// (3B:FE:18:00:00:80:31:FE:45:80:31:80:66:40:90:A4:16:2A:00:83:01:90:00:E1)
				new byte[] { (byte) 0x3b, (byte) 0xfe, (byte) 0x18,
						(byte) 0x00, (byte) 0x00, (byte) 0x80, (byte) 0x31,
						(byte) 0xfe, (byte) 0x45, (byte) 0x80, (byte) 0x31,
						(byte) 0x80, (byte) 0x66, (byte) 0x40, (byte) 0x90,
						(byte) 0xA4, (byte) 0x16, (byte) 0x2A, (byte) 0x00,
						(byte) 0x83, (byte) 0x01, (byte) 0x90, (byte) 0x00,
						(byte) 0xE1 },
				// historical bytes pattern
				new byte[] { (byte) 0x80, (byte) 0x31,
						(byte) 0x80, (byte) 0x66, (byte) 0x40, (byte) 0x90,
						(byte) 0xA4, (byte) 0x16, (byte) 0x2A, (byte) 0x00,
						(byte) 0x83, (byte) 0x01, (byte) 0x90, (byte) 0x00,
						(byte) 0xE1 },
				// mask
				// (ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff)
				new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff }, "at.gv.egiz.smcc.EstEIDCard"));

		supportedCards.add(new SupportedCard(
		// EstEID v3.0 and v3.4 - Warm ATR
		// (3B:FE:18:00:00:80:31:FE:45:80:31:80:66:40:90:A4:16:2A:00:83:0F:90:00:EF)
				new byte[] { (byte) 0x3b, (byte) 0xfe, (byte) 0x18,
						(byte) 0x00, (byte) 0x00, (byte) 0x80, (byte) 0x31,
						(byte) 0xfe, (byte) 0x45, (byte) 0x80, (byte) 0x31,
						(byte) 0x80, (byte) 0x66, (byte) 0x40, (byte) 0x90,
						(byte) 0xA4, (byte) 0x16, (byte) 0x2A, (byte) 0x00,
						(byte) 0x83, (byte) 0x0F, (byte) 0x90, (byte) 0x00,
						(byte) 0xEF },
				// historical bytes pattern
				new byte[] { (byte) 0x80, (byte) 0x31,
						(byte) 0x80, (byte) 0x66, (byte) 0x40, (byte) 0x90,
						(byte) 0xA4, (byte) 0x16, (byte) 0x2A, (byte) 0x00,
						(byte) 0x83, (byte) 0x01, (byte) 0x90, (byte) 0x00,
						(byte) 0xEF },
				// mask
				// (ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff)
				new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff }, "at.gv.egiz.smcc.EstEIDCard"));
		
		supportedCards.add(new SupportedCard(
				// ATR (3B:7D:95:00:00:80:31:80:65:B0:83:11:C0:A9:83:00:90:00 -
				// 00:00:00:00)
				new byte[] { (byte) 0x3b, (byte) 0x7d, (byte) 0x95,
						(byte) 0x00, (byte) 0x00, (byte) 0x80, (byte) 0x31,
						(byte) 0x80, (byte) 0x65, (byte) 0xb0, (byte) 0x83,
						(byte) 0x11, (byte) 0xc0, (byte) 0xa9, (byte) 0x83,
						(byte) 0x00, (byte) 0x90, (byte) 0x00 },
				// mask
				// (ff:ff:ff:00:00:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:00:ff:00)
				new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0x00, (byte) 0x00, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0x00, (byte) 0xff, (byte) 0x00 },
				"at.gv.egiz.smcc.PtEidCard"));

		supportedCards.add(new SupportedCard(
				//ATR: (3B:7D:95:00:00:80:31:80:65:B0:83:11:00:C8:83:00:90:00 -
				// 00:00:00:00)
				new byte[] { (byte) 0x3b, (byte) 0x7d, (byte) 0x95,
						(byte) 0x00, (byte) 0x00, (byte) 0x80, (byte) 0x31,
						(byte) 0x80, (byte) 0x65, (byte) 0xb0, (byte) 0x83,
						(byte) 0x11, (byte) 0x00, (byte) 0xc8, (byte) 0x83,
						(byte) 0x00, (byte) 0x90, (byte) 0x00 },
				// mask
				// (ff:ff:ff:00:00:ff:ff:ff:ff:ff:ff:ff:ff:ff:ff:00:ff:00)
				new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0x00, (byte) 0x00, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0x00, (byte) 0xff, (byte) 0x00 },
				"at.gv.egiz.smcc.PtEidCard"));

		supportedCards.add(new SupportedCard(
				// SwissSign ATR
				// 3b:fa:18:00:02:c1:0a:31:fe:58:4b:53:77:69:73:73:53:69:67:6e:89
				new byte[] { (byte) 0x3b, (byte) 0xfa, (byte) 0x18,
						(byte) 0x00, (byte) 0x02, (byte) 0xc1, (byte) 0x0a,
						(byte) 0x31, (byte) 0xfe, (byte) 0x58, (byte) 0x4b,
						'S', 'w', 'i', 's', 's', 'S', 'i', 'g', 'n',
						(byte) 0x89 },
				// historical bytes pattern
				new byte[] { 'S', 'w', 'i', 's', 's', 'S', 'i', 'g', 'n' },
				// mask
				new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff },
				"at.gv.egiz.smcc.SuisseIDCard"));

		supportedCards.add(new SupportedCard(
				// QuoVadis ATR 3b:f2:18:00:02:c1:0a:31:fe:58:c8:08:74
				new byte[] { (byte) 0x3b, (byte) 0xf2, (byte) 0x18,
						(byte) 0x00, (byte) 0x02, (byte) 0xc1, (byte) 0x0a,
						(byte) 0x31, (byte) 0xfe, (byte) 0x58, (byte) 0xc8,
						(byte) 0x08, (byte) 0x74 },
				// mask
				new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff },
				"at.gv.egiz.smcc.SuisseIDCard"));

		supportedCards.add(new SupportedCard(
				// FL-Post card
				// [3b:bb:18:00:c0:10:31:fe:45:80:67:04:12:
				// b0:03:03:00:00:81:05:3c]
				new byte[] { (byte) 0x3b, (byte) 0xbb, (byte) 0x18,
						(byte) 0x00, (byte) 0xc0, (byte) 0x10, (byte) 0x31,
						(byte) 0xfe, (byte) 0x45, (byte) 0x80, (byte) 0x67,
						(byte) 0x04, (byte) 0x12, (byte) 0xb0, (byte) 0x03,
						(byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x81,
						(byte) 0x05, (byte) 0x3c },
				// mask
				new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff },
				"at.gv.egiz.smcc.LIEZertifikatCard"));

	}

	/**
	 * Creates a SignatureCard instance with the given smart card.
	 * 
	 * @param card
	 *            the smart card, or <code>null</code> if a software card should
	 *            be created
	 * @param cardTerminal
	 *            TODO
	 * 
	 * @return a SignatureCard instance
	 * 
	 * @throws CardNotSupportedException
	 *             if no implementation of the given <code>card</code> could be
	 *             found
	 */
	public SignatureCard createSignatureCard(Card card,
			CardTerminal cardTerminal) throws CardNotSupportedException {

		if (card == null) {
			SignatureCard sCard = new SWCard();
			sCard.init(card, cardTerminal);
			return sCard;
		}

		ATR atr = card.getATR();
		Iterator<SupportedCard> cards = supportedCards.iterator();
		while (cards.hasNext()) {
			SupportedCard supportedCard = cards.next();
			if (supportedCard.matches(atr)) {

				return instantiateSignatureCard(cardTerminal, card,
						supportedCard);
			}
		}

		// if no matching implementation has been found yet, check for pattern
		// match in historical bytes
		log.trace("No card matching complete ATR found - checking candidates with historical bytes matches.");
		Iterator<SupportedCard> cardsIterator = supportedCards.iterator();
		List<SupportedCard> historicalBytesCandidates = new ArrayList<SupportedCard>();
		while (cardsIterator.hasNext()) {
			SupportedCard supportedCard = cardsIterator.next();

			if (supportedCard.matchesHistoricalBytesPattern(atr)) {

				historicalBytesCandidates.add(supportedCard);
			}
		}

		historicalBytesCandidates = reduceCandidateList(historicalBytesCandidates);
		if (historicalBytesCandidates.size() != 1) {

			log.warn("Found {} cards with matching historical bytes pattern.",
					historicalBytesCandidates.size());
		} else {

			log.trace("Instantiating class "
					+ historicalBytesCandidates.get(0)
							.getImplementationClassName()
					+ " according to historical bytes pattern match.");
			return instantiateSignatureCard(cardTerminal, card,
					historicalBytesCandidates.get(0));
		}

		throw new CardNotSupportedException("Card not supported: ATR="
				+ toString(atr.getBytes()));

	}

	private SignatureCard instantiateSignatureCard(CardTerminal cardTerminal,
			Card card, SupportedCard supportedCard)
			throws CardNotSupportedException {

		ClassLoader cl = SignatureCardFactory.class.getClassLoader();
		SignatureCard sc;
		try {
			Class<?> scClass = cl.loadClass(supportedCard
					.getImplementationClassName());
			sc = (SignatureCard) scClass.newInstance();

			sc = ExclSignatureCardProxy.newInstance(sc);

			sc.init(card, cardTerminal);

			return sc;

		} catch (ClassNotFoundException e) {
			log.warn("Cannot find signature card implementation class.", e);
			throw new CardNotSupportedException(
					"Cannot find signature card implementation class.", e);
		} catch (InstantiationException e) {
			log.warn("Failed to instantiate signature card implementation.", e);
			throw new CardNotSupportedException(
					"Failed to instantiate signature card implementation.", e);
		} catch (IllegalAccessException e) {
			log.warn("Failed to instantiate signature card implementation.", e);
			throw new CardNotSupportedException(
					"Failed to instantiate signature card implementation.", e);
		}
	}

	private List<SupportedCard> reduceCandidateList(
			List<SupportedCard> candidates) {

		List<SupportedCard> result = new ArrayList<SupportedCard>();

		for (SupportedCard current : candidates) {

			String implName = current.getImplementationClassName();
			boolean alreadyPresent = false;

			for (SupportedCard card : result) {

				if (card.getImplementationClassName().equals(implName)) {

					alreadyPresent = true;
				}
			}

			if (!alreadyPresent) {
				result.add(current);
			}
		}
		return result;
	}

	public static String toString(byte[] b) {
		StringBuffer sb = new StringBuffer();
		if (b != null && b.length > 0) {
			sb.append(Integer.toHexString((b[0] & 240) >> 4));
			sb.append(Integer.toHexString(b[0] & 15));
		}
		for (int i = 1; i < b.length; i++) {
			sb.append(':');
			sb.append(Integer.toHexString((b[i] & 240) >> 4));
			sb.append(Integer.toHexString(b[i] & 15));
		}
		return sb.toString();
	}

}
