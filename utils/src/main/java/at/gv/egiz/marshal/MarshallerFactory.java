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
package at.gv.egiz.marshal;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class MarshallerFactory {

  private static final Log log = LogFactory.getLog(MarshallerFactory.class);
  
  public static Marshaller createMarshaller(JAXBContext ctx, boolean formattedOutput) throws JAXBException {
    Marshaller m = ctx.createMarshaller();
    try {
      if (formattedOutput) {
        log.trace("setting marshaller property FORMATTED_OUTPUT");
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      }
      log.trace("setting marshaller property NamespacePrefixMapper");
      m.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapperImpl());
    } catch (PropertyException ex) {
      log.info("failed to set marshaller property: " + ex.getMessage());
    }
    return m;
  }

  public static Marshaller createMarshaller(JAXBContext ctx) throws JAXBException {
    return createMarshaller(ctx, false);
  }
}
