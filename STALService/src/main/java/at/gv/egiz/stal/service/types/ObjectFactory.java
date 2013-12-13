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



package at.gv.egiz.stal.service.types;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the at.gv.egiz.stal.service.types package. 
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

    private final static QName _GetHashDataInput_QNAME = new QName("http://www.egiz.gv.at/stal", "GetHashDataInput");
    private final static QName _GetHashDataInputResponse_QNAME = new QName("http://www.egiz.gv.at/stal", "GetHashDataInputResponse");
    private final static QName _OtherRequest_QNAME = new QName("http://www.egiz.gv.at/stal", "OtherRequest");
    private final static QName _GetNextRequest_QNAME = new QName("http://www.egiz.gv.at/stal", "GetNextRequest");
    private final static QName _OtherResponse_QNAME = new QName("http://www.egiz.gv.at/stal", "OtherResponse");
    private final static QName _SessionId_QNAME = new QName("http://www.egiz.gv.at/stal", "SessionId");
    private final static QName _GetHashDataInputFault_QNAME = new QName("http://www.egiz.gv.at/stal", "GetHashDataInputFault");
    private final static QName _GetNextRequestResponse_QNAME = new QName("http://www.egiz.gv.at/stal", "GetNextRequestResponse");
    private final static QName _GetNextRequestResponseTypeQuitRequest_QNAME = new QName("http://www.egiz.gv.at/stal", "QuitRequest");
    private final static QName _GetNextRequestResponseTypeInfoboxReadRequest_QNAME = new QName("http://www.egiz.gv.at/stal", "InfoboxReadRequest");
    private final static QName _GetNextRequestResponseTypeSignRequest_QNAME = new QName("http://www.egiz.gv.at/stal", "SignRequest");
    private final static QName _GetNextRequestTypeErrorResponse_QNAME = new QName("http://www.egiz.gv.at/stal", "ErrorResponse");
    private final static QName _GetNextRequestTypeSignResponse_QNAME = new QName("http://www.egiz.gv.at/stal", "SignResponse");
    private final static QName _GetNextRequestTypeInfoboxReadResponse_QNAME = new QName("http://www.egiz.gv.at/stal", "InfoboxReadResponse");
    private final static QName _GetNextRequestResponseTypeStatusRequest_QNAME = new QName("http://www.egiz.gv.at/stal", "StatusRequest");
    private final static QName _GetNextRequestTypeStatusResponse_QNAME = new QName("http://www.egiz.gv.at/stal", "StatusResponse");
    
    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: at.gv.egiz.stal.service.types
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link StatusResponseType }
     *
     */
    public StatusResponseType createStatusResponseType() {
        return new StatusResponseType();
    }

    /**
     * Create an instance of {@link StatusRequestType }
     *
     */
    public StatusRequestType createStatusRequestType() {
        return new StatusRequestType();
    }
    
    /**
     * Create an instance of {@link GetHashDataInputType }
     * 
     */
    public GetHashDataInputType createGetHashDataInputType() {
        return new GetHashDataInputType();
    }

    /**
     * Create an instance of {@link GetHashDataInputResponseType.Reference }
     * 
     */
    public GetHashDataInputResponseType.Reference createGetHashDataInputResponseTypeReference() {
        return new GetHashDataInputResponseType.Reference();
    }

    /**
     * Create an instance of {@link GetNextRequestType }
     * 
     */
    public GetNextRequestType createGetNextRequestType() {
        return new GetNextRequestType();
    }

    /**
     * Create an instance of {@link SignRequestType }
     * 
     */
    public SignRequestType createSignRequestType() {
        return new SignRequestType();
    }

    /**
     * Create an instance of {@link SignRequestType.SignedInfo }
     * 
     */
    public SignRequestType.SignedInfo createSignRequestTypeSignedInfo() {
      return new SignRequestType.SignedInfo();
    }

    /**
     * Create an instance of {@link SignRequestType.ExcludedByteRange }
     * 
     */
    public SignRequestType.ExcludedByteRange createSignRequestTypeExcludedByteRange() {
        return new SignRequestType.ExcludedByteRange();
    }

    /**
     * Create an instance of {@link GetHashDataInputType.Reference }
     * 
     */
    public GetHashDataInputType.Reference createGetHashDataInputTypeReference() {
        return new GetHashDataInputType.Reference();
    }

    /**
     * Create an instance of {@link ErrorResponseType }
     * 
     */
    public ErrorResponseType createErrorResponseType() {
        return new ErrorResponseType();
    }

    /**
     * Create an instance of {@link QuitRequestType }
     * 
     */
    public QuitRequestType createQuitRequestType() {
        return new QuitRequestType();
    }

    /**
     * Create an instance of {@link InfoboxReadResponseType }
     * 
     */
    public InfoboxReadResponseType createInfoboxReadResponseType() {
        return new InfoboxReadResponseType();
    }

    /**
     * Create an instance of {@link InfoboxReadRequestType }
     * 
     */
    public InfoboxReadRequestType createInfoboxReadRequestType() {
        return new InfoboxReadRequestType();
    }

    /**
     * Create an instance of {@link GetNextRequestResponseType }
     * 
     */
    public GetNextRequestResponseType createGetNextRequestResponseType() {
        return new GetNextRequestResponseType();
    }

    /**
     * Create an instance of {@link GetHashDataInputResponseType }
     * 
     */
    public GetHashDataInputResponseType createGetHashDataInputResponseType() {
        return new GetHashDataInputResponseType();
    }

    /**
     * Create an instance of {@link SignResponseType }
     * 
     */
    public SignResponseType createSignResponseType() {
        return new SignResponseType();
    }

    /**
     * Create an instance of {@link GetHashDataInputFaultType }
     * 
     */
    public GetHashDataInputFaultType createGetHashDataInputFaultType() {
        return new GetHashDataInputFaultType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StatusRequestType }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://www.egiz.gv.at/stal", name = "StatusRequest", scope = GetNextRequestResponseType.class)
    public JAXBElement<StatusRequestType> createGetNextRequestResponseTypeStatusRequest(StatusRequestType value) {
        return new JAXBElement<StatusRequestType>(_GetNextRequestResponseTypeStatusRequest_QNAME, StatusRequestType.class, GetNextRequestResponseType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StatusResponseType }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://www.egiz.gv.at/stal", name = "StatusResponse", scope = GetNextRequestType.class)
    public JAXBElement<StatusResponseType> createGetNextRequestTypeStatusResponse(StatusResponseType value) {
        return new JAXBElement<StatusResponseType>(_GetNextRequestTypeStatusResponse_QNAME, StatusResponseType.class, GetNextRequestType.class, value);
    }


    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetHashDataInputType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.egiz.gv.at/stal", name = "GetHashDataInput")
    public JAXBElement<GetHashDataInputType> createGetHashDataInput(GetHashDataInputType value) {
        return new JAXBElement<GetHashDataInputType>(_GetHashDataInput_QNAME, GetHashDataInputType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetHashDataInputResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.egiz.gv.at/stal", name = "GetHashDataInputResponse")
    public JAXBElement<GetHashDataInputResponseType> createGetHashDataInputResponse(GetHashDataInputResponseType value) {
        return new JAXBElement<GetHashDataInputResponseType>(_GetHashDataInputResponse_QNAME, GetHashDataInputResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.egiz.gv.at/stal", name = "OtherRequest")
    public JAXBElement<RequestType> createOtherRequest(RequestType value) {
        return new JAXBElement<RequestType>(_OtherRequest_QNAME, RequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetNextRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.egiz.gv.at/stal", name = "GetNextRequest")
    public JAXBElement<GetNextRequestType> createGetNextRequest(GetNextRequestType value) {
        return new JAXBElement<GetNextRequestType>(_GetNextRequest_QNAME, GetNextRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.egiz.gv.at/stal", name = "OtherResponse")
    public JAXBElement<ResponseType> createOtherResponse(ResponseType value) {
        return new JAXBElement<ResponseType>(_OtherResponse_QNAME, ResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.egiz.gv.at/stal", name = "SessionId")
    public JAXBElement<String> createSessionId(String value) {
        return new JAXBElement<String>(_SessionId_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetHashDataInputFaultType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.egiz.gv.at/stal", name = "GetHashDataInputFault")
    public JAXBElement<GetHashDataInputFaultType> createGetHashDataInputFault(GetHashDataInputFaultType value) {
        return new JAXBElement<GetHashDataInputFaultType>(_GetHashDataInputFault_QNAME, GetHashDataInputFaultType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetNextRequestResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.egiz.gv.at/stal", name = "GetNextRequestResponse")
    public JAXBElement<GetNextRequestResponseType> createGetNextRequestResponse(GetNextRequestResponseType value) {
        return new JAXBElement<GetNextRequestResponseType>(_GetNextRequestResponse_QNAME, GetNextRequestResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link QuitRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.egiz.gv.at/stal", name = "QuitRequest", scope = GetNextRequestResponseType.class)
    public JAXBElement<QuitRequestType> createGetNextRequestResponseTypeQuitRequest(QuitRequestType value) {
        return new JAXBElement<QuitRequestType>(_GetNextRequestResponseTypeQuitRequest_QNAME, QuitRequestType.class, GetNextRequestResponseType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link InfoboxReadRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.egiz.gv.at/stal", name = "InfoboxReadRequest", scope = GetNextRequestResponseType.class)
    public JAXBElement<InfoboxReadRequestType> createGetNextRequestResponseTypeInfoboxReadRequest(InfoboxReadRequestType value) {
        return new JAXBElement<InfoboxReadRequestType>(_GetNextRequestResponseTypeInfoboxReadRequest_QNAME, InfoboxReadRequestType.class, GetNextRequestResponseType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SignRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.egiz.gv.at/stal", name = "SignRequest", scope = GetNextRequestResponseType.class)
    public JAXBElement<SignRequestType> createGetNextRequestResponseTypeSignRequest(SignRequestType value) {
        return new JAXBElement<SignRequestType>(_GetNextRequestResponseTypeSignRequest_QNAME, SignRequestType.class, GetNextRequestResponseType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ErrorResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.egiz.gv.at/stal", name = "ErrorResponse", scope = GetNextRequestType.class)
    public JAXBElement<ErrorResponseType> createGetNextRequestTypeErrorResponse(ErrorResponseType value) {
        return new JAXBElement<ErrorResponseType>(_GetNextRequestTypeErrorResponse_QNAME, ErrorResponseType.class, GetNextRequestType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SignResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.egiz.gv.at/stal", name = "SignResponse", scope = GetNextRequestType.class)
    public JAXBElement<SignResponseType> createGetNextRequestTypeSignResponse(SignResponseType value) {
        return new JAXBElement<SignResponseType>(_GetNextRequestTypeSignResponse_QNAME, SignResponseType.class, GetNextRequestType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link InfoboxReadResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.egiz.gv.at/stal", name = "InfoboxReadResponse", scope = GetNextRequestType.class)
    public JAXBElement<InfoboxReadResponseType> createGetNextRequestTypeInfoboxReadResponse(InfoboxReadResponseType value) {
        return new JAXBElement<InfoboxReadResponseType>(_GetNextRequestTypeInfoboxReadResponse_QNAME, InfoboxReadResponseType.class, GetNextRequestType.class, value);
    }

}
