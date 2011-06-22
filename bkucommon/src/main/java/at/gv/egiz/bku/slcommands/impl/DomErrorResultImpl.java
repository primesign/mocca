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

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import at.gv.egiz.bku.slcommands.ErrorResult;
import at.gv.egiz.bku.slcommands.SLCommand;

public class DomErrorResultImpl extends DomSLResult implements
    ErrorResult {
  
  public DomErrorResultImpl(Element resultElement) {
    super(resultElement);
  }

  @Override
  public int getErrorCode() {
    
    NodeList childNodes = resultElement.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node n = childNodes.item(i);
      if (n.getNodeType() == Node.ELEMENT_NODE
          && SLCommand.NAMESPACE_URI.equals(n.getNamespaceURI())
          && "ErrorCode".equals(n.getLocalName())) {
        try {
          return Integer.parseInt(n.getTextContent());
        } catch (NumberFormatException e) { }
      }
    }
    
    return 0;
    
  }

  @Override
  public String getInfo() {

    NodeList childNodes = resultElement.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node n = childNodes.item(i);
      if (n.getNodeType() == Node.ELEMENT_NODE
          && SLCommand.NAMESPACE_URI.equals(n.getNamespaceURI())
          && "Info".equals(n.getLocalName())) {
        return n.getTextContent();
      }
    }
    
    return null;
    
  }

}
