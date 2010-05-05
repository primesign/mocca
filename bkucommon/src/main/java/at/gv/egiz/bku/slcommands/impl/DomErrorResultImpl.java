/*
* Copyright 2009 Federal Chancellery Austria and
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
