
package at.gv.egiz.stal.service.types;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BulkSignResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BulkSignResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence maxOccurs="unbounded">
 *         &lt;element name="SignResponse" type="{http://www.egiz.gv.at/stal}SignResponseType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BulkSignResponseType", propOrder = {
    "signResponse"
})
public class BulkSignResponseType extends ResponseType {

    @XmlElement(name = "SignResponse", required = true)
    protected List<SignResponseType> signResponse;

    /**
     * Gets the value of the signResponse property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the signResponse property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>SignRequestType
     *    getSignResponse().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SignResponseType }
     * 
     * 
     */
    public List<SignResponseType> getSignResponse() {
        if (signResponse == null) {
            signResponse = new ArrayList<SignResponseType>();
        }
        return this.signResponse;
    }

}
