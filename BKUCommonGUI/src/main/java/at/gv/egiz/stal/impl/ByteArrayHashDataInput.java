/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.gv.egiz.stal.impl;

import at.gv.egiz.stal.HashDataInput;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author clemens
 */
public class ByteArrayHashDataInput implements HashDataInput {

    private static final Log log = LogFactory.getLog(ByteArrayHashDataInput.class);
  
    protected byte[] hashData;
    protected String id;
    protected String mimeType;
    protected String encoding;

    public ByteArrayHashDataInput(byte[] hashData, String id, String mimeType, String encoding) {
        if (hashData == null) {
            throw new NullPointerException("HashDataInput not provided.");
        }
        this.hashData = hashData;
        this.id = id;
        this.mimeType = mimeType;
        this.encoding = encoding;
    }
    
    /**
     * caches the hashdata input's stream
     * @param hdi to be cached
     */
    public ByteArrayHashDataInput(HashDataInput hdi) {
      if (hdi == null) {
        throw new NullPointerException("HashDataInput not provided.");
      }
      InputStream is = hdi.getHashDataInput();
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      try {
        byte[] buffer = new byte[1024];
        for (int i = is.read(buffer); i > -1; i = is.read(buffer)) {
          baos.write(buffer, 0, i);
        }
        this.hashData = baos.toByteArray();
      } catch (IOException ex) {
        log.error("Failed to cache provided HashDataInput: " + ex.getMessage(), ex);
        this.hashData = new byte[0];
      }
      this.id = hdi.getReferenceId();
      this.mimeType = hdi.getMimeType();
      this.encoding = hdi.getEncoding();
    }
    
    @Override
    public String getReferenceId() {
        return id;
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }

    @Override
    public InputStream getHashDataInput() {
        return new ByteArrayInputStream(hashData);
    }

    /**
     * may be null
     * @return
     */
  @Override
  public String getEncoding() {
    return encoding;
  }

    
}
