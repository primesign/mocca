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
import at.gv.egiz.smcc.util.TLVSequence;
import iaik.me.asn1.ASN1;
import java.io.IOException;
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
public class CIOCertificateDirectory {

	protected static final boolean RETRIEVE_AUTH_ID_FROM_ASN1 = Boolean.TRUE;
	
    protected static final Logger log = LoggerFactory.getLogger(CIOCertificateDirectory.class);
    protected byte[] fid;
    protected List<CIOCertificate> cios;

    public CIOCertificateDirectory(byte[] fid) {
        this.fid = fid;
        cios = new ArrayList<CIOCertificate>();
    }

    /**
     * assume DF.CIA selected
     * CIO.CD selected afterwards
     *
     * @param channel
     * @throws CardException
     * @throws SignatureCardException
     * @throws IOException if ASN.1 structure cannot be parsed
     */
    public void selectAndRead(CardChannel channel) throws CardException, SignatureCardException, IOException {

    	byte[] fd = executeSelect(channel);
    	
        if ((fd[0] & 0x04) > 0) {
        	
        	readCIOCertificatesFromRecords(channel, fd);
        	
        } else if ((fd[0] & 0x05) == 0x01) {

        	readCIOCertificatesFromTransparentFile(channel);
        }
    }

    protected byte[] executeSelect(CardChannel channel) throws CardException {
    	
        CommandAPDU cmd = new CommandAPDU(0x00, 0xA4, 0x02, ISO7816Utils.P2_FCP, fid, 256);
        ResponseAPDU resp = channel.transmit(cmd);

        byte[] fcx = new TLVSequence(resp.getBytes()).getValue(ISO7816Utils.TAG_FCP);
        byte[] fd = new TLVSequence(fcx).getValue(0x82);
        
        return fd;
    }
    
    protected void readCIOCertificatesFromRecords(CardChannel channel, byte[] fd) throws CardException, SignatureCardException, IOException {
    	
        for (int r = 1; r < fd[fd.length - 1]; r++) {
            log.trace("read CIO record {}", r);
            byte[] record = ISO7816Utils.readRecord(channel, r);
            log.trace("{} bytes", record.length);
            addCIOCertificate(record);
        }
    }
    
    protected byte[] doReadTransparentFile(CardChannel channel) throws CardException, SignatureCardException {
    	
    	return ISO7816Utils.readTransparentFile(channel, -1);
    }
    
    protected void readCIOCertificatesFromTransparentFile(CardChannel channel) throws CardException, SignatureCardException, IOException {
    	
    	byte[] ef = doReadTransparentFile(channel);
    	
        int i = 0;
        int j;

        do {
            int length = 0;
            int ll = 0;
            if ((ef[i + 1] & 0xf0) == 0x80) {
                ll = ef[i + 1] & 0x7f;
                for (int it = 0; it < ll; it++) {
                    length = (length << 8) + (ef[i + it + 2] & 0xff);
                }
            } else {
                length = (ef[i + 1] & 0xff);
            }

            log.trace("read transparent file entry: tag 0x{}, length 0x{}", Integer.toHexString(ef[i]),
                    Integer.toHexString(length));

            j = i + 2 + ll + length;
            addCIOCertificate(Arrays.copyOfRange(ef, i, j));
            i = j;
        } while (i < ef.length && ef[i] > 0);
    	
    }
    
    protected void addCIOCertificate(byte[] cio) throws IOException {
        
        ASN1 x509Certificate = new ASN1(cio);

        CIOCertificate cioCert = new CIOCertificate();
        cioCert.setLabel(x509Certificate.getElementAt(0).getElementAt(0).gvString());
        if(retrieveAuthIdFromASN1()) {
        	cioCert.setAuthId(x509Certificate.getElementAt(0).getElementAt(2).gvByteArray());
        }
        cioCert.setiD(x509Certificate.getElementAt(1).getElementAt(0).gvByteArray());

        //read CONTEXTSPECIFIC manually
        byte[] ctxSpecific = x509Certificate.getElementAt(x509Certificate.getSize()-1).getEncoded();
        if ((ctxSpecific[0] & 0xff) == 0xa1) {
            int ll = ((ctxSpecific[1] & 0xf0) == 0x80)
                    ? (ctxSpecific[1] & 0x0f) + 2 : 2;
            ASN1 x509CertificateAttributes = new ASN1(Arrays.copyOfRange(ctxSpecific, ll, ctxSpecific.length));

            cioCert.setEfidOrPath(x509CertificateAttributes.getElementAt(0).getElementAt(0).gvByteArray());

        } else {
            log.warn("expected CONTEXTSPECIFIC, got 0x{}",
                    Integer.toHexString(ctxSpecific[0]));
        }

        log.debug("adding {}", cioCert);
        cios.add(cioCert);

    }

    public List<CIOCertificate> getCIOs() {
        return cios;
    }
    
	protected boolean retrieveAuthIdFromASN1() {
		
		return RETRIEVE_AUTH_ID_FROM_ASN1;
	}
}
