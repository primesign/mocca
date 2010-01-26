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
package at.gv.egiz.bku.accesscontrol.config;

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
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element name="ChainRef" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="RuleAction">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="allow"/>
 *               &lt;enumeration value="deny"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "chainRef",
    "ruleAction"
})
@XmlRootElement(name = "Action")
public class Action {

    @XmlElement(name = "ChainRef")
    protected String chainRef;
    @XmlElement(name = "RuleAction")
    protected String ruleAction;

    /**
     * Gets the value of the chainRef property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChainRef() {
        return chainRef;
    }

    /**
     * Sets the value of the chainRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChainRef(String value) {
        this.chainRef = value;
    }

    /**
     * Gets the value of the ruleAction property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRuleAction() {
        return ruleAction;
    }

    /**
     * Sets the value of the ruleAction property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRuleAction(String value) {
        this.ruleAction = value;
    }

}
