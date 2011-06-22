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



package moaspss.generated;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for VerifyXMLSignatureResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="VerifyXMLSignatureResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="SignerInfo" type="{http://www.w3.org/2000/09/xmldsig#}KeyInfoType"/>
 *         &lt;element name="HashInputData" type="{http://reference.e-government.gv.at/namespace/moa/20020822#}InputDataType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ReferenceInputData" type="{http://reference.e-government.gv.at/namespace/moa/20020822#}InputDataType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="SignatureCheck" type="{http://reference.e-government.gv.at/namespace/moa/20020822#}ReferencesCheckResultType"/>
 *         &lt;element name="SignatureManifestCheck" type="{http://reference.e-government.gv.at/namespace/moa/20020822#}ReferencesCheckResultType" minOccurs="0"/>
 *         &lt;element name="XMLDSIGManifestCheck" type="{http://reference.e-government.gv.at/namespace/moa/20020822#}ManifestRefsCheckResultType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="CertificateCheck" type="{http://reference.e-government.gv.at/namespace/moa/20020822#}CheckResultType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VerifyXMLSignatureResponseType", propOrder = {
    "signerInfo",
    "hashInputData",
    "referenceInputData",
    "signatureCheck",
    "signatureManifestCheck",
    "xmldsigManifestCheck",
    "certificateCheck"
})
public class VerifyXMLSignatureResponseType {

    @XmlElement(name = "SignerInfo", required = true)
    protected KeyInfoType signerInfo;
    @XmlElement(name = "HashInputData")
    protected List<InputDataType> hashInputData;
    @XmlElement(name = "ReferenceInputData")
    protected List<InputDataType> referenceInputData;
    @XmlElement(name = "SignatureCheck", required = true)
    protected ReferencesCheckResultType signatureCheck;
    @XmlElement(name = "SignatureManifestCheck")
    protected ReferencesCheckResultType signatureManifestCheck;
    @XmlElement(name = "XMLDSIGManifestCheck")
    protected List<ManifestRefsCheckResultType> xmldsigManifestCheck;
    @XmlElement(name = "CertificateCheck", required = true)
    protected CheckResultType certificateCheck;

    /**
     * Gets the value of the signerInfo property.
     * 
     * @return
     *     possible object is
     *     {@link KeyInfoType }
     *     
     */
    public KeyInfoType getSignerInfo() {
        return signerInfo;
    }

    /**
     * Sets the value of the signerInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link KeyInfoType }
     *     
     */
    public void setSignerInfo(KeyInfoType value) {
        this.signerInfo = value;
    }

    /**
     * Gets the value of the hashInputData property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the hashInputData property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHashInputData().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link InputDataType }
     * 
     * 
     */
    public List<InputDataType> getHashInputData() {
        if (hashInputData == null) {
            hashInputData = new ArrayList<InputDataType>();
        }
        return this.hashInputData;
    }

    /**
     * Gets the value of the referenceInputData property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the referenceInputData property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReferenceInputData().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link InputDataType }
     * 
     * 
     */
    public List<InputDataType> getReferenceInputData() {
        if (referenceInputData == null) {
            referenceInputData = new ArrayList<InputDataType>();
        }
        return this.referenceInputData;
    }

    /**
     * Gets the value of the signatureCheck property.
     * 
     * @return
     *     possible object is
     *     {@link ReferencesCheckResultType }
     *     
     */
    public ReferencesCheckResultType getSignatureCheck() {
        return signatureCheck;
    }

    /**
     * Sets the value of the signatureCheck property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReferencesCheckResultType }
     *     
     */
    public void setSignatureCheck(ReferencesCheckResultType value) {
        this.signatureCheck = value;
    }

    /**
     * Gets the value of the signatureManifestCheck property.
     * 
     * @return
     *     possible object is
     *     {@link ReferencesCheckResultType }
     *     
     */
    public ReferencesCheckResultType getSignatureManifestCheck() {
        return signatureManifestCheck;
    }

    /**
     * Sets the value of the signatureManifestCheck property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReferencesCheckResultType }
     *     
     */
    public void setSignatureManifestCheck(ReferencesCheckResultType value) {
        this.signatureManifestCheck = value;
    }

    /**
     * Gets the value of the xmldsigManifestCheck property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the xmldsigManifestCheck property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getXMLDSIGManifestCheck().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ManifestRefsCheckResultType }
     * 
     * 
     */
    public List<ManifestRefsCheckResultType> getXMLDSIGManifestCheck() {
        if (xmldsigManifestCheck == null) {
            xmldsigManifestCheck = new ArrayList<ManifestRefsCheckResultType>();
        }
        return this.xmldsigManifestCheck;
    }

    /**
     * Gets the value of the certificateCheck property.
     * 
     * @return
     *     possible object is
     *     {@link CheckResultType }
     *     
     */
    public CheckResultType getCertificateCheck() {
        return certificateCheck;
    }

    /**
     * Sets the value of the certificateCheck property.
     * 
     * @param value
     *     allowed object is
     *     {@link CheckResultType }
     *     
     */
    public void setCertificateCheck(CheckResultType value) {
        this.certificateCheck = value;
    }

}
