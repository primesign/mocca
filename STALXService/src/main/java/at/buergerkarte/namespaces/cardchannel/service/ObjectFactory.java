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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the at.buergerkarte.namespaces.cardchannel.service package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _EHIC_QNAME = new QName("http://www.buergerkarte.at/cardchannel", "EHIC");
    private final static QName _Status_QNAME = new QName("http://www.buergerkarte.at/cardchannel", "Status");
    private final static QName _SVPersonenbindung_QNAME = new QName("http://www.buergerkarte.at/cardchannel", "SV-Personenbindung");
    private final static QName _Grunddaten_QNAME = new QName("http://www.buergerkarte.at/cardchannel", "Grunddaten");

    /** TODO */
    private final static QName _Response_QNAME = new QName("http://www.buergerkarte.at/cardchannel", "Response");
    private final static QName _Script_QNAME = new QName("http://www.buergerkarte.at/cardchannel", "Script");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: at.buergerkarte.namespaces.cardchannel.service
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ResponseAPDUType }
     * 
     */
    public ResponseAPDUType createResponseAPDUType() {
        return new ResponseAPDUType();
    }

    /**
     * Create an instance of {@link AttributeType }
     * 
     */
    public AttributeType createAttributeType() {
        return new AttributeType();
    }

    /**
     * Create an instance of {@link VerifyAPDUType }
     * 
     */
    public VerifyAPDUType createVerifyAPDUType() {
        return new VerifyAPDUType();
    }

    /**
     * Create an instance of {@link ATRType }
     * 
     */
    public ATRType createATRType() {
        return new ATRType();
    }

    /**
     * Create an instance of {@link ResponseType }
     * 
     */
    public ResponseType createResponseType() {
        return new ResponseType();
    }

    /**
     * Create an instance of {@link CommandAPDUType }
     * 
     */
    public CommandAPDUType createCommandAPDUType() {
        return new CommandAPDUType();
    }

    /**
     * Create an instance of {@link ResetType }
     * 
     */
    public ResetType createResetType() {
        return new ResetType();
    }

    /**
     * Create an instance of {@link ScriptType }
     * 
     */
    public ScriptType createScriptType() {
        return new ScriptType();
    }

    /**
     * Create an instance of {@link AttributeList }
     * 
     */
    public AttributeList createAttributeList() {
        return new AttributeList();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AttributeList }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.buergerkarte.at/cardchannel", name = "EHIC")
    public JAXBElement<AttributeList> createEHIC(AttributeList value) {
        return new JAXBElement<AttributeList>(_EHIC_QNAME, AttributeList.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AttributeList }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.buergerkarte.at/cardchannel", name = "Status")
    public JAXBElement<AttributeList> createStatus(AttributeList value) {
        return new JAXBElement<AttributeList>(_Status_QNAME, AttributeList.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.buergerkarte.at/cardchannel", name = "Response", substitutionHeadNamespace = "http://www.egiz.gv.at/stal", substitutionHeadName = "OtherResponse")
    public JAXBElement<ResponseType> createResponse(ResponseType value) {
        return new JAXBElement<ResponseType>(_Response_QNAME, ResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link byte[]}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.buergerkarte.at/cardchannel", name = "SV-Personenbindung")
    @XmlJavaTypeAdapter(HexBinaryAdapter.class)
    public JAXBElement<byte[]> createSVPersonenbindung(byte[] value) {
        return new JAXBElement<byte[]>(_SVPersonenbindung_QNAME, byte[].class, null, ((byte[]) value));
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ScriptType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.buergerkarte.at/cardchannel", name = "Script", substitutionHeadNamespace = "http://www.egiz.gv.at/stal", substitutionHeadName = "OtherRequest")
    public JAXBElement<ScriptType> createScript(ScriptType value) {
        return new JAXBElement<ScriptType>(_Script_QNAME, ScriptType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AttributeList }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.buergerkarte.at/cardchannel", name = "Grunddaten")
    public JAXBElement<AttributeList> createGrunddaten(AttributeList value) {
        return new JAXBElement<AttributeList>(_Grunddaten_QNAME, AttributeList.class, null, value);
    }

}
