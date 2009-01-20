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
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
  private static Log log = LogFactory.getLog(InfoboxFactory.class);
  
  /**
   * The singleton instance of this InfoboxFactory.
   */
  private static InfoboxFactory instance;

  /**
   * @return an instance of this InfoboxFactory
   */
  public synchronized static InfoboxFactory getInstance() {
    if (instance == null) {
      instance = new InfoboxFactory();
    }
    return instance;
  }
 
  /**
   * The mapping of infobox identifier to implementation class.
   */
  private HashMap<String, Class<? extends Infobox>> implementations;

  /**
   * Private constructor.
   */
  private InfoboxFactory() {
  }

  /**
   * Sets the mapping of infobox identifier to implementation class name.
   * 
   * @param infoboxImplMap
   *          a mapping of infobox identifiers to implementation class names
   * 
   * @throws ClassNotFoundException
   *           if implementation class is not an instance of {@link Infobox}
   */
  @SuppressWarnings("unchecked")
  public void setInfoboxImpl(Map<String, String> infoboxImplMap) throws ClassNotFoundException {
    HashMap<String, Class<? extends Infobox>> implMap = new HashMap<String, Class<? extends Infobox>>();
    ClassLoader cl = getClass().getClassLoader();
    for (String key : infoboxImplMap.keySet()) {
      Class<? extends Infobox> impl = (Class<? extends Infobox>) cl.loadClass(infoboxImplMap.get(key));
      log.debug("Registering infobox '" + key + "' implementation '" + impl.getCanonicalName() + "'.");
      implMap.put(key, impl);
    }
    implementations = implMap;
  }

  /**
   * Returns the configured implementation class for the given
   * <code>infoboxIdentifier</code>.
   * 
   * @param infoboxIdentifier
   *          the infobox identifier
   * 
   * @return the implementation class for the given infobox identifier or
   *         <code>null</code> if there is no implementation class configured
   */
  public Class<? extends Infobox> getImplClass(String infoboxIdentifier) {
    if (implementations != null) {
      return implementations.get(infoboxIdentifier);
    } else {
      return null;
    }
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
    
    Class<? extends Infobox> implClass = getImplClass(infoboxIdentifier);
    if (implClass == null) {
      // infobox not supported
      log.info("Unsupported infobox '" + infoboxIdentifier + ".");
      throw new SLCommandException(4002,
          SLExceptionMessages.EC4002_INFOBOX_UNKNOWN,
          new Object[] { infoboxIdentifier });
    }
    
    // try to instantiate
    Infobox infobox;
    try {
      infobox = implClass.newInstance();
      log.debug("Infobox '" + infobox.getIdentifier() + "' created.");
    } catch (InstantiationException e) {
      // unexpected error
      log.error("Failed to instantiate infobox implementation.", e);
      throw new SLRuntimeException(e);
    } catch (IllegalAccessException e) {
      // unexpected error
      log.error("Failed to instantiate infobox implementation.", e);
      throw new SLRuntimeException(e);
    }
    
    return infobox;
    
  }
  

}
