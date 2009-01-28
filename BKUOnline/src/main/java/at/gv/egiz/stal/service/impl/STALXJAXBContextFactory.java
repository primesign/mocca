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
package at.gv.egiz.stal.service.impl;

import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.bind.api.TypeReference;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.developer.JAXBContextFactory;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class STALXJAXBContextFactory implements JAXBContextFactory {

  private static final Log log = LogFactory.getLog(STALXJAXBContextFactory.class);

  @Override
  public JAXBRIContext createJAXBContext(SEIModel sei, List<Class> classesToBind, List<TypeReference> typeReferences) throws JAXBException {
    if (log.isTraceEnabled()) {
      log.trace("JAXBContext seed for SEI " + sei.getTargetNamespace() + ":");
      for (Class class1 : classesToBind) {
        log.trace(" " + class1);
      }
      for (TypeReference typeReference : typeReferences) {
        log.trace(" typeRef " + typeReference.tagName + " -> " + typeReference.type);
      }
    }
    List<Class> classes = new ArrayList<Class>();
    classes.addAll(classesToBind);
    Class ccOF = at.buergerkarte.namespaces.cardchannel.service.ObjectFactory.class;
    if (!classes.contains(ccOF)) {
      log.debug("adding " + ccOF + " to JAXBContext seed");
      classes.add(ccOF);
    }

    //TODO add typeReference?

    return JAXBRIContext.newInstance(classes.toArray(new Class[classes.size()]),
            typeReferences, null, sei.getTargetNamespace(), false, null);
  }
}
