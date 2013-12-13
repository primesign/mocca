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


package at.gv.egiz.stal;

import java.math.BigInteger;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


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
 *         &lt;element name="SignedInfo">
 *           &lt;complexType>
 *             &lt;simpleContent>
 *               &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>base64Binary">
 *                 &lt;attribute name="IsCMSSignedAttributes" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *               &lt;/extension>
 *             &lt;/simpleContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="SignatureMethod" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DigestMethod" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ExcludedByteRange" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="from" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedLong" />
 *                 &lt;attribute name="to" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedLong" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
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
    "signedInfo",
    "signatureMethod",
    "digestMethod",
    "excludedByteRange"
})
public class SignRequest
  extends STALRequest {

    @XmlElement(name = "KeyIdentifier", required = true)
    protected String keyIdentifier;
    @XmlElement(name = "SignedInfo", required = true)
    protected SignRequest.SignedInfo signedInfo;
    @XmlElement(name = "SignatureMethod")
    protected String signatureMethod;
    @XmlElement(name = "DigestMethod")
    protected String digestMethod;
    @XmlElement(name = "ExcludedByteRange")
    protected SignRequest.ExcludedByteRange excludedByteRange;
    @XmlTransient
    protected List<HashDataInput> hashData;

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
     *     {@link SignRequestType.SignedInfo }
     *     
     */
    public SignRequest.SignedInfo getSignedInfo() {
        return signedInfo;
    }

    /**
     * Sets the value of the signedInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link SignRequestType.SignedInfo }
     *     
     */
    public void setSignedInfo(SignRequest.SignedInfo value) {
        this.signedInfo = value;
    }

    /**
     * Gets the value of the signatureMethod property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSignatureMethod() {
        return signatureMethod;
    }

    /**
     * Sets the value of the signatureMethod property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSignatureMethod(String value) {
        this.signatureMethod = value;
    }

    /**
     * Gets the value of the digestMethod property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDigestMethod() {
        return digestMethod;
    }

    /**
     * Sets the value of the digestMethod property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDigestMethod(String value) {
        this.digestMethod = value;
    }

    /**
     * Gets the value of the excludedByteRange property.
     * 
     * @return
     *     possible object is
     *     {@link ExcludedByteRange.ExcludedByteRange }
     *     
     */
    public SignRequest.ExcludedByteRange getExcludedByteRange() {
        return excludedByteRange;
    }

    /**
     * Sets the value of the excludedByteRange property.
     * 
     * @param value
     *     allowed object is
     *     {@link ExcludedByteRange.ExcludedByteRange }
     *     
     */
    public void setExcludedByteRange(SignRequest.ExcludedByteRange value) {
        this.excludedByteRange = value;
    }

    public List<HashDataInput> getHashDataInput() {
        return hashData;
    }

    public void setHashDataInput(List<HashDataInput> hashData) {
        this.hashData = hashData;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="from" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedLong" />
     *       &lt;attribute name="to" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedLong" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class ExcludedByteRange {

        @XmlAttribute(required = true)
        @XmlSchemaType(name = "unsignedLong")
        protected BigInteger from;
        @XmlAttribute(required = true)
        @XmlSchemaType(name = "unsignedLong")
        protected BigInteger to;

        /**
         * Gets the value of the from property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getFrom() {
            return from;
        }

        /**
         * Sets the value of the from property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setFrom(BigInteger value) {
            this.from = value;
        }

        /**
         * Gets the value of the to property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getTo() {
            return to;
        }

        /**
         * Sets the value of the to property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setTo(BigInteger value) {
            this.to = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;simpleContent>
     *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>base64Binary">
     *       &lt;attribute name="IsCMSSignedAttributes" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
     *     &lt;/extension>
     *   &lt;/simpleContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class SignedInfo {

        @XmlValue
        protected byte[] value;
        @XmlAttribute(name = "IsCMSSignedAttributes")
        protected Boolean isCMSSignedAttributes;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     byte[]
         */
        public byte[] getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     byte[]
         */
        public void setValue(byte[] value) {
            this.value = ((byte[]) value);
        }

        /**
         * Gets the value of the isCMSSignedAttributes property.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public boolean isIsCMSSignedAttributes() {
            if (isCMSSignedAttributes == null) {
                return false;
            } else {
                return isCMSSignedAttributes;
            }
        }

        /**
         * Sets the value of the isCMSSignedAttributes property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setIsCMSSignedAttributes(Boolean value) {
            this.isCMSSignedAttributes = value;
        }

    }

}
