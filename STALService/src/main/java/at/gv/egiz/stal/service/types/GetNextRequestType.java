
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


/**
 * <p>Java class for GetNextRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetNextRequestType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice maxOccurs="unbounded"&gt;
 *         &lt;element name="InfoboxReadResponse" type="{http://www.egiz.gv.at/stal}InfoboxReadResponseType"/&gt;
 *         &lt;element name="SignResponse" type="{http://www.egiz.gv.at/stal}SignResponseType"/&gt;
 *         &lt;element name="BulkSignResponse" type="{http://www.egiz.gv.at/stal}BulkSignResponseType"/&gt;
 *         &lt;element name="ErrorResponse" type="{http://www.egiz.gv.at/stal}ErrorResponseType"/&gt;
 *         &lt;element name="StatusResponse" type="{http://www.egiz.gv.at/stal}StatusResponseType"/&gt;
 *         &lt;element ref="{http://www.egiz.gv.at/stal}OtherResponse"/&gt;
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
@XmlType(name = "GetNextRequestType", propOrder = {
    "infoboxReadResponseOrSignResponseOrBulkSignResponse"
})
public class GetNextRequestType {

    @XmlElementRefs({
        @XmlElementRef(name = "SignResponse", namespace = "http://www.egiz.gv.at/stal", type = JAXBElement.class),
        @XmlElementRef(name = "InfoboxReadResponse", namespace = "http://www.egiz.gv.at/stal", type = JAXBElement.class),
        @XmlElementRef(name = "BulkSignResponse", namespace = "http://www.egiz.gv.at/stal", type = JAXBElement.class),
        @XmlElementRef(name = "ErrorResponse", namespace = "http://www.egiz.gv.at/stal", type = JAXBElement.class),
        @XmlElementRef(name = "StatusResponse", namespace = "http://www.egiz.gv.at/stal", type = JAXBElement.class),
        @XmlElementRef(name = "OtherResponse", namespace = "http://www.egiz.gv.at/stal", type = JAXBElement.class)
    })
    protected List<JAXBElement<? extends at.gv.egiz.stal.service.types.ResponseType>> infoboxReadResponseOrSignResponseOrBulkSignResponse;
    @XmlAttribute(name = "SessionId")
    protected String sessionId;

    /**
     * Gets the value of the infoboxReadResponseOrSignResponseOrBulkSignResponse property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the infoboxReadResponseOrSignResponseOrBulkSignResponse property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInfoboxReadResponseOrSignResponseOrBulkSignResponse().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link SignResponseType }{@code >}
     * {@link JAXBElement }{@code <}{@link InfoboxReadResponseType }{@code >}
     * {@link JAXBElement }{@code <}{@link BulkSignResponseType }{@code >}
     * {@link JAXBElement }{@code <}{@link ErrorResponseType }{@code >}
     * {@link JAXBElement }{@code <}{@link StatusResponseType }{@code >}
     * {@link JAXBElement }{@code <}{@link at.buergerkarte.namespaces.cardchannel.service.ResponseType }{@code >}
     * {@link JAXBElement }{@code <}{@link at.gv.egiz.stal.service.types.ResponseType }{@code >}
     * 
     * 
     */
    public List<JAXBElement<? extends at.gv.egiz.stal.service.types.ResponseType>> getInfoboxReadResponseOrSignResponseOrBulkSignResponse() {
        if (infoboxReadResponseOrSignResponseOrBulkSignResponse == null) {
            infoboxReadResponseOrSignResponseOrBulkSignResponse = new ArrayList<JAXBElement<? extends at.gv.egiz.stal.service.types.ResponseType>>();
        }
        return this.infoboxReadResponseOrSignResponseOrBulkSignResponse;
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
