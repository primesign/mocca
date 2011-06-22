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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for DataObjectInfoType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DataObjectInfoType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="DataObject">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;extension base="{http://reference.e-government.gv.at/namespace/moa/20020822#}ContentOptionalRefType">
 *               &lt;/extension>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;choice>
 *           &lt;element ref="{http://reference.e-government.gv.at/namespace/moa/20020822#}CreateTransformsInfoProfile"/>
 *           &lt;element name="CreateTransformsInfoProfileID" type="{http://reference.e-government.gv.at/namespace/moa/20020822#}ProfileIdentifierType"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *       &lt;attribute name="Structure" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="detached"/>
 *             &lt;enumeration value="enveloping"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataObjectInfoType", propOrder = {
    "dataObject",
    "createTransformsInfoProfile",
    "createTransformsInfoProfileID"
})
@XmlSeeAlso({
    moaspss.generated.CreateXMLSignatureRequestType.SingleSignatureInfo.DataObjectInfo.class
})
public class DataObjectInfoType {

    @XmlElement(name = "DataObject", required = true)
    protected DataObjectInfoType.DataObject dataObject;
    @XmlElement(name = "CreateTransformsInfoProfile")
    protected CreateTransformsInfoProfile createTransformsInfoProfile;
    @XmlElement(name = "CreateTransformsInfoProfileID")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String createTransformsInfoProfileID;
    @XmlAttribute(name = "Structure", required = true)
    protected String structure;

    /**
     * Gets the value of the dataObject property.
     * 
     * @return
     *     possible object is
     *     {@link DataObjectInfoType.DataObject }
     *     
     */
    public DataObjectInfoType.DataObject getDataObject() {
        return dataObject;
    }

    /**
     * Sets the value of the dataObject property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataObjectInfoType.DataObject }
     *     
     */
    public void setDataObject(DataObjectInfoType.DataObject value) {
        this.dataObject = value;
    }

    /**
     * Gets the value of the createTransformsInfoProfile property.
     * 
     * @return
     *     possible object is
     *     {@link CreateTransformsInfoProfile }
     *     
     */
    public CreateTransformsInfoProfile getCreateTransformsInfoProfile() {
        return createTransformsInfoProfile;
    }

    /**
     * Sets the value of the createTransformsInfoProfile property.
     * 
     * @param value
     *     allowed object is
     *     {@link CreateTransformsInfoProfile }
     *     
     */
    public void setCreateTransformsInfoProfile(CreateTransformsInfoProfile value) {
        this.createTransformsInfoProfile = value;
    }

    /**
     * Gets the value of the createTransformsInfoProfileID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCreateTransformsInfoProfileID() {
        return createTransformsInfoProfileID;
    }

    /**
     * Sets the value of the createTransformsInfoProfileID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCreateTransformsInfoProfileID(String value) {
        this.createTransformsInfoProfileID = value;
    }

    /**
     * Gets the value of the structure property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStructure() {
        return structure;
    }

    /**
     * Sets the value of the structure property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStructure(String value) {
        this.structure = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;extension base="{http://reference.e-government.gv.at/namespace/moa/20020822#}ContentOptionalRefType">
     *     &lt;/extension>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class DataObject
        extends ContentOptionalRefType
    {


    }

}
