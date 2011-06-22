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
