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

package at.gv.egiz.smcc.cio;

import iaik.me.asn1.ASN1;
import java.io.IOException;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author clemens
 */
public class CIOCertificate extends CIO {

    protected static final Logger log = LoggerFactory.getLogger(CIOCertificate.class);

    /** CommonCertificateAttributes */
    private byte[] iD;

    /** X509CertificateAttributes*/
    private byte[] efidOrPath;
    private int serialNumber;

    public CIOCertificate(byte[] cio) throws IOException {

        ASN1 x509Certificate = new ASN1(cio);
        ASN1 commonObjAttrs = x509Certificate.getElementAt(0);
        label = commonObjAttrs.getElementAt(0).gvString();
        try {
            // FINeID does not provide authId
            authId = commonObjAttrs.getElementAt(2).gvByteArray();
        } catch (IOException e) {
            log.info("failed to get authId from CommonObjectAttributes: {}", e.getMessage());
        }

        iD = x509Certificate.getElementAt(1).getElementAt(0).gvByteArray();

        //read CONTEXTSPECIFIC manually
        byte[] ctxSpecific = x509Certificate.getElementAt(x509Certificate.getSize()-1).getEncoded();
        if ((ctxSpecific[0] & 0xff) == 0xa1) {
            int ll = ((ctxSpecific[1] & 0xf0) == 0x80)
                    ? (ctxSpecific[1] & 0x0f) + 2 : 2;
            ASN1 x509CertificateAttributes = new ASN1(Arrays.copyOfRange(ctxSpecific, ll, ctxSpecific.length));

            efidOrPath = x509CertificateAttributes.getElementAt(0).getElementAt(0).gvByteArray();

        } else {
            log.warn("expected CONTEXTSPECIFIC, got 0x{}",
                    Integer.toHexString(ctxSpecific[0]));
        }

    }

    /**
     * @return the iD
     */
    public byte[] getiD() {
        return iD;
    }

    /**
     * @param iD the iD to set
     */
    public void setiD(byte[] iD) {
        this.iD = iD;
    }

    /**
     * @return the efidOrPath
     */
    public byte[] getEfidOrPath() {
        return efidOrPath;
    }

    /**
     * @deprecated
     * @param efidOrPath the efidOrPath to set
     */
    public void setEfidOrPath(byte[] efidOrPath) {
        this.efidOrPath = efidOrPath;
    }

    /**
     * @deprecated
     * @return the serialNumber
     */
    public int getSerialNumber() {
        return serialNumber;
    }

    /**
     * @deprecated 
     * @param serialNumber the serialNumber to set
     */
    public void setSerialNumber(int serialNumber) {
        this.serialNumber = serialNumber;
    }


    
}
