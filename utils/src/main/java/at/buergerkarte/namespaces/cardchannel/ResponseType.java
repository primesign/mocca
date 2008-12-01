
package at.buergerkarte.namespaces.cardchannel;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * Contains the result of the script executed by the
 *                 BKU
 * 
 * <p>Java class for ResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element name="ATR" type="{}ATRType"/>
 *         &lt;element name="ResponseAPDU" type="{}ResponseAPDUType"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResponseType", propOrder = {
    "atrOrResponseAPDU"
})
public class ResponseType {

    @XmlElements({
        @XmlElement(name = "ATR", type = ATRType.class),
        @XmlElement(name = "ResponseAPDU", type = ResponseAPDUType.class)
    })
    protected List<Object> atrOrResponseAPDU;

    /**
     * Gets the value of the atrOrResponseAPDU property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the atrOrResponseAPDU property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getATROrResponseAPDU().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ATRType }
     * {@link ResponseAPDUType }
     * 
     * 
     */
    public List<Object> getATROrResponseAPDU() {
        if (atrOrResponseAPDU == null) {
            atrOrResponseAPDU = new ArrayList<Object>();
        }
        return this.atrOrResponseAPDU;
    }

}
