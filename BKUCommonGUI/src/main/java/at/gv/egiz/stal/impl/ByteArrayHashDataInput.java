/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.gv.egiz.stal.impl;

import at.gv.egiz.stal.HashDataInput;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 *
 * @author clemens
 */
public class ByteArrayHashDataInput implements HashDataInput {

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
