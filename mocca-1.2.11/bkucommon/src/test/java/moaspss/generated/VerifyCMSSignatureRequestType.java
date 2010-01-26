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

package moaspss.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for VerifyCMSSignatureRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="VerifyCMSSignatureRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="DateTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="CMSSignature" type="{http://www.w3.org/2001/XMLSchema}base64Binary"/>
 *         &lt;element name="DataObject" type="{http://reference.e-government.gv.at/namespace/moa/20020822#}CMSDataObjectOptionalMetaType" minOccurs="0"/>
 *         &lt;element name="TrustProfileID" type="{http://www.w3.org/2001/XMLSchema}token"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VerifyCMSSignatureRequestType", propOrder = {
    "dateTime",
    "cmsSignature",
    "dataObject",
    "trustProfileID"
})
@XmlSeeAlso({
    VerifyCMSSignatureRequest.class
})
public class VerifyCMSSignatureRequestType {

    @XmlElement(name = "DateTime")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar dateTime;
    @XmlElement(name = "CMSSignature", required = true)
    protected byte[] cmsSignature;
    @XmlElement(name = "DataObject")
    protected CMSDataObjectOptionalMetaType dataObject;
    @XmlElement(name = "TrustProfileID", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String trustProfileID;

    /**
     * Gets the value of the dateTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDateTime() {
        return dateTime;
    }

    /**
     * Sets the value of the dateTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDateTime(XMLGregorianCalendar value) {
        this.dateTime = value;
    }

    /**
     * Gets the value of the cmsSignature property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getCMSSignature() {
        return cmsSignature;
    }

    /**
     * Sets the value of the cmsSignature property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setCMSSignature(byte[] value) {
        this.cmsSignature = ((byte[]) value);
    }

    /**
     * Gets the value of the dataObject property.
     * 
     * @return
     *     possible object is
     *     {@link CMSDataObjectOptionalMetaType }
     *     
     */
    public CMSDataObjectOptionalMetaType getDataObject() {
        return dataObject;
    }

    /**
     * Sets the value of the dataObject property.
     * 
     * @param value
     *     allowed object is
     *     {@link CMSDataObjectOptionalMetaType }
     *     
     */
    public void setDataObject(CMSDataObjectOptionalMetaType value) {
        this.dataObject = value;
    }

    /**
     * Gets the value of the trustProfileID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTrustProfileID() {
        return trustProfileID;
    }

    /**
     * Sets the value of the trustProfileID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTrustProfileID(String value) {
        this.trustProfileID = value;
    }

}
