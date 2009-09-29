/**
 * 
 */
package at.gv.egiz.bku.conf;

import org.apache.commons.logging.impl.WeakHashtable;

import iaik.logging.Log;
import iaik.logging.LogConfigurationException;
import iaik.logging.LogFactory;

/**
 * @author mcentner
 *
 */
public class IAIKCommonsLogFactory extends LogFactory {

  protected WeakHashtable instances = new WeakHashtable();
  
  /* (non-Javadoc)
   * @see iaik.logging.LogFactory#getInstance(java.lang.String)
   */
  @Override
  public Log getInstance(String name) throws LogConfigurationException {
    org.apache.commons.logging.Log commonsLog = org.apache.commons.logging.LogFactory.getLog(name);
    Log log = (Log) instances.get(commonsLog);
    if (log == null) {
      log = new IAIKCommonsLog(commonsLog);
      log.setNodeId(node_id_);
      instances.put(commonsLog, log);
    }
    return log;
  }

  /* (non-Javadoc)
   * @see iaik.logging.LogFactory#getInstance(java.lang.Class)
   */
  @SuppressWarnings("unchecked")
  @Override
  public Log getInstance(Class clazz) throws LogConfigurationException {
    org.apache.commons.logging.Log commonsLog = org.apache.commons.logging.LogFactory.getLog(clazz);
    Log log = (Log) instances.get(commonsLog);
    if (log == null) {
      log = new IAIKCommonsLog(commonsLog);
      log.setNodeId(node_id_);
      instances.put(commonsLog, log);
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
