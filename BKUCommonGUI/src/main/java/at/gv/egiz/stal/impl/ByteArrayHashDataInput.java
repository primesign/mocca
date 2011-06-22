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


package at.gv.egiz.stal.impl;

import at.gv.egiz.stal.HashDataInput;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author clemens
 */
public class ByteArrayHashDataInput implements HashDataInput {

    private final Logger log = LoggerFactory.getLogger(ByteArrayHashDataInput.class);
  
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
        log.error("Failed to cache provided HashDataInput: {}.", ex.getMessage(), ex);
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
