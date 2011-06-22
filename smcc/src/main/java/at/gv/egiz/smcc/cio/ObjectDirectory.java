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



package at.gv.egiz.smcc.cio;

import at.gv.egiz.smcc.SignatureCardException;
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
 * TODO ObjectDirectory has access to card filesystem (to readTransparentFile(fid))
 * 
 * @author clemens
 */
public class ObjectDirectory {

	protected static final Logger log = LoggerFactory
			.getLogger(ObjectDirectory.class);

	protected byte[] fid;

        protected CIOCertificateDirectory efCD;
        /** TODO */
        protected CIOCertificateDirectory efPrKD;

        /** References to CIO EFs */
	private List<byte[]> PrKD_refs;
	private List<byte[]> PuKD_refs;
	private List<byte[]> AOD_refs;
	private List<byte[]> CD_refs;

	private Integer padding;
    private int P1 = 0x02;

	public ObjectDirectory() {
		fid = new byte[] { (byte) 0x50, (byte) 0x31 };
	}

	public ObjectDirectory(byte[] fid) {
		this.fid = fid;
	}

        /**
         * @deprecated check while reading if tag is valid
         * @param padding
         */
	public ObjectDirectory(int padding, int p1) {

		fid = new byte[] { (byte) 0x50, (byte) 0x31 };
		this.padding = padding;
                this.P1 = p1;
	}

        /**
	 * assume DF.CIA selected EF.OD selected afterwards
	 *
         * @deprecated will be made private, use getCD/... instead
         * 
	 * @param channel
	 * @throws CardException
	 * @throws SignatureCardException
	 */
	 public void selectAndRead(CardChannel channel) throws CardException,
			SignatureCardException {

		CommandAPDU cmd = new CommandAPDU(0x00, 0xA4, P1, 0x00, fid, 256);
		ResponseAPDU resp = channel.transmit(cmd);

		if (resp.getSW() != 0x9000) {
			throw new SignatureCardException("SELECT EF.OD failed: SW=0x"
					+ Integer.toHexString(resp.getSW()));
		}

		byte[] efod = ISO7816Utils.readTransparentFile(channel, -1);

		
                PrKD_refs = new ArrayList<byte[]>();
                PuKD_refs = new ArrayList<byte[]>();
                AOD_refs = new ArrayList<byte[]>();
                CD_refs = new ArrayList<byte[]>();

		for (TLV cio : new TLVSequence(efod)) {
			int tag = cio.getTag();

            //TODO FIN EID: check if unknown tag and tag length > array
			if (padding != null && tag == padding) {
				// reached padding - quit record extraction
				break;
			}

			byte[] seq = cio.getValue();
			
			if ((tag & 0xf0) == 0xa0 && seq.length >= 4) {

				byte[] path = Arrays.copyOfRange(seq, 4, 4 + seq[3]);

				switch (cio.getTag() & 0x0f) {
				case 0:
					PrKD_refs.add(path);
					break;
				case 1:
					PuKD_refs.add(path);
					break;
				case 4:
					CD_refs.add(path);
					break;
				case 8:
					AOD_refs.add(path);
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

        /**
         *
         * @return the CertificateDirectory CIO file referenced in this EF.OD.
         * If multiple directory files are referenced, the returned CD covers
         * all of them.
         */
        public CIOCertificateDirectory getCD(CardChannel channel) throws CardException, SignatureCardException {

            if (efCD == null) {
        
                if (CD_refs == null) {
                    selectAndRead(channel);
                }
                efCD = new LIEZertifikatCertificateDirectory(CD_refs);
            }
            return efCD;
        }

        public CIOCertificateDirectory getPrKD(CardChannel channel)  throws CardException, SignatureCardException {

            if (efPrKD == null) {

                if (PrKD_refs == null) {
                    selectAndRead(channel);
                }
                efPrKD = new LIEZertifikatCertificateDirectory(PrKD_refs);
            }
            return efPrKD;
        }



        /**
         * @deprecated use getPrKD instead
         * @return the references (FIDs) of the CIO files
         */
        public List<byte[]> getPrKDReferences() {
		return PrKD_refs;
	}

        /**
         * @deprecated use getPuKD instead
         * @return the references (FIDs) of the CIO files
         */
        public List<byte[]> getPuKDReferences() {
		return PuKD_refs;
	}

	/**
         * @deprecated use getAOD instead
         * @return the references (FIDs) of the CIO files
         */
        public List<byte[]> getAODReferences() {
		return AOD_refs;
	}

	/**
         * @deprecated use getCD instead
         * @return the references (FIDs) of the CIO files
         */
        public List<byte[]> getCDReferences() {
		return CD_refs;
	}

	public int getP1() {
		return P1;
	}

	public void setP1(int p1) {
		P1 = p1;
	}
        
       

	
}
