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


//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-520 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.07.21 at 09:30:44 AM GMT 
//


package org.w3._2001._04.xmldsig_more_;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DomainParamsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DomainParamsType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="ExplicitParams" type="{http://www.w3.org/2001/04/xmldsig-more#}ExplicitParamsType"/&gt;
 *         &lt;element name="NamedCurve"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;attribute name="URN" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DomainParamsType", propOrder = {
    "explicitParams",
    "namedCurve"
})
public class DomainParamsType {

    @XmlElement(name = "ExplicitParams")
    protected ExplicitParamsType explicitParams;
    @XmlElement(name = "NamedCurve")
    protected DomainParamsType.NamedCurve namedCurve;

    /**
     * Gets the value of the explicitParams property.
     * 
     * @return
     *     possible object is
     *     {@link ExplicitParamsType }
     *     
     */
    public ExplicitParamsType getExplicitParams() {
        return explicitParams;
    }

    /**
     * Sets the value of the explicitParams property.
     * 
     * @param value
     *     allowed object is
     *     {@link ExplicitParamsType }
     *     
     */
    public void setExplicitParams(ExplicitParamsType value) {
        this.explicitParams = value;
    }

    /**
     * Gets the value of the namedCurve property.
     * 
     * @return
     *     possible object is
     *     {@link DomainParamsType.NamedCurve }
     *     
     */
    public DomainParamsType.NamedCurve getNamedCurve() {
        return namedCurve;
    }

    /**
     * Sets the value of the namedCurve property.
     * 
     * @param value
     *     allowed object is
     *     {@link DomainParamsType.NamedCurve }
     *     
     */
    public void setNamedCurve(DomainParamsType.NamedCurve value) {
        this.namedCurve = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;attribute name="URN" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class NamedCurve {

        @XmlAttribute(name = "URN", required = true)
        @XmlSchemaType(name = "anyURI")
        protected String urn;

        /**
         * Gets the value of the urn property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getURN() {
            return urn;
        }

        /**
         * Sets the value of the urn property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setURN(String value) {
            this.urn = value;
        }

    }

}
