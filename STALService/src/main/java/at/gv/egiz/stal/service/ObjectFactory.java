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

package at.gv.egiz.stal.service;


import at.gv.egiz.stal.ErrorResponse;
import at.gv.egiz.stal.InfoboxReadRequest;
import at.gv.egiz.stal.InfoboxReadResponse;
import at.gv.egiz.stal.QuitRequest;
import at.gv.egiz.stal.SignRequest;
import at.gv.egiz.stal.SignResponse;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the at.gv.egiz.stal package. 
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

    private final static QName _GetHashDataInputFault_QNAME = new QName("http://www.egiz.gv.at/stal", "GetHashDataInputFault");
    private final static QName _GetHashDataInput_QNAME = new QName("http://www.egiz.gv.at/stal", "GetHashDataInput");
    private final static QName _GetNextRequestResponse_QNAME = new QName("http://www.egiz.gv.at/stal", "GetNextRequestResponse");
    private final static QName _GetHashDataInputResponse_QNAME = new QName("http://www.egiz.gv.at/stal", "GetHashDataInputResponse");
    private final static QName _GetNextRequest_QNAME = new QName("http://www.egiz.gv.at/stal", "GetNextRequest");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: at.gv.egiz.stal
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetNextRequestType }
     * 
     */
    public GetNextRequestType createGetNextRequestType() {
        return new GetNextRequestType();
    }

    /**
     * Create an instance of {@link SignRequest }
     * 
     */
    public SignRequest createSignRequest() {
        return new SignRequest();
    }

    /**
     * Create an instance of {@link GetHashDataInputResponseType }
     * 
     */
    public GetHashDataInputResponseType createGetHashDataInputResponseType() {
        return new GetHashDataInputResponseType();
    }

    /**
     * Create an instance of {@link InfoboxReadResponse }
     * 
     */
    public InfoboxReadResponse createInfoboxReadResponse() {
        return new InfoboxReadResponse();
    }

    /**
     * Create an instance of {@link ErrorResponse }
     * 
     */
    public ErrorResponse createErrorResponse() {
        return new ErrorResponse();
    }

    /**
     * Create an instance of {@link GetHashDataInputFaultType }
     * 
     */
    public GetHashDataInputFaultType createGetHashDataInputFaultType() {
        return new GetHashDataInputFaultType();
    }

    /**
     * Create an instance of {@link GetHashDataInputType.Reference }
     * 
     */
    public GetHashDataInputType.Reference createGetHashDataInputTypeReference() {
        return new GetHashDataInputType.Reference();
    }

    /**
     * Create an instance of {@link InfoboxReadRequest }
     * 
     */
    public InfoboxReadRequest createInfoboxReadRequest() {
        return new InfoboxReadRequest();
    }

    /**
     * Create an instance of {@link SignResponse }
     * 
     */
    public SignResponse createSignResponse() {
        return new SignResponse();
    }

    /**
     * Create an instance of {@link GetNextRequestResponseType }
     * 
     */
    public GetNextRequestResponseType createGetNextRequestResponseType() {
        return new GetNextRequestResponseType();
    }

    /**
     * Create an instance of {@link GetHashDataInputType }
     * 
     */
    public GetHashDataInputType createGetHashDataInputType() {
        return new GetHashDataInputType();
    }

    /**
     * Create an instance of {@link QuitRequest }
     * 
     */
    public QuitRequest createQuitRequest() {
        return new QuitRequest();
    }

    /**
     * Create an instance of {@link GetHashDataInputResponseType.Reference }
     * 
     */
    public GetHashDataInputResponseType.Reference createGetHashDataInputResponseTypeReference() {
        return new GetHashDataInputResponseType.Reference();
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
     * Create an instance of {@link JAXBElement }{@code <}{@link GetHashDataInputType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.egiz.gv.at/stal", name = "GetHashDataInput")
    public JAXBElement<GetHashDataInputType> createGetHashDataInput(GetHashDataInputType value) {
        return new JAXBElement<GetHashDataInputType>(_GetHashDataInput_QNAME, GetHashDataInputType.class, null, value);
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
     * Create an instance of {@link JAXBElement }{@code <}{@link GetHashDataInputResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.egiz.gv.at/stal", name = "GetHashDataInputResponse")
    public JAXBElement<GetHashDataInputResponseType> createGetHashDataInputResponse(GetHashDataInputResponseType value) {
        return new JAXBElement<GetHashDataInputResponseType>(_GetHashDataInputResponse_QNAME, GetHashDataInputResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetNextRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.egiz.gv.at/stal", name = "GetNextRequest")
    public JAXBElement<GetNextRequestType> createGetNextRequest(GetNextRequestType value) {
        return new JAXBElement<GetNextRequestType>(_GetNextRequest_QNAME, GetNextRequestType.class, null, value);
    }

}
