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


/**
 * 
 */
package at.gv.egiz.bku.conf;

import java.util.WeakHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iaik.logging.Log;
import iaik.logging.LogConfigurationException;
import iaik.logging.LogFactory;

/**
 * @author mcentner
 *
 */
public class IAIKLogAdapterFactory extends LogFactory {

  protected WeakHashMap<Logger, Log> instances = new WeakHashMap<Logger, Log>();
  
  /* (non-Javadoc)
   * @see iaik.logging.LogFactory#getInstance(java.lang.String)
   */
  @Override
  public synchronized Log getInstance(String name) throws LogConfigurationException {
    Logger logger = LoggerFactory.getLogger(name);
    Log log = instances.get(logger);
    if (log == null) {
      log = new IAIKLogAdapter(logger);
      log.setNodeId(node_id_);
      instances.put(logger, log);
    }
    return log;
  }

  /* (non-Javadoc)
   * @see iaik.logging.LogFactory#getInstance(java.lang.Class)
   */
  @Override
  public synchronized Log getInstance(@SuppressWarnings("rawtypes") Class clazz) throws LogConfigurationException {
    Logger logger = LoggerFactory.getLogger(clazz);
    Log log = instances.get(logger);
    if (log == null) {
      log = new IAIKLogAdapter(logger);
      log.setNodeId(node_id_);
      instances.put(logger, log);
    }
    return log;
  }

  /* (non-Javadoc)
   * @see iaik.logging.LogFactory#release()
   */
  @Override
  public void release() {
    instances.clear();
  }

}
