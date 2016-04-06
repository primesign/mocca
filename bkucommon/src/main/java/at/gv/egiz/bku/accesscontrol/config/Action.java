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
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="ChainRef" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="RuleAction"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *               &lt;enumeration value="allow"/&gt;
 *               &lt;enumeration value="deny"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
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
