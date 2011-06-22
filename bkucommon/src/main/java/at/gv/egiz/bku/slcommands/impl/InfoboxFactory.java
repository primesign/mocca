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

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.bku.slexceptions.SLCommandException;
import at.gv.egiz.bku.slexceptions.SLExceptionMessages;
import at.gv.egiz.bku.slexceptions.SLRuntimeException;

/**
 * A factory for creating {@link Infobox}es.
 * 
 * @author mcentner
 */
public class InfoboxFactory {
  
  /**
   * Logging facility.
   */
  private final Logger log = LoggerFactory.getLogger(InfoboxFactory.class);
  
  /**
   * The mapping of Infobox name to concrete Infobox factory.
   */
  private HashMap<String, AbstractInfoboxFactory> infoboxFactories = new HashMap<String, AbstractInfoboxFactory>();
  
  /**
   * @param infoboxFactories the infoboxFactories to set
   */
  public void setInfoboxFactories(
      HashMap<String, AbstractInfoboxFactory> factories) {
    if (log.isDebugEnabled()) {
      StringBuilder sb = new StringBuilder();
      sb.append("Registered infobox factories for");
      for (String name : factories.keySet()) {
        sb.append("\n  " + name + " : " + factories.get(name).getClass());
      }
      log.debug(sb.toString());
    }
    this.infoboxFactories = factories;
  }

  /**
   * Create a new {@link Infobox} instance for the given
   * <code>infoboxIdentifier</code>.
   * 
   * @param infoboxIdentifier
   *          the infobox identifier
   * 
   * @return an {@link Infobox} implementation for the given infobox identifier
   * 
   * @throws SLCommandException
   *           if there is no implementation for the given infobox identifier
   * @throws SLRuntimeException
   *           if creating an {@link Infobox} instance fails
   */
  public Infobox createInfobox(String infoboxIdentifier) throws SLCommandException, SLRuntimeException {
    
    AbstractInfoboxFactory factory = infoboxFactories.get(infoboxIdentifier);
    if (factory == null) {
      log.info("Unsupported infobox '{}'.", infoboxIdentifier);
      throw new SLCommandException(4002,
          SLExceptionMessages.EC4002_INFOBOX_UNKNOWN,
          new Object[] { infoboxIdentifier });
    }
    
    return factory.createInfobox();
    
  }
  

}
