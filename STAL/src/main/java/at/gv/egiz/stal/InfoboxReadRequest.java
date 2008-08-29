/*
* Copyright 2008 Federal Chancellery Austria and
* Graz University of Technology
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package at.gv.egiz.stal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for InfoboxReadRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="InfoboxReadRequestType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.egiz.gv.at/stal}RequestType">
 *       &lt;sequence>
 *         &lt;element name="InfoboxIdentifier" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="DomainIdentifier" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InfoboxReadRequestType", propOrder = {
    "infoboxIdentifier",
    "domainIdentifier"
})
public class InfoboxReadRequest
    extends STALRequest
{

    @XmlElement(name = "InfoboxIdentifier", required = true)
    protected String infoboxIdentifier;
    @XmlElement(name = "DomainIdentifier")
    protected String domainIdentifier;

    /**
     * Gets the value of the infoboxIdentifier property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInfoboxIdentifier() {
        return infoboxIdentifier;
    }

    /**
     * Sets the value of the infoboxIdentifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInfoboxIdentifier(String value) {
        this.infoboxIdentifier = value;
    }

    /**
     * Gets the value of the domainIdentifier property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDomainIdentifier() {
        return domainIdentifier;
    }

    /**
     * Sets the value of the domainIdentifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDomainIdentifier(String value) {
        this.domainIdentifier = value;
    }
    
    public String toString() {
      return "InfoboxReadRequest for: "+infoboxIdentifier;
    }

}
