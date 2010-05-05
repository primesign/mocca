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
