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
import javax.xml.transform.Result;
import javax.xml.transform.Templates;

import at.buergerkarte.namespaces.securitylayer._1_2_3.Base64XMLContentType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.InfoboxReadDataAssocArrayType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.InfoboxReadResponseType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.ObjectFactory;
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
  
  public InfoboxReadResultImpl(Base64XMLContentType value) {
    
    ObjectFactory objectFactory = new ObjectFactory();
    InfoboxReadResponseType infoboxReadResponseType = objectFactory.createInfoboxReadResponseType();
    
    infoboxReadResponseType.setBinaryFileData(value);
    
    this.infoboxReadResponse = infoboxReadResponseType;
    
  }

  @Override
  public void writeTo(Result result, Templates templates, boolean fragment) {
    ObjectFactory objectFactory = new ObjectFactory();
    JAXBElement<InfoboxReadResponseType> response = objectFactory.createInfoboxReadResponse(infoboxReadResponse);
    writeTo(response, result, templates, fragment);
  }

  @Override
  public Object getContent() {
    if (infoboxReadResponse != null) {
      if (infoboxReadResponse.getAssocArrayData() != null) {
        return infoboxReadResponse.getAssocArrayData();
      } else {
        Base64XMLContentType binaryFileData = infoboxReadResponse.getBinaryFileData();
        if (binaryFileData.getBase64Content() != null) {
          return binaryFileData.getBase64Content();
        } else {
          return binaryFileData.getXMLContent().getContent();
        }
      }
    } else {
      return null;
    }
  }

}
