//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.06.01 at 04:42:31 PM CEST 
//


package at.buergerkarte.namespaces.securitylayer._1_2_3;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for InfoboxReadParamsAssocArrayType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="InfoboxReadParamsAssocArrayType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="ReadKeys"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;attribute name="SearchString" use="required" type="{http://www.buergerkarte.at/namespaces/securitylayer/1.2#}WildCardSearchStringType" /&gt;
 *                 &lt;attribute name="UserMakesUnique" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="ReadPairs"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;attribute name="SearchString" use="required" type="{http://www.buergerkarte.at/namespaces/securitylayer/1.2#}WildCardSearchStringType" /&gt;
 *                 &lt;attribute name="UserMakesUnique" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *                 &lt;attribute name="ValuesAreXMLEntities" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="ReadValue"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;attribute name="Key" use="required" type="{http://www.buergerkarte.at/namespaces/securitylayer/1.2#}BoxIdentifierType" /&gt;
 *                 &lt;attribute name="ValueIsXMLEntity" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
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
@XmlType(name = "InfoboxReadParamsAssocArrayType", propOrder = {
    "readKeys",
    "readPairs",
    "readValue"
})
public class InfoboxReadParamsAssocArrayType {

    @XmlElement(name = "ReadKeys")
    protected InfoboxReadParamsAssocArrayType.ReadKeys readKeys;
    @XmlElement(name = "ReadPairs")
    protected InfoboxReadParamsAssocArrayType.ReadPairs readPairs;
    @XmlElement(name = "ReadValue")
    protected InfoboxReadParamsAssocArrayType.ReadValue readValue;

    /**
     * Gets the value of the readKeys property.
     * 
     * @return
     *     possible object is
     *     {@link InfoboxReadParamsAssocArrayType.ReadKeys }
     *     
     */
    public InfoboxReadParamsAssocArrayType.ReadKeys getReadKeys() {
        return readKeys;
    }

    /**
     * Sets the value of the readKeys property.
     * 
     * @param value
     *     allowed object is
     *     {@link InfoboxReadParamsAssocArrayType.ReadKeys }
     *     
     */
    public void setReadKeys(InfoboxReadParamsAssocArrayType.ReadKeys value) {
        this.readKeys = value;
    }

    /**
     * Gets the value of the readPairs property.
     * 
     * @return
     *     possible object is
     *     {@link InfoboxReadParamsAssocArrayType.ReadPairs }
     *     
     */
    public InfoboxReadParamsAssocArrayType.ReadPairs getReadPairs() {
        return readPairs;
    }

    /**
     * Sets the value of the readPairs property.
     * 
     * @param value
     *     allowed object is
     *     {@link InfoboxReadParamsAssocArrayType.ReadPairs }
     *     
     */
    public void setReadPairs(InfoboxReadParamsAssocArrayType.ReadPairs value) {
        this.readPairs = value;
    }

    /**
     * Gets the value of the readValue property.
     * 
     * @return
     *     possible object is
     *     {@link InfoboxReadParamsAssocArrayType.ReadValue }
     *     
     */
    public InfoboxReadParamsAssocArrayType.ReadValue getReadValue() {
        return readValue;
    }

    /**
     * Sets the value of the readValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link InfoboxReadParamsAssocArrayType.ReadValue }
     *     
     */
    public void setReadValue(InfoboxReadParamsAssocArrayType.ReadValue value) {
        this.readValue = value;
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
     *       &lt;attribute name="SearchString" use="required" type="{http://www.buergerkarte.at/namespaces/securitylayer/1.2#}WildCardSearchStringType" /&gt;
     *       &lt;attribute name="UserMakesUnique" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class ReadKeys {

        @XmlAttribute(name = "SearchString", required = true)
        protected String searchString;
        @XmlAttribute(name = "UserMakesUnique")
        protected Boolean userMakesUnique;

        /**
         * Gets the value of the searchString property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getSearchString() {
            return searchString;
        }

        /**
         * Sets the value of the searchString property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSearchString(String value) {
            this.searchString = value;
        }

        /**
         * Gets the value of the userMakesUnique property.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public boolean isUserMakesUnique() {
            if (userMakesUnique == null) {
                return false;
            } else {
                return userMakesUnique;
            }
        }

        /**
         * Sets the value of the userMakesUnique property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setUserMakesUnique(Boolean value) {
            this.userMakesUnique = value;
        }

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
     *       &lt;attribute name="SearchString" use="required" type="{http://www.buergerkarte.at/namespaces/securitylayer/1.2#}WildCardSearchStringType" /&gt;
     *       &lt;attribute name="UserMakesUnique" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
     *       &lt;attribute name="ValuesAreXMLEntities" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class ReadPairs {

        @XmlAttribute(name = "SearchString", required = true)
        protected String searchString;
        @XmlAttribute(name = "UserMakesUnique")
        protected Boolean userMakesUnique;
        @XmlAttribute(name = "ValuesAreXMLEntities")
        protected Boolean valuesAreXMLEntities;

        /**
         * Gets the value of the searchString property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getSearchString() {
            return searchString;
        }

        /**
         * Sets the value of the searchString property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSearchString(String value) {
            this.searchString = value;
        }

        /**
         * Gets the value of the userMakesUnique property.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public boolean isUserMakesUnique() {
            if (userMakesUnique == null) {
                return false;
            } else {
                return userMakesUnique;
            }
        }

        /**
         * Sets the value of the userMakesUnique property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setUserMakesUnique(Boolean value) {
            this.userMakesUnique = value;
        }

        /**
         * Gets the value of the valuesAreXMLEntities property.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public boolean isValuesAreXMLEntities() {
            if (valuesAreXMLEntities == null) {
                return false;
            } else {
                return valuesAreXMLEntities;
            }
        }

        /**
         * Sets the value of the valuesAreXMLEntities property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setValuesAreXMLEntities(Boolean value) {
            this.valuesAreXMLEntities = value;
        }

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
     *       &lt;attribute name="Key" use="required" type="{http://www.buergerkarte.at/namespaces/securitylayer/1.2#}BoxIdentifierType" /&gt;
     *       &lt;attribute name="ValueIsXMLEntity" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class ReadValue {

        @XmlAttribute(name = "Key", required = true)
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        protected String key;
        @XmlAttribute(name = "ValueIsXMLEntity")
        protected Boolean valueIsXMLEntity;

        /**
         * Gets the value of the key property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getKey() {
            return key;
        }

        /**
         * Sets the value of the key property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setKey(String value) {
            this.key = value;
        }

        /**
         * Gets the value of the valueIsXMLEntity property.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public boolean isValueIsXMLEntity() {
            if (valueIsXMLEntity == null) {
                return false;
            } else {
                return valueIsXMLEntity;
            }
        }

        /**
         * Sets the value of the valueIsXMLEntity property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setValueIsXMLEntity(Boolean value) {
            this.valueIsXMLEntity = value;
        }

    }

}
