
package at.buergerkarte.namespaces.cardchannel.service;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * Contains a sequence of bytes received from the card as response
 *                 APDU
 * 
 * <p>Java class for ResponseAPDUType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ResponseAPDUType">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>hexBinary">
 *       &lt;attribute name="sequence" use="required" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" />
 *       &lt;attribute name="rc" type="{http://www.w3.org/2001/XMLSchema}integer" default="0" />
 *       &lt;attribute name="SW" type="{http://www.w3.org/2001/XMLSchema}hexBinary" default="9000" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResponseAPDUType", propOrder = {
    "value"
})
public class ResponseAPDUType {

    @XmlValue
    @XmlJavaTypeAdapter(HexBinaryAdapter.class)
    @XmlSchemaType(name = "hexBinary")
    protected byte[] value;
    @XmlAttribute(required = true)
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger sequence;
    @XmlAttribute
    protected BigInteger rc;
    @XmlAttribute(name = "SW")
    @XmlJavaTypeAdapter(HexBinaryAdapter.class)
    @XmlSchemaType(name = "hexBinary")
    protected byte[] sw;

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public byte[] getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValue(byte[] value) {
        this.value = ((byte[]) value);
    }

    /**
     * Gets the value of the sequence property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getSequence() {
        return sequence;
    }

    /**
     * Sets the value of the sequence property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setSequence(BigInteger value) {
        this.sequence = value;
    }

    /**
     * Gets the value of the rc property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getRc() {
        if (rc == null) {
            return new BigInteger("0");
        } else {
            return rc;
        }
    }

    /**
     * Sets the value of the rc property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setRc(BigInteger value) {
        this.rc = value;
    }

    /**
     * Gets the value of the sw property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public byte[] getSW() {
        if (sw == null) {
            return new HexBinaryAdapter().unmarshal("9000");
        } else {
            return sw;
        }
    }

    /**
     * Sets the value of the sw property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSW(byte[] value) {
        this.sw = ((byte[]) value);
    }

}
