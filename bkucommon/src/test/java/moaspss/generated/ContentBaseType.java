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

package moaspss.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ContentBaseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ContentBaseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice minOccurs="0">
 *         &lt;element name="Base64Content" type="{http://www.w3.org/2001/XMLSchema}base64Binary"/>
 *         &lt;element name="XMLContent" type="{http://reference.e-government.gv.at/namespace/moa/20020822#}XMLContentType"/>
 *         &lt;element name="LocRefContent" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ContentBaseType", propOrder = {
    "base64Content",
    "xmlContent",
    "locRefContent"
})
@XmlSeeAlso({
    ContentExLocRefBaseType.class,
    ContentOptionalRefType.class
})
public class ContentBaseType {

    @XmlElement(name = "Base64Content")
    protected byte[] base64Content;
    @XmlElement(name = "XMLContent")
    protected XMLContentType xmlContent;
    @XmlElement(name = "LocRefContent")
    @XmlSchemaType(name = "anyURI")
    protected String locRefContent;

    /**
     * Gets the value of the base64Content property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getBase64Content() {
        return base64Content;
    }

    /**
     * Sets the value of the base64Content property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setBase64Content(byte[] value) {
        this.base64Content = ((byte[]) value);
    }

    /**
     * Gets the value of the xmlContent property.
     * 
     * @return
     *     possible object is
     *     {@link XMLContentType }
     *     
     */
    public XMLContentType getXMLContent() {
        return xmlContent;
    }

    /**
     * Sets the value of the xmlContent property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLContentType }
     *     
     */
    public void setXMLContent(XMLContentType value) {
        this.xmlContent = value;
    }

    /**
     * Gets the value of the locRefContent property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocRefContent() {
        return locRefContent;
    }

    /**
     * Sets the value of the locRefContent property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocRefContent(String value) {
        this.locRefContent = value;
    }

}
