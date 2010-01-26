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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for TransformParameterType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TransformParameterType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice minOccurs="0">
 *         &lt;element name="Base64Content" type="{http://www.w3.org/2001/XMLSchema}base64Binary"/>
 *         &lt;element name="Hash">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}DigestMethod"/>
 *                   &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}DigestValue"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/choice>
 *       &lt;attribute name="URI" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TransformParameterType", propOrder = {
    "base64Content",
    "hash"
})
public class TransformParameterType {

    @XmlElement(name = "Base64Content")
    protected byte[] base64Content;
    @XmlElement(name = "Hash")
    protected TransformParameterType.Hash hash;
    @XmlAttribute(name = "URI", required = true)
    @XmlSchemaType(name = "anyURI")
    protected String uri;

    /**
     * Gets the value of the base64Content property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getBase64Content() {
        return base64Content;
    }

    /**
     * Sets the value of the base64Content property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setBase64Content(byte[] value) {
        this.base64Content = ((byte[]) value);
    }

    /**
     * Gets the value of the hash property.
     * 
     * @return
     *     possible object is
     *     {@link TransformParameterType.Hash }
     *     
     */
    public TransformParameterType.Hash getHash() {
        return hash;
    }

    /**
     * Sets the value of the hash property.
     * 
     * @param value
     *     allowed object is
     *     {@link TransformParameterType.Hash }
     *     
     */
    public void setHash(TransformParameterType.Hash value) {
        this.hash = value;
    }

    /**
     * Gets the value of the uri property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getURI() {
        return uri;
    }

    /**
     * Sets the value of the uri property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setURI(String value) {
        this.uri = value;
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
     *       &lt;sequence>
     *         &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}DigestMethod"/>
     *         &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}DigestValue"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "digestMethod",
        "digestValue"
    })
    public static class Hash {

        @XmlElement(name = "DigestMethod", namespace = "http://www.w3.org/2000/09/xmldsig#", required = true)
        protected DigestMethodType digestMethod;
        @XmlElement(name = "DigestValue", namespace = "http://www.w3.org/2000/09/xmldsig#", required = true)
        protected byte[] digestValue;

        /**
         * Gets the value of the digestMethod property.
         * 
         * @return
         *     possible object is
         *     {@link DigestMethodType }
         *     
         */
        public DigestMethodType getDigestMethod() {
            return digestMethod;
        }

        /**
         * Sets the value of the digestMethod property.
         * 
         * @param value
         *     allowed object is
         *     {@link DigestMethodType }
         *     
         */
        public void setDigestMethod(DigestMethodType value) {
            this.digestMethod = value;
        }

        /**
         * Gets the value of the digestValue property.
         * 
         * @return
         *     possible object is
         *     byte[]
         */
        public byte[] getDigestValue() {
            return digestValue;
        }

        /**
         * Sets the value of the digestValue property.
         * 
         * @param value
         *     allowed object is
         *     byte[]
         */
        public void setDigestValue(byte[] value) {
            this.digestValue = ((byte[]) value);
        }

    }

}
