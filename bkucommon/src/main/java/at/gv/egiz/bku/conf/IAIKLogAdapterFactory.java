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
  @SuppressWarnings("unchecked")
  @Override
  public synchronized Log getInstance(Class clazz) throws LogConfigurationException {
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
