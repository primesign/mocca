
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

    private final static QName _GetHashDataInputFault_QNAME = new QName("http://www.egiz.gv.at/stal", "GetHashDataInputFault");
    private final static QName _GetHashDataInput_QNAME = new QName("http://www.egiz.gv.at/stal", "GetHashDataInput");
    private final static QName _GetNextRequestResponse_QNAME = new QName("http://www.egiz.gv.at/stal", "GetNextRequestResponse");
    private final static QName _GetHashDataInputResponse_QNAME = new QName("http://www.egiz.gv.at/stal", "GetHashDataInputResponse");
    private final static QName _GetNextRequest_QNAME = new QName("http://www.egiz.gv.at/stal", "GetNextRequest");
    private final static QName _SessionId_QNAME = new QName("http://www.egiz.gv.at/stal", "SessionId");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: at.gv.egiz.stal.service.types
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
     * Create an instance of {@link InfoboxReadRequestType }
     * 
     */
    public InfoboxReadRequestType createInfoboxReadRequestType() {
        return new InfoboxReadRequestType();
    }

    /**
     * Create an instance of {@link GetHashDataInputResponseType.Reference }
     * 
     */
    public GetHashDataInputResponseType.Reference createGetHashDataInputResponseTypeReference() {
        return new GetHashDataInputResponseType.Reference();
    }

    /**
     * Create an instance of {@link ErrorResponseType }
     * 
     */
    public ErrorResponseType createErrorResponseType() {
        return new ErrorResponseType();
    }

    /**
     * Create an instance of {@link GetHashDataInputType }
     * 
     */
    public GetHashDataInputType createGetHashDataInputType() {
        return new GetHashDataInputType();
    }

    /**
     * Create an instance of {@link SignRequestType }
     * 
     */
    public SignRequestType createSignRequestType() {
        return new SignRequestType();
    }

    /**
     * Create an instance of {@link GetHashDataInputFaultType }
     * 
     */
    public GetHashDataInputFaultType createGetHashDataInputFaultType() {
        return new GetHashDataInputFaultType();
    }

    /**
     * Create an instance of {@link SignResponseType }
     * 
     */
    public SignResponseType createSignResponseType() {
        return new SignResponseType();
    }

    /**
     * Create an instance of {@link GetHashDataInputType.Reference }
     * 
     */
    public GetHashDataInputType.Reference createGetHashDataInputTypeReference() {
        return new GetHashDataInputType.Reference();
    }

    /**
     * Create an instance of {@link GetHashDataInputResponseType }
     * 
     */
    public GetHashDataInputResponseType createGetHashDataInputResponseType() {
        return new GetHashDataInputResponseType();
    }

    /**
     * Create an instance of {@link InfoboxReadResponseType }
     * 
     */
    public InfoboxReadResponseType createInfoboxReadResponseType() {
        return new InfoboxReadResponseType();
    }

    /**
     * Create an instance of {@link QuitRequestType }
     * 
     */
    public QuitRequestType createQuitRequestType() {
        return new QuitRequestType();
    }

    /**
     * Create an instance of {@link GetNextRequestResponseType }
     * 
     */
    public GetNextRequestResponseType createGetNextRequestResponseType() {
        return new GetNextRequestResponseType();
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

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.egiz.gv.at/stal", name = "SessionId")
    public JAXBElement<String> createSessionId(String value) {
        return new JAXBElement<String>(_SessionId_QNAME, String.class, null, value);
    }

}
