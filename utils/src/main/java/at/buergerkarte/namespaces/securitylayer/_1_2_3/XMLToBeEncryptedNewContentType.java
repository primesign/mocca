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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for XMLToBeEncryptedNewContentType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="XMLToBeEncryptedNewContentType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.buergerkarte.at/namespaces/securitylayer/1.2#}Base64XMLLocRefContentType"&gt;
 *       &lt;attribute name="EncDataReference" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "XMLToBeEncryptedNewContentType")
public class XMLToBeEncryptedNewContentType
    extends Base64XMLLocRefContentType
{

    @XmlAttribute(name = "EncDataReference")
    @XmlSchemaType(name = "anyURI")
    protected String encDataReference;

    /**
     * Gets the value of the encDataReference property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEncDataReference() {
        return encDataReference;
    }

    /**
     * Sets the value of the encDataReference property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEncDataReference(String value) {
        this.encDataReference = value;
    }

}
