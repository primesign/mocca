
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
 * <p>Java class for GetNextRequestResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetNextRequestResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element name="InfoboxReadRequest" type="{http://www.egiz.gv.at/stal}InfoboxReadRequestType"/>
 *         &lt;element name="SignRequest" type="{http://www.egiz.gv.at/stal}SignRequestType"/>
 *         &lt;element name="QuitRequest" type="{http://www.egiz.gv.at/stal}QuitRequestType"/>
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
@XmlType(name = "GetNextRequestResponseType", propOrder = {
    "infoboxReadRequestOrSignRequestOrQuitRequest"
})
public class GetNextRequestResponseType {

    @XmlElements({
        @XmlElement(name = "InfoboxReadRequest", type = InfoboxReadRequestType.class),
        @XmlElement(name = "QuitRequest", type = QuitRequestType.class),
        @XmlElement(name = "SignRequest", type = SignRequestType.class)
    })
    protected List<RequestType> infoboxReadRequestOrSignRequestOrQuitRequest;
    @XmlAttribute(name = "SessionId")
    protected String sessionId;

    /**
     * Gets the value of the infoboxReadRequestOrSignRequestOrQuitRequest property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the infoboxReadRequestOrSignRequestOrQuitRequest property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInfoboxReadRequestOrSignRequestOrQuitRequest().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link InfoboxReadRequestType }
     * {@link QuitRequestType }
     * {@link SignRequestType }
     * 
     * 
     */
    public List<RequestType> getInfoboxReadRequestOrSignRequestOrQuitRequest() {
        if (infoboxReadRequestOrSignRequestOrQuitRequest == null) {
            infoboxReadRequestOrSignRequestOrQuitRequest = new ArrayList<RequestType>();
        }
        return this.infoboxReadRequestOrSignRequestOrQuitRequest;
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
