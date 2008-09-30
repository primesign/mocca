
package at.gv.egiz.stal.service.types;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for GetNextRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetNextRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element name="InfoboxReadResponse" type="{http://www.egiz.gv.at/stal}InfoboxReadResponseType"/>
 *         &lt;element name="SignResponse" type="{http://www.egiz.gv.at/stal}SignResponseType"/>
 *         &lt;element name="ErrorResponse" type="{http://www.egiz.gv.at/stal}ErrorResponseType"/>
 *       &lt;/choice>
 *       &lt;attribute name="SessionId" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetNextRequestType", propOrder = {
    "infoboxReadResponseOrSignResponseOrErrorResponse"
})
public class GetNextRequestType {

    @XmlElements({
        @XmlElement(name = "SignResponse", type = SignResponseType.class),
        @XmlElement(name = "InfoboxReadResponse", type = InfoboxReadResponseType.class),
        @XmlElement(name = "ErrorResponse", type = ErrorResponseType.class)
    })
    protected List<ResponseType> infoboxReadResponseOrSignResponseOrErrorResponse;
    @XmlAttribute(name = "SessionId")
    protected String sessionId;

    /**
     * Gets the value of the infoboxReadResponseOrSignResponseOrErrorResponse property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the infoboxReadResponseOrSignResponseOrErrorResponse property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInfoboxReadResponseOrSignResponseOrErrorResponse().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SignResponseType }
     * {@link InfoboxReadResponseType }
     * {@link ErrorResponseType }
     * 
     * 
     */
    public List<ResponseType> getInfoboxReadResponseOrSignResponseOrErrorResponse() {
        if (infoboxReadResponseOrSignResponseOrErrorResponse == null) {
            infoboxReadResponseOrSignResponseOrErrorResponse = new ArrayList<ResponseType>();
        }
        return this.infoboxReadResponseOrSignResponseOrErrorResponse;
    }

    /**
     * Gets the value of the sessionId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Sets the value of the sessionId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSessionId(String value) {
        this.sessionId = value;
    }

}
