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



package at.buergerkarte.namespaces.cardchannel.service;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * extends abstract stal:ResponseType
 * 
 * Contains the result of the script executed by the
 *                         BKU
 * 
 * <p>Java class for ResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ResponseType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.egiz.gv.at/stal}ResponseType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element name="ATR" type="{http://www.buergerkarte.at/cardchannel}ATRType"/>
 *         &lt;element name="ResponseAPDU" type="{http://www.buergerkarte.at/cardchannel}ResponseAPDUType"/>
 *       &lt;/choice>
 *     &lt;/extension>
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
public class ResponseType
    extends at.gv.egiz.stal.service.types.ResponseType
{

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
