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


package at.gv.egiz.bku.slcommands.impl;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.Result;
import javax.xml.transform.Templates;
import javax.xml.transform.dom.DOMResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import at.buergerkarte.namespaces.securitylayer._1_2_3.CreateCMSSignatureResponseType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.ObjectFactory;
import at.gv.egiz.bku.slcommands.CreateCMSSignatureResult;
import at.gv.egiz.bku.slcommands.SLMarshallerFactory;
import at.gv.egiz.bku.slexceptions.SLRuntimeException;

/**
 * This implements the result of the security layer command <code>CreateCMSSignature</code>.
 * 
 * @author tkellner
 */
public class CreateCMSSignatureResultImpl extends SLResultImpl implements CreateCMSSignatureResult {

  /**
   * Logging facility.
   */
  private final Logger log = LoggerFactory.getLogger(CreateCMSSignatureResultImpl.class);

  /**
   * The CMSSignature data.
   */
  protected byte[] signature;

  /**
   * The CMSSignatureResponse.
   */
  private Element content;

  /**
   * Creates a new instance of this CreateCMSSignatureResultImpl with the given
   * signature <code>signature</code>.
   * 
   * @param document the signature document
   * 
   * @throws NullPointerException if <code>document</code> is <code>null</code>
   */
  public CreateCMSSignatureResultImpl(byte[] signature) {
    super();

    if (signature == null)
      throw new NullPointerException("Argument 'signature' must not be null.");
    this.signature = signature;

    marshallCreateCMSSignatureResponse();
  }

  /**
   * Marshalls the <code>CreateCMSSignatureResponse</code>.
   */
  private void marshallCreateCMSSignatureResponse() {

    ObjectFactory factory = new ObjectFactory();

    CreateCMSSignatureResponseType createCreateCMSSignatureResponseType = factory.createCreateCMSSignatureResponseType();
    createCreateCMSSignatureResponseType.setCMSSignature(signature);
    JAXBElement<CreateCMSSignatureResponseType> createCreateCMSSignatureResponse = factory.createCreateCMSSignatureResponse(createCreateCMSSignatureResponseType);

    DOMResult res = new DOMResult();

    Marshaller marshaller = SLMarshallerFactory.getInstance().createMarshaller(false);
    try {
      marshaller.marshal(createCreateCMSSignatureResponse, res);
    } catch (JAXBException e) {
      log.error("Failed to marshall 'CreateCMSSignatureResponse'.", e);
      throw new SLRuntimeException(e);
    }
    content = ((Document)res.getNode()).getDocumentElement();
  }

  @Override
  public void writeTo(Result result, Templates templates, boolean fragment) {
    writeTo(content, result, templates, fragment);
  }

  @Override
  public Element getContent() {
    return content;
  }
}
