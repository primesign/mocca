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



package moaspss.generated;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="CreateSignatureLocation" type="{http://reference.e-government.gv.at/namespace/moa/20020822#}CreateSignatureLocationType"/&gt;
 *         &lt;element name="Supplement" type="{http://reference.e-government.gv.at/namespace/moa/20020822#}XMLDataObjectAssociationType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "createSignatureLocation",
    "supplement"
})
@XmlRootElement(name = "CreateSignatureEnvironmentProfile")
public class CreateSignatureEnvironmentProfile {

    @XmlElement(name = "CreateSignatureLocation", required = true)
    protected CreateSignatureLocationType createSignatureLocation;
    @XmlElement(name = "Supplement")
    protected List<XMLDataObjectAssociationType> supplement;

    /**
     * Gets the value of the createSignatureLocation property.
     * 
     * @return
     *     possible object is
     *     {@link CreateSignatureLocationType }
     *     
     */
    public CreateSignatureLocationType getCreateSignatureLocation() {
        return createSignatureLocation;
    }

    /**
     * Sets the value of the createSignatureLocation property.
     * 
     * @param value
     *     allowed object is
     *     {@link CreateSignatureLocationType }
     *     
     */
    public void setCreateSignatureLocation(CreateSignatureLocationType value) {
        this.createSignatureLocation = value;
    }

    /**
     * Gets the value of the supplement property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the supplement property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSupplement().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link XMLDataObjectAssociationType }
     * 
     * 
     */
    public List<XMLDataObjectAssociationType> getSupplement() {
        if (supplement == null) {
            supplement = new ArrayList<XMLDataObjectAssociationType>();
        }
        return this.supplement;
    }

}
