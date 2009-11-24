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
    protected String filename;

    public ByteArrayHashDataInput(byte[] hashData, String id, String mimeType, String encoding, String filename) {
        if (hashData == null) {
            throw new NullPointerException("HashDataInput not provided.");
        }
        this.hashData = hashData;
        this.id = id;
        this.mimeType = mimeType;
        this.encoding = encoding;
        this.filename = filename;
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

  @Override
  public String getFilename() {
    return filename;
  }

    
}
