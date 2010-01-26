package at.gv.egiz.stalx.service;

//import at.buergerkarte.namespaces.cardchannel.service.ObjectFactory;
//import at.buergerkarte.namespaces.cardchannel.service.ScriptType;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * Dummy PortType to add at.buergerkarte.namespaces.cardchannel.service to the
 * JAXB context seed.
 * 
 * overriding a webmethod results in ClassCastEx for the WebResult 
 * ClassCastException: at.gv.egiz.stal.service.types.GetNextRequestResponseType
 * cannot be cast to at.buergerkarte.namespaces.cardchannel.service.GetNextRequestResponseType
 *
 * adding a new method results in Error: Undefined operation name
 *
 * adding a constant doesn't seed
 */
@WebService(name = "STALPortType", targetNamespace = "http://www.egiz.gv.at/wsdl/stal")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
@XmlSeeAlso({
  at.gv.egiz.stal.service.types.ObjectFactory.class,
  at.buergerkarte.namespaces.cardchannel.service.ObjectFactory.class
})
interface STALPortType extends at.gv.egiz.stal.service.STALPortType {

  // doesn't seed
//  public static final ScriptType seed = (new ObjectFactory()).createScriptType();
  /**
   * dummy method to put at.buergerkarte.namespaces.cardchannel.service as JAXB context seed
   * @return
   */
//  @WebMethod
//  @WebResult(name = "Script", targetNamespace = "http://www.buergerkarte.at/cardchannel", partName = "part1")
  //java.lang.Error: Undefined operation name seedJAXBContext
//  ScriptType seedJAXBContext();

//  @WebMethod
//  @WebResult(name = "GetNextRequestResponse", targetNamespace = "http://www.egiz.gv.at/stal", partName = "part1")
//  @Override
//  public GetNextRequestResponseType connect(
//          @WebParam(name = "SessionId", targetNamespace = "http://www.egiz.gv.at/stal", partName = "part1") String sessionId);
}
