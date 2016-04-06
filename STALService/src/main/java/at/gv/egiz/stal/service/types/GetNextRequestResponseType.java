
package at.gv.egiz.stal.service.types;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlType;
//import at.buergerkarte.namespaces.cardchannel.service.ScriptType;


/**
 * <p>Java class for GetNextRequestResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetNextRequestResponseType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice maxOccurs="unbounded"&gt;
 *         &lt;element name="InfoboxReadRequest" type="{http://www.egiz.gv.at/stal}InfoboxReadRequestType"/&gt;
 *         &lt;element name="SignRequest" type="{http://www.egiz.gv.at/stal}SignRequestType"/&gt;
 *         &lt;element name="BulkSignRequest" type="{http://www.egiz.gv.at/stal}BulkSignRequestType"/&gt;
 *         &lt;element name="QuitRequest" type="{http://www.egiz.gv.at/stal}QuitRequestType"/&gt;
 *         &lt;element name="StatusRequest" type="{http://www.egiz.gv.at/stal}StatusRequestType"/&gt;
 *         &lt;element ref="{http://www.egiz.gv.at/stal}OtherRequest"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="SessionId" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetNextRequestResponseType", propOrder = {
    "infoboxReadRequestOrSignRequestOrBulkSignRequest"
})
public class GetNextRequestResponseType {

    @XmlElementRefs({
        @XmlElementRef(name = "BulkSignRequest", namespace = "http://www.egiz.gv.at/stal", type = JAXBElement.class),
        @XmlElementRef(name = "QuitRequest", namespace = "http://www.egiz.gv.at/stal", type = JAXBElement.class),
        @XmlElementRef(name = "InfoboxReadRequest", namespace = "http://www.egiz.gv.at/stal", type = JAXBElement.class),
        @XmlElementRef(name = "StatusRequest", namespace = "http://www.egiz.gv.at/stal", type = JAXBElement.class),
        @XmlElementRef(name = "SignRequest", namespace = "http://www.egiz.gv.at/stal", type = JAXBElement.class),
        @XmlElementRef(name = "OtherRequest", namespace = "http://www.egiz.gv.at/stal", type = JAXBElement.class)
    })
    protected List<JAXBElement<? extends RequestType>> infoboxReadRequestOrSignRequestOrBulkSignRequest;
    @XmlAttribute(name = "SessionId")
    protected String sessionId;

    /**
     * Gets the value of the infoboxReadRequestOrSignRequestOrBulkSignRequest property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the infoboxReadRequestOrSignRequestOrBulkSignRequest property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInfoboxReadRequestOrSignRequestOrBulkSignRequest().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link BulkSignRequestType }{@code >}
     * {@link JAXBElement }{@code <}{@link QuitRequestType }{@code >}
     * {@link JAXBElement }{@code <}{@link InfoboxReadRequestType }{@code >}
     * {@link JAXBElement }{@code <}{@link StatusRequestType }{@code >}
     * {@link JAXBElement }{@code <}{@link SignRequestType }{@code >}
     * {@link JAXBElement }{@code <}{@link ScriptType }{@code >}
     * {@link JAXBElement }{@code <}{@link RequestType }{@code >}
     * 
     * 
     */
    public List<JAXBElement<? extends RequestType>> getInfoboxReadRequestOrSignRequestOrBulkSignRequest() {
        if (infoboxReadRequestOrSignRequestOrBulkSignRequest == null) {
            infoboxReadRequestOrSignRequestOrBulkSignRequest = new ArrayList<JAXBElement<? extends RequestType>>();
        }
        return this.infoboxReadRequestOrSignRequestOrBulkSignRequest;
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
