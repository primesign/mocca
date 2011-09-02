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


package at.gv.egiz.stal.service.impl;

import at.buergerkarte.namespaces.cardchannel.service.ObjectFactory;

import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.bind.api.TypeReference;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.developer.JAXBContextFactory;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class STALXJAXBContextFactory implements JAXBContextFactory {

  private final Logger log = LoggerFactory.getLogger(STALXJAXBContextFactory.class);

  @Override
  public JAXBRIContext createJAXBContext(SEIModel sei, @SuppressWarnings("rawtypes") List<Class> classesToBind, List<TypeReference> typeReferences) throws JAXBException {
    if (log.isTraceEnabled()) {
      log.trace("JAXBContext seed for SEI " + sei.getTargetNamespace() + ":");
      for (Class<?> class1 : classesToBind) {
        log.trace(" " + class1);
      }
      for (TypeReference typeReference : typeReferences) {
        log.trace(" typeRef " + typeReference.tagName + " -> " + typeReference.type);
      }
    }
    @SuppressWarnings("rawtypes")
	List<Class> classes = new ArrayList<Class>();
    classes.addAll(classesToBind);
    Class<ObjectFactory> ccOF = ObjectFactory.class;
    if (!classes.contains(ccOF)) {
      log.debug("adding " + ccOF + " to JAXBContext seed");
      classes.add(ccOF);
    }

    //TODO add typeReference?

    return JAXBRIContext.newInstance(classes.toArray(new Class[classes.size()]),
            typeReferences, null, sei.getTargetNamespace(), false, null);
  }
}
