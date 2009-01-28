
package at.buergerkarte.namespaces.cardchannel.service;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;
import at.gv.egiz.stal.service.types.RequestType;


/**
 * extends abstract stal:RequestType
 * 
 * Contains the script to be executed by the BKU
 * 
 * <p>Java class for ScriptType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ScriptType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.egiz.gv.at/stal}RequestType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element name="Reset" type="{http://www.buergerkarte.at/cardchannel}ResetType"/>
 *         &lt;element name="CommandAPDU" type="{http://www.buergerkarte.at/cardchannel}CommandAPDUType"/>
 *         &lt;element name="VerifyAPDU" type="{http://www.buergerkarte.at/cardchannel}VerifyAPDUType"/>
 *       &lt;/choice>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ScriptType", propOrder = {
    "resetOrCommandAPDUOrVerifyAPDU"
})
public class ScriptType
    extends RequestType
{

    @XmlElements({
        @XmlElement(name = "VerifyAPDU", type = VerifyAPDUType.class),
        @XmlElement(name = "Reset", type = ResetType.class),
        @XmlElement(name = "CommandAPDU", type = CommandAPDUType.class)
    })
    protected List<Object> resetOrCommandAPDUOrVerifyAPDU;

    /**
     * Gets the value of the resetOrCommandAPDUOrVerifyAPDU property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the resetOrCommandAPDUOrVerifyAPDU property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getResetOrCommandAPDUOrVerifyAPDU().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link VerifyAPDUType }
     * {@link ResetType }
     * {@link CommandAPDUType }
     * 
     * 
     */
    public List<Object> getResetOrCommandAPDUOrVerifyAPDU() {
        if (resetOrCommandAPDUOrVerifyAPDU == null) {
            resetOrCommandAPDUOrVerifyAPDU = new ArrayList<Object>();
        }
        return this.resetOrCommandAPDUOrVerifyAPDU;
    }

}
