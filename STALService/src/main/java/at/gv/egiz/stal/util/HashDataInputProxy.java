package at.gv.egiz.stal.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.bku.utils.StreamUtil;
import at.gv.egiz.stal.HashDataInput;

/**
 * Enables multiple read requests.
 * @deprecated use at.gv.egiz.stal.impl.ByteArrayHashDataInput
 */
public class HashDataInputProxy implements HashDataInput {

  private static Log log = LogFactory.getLog(HashDataInputProxy.class);

  private HashDataInput delegate;
  private byte[] hashInput;

  /**
   * 
   * @param delegate
   *          != null
   */
  public HashDataInputProxy(HashDataInput delegate) {
    if (delegate == null) {
      throw new NullPointerException("Constructor argument must not be null");
    }
    this.delegate = delegate;
  }

  @Override
  public String getEncoding() {
    return delegate.getEncoding();
  }

  @Override
  public InputStream getHashDataInput() {
    if (hashInput == null) {
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      try {
        StreamUtil.copyStream(delegate.getHashDataInput(), os);
        hashInput = os.toByteArray();
      } catch (IOException e) {
        log.error("Cannot access hashdatainput stream", e);
        hashInput = new byte[0];
      }
    }
    return new ByteArrayInputStream(hashInput);
  }

  @Override
  public String getMimeType() {
    return delegate.getMimeType();
  }

  @Override
  public String getReferenceId() {
    return delegate.getReferenceId();
  }

}
