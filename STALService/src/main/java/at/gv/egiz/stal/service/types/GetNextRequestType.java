/*
 * Copyright 2011 by Graz University of Technology, Austria
 * MOCCA has been developed by the E-Government Innovation Center EGIZ, a joint
 * initiative of the Federal Chancellery Austria and Graz University of Technology.
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 */



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
 * &lt;complexType name="GetNextRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element name="InfoboxReadResponse" type="{http://www.egiz.gv.at/stal}InfoboxReadResponseType"/>
 *         &lt;element name="SignResponse" type="{http://www.egiz.gv.at/stal}SignResponseType"/>
 *         &lt;element name="ErrorResponse" type="{http://www.egiz.gv.at/stal}ErrorResponseType"/>
 *         &lt;element name="StatusResponse" type="{http://www.egiz.gv.at/stal}StatusResponseType"/>
 *         &lt;element ref="{http://www.egiz.gv.at/stal}OtherResponse"/>
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

    @XmlElementRefs({
        @XmlElementRef(name = "StatusResponse", namespace = "http://www.egiz.gv.at/stal", type = JAXBElement.class),
        @XmlElementRef(name = "InfoboxReadResponse", namespace = "http://www.egiz.gv.at/stal", type = JAXBElement.class),
        @XmlElementRef(name = "OtherResponse", namespace = "http://www.egiz.gv.at/stal", type = JAXBElement.class),
        @XmlElementRef(name = "ErrorResponse", namespace = "http://www.egiz.gv.at/stal", type = JAXBElement.class),
        @XmlElementRef(name = "SignResponse", namespace = "http://www.egiz.gv.at/stal", type = JAXBElement.class)
    })
    protected List<JAXBElement<? extends at.gv.egiz.stal.service.types.ResponseType>> infoboxReadResponseOrSignResponseOrErrorResponse;
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
     * {@link JAXBElement }{@code <}{@link at.buergerkarte.namespaces.cardchannel.service.ResponseType }{@code >}
     * {@link JAXBElement }{@code <}{@link StatusResponseType }{@code >}
     * {@link JAXBElement }{@code <}{@link at.gv.egiz.stal.service.types.ResponseType }{@code >}
     * {@link JAXBElement }{@code <}{@link ErrorResponseType }{@code >}
     * {@link JAXBElement }{@code <}{@link SignResponseType }{@code >}
     * {@link JAXBElement }{@code <}{@link InfoboxReadResponseType }{@code >}
     * 
     * 
     */
    public List<JAXBElement<? extends at.gv.egiz.stal.service.types.ResponseType>> getInfoboxReadResponseOrSignResponseOrErrorResponse() {
        if (infoboxReadResponseOrSignResponseOrErrorResponse == null) {
            infoboxReadResponseOrSignResponseOrErrorResponse = new ArrayList<JAXBElement<? extends at.gv.egiz.stal.service.types.ResponseType>>();
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
