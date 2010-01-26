
package at.gv.egiz.stal.service.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for InfoboxReadResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="InfoboxReadResponseType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.egiz.gv.at/stal}ResponseType">
 *       &lt;sequence>
 *         &lt;element name="InfoboxValue" type="{http://www.w3.org/2001/XMLSchema}base64Binary"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InfoboxReadResponseType", propOrder = {
    "infoboxValue"
})
public class InfoboxReadResponseType
    extends ResponseType
{

    @XmlElement(name = "InfoboxValue", required = true)
    protected byte[] infoboxValue;

    /**
     * Gets the value of the infoboxValue property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getInfoboxValue() {
        return infoboxValue;
    }

    /**
     * Sets the value of the infoboxValue property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setInfoboxValue(byte[] value) {
        this.infoboxValue = ((byte[]) value);
    }

}
