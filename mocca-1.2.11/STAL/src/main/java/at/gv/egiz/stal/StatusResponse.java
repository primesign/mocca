
package at.gv.egiz.stal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for StatusResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="StatusResponseType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.egiz.gv.at/stal}ResponseType">
 *       &lt;attribute name="cardReady" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StatusResponseType")
public class StatusResponse
    extends STALResponse
{

    @XmlAttribute
    protected Boolean cardReady;

    /**
     * Gets the value of the cardReady property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCardReady() {
        return cardReady;
    }

    /**
     * Sets the value of the cardReady property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCardReady(Boolean value) {
        this.cardReady = value;
    }

}
