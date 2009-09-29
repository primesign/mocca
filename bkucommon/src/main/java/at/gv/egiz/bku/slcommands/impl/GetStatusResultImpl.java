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

import at.buergerkarte.namespaces.securitylayer._1.GetStatusResponseType;
import at.buergerkarte.namespaces.securitylayer._1.ObjectFactory;
import at.buergerkarte.namespaces.securitylayer._1.TokenStatusType;
import at.gv.egiz.bku.slcommands.GetStatusResult;
import javax.xml.bind.JAXBElement;
import javax.xml.transform.Result;
import javax.xml.transform.Templates;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class GetStatusResultImpl extends SLResultImpl implements GetStatusResult {

  protected ObjectFactory of;
  protected GetStatusResponseType responseType;

  public GetStatusResultImpl(boolean ready) {
    of = new ObjectFactory();
    responseType = of.createGetStatusResponseType();
    if (ready) {
      responseType.setTokenStatus(TokenStatusType.READY);
    } else {
      responseType.setTokenStatus(TokenStatusType.REMOVED);
    }

  }

  @Override
  public void writeTo(Result result, Templates templates, boolean fragment) {
    JAXBElement<GetStatusResponseType> response = of.createGetStatusResponse(responseType);
    writeTo(response, result, templates, fragment);
  }
}
