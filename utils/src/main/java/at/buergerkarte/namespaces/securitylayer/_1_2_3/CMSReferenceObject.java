//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.5 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2015.09.28 um 04:08:24 PM CEST 
//

package at.buergerkarte.namespaces.securitylayer._1_2_3;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für CMSReferenceObject complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="CMSReferenceObject">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.buergerkarte.at/namespaces/securitylayer/1.2#}CMSDataObjectOptionalMetaType">
 *       &lt;sequence>
 *         &lt;element name="MetaInfo" type="{http://www.buergerkarte.at/namespaces/securitylayer/1.2#}MetaInfoType"/>
 *         &lt;choice>
 *           &lt;element name="Content" type="{http://www.buergerkarte.at/namespaces/securitylayer/1.2#}Base64OptRefContentType"/>
 *           &lt;element name="DigestAndRef" type="{http://www.buergerkarte.at/namespaces/securitylayer/1.2#}DigestAndRefType"/>
 *         &lt;/choice>
 *         &lt;element name="ExcludedByteRange" type="{http://www.buergerkarte.at/namespaces/securitylayer/1.2#}ExcludedByteRangeType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CMSReferenceObject")
public class CMSReferenceObject
    extends CMSDataObjectOptionalMetaType
{


}
