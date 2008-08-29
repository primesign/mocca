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
package at.gv.egiz.bku.slcommands.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.bku.slcommands.SLCommandFactory;
import at.gv.egiz.bku.slcommands.SLResult;

/**
 * This class serves as an abstract base class for the implementation of a
 * security layer result.
 * 
 * @author mcentner
 */
public abstract class SLResultImpl implements SLResult {

  /**
   * Logging facility.
   */
  private static Log log = LogFactory.getLog(SLResult.class);

  /**
   * The security layer result type (default = XML).
   */
  protected SLResultType resultType = SLResultType.XML;
  
  /**
   * The security layer result MIME-type (default = <code>text/xml</code>).
   */
  protected String resultingMimeType = "text/xml";

  /* (non-Javadoc)
   * @see at.gv.egiz.bku.slcommands.SLResult#getResultType()
   */
  public SLResultType getResultType() {
    return resultType;
  }

  /* (non-Javadoc)
   * @see at.gv.egiz.bku.slcommands.SLResult#getMimeType()
   */
  public String getMimeType() {
    return resultingMimeType;
  }

  /**
   * Writes the given <code>response</code> to the <code>result</code>.
   * 
   * @param response the security layer response element
   * @param result the result to marshal the response to
   */
  @SuppressWarnings("unchecked")
  public void writeTo(JAXBElement response, Result result) {

    try {
      JAXBContext context = SLCommandFactory.getJaxbContext();
      Marshaller marshaller = context.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      marshaller.marshal(response, result);
    } catch (JAXBException e) {
      // TODO Add throws clause to interface
      log.fatal("Failed to marshall JAXBElement.", e);
      throw new RuntimeException("Failed to marshall JAXBElement.", e);
    }

  }

  /* (non-Javadoc)
   * @see at.gv.egiz.bku.slcommands.SLResult#writeTo(javax.xml.transform.Result, javax.xml.transform.Transformer)
   */
  @Override
  public void writeTo(Result result, Transformer transformer) throws TransformerException {
    // TODO Auto-generated method stub
    // fixxme: wb added for testing purposes to be completed
    // begin hack
    if (transformer == null) {
      writeTo(result);
      return;
    }
    // just a quick hack to proceed with testing
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    writeTo(new StreamResult(os));
    ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
    transformer.transform(new StreamSource(is), result);
    //end hack
  }

}
