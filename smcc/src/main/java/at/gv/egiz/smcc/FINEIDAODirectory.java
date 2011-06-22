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

import at.gv.egiz.smcc.util.ISO7816Utils;
import at.gv.egiz.smcc.util.TLVSequence;

public class FINEIDAODirectory {
	
    protected static final Logger log = LoggerFactory.getLogger(CIOCertificateDirectory.class);
    protected byte[] fid;
    protected List<FINEIDAuthenticationObject> aos;

    public FINEIDAODirectory(byte[] fid) {
    	
    	this.fid = FINEIDUtil.removeMFPath(fid);    	
        aos = new ArrayList<FINEIDAuthenticationObject>();
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

        CommandAPDU cmd = new CommandAPDU(0x00, 0xA4, 0x08, ISO7816Utils.P2_FCI, fid, 256);
        ResponseAPDU resp = channel.transmit(cmd);

        byte[] fcx = new TLVSequence(resp.getBytes()).getValue(ISO7816Utils.TAG_FCI);
        byte[] fd = new TLVSequence(fcx).getValue(0x82);
        
        if ((fd[0] & 0x05) == 0x01) {

        	readAuthenticationObjectsFromTransparentFile(channel);
        }
    }
    
    protected byte[] doReadTransparentFile(CardChannel channel) throws CardException, SignatureCardException {
    	
    	return ISO7816Utils.readTransparentFile(channel, -1);
    }
    
    protected void readAuthenticationObjectsFromTransparentFile(CardChannel channel) throws CardException, SignatureCardException, IOException {
    	
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
            addAuthenticationObject(Arrays.copyOfRange(ef, i, j));
            i = j;
        } while (i < ef.length && ef[i] > 0);
        
    }
    
    protected void addAuthenticationObject(byte[] ao) throws IOException {
        
        ASN1 authenticationObjects = new ASN1(ao);

        FINEIDAuthenticationObject authObject = new FINEIDAuthenticationObject();
        authObject.setLabel(authenticationObjects.getElementAt(0).getElementAt(0).gvString());

        authObject.setAuthId(authenticationObjects.getElementAt(1).getElementAt(0).gvByteArray());

        //read CONTEXTSPECIFIC manually
        byte[] ctxSpecific = authenticationObjects.getElementAt(authenticationObjects.getSize()-1).getEncoded();
        
        if ((ctxSpecific[0] & 0xff) == 0xa1) {
            int ll = ((ctxSpecific[1] & 0xf0) == 0x80)
                    ? (ctxSpecific[1] & 0x0f) + 2 : 2;
            ASN1 aoAttributes = new ASN1(Arrays.copyOfRange(ctxSpecific, ll, ctxSpecific.length));

            authObject.setPath(aoAttributes.getElementAt(aoAttributes.getSize()-1).getElementAt(0).gvByteArray());

            // get pwdReference
            byte[] ctxSpecific2 = aoAttributes.getElementAt(4).getEncoded();
            ASN1 pwdRef = new ASN1(ctxSpecific2);
            
            authObject.setPwdReference(pwdRef.gvByteArray());
            
        } else {
            log.warn("expected CONTEXTSPECIFIC, got 0x{}",
                    Integer.toHexString(ctxSpecific[0]));
        }
        
        log.debug("adding {}", authObject);
        aos.add(authObject);
    }

    public List<FINEIDAuthenticationObject> getAOs() {
        return aos;
    }
}
