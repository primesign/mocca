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

import iaik.utils.Base64InputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import at.gv.egiz.bku.slcommands.InfoboxReadResult;
import at.gv.egiz.bku.slcommands.SLCommand;
import at.gv.egiz.bku.slexceptions.SLRuntimeException;
import at.gv.egiz.bku.utils.StreamUtil;

public class DomInfoboxReadResultImpl extends DomSLResult implements
    InfoboxReadResult {

  public DomInfoboxReadResultImpl(Element resultElement) {
    super(resultElement);
  }

  private List<Node> getXMLContent(Node node) {
    ArrayList<Node> content = new ArrayList<Node>();
    NodeList xmlContent = node.getChildNodes();
    for (int i = 0; i < xmlContent.getLength(); i++) {
      content.add(xmlContent.item(i));
    }
    return content;
  }
  
  private byte[] getBase64Content(Node node) {
    String content = node.getTextContent();
    if (content != null) {
      try {
        byte[] bytes = content.getBytes("ASCII");
        Base64InputStream bis = new Base64InputStream(new ByteArrayInputStream(bytes));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        StreamUtil.copyStream(bis, bos);
        return bos.toByteArray();
      } catch (UnsupportedEncodingException e) {
        throw new SLRuntimeException(e);
      } catch (IOException e) {
        throw new SLRuntimeException(e);
      }
    } else {
      return new byte[] {};
    }
  }
  
  private Object getBinaryFileDataContent(Node node) {
    NodeList childNodes = node.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node n = childNodes.item(i);
      if (n.getNodeType() == Node.ELEMENT_NODE && SLCommand.NAMESPACE_URI.equals(n.getNamespaceURI())) {
        if ("XMLContent".equals(n.getLocalName())) {
          return getXMLContent(n);
        } else if ("Base64Content".equals(n.getLocalName())) {
          return getBase64Content(n);
        }
      }
    }
    return Collections.EMPTY_LIST;
  }
  
  @Override
  public Object getContent() {

    NodeList childNodes = resultElement.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node node = childNodes.item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE
          && SLCommand.NAMESPACE_URI.equals(node.getNamespaceURI())
          && "BinaryFileData".equals(node.getLocalName())) {
        return getBinaryFileDataContent(node);
      }
    }
    return Collections.EMPTY_LIST;
    
  }

}
