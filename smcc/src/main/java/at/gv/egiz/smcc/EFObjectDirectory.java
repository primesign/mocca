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

import at.gv.egiz.smcc.util.ISO7816Utils;
import at.gv.egiz.smcc.util.TLV;
import at.gv.egiz.smcc.util.TLVSequence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author clemens
 */
public class EFObjectDirectory {

	protected static final Logger log = LoggerFactory
			.getLogger(EFObjectDirectory.class);

	protected byte[] fid;
	private byte[] ef_prkd;
	private byte[] ef_pukd;
	private byte[] ef_aod;

	private List<byte[]> ef_cd_list = new ArrayList<byte[]>();;

	private Integer padding;

	public EFObjectDirectory() {
		fid = new byte[] { (byte) 0x50, (byte) 0x31 };
	}

	public EFObjectDirectory(byte[] fid) {
		this.fid = fid;
	}

	public EFObjectDirectory(int padding) {

		fid = new byte[] { (byte) 0x50, (byte) 0x31 };
		this.padding = padding;

	}

	/**
	 * assume DF.CIA selected EF.OD selected afterwards
	 * 
	 * @param channel
	 * @throws CardException
	 * @throws SignatureCardException
	 */
	public void selectAndRead(CardChannel channel) throws CardException,
			SignatureCardException {

		executeSelect(channel);

		byte[] efod = ISO7816Utils.readTransparentFile(channel, -1);

		for (TLV cio : new TLVSequence(efod)) {
			int tag = cio.getTag();
			
			if (padding != null && tag == padding) {
				// reached padding - quit record extraction
				break;
			}

			byte[] seq = cio.getValue();
			
			if ((tag & 0xf0) == 0xa0 && seq.length >= 4) {

				byte[] path = Arrays.copyOfRange(seq, 4, 4 + seq[3]);

				switch (cio.getTag() & 0x0f) {
				case 0:
					setEf_prkd(path);
					break;
				case 1:
					setEf_pukd(path);
					break;
				case 4:
					addCdToEf_cd_list(path);
					break;
				case 8:
					setEf_aod(path);
					break;
				default:
					log.warn("CIOChoice 0x{} not supported: ",
							(cio.getTag() & 0x0f));
				}
			} else {
				log.trace("ignoring invalid CIO reference entry: {}", seq);
			}
		}
	}

	protected void executeSelect(CardChannel channel)
			throws SignatureCardException, CardException {

		CommandAPDU cmd = new CommandAPDU(0x00, 0xA4, 0x02, 0x00, fid, 256);
		ResponseAPDU resp = channel.transmit(cmd);

		if (resp.getSW() != 0x9000) {
			throw new SignatureCardException("SELECT EF.OD failed: SW=0x"
					+ Integer.toHexString(resp.getSW()));
		}

	}

	/**
	 * @return the ef_prkd
	 */
	public byte[] getEf_prkd() {
		return ef_prkd;
	}

	/**
	 * @param ef_prkd
	 *            the ef_prkd to set
	 */
	public void setEf_prkd(byte[] ef_prkd) {
		this.ef_prkd = ef_prkd;
	}

	/**
	 * @return the ef_pukd
	 */
	public byte[] getEf_pukd() {
		return ef_pukd;
	}

	/**
	 * @param ef_pukd
	 *            the ef_pukd to set
	 */
	public void setEf_pukd(byte[] ef_pukd) {
		this.ef_pukd = ef_pukd;
	}

	/**
	 * @return the ef_aod
	 */
	public byte[] getEf_aod() {
		return ef_aod;
	}

	public List<byte[]> getEf_cd_list() {
		return ef_cd_list;
	}

	public void setEf_cd_list(List<byte[]> ef_cd_list) {
		this.ef_cd_list = ef_cd_list;
	}

	public void addCdToEf_cd_list(byte[] ef_cd) {

		this.ef_cd_list.add(ef_cd);
	}

	/**
	 * @param ef_aod
	 *            the ef_aod to set
	 */
	public void setEf_aod(byte[] ef_aod) {
		this.ef_aod = ef_aod;
	}

}
