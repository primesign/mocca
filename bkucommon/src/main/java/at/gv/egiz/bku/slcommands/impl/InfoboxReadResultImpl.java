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

import javax.xml.bind.JAXBElement;
import javax.xml.transform.Result;
import javax.xml.transform.Templates;

import at.buergerkarte.namespaces.securitylayer._1.InfoboxReadDataAssocArrayType;
import at.buergerkarte.namespaces.securitylayer._1.InfoboxReadResponseType;
import at.buergerkarte.namespaces.securitylayer._1.ObjectFactory;
import at.gv.egiz.bku.slcommands.InfoboxReadResult;

public class InfoboxReadResultImpl extends SLResultImpl implements InfoboxReadResult {

  /**
   * The <code>InfoboxReadResponse</code>
   */
  protected InfoboxReadResponseType infoboxReadResponse;
  
  public InfoboxReadResultImpl(InfoboxReadDataAssocArrayType assocArray) {
    
    ObjectFactory objectFactory = new ObjectFactory();
    InfoboxReadResponseType infoboxReadResponseType = objectFactory.createInfoboxReadResponseType();
    
    infoboxReadResponseType.setAssocArrayData(assocArray);
    
    this.infoboxReadResponse = infoboxReadResponseType;
  }

  @Override
  public void writeTo(Result result, Templates templates) {
    ObjectFactory objectFactory = new ObjectFactory();
    JAXBElement<InfoboxReadResponseType> response = objectFactory.createInfoboxReadResponse(infoboxReadResponse);
    writeTo(response, result, templates);
  }

}
