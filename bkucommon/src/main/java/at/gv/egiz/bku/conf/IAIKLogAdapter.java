/**
 * 
 */
package at.gv.egiz.bku.conf;

import org.slf4j.Logger;

import iaik.logging.Log;
import iaik.logging.TransactionId;

/**
 * @author mcentner
 *
 */
public class IAIKLogAdapter implements Log {
  
  /**
   * The id that will be written to the log if the transactionid == null
   */
  public final static String NO_ID = "Null-ID";

  protected Logger log;
  
  protected String nodeId;

  public IAIKLogAdapter(Logger logger) {
    this.log = logger;
  }

  /* (non-Javadoc)
   * @see iaik.logging.Log#debug(iaik.logging.TransactionId, java.lang.Object, java.lang.Throwable)
   */
  @Override
  public void debug(TransactionId transactionId, Object message, Throwable t) {
    if (log.isDebugEnabled()) {
      log.debug(nodeId + ": "
          + ((transactionId != null) ? transactionId.getLogID() : NO_ID) + ": "
          + message, t);
    }
  }

  /* (non-Javadoc)
   * @see iaik.logging.Log#info(iaik.logging.TransactionId, java.lang.Object, java.lang.Throwable)
   */
  @Override
  public void info(TransactionId transactionId, Object message, Throwable t) {
    if (log.isInfoEnabled()) {
      log.info(nodeId + ": "
          + ((transactionId != null) ? transactionId.getLogID() : NO_ID) + ": "
          + message, t);
    }
  }

  /* (non-Javadoc)
   * @see iaik.logging.Log#warn(iaik.logging.TransactionId, java.lang.Object, java.lang.Throwable)
   */
  @Override
  public void warn(TransactionId transactionId, Object message, Throwable t) {
    if (log.isWarnEnabled()) {
      log.warn(nodeId + ": "
          + ((transactionId != null) ? transactionId.getLogID() : NO_ID) + ": "
          + message, t);
    }
  }

  /* (non-Javadoc)
   * @see iaik.logging.Log#error(iaik.logging.TransactionId, java.lang.Object, java.lang.Throwable)
   */
  @Override
  public void error(TransactionId transactionId, Object message, Throwable t) {
    if (log.isErrorEnabled()) {
      log.error(nodeId + ": "
          + ((transactionId != null) ? transactionId.getLogID() : NO_ID) + ": "
          + message, t);
    }
  }

  /* (non-Javadoc)
   * @see iaik.logging.Log#fatal(iaik.logging.TransactionId, java.lang.Object, java.lang.Throwable)
   */
  @Override
  public void fatal(TransactionId transactionId, Object message, Throwable t) {
    if (log.isErrorEnabled()) {
      log.error(nodeId + ": "
          + ((transactionId != null) ? transactionId.getLogID() : NO_ID) + ": "
          + message, t);
    }
  }

  /* (non-Javadoc)
   * @see iaik.logging.Log#setNodeId(java.lang.String)
   */
  @Override
  public void setNodeId(String nodeId) {
    this.nodeId = nodeId;
  }

  /* (non-Javadoc)
   * @see iaik.logging.Log#getNodeId()
   */
  @Override
  public String getNodeId() {
    return nodeId;
  }

  /* (non-Javadoc)
   * @see iaik.logging.Log#isDebugEnabled()
   */
  @Override
  public boolean isDebugEnabled() {
    return log.isDebugEnabled();
  }

  /* (non-Javadoc)
   * @see iaik.logging.Log#isInfoEnabled()
   */
  @Override
  public boolean isInfoEnabled() {
    return log.isInfoEnabled();
  }

  /* (non-Javadoc)
   * @see iaik.logging.Log#isWarnEnabled()
   */
  @Override
  public boolean isWarnEnabled() {
    return log.isWarnEnabled();
  }

  /* (non-Javadoc)
   * @see iaik.logging.Log#isErrorEnabled()
   */
  @Override
  public boolean isErrorEnabled() {
    return log.isErrorEnabled();
  }

  /* (non-Javadoc)
   * @see iaik.logging.Log#isFatalEnabled()
   */
  @Override
  public boolean isFatalEnabled() {
    return log.isErrorEnabled();
  }

}
