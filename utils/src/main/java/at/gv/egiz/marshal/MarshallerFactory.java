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


package at.gv.egiz.marshal;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class MarshallerFactory {

  public static Marshaller createMarshaller(JAXBContext ctx, boolean formattedOutput, boolean fragment) throws JAXBException {
    Logger log = LoggerFactory.getLogger(MarshallerFactory.class);
    Marshaller m = ctx.createMarshaller();
    try {
      if (formattedOutput) {
        log.trace("Setting marshaller property FORMATTED_OUTPUT.");
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      }
      if (fragment) {
        log.trace("Setting marshaller property FRAGMENT.");
        m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
      }
      log.trace("Setting marshaller property NamespacePrefixMapper.");
      m.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapperImpl());
    } catch (PropertyException ex) {
      log.info("Failed to set marshaller property: {}.", ex.getMessage());
    }
    return m;
  }
  
  public static Marshaller createMarshaller(JAXBContext ctx, boolean formattedOutput) throws JAXBException {
    return createMarshaller(ctx, formattedOutput, false);
  }

  public static Marshaller createMarshaller(JAXBContext ctx) throws JAXBException {
    return createMarshaller(ctx, false, false);
  }
}
