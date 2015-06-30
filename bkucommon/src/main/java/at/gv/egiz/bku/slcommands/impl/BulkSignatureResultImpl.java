/*
 * Copyright 2015 Datentechnik Innovation and Prime Sign GmbH, Austria
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

import java.util.List;

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

import at.buergerkarte.namespaces.securitylayer._1_2_3.BulkResponseType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.BulkResponseType.CreateSignatureResponse;
import at.buergerkarte.namespaces.securitylayer._1_2_3.CreateCMSSignatureResponseType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.ObjectFactory;
import at.gv.egiz.bku.slcommands.BulkSignatureResult;
import at.gv.egiz.bku.slcommands.SLMarshallerFactory;
import at.gv.egiz.bku.slexceptions.SLRuntimeException;

/**
 * This implements the result of the security layer command
 * <code>BulkRequest</code>.
 * 
 * @author szoescher
 */
public class BulkSignatureResultImpl extends SLResultImpl implements BulkSignatureResult {

  /**
   * Logging facility.
   */
  private final Logger log = LoggerFactory.getLogger(BulkSignatureResultImpl.class);

  /**
   * The CMSSignatures data.
   */
  protected List<byte[]> signatures;

  /**
   * The BulkResponse.
   */
  private Element content;

  /**
   * Creates a new instance of this BulkSignatureResultImpl with the given
   * signatures <code>signatures</code>.
   */
  public BulkSignatureResultImpl(List<byte[]> signatures) {
    super();

    if (signatures == null || signatures.size() == 0)
      throw new NullPointerException("Argument 'signature' must not be null.");
    this.signatures = signatures;

    marshallBulkSignatureResponse();
  }

  /**
   * Marshalls the <code>BulkResponseType</code>.
   */
  private void marshallBulkSignatureResponse() {

    ObjectFactory factory = new ObjectFactory();

    BulkResponseType bulkResponseType = factory.createBulkResponseType();

    for (byte[] signature : signatures) {

      CreateSignatureResponse createSignatureResponse = factory.createBulkResponseTypeCreateSignatureResponse();
      CreateCMSSignatureResponseType createCreateCMSSignatureResponseType = factory
          .createCreateCMSSignatureResponseType();
      createCreateCMSSignatureResponseType.setCMSSignature(signature);
      createSignatureResponse.setCreateCMSSignatureResponse(createCreateCMSSignatureResponseType);
      bulkResponseType.getCreateSignatureResponse().add(createSignatureResponse);

    }

    JAXBElement<BulkResponseType> createBulkResponse = factory.createBulkResponse(bulkResponseType);
    DOMResult res = new DOMResult();

    Marshaller marshaller = SLMarshallerFactory.getInstance().createMarshaller(false);

    try {
      marshaller.marshal(createBulkResponse, res);
    } catch (JAXBException e) {
      log.error("Failed to marshall 'createBulkResponse'.", e);
      throw new SLRuntimeException(e);
    }
    content = ((Document) res.getNode()).getDocumentElement();
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
