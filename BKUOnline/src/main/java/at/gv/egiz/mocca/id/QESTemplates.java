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

package at.gv.egiz.mocca.id;

import java.io.InputStream;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.crypto.MarshalException;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import at.gv.egiz.bku.slexceptions.SLRuntimeException;

public class QESTemplates {

  private Map<String, Templates> templatesMap = Collections.synchronizedMap(new HashMap<String, Templates>());
  
  private synchronized Templates getTemplates(String id) {
    
    Templates templates = templatesMap.get(id);
    if (templates == null) {
      templates = loadTemplates(id);
      templatesMap.put(id, templates);
    }
    return templates;
    
  }

  protected Templates loadTemplates(String id) {
    
    InputStream xsl = QESTemplates.class.getResourceAsStream("/templates/template.xsl");
    if (xsl == null) {
      throw new IllegalArgumentException("Template not found.");
    }
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    try {
      return transformerFactory.newTemplates(new StreamSource(xsl));
    } catch (TransformerConfigurationException e) {
      throw new SLRuntimeException(e);
    }
    
  }
  
  public String createQESTemplate(String id, Locale locale, IdLink idLink, String url, PersonalIdentifier derivedIdentifier, Date dateTime) {

    Templates templates = getTemplates(id);
    try {
      Transformer transformer = templates.newTransformer();
      
      DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
      DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM, locale);

      IdLinkPersonData personData = idLink.getPersonData();
      
      transformer.setParameter("givenName", personData.getGivenName());
      transformer.setParameter("familyName", personData.getFamilyName());
      transformer.setParameter("dateOfBirth", dateFormat.format(personData.getDateOfBirth()));
      
      transformer.setParameter("url", url);
      transformer.setParameter("identifierType", derivedIdentifier.getType());
      transformer.setParameter("identifierValue", derivedIdentifier.getValue());
      
      transformer.setParameter("date", dateFormat.format(dateTime));
      transformer.setParameter("time", timeFormat.format(dateTime));
      
      
      StringWriter writer = new StringWriter();
      transformer.transform(new StreamSource(), new StreamResult(writer));
      
      
      return writer.toString();
    } catch (TransformerConfigurationException e) {
      throw new SLRuntimeException(e);
    } catch (TransformerException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (MarshalException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ParseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
    
  }
  
}
