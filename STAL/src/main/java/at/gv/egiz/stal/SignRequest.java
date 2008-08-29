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
package at.gv.egiz.stal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for SignRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SignRequestType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.egiz.gv.at/stal}RequestType">
 *       &lt;sequence>
 *         &lt;element name="KeyIdentifier" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="SignedInfo" type="{http://www.w3.org/2001/XMLSchema}base64Binary"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SignRequestType", propOrder = {
    "keyIdentifier",
    "signedInfo"
})
public class SignRequest
  extends STALRequest {

    @XmlElement(name = "KeyIdentifier", required = true)
    protected String keyIdentifier;
    @XmlElement(name = "SignedInfo", required = true)
    protected byte[] signedInfo;
    @XmlTransient
    protected HashDataInputCallback hashData;

    /**
     * Gets the value of the keyIdentifier property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKeyIdentifier() {
        return keyIdentifier;
    }

    /**
     * Sets the value of the keyIdentifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKeyIdentifier(String value) {
        this.keyIdentifier = value;
    }

    /**
     * Gets the value of the signedInfo property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getSignedInfo() {
        return signedInfo;
    }

    /**
     * Sets the value of the signedInfo property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setSignedInfo(byte[] value) {
        this.signedInfo = ((byte[]) value);
    }

    public HashDataInputCallback getHashDataInput() {
        return hashData;
    }

    public void setHashDataInput(HashDataInputCallback hashData) {
        this.hashData = hashData;
    }
}
