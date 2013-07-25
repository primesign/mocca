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

import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.smcc.util.ISO7816Utils;
import at.gv.egiz.smcc.util.TransparentFileInputStream;

public class ACOSATCard extends AbstractACOSCardInfoboxHandler {

	public ACOSATCard(ACOSCard acoscard) {
		super(acoscard);
	}

	private final Logger log = LoggerFactory.getLogger(ACOSATCard.class);

	@Override
	@Exclusive
	public byte[] getInfobox(String infobox, PINGUI provider, String domainId)
			throws SignatureCardException, InterruptedException {

		if ("IdentityLink".equals(infobox)) {
			if (_acoscard.getAppVersion() < 2) {
				return getIdentityLinkV1(provider, domainId);
			} else {
				return getIdentityLinkV2(provider, domainId);
			}
		} else {
			throw new IllegalArgumentException("Infobox '" + infobox
					+ "' not supported.");
		}

	}

	protected byte[] getIdentityLinkV1(PINGUI provider, String domainId)
			throws SignatureCardException, InterruptedException {

		try {
			CardChannel channel = _acoscard.getCardChannel();
			// SELECT application
			_acoscard.execSELECT_AID(channel, ACOSCard.AID_DEC);
			// SELECT file
			byte[] fcx = _acoscard.execSELECT_FID(channel, ACOSCard.EF_INFOBOX);
			int maxSize = ISO7816Utils.getLengthFromFCx(fcx);
			log.debug("Size of selected file = {}.", maxSize);
			// READ BINARY
			while (true) {
				try {
					byte[] idLink = ISO7816Utils.readTransparentFileTLV(
							channel, maxSize, (byte) 0x30);
					if (idLink != null) {
						return idLink;
					} else {
						throw new NotActivatedException();
					}
				} catch (SecurityStatusNotSatisfiedException e) {
					_acoscard.verifyPINLoop(channel, _acoscard.infPinInfo, provider);
				}
			}

		} catch (FileNotFoundException e) {
			throw new NotActivatedException();
		} catch (CardException e) {
			log.info("Failed to get infobox.", e);
			throw new SignatureCardException(e);
		}

	}

	protected byte[] getIdentityLinkV2(PINGUI provider, String domainId)
			throws SignatureCardException, InterruptedException {

		try {
			CardChannel channel = _acoscard.getCardChannel();
			// SELECT application
			_acoscard.execSELECT_AID(channel, ACOSCard.AID_DEC);
			// SELECT file
			_acoscard.execSELECT_FID(channel, ACOSCard.EF_INFOBOX);

			// READ BINARY
			TransparentFileInputStream is = ISO7816Utils
					.openTransparentFileInputStream(channel, -1);
			InfoboxContainer infoboxContainer = new InfoboxContainer(is,
					(byte) 0x30);

			for (Infobox box : infoboxContainer.getInfoboxes()) {
				if (box.getTag() == 0x01) {
					if (box.isEncrypted()) {

						_acoscard.execMSE(channel, 0x41, 0xb8, new byte[] { (byte) 0x84,
								(byte) 0x01, (byte) 0x88, (byte) 0x80,
								(byte) 0x01, (byte) 0x02 });

						byte[] plainKey = null;

						while (true) {
							try {
								plainKey = _acoscard.execPSO_DECIPHER(channel, box
										.getEncryptedKey());
								break;
							} catch (SecurityStatusNotSatisfiedException e) {
								_acoscard.verifyPINLoop(channel, _acoscard.decPinInfo, provider);
							}
						}

						return box.decipher(plainKey);

					} else {
						return box.getData();
					}
				}
			}

			// empty
			throw new NotActivatedException();

		} catch (FileNotFoundException e) {
			throw new NotActivatedException();
		} catch (CardException e) {
			log.info("Faild to get infobox.", e);
			throw new SignatureCardException(e);
		} catch (IOException e) {
			if (e.getCause() instanceof SignatureCardException) {
				throw (SignatureCardException) e.getCause();
			} else {
				throw new SignatureCardException(e);
			}
		}

	}

}
