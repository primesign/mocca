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

/**
 *
 * @author clemens
 */
public class CIOCertificate {

    /** CommonObjectAttributes */
    private String label;
    private byte[] authId;

    /** CommonCertificateAttributes */
    private byte[] iD;

    /** X509CertificateAttributes*/
    private byte[] efidOrPath;
    private int serialNumber;

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return the authId
     */
    public byte[] getAuthId() {
        return authId;
    }

    /**
     * @param authId the authId to set
     */
    public void setAuthId(byte[] authId) {
        this.authId = authId;
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
     * @param efidOrPath the efidOrPath to set
     */
    public void setEfidOrPath(byte[] efidOrPath) {
        this.efidOrPath = efidOrPath;
    }

    /**
     * @return the serialNumber
     */
    public int getSerialNumber() {
        return serialNumber;
    }

    /**
     * @param serialNumber the serialNumber to set
     */
    public void setSerialNumber(int serialNumber) {
        this.serialNumber = serialNumber;
    }

    @Override
    public String toString() {
        return "CIOCertificate " + label;
    }


    
}
