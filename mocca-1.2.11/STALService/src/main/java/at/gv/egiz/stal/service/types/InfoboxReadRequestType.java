
package at.gv.egiz.stal.service.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for InfoboxReadRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="InfoboxReadRequestType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.egiz.gv.at/stal}RequestType">
 *       &lt;sequence>
 *         &lt;element name="InfoboxIdentifier">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="Certificates"/>
 *               &lt;enumeration value="IdentityLink"/>
 *               &lt;enumeration value="Mandates"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="DomainIdentifier" type="{http://www.w3.org/2001/XMLSchema}anyURI" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InfoboxReadRequestType", propOrder = {
    "infoboxIdentifier",
    "domainIdentifier"
})
public class InfoboxReadRequestType
    extends RequestType
{

    @XmlElement(name = "InfoboxIdentifier", required = true)
    protected String infoboxIdentifier;
    @XmlElement(name = "DomainIdentifier")
    @XmlSchemaType(name = "anyURI")
    protected String domainIdentifier;

    /**
     * Gets the value of the infoboxIdentifier property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInfoboxIdentifier() {
        return infoboxIdentifier;
    }

    /**
     * Sets the value of the infoboxIdentifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInfoboxIdentifier(String value) {
        this.infoboxIdentifier = value;
    }

    /**
     * Gets the value of the domainIdentifier property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDomainIdentifier() {
        return domainIdentifier;
    }

    /**
     * Sets the value of the domainIdentifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDomainIdentifier(String value) {
        this.domainIdentifier = value;
    }

}
