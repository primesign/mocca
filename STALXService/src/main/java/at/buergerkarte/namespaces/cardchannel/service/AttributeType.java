
package at.buergerkarte.namespaces.cardchannel.service;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * Contains an attribute converted from ASN.1
 * 
 * <p>Java class for AttributeType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AttributeType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Integer" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="Latin1String" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="UTF8String" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="NumericString" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="PrintableString" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="GeneralizedTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="Date" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="oid" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AttributeType", propOrder = {
    "integer",
    "latin1String",
    "utf8String",
    "numericString",
    "printableString",
    "generalizedTime",
    "date"
})
public class AttributeType {

    @XmlElement(name = "Integer")
    protected BigInteger integer;
    @XmlElement(name = "Latin1String")
    protected String latin1String;
    @XmlElement(name = "UTF8String")
    protected String utf8String;
    @XmlElement(name = "NumericString")
    protected String numericString;
    @XmlElement(name = "PrintableString")
    protected String printableString;
    @XmlElement(name = "GeneralizedTime")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar generalizedTime;
    @XmlElement(name = "Date")
    protected String date;
    @XmlAttribute(required = true)
    protected String oid;

    /**
     * Gets the value of the integer property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getInteger() {
        return integer;
    }

    /**
     * Sets the value of the integer property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setInteger(BigInteger value) {
        this.integer = value;
    }

    /**
     * Gets the value of the latin1String property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLatin1String() {
        return latin1String;
    }

    /**
     * Sets the value of the latin1String property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLatin1String(String value) {
        this.latin1String = value;
    }

    /**
     * Gets the value of the utf8String property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUTF8String() {
        return utf8String;
    }

    /**
     * Sets the value of the utf8String property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUTF8String(String value) {
        this.utf8String = value;
    }

    /**
     * Gets the value of the numericString property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNumericString() {
        return numericString;
    }

    /**
     * Sets the value of the numericString property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNumericString(String value) {
        this.numericString = value;
    }

    /**
     * Gets the value of the printableString property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPrintableString() {
        return printableString;
    }

    /**
     * Sets the value of the printableString property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPrintableString(String value) {
        this.printableString = value;
    }

    /**
     * Gets the value of the generalizedTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getGeneralizedTime() {
        return generalizedTime;
    }

    /**
     * Sets the value of the generalizedTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setGeneralizedTime(XMLGregorianCalendar value) {
        this.generalizedTime = value;
    }

    /**
     * Gets the value of the date property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDate() {
        return date;
    }

    /**
     * Sets the value of the date property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDate(String value) {
        this.date = value;
    }

    /**
     * Gets the value of the oid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOid() {
        return oid;
    }

    /**
     * Sets the value of the oid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOid(String value) {
        this.oid = value;
    }

}
