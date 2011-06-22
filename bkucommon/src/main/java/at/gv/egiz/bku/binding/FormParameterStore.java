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


package at.gv.egiz.bku.binding;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;

import org.apache.commons.fileupload.FileItemHeaders;
import org.apache.commons.fileupload.util.FileItemHeadersImpl;

import at.gv.egiz.bku.slexceptions.SLRuntimeException;
import at.gv.egiz.bku.utils.StreamUtil;

/**
 * Simple store for form parameters based on a byte[]
 * 
 * @author wbauer
 * 
 */
public class FormParameterStore implements FormParameter {

  private byte[] dataBuffer;
  private String contentType;
  private String parameterName;
  private boolean initialized = false;
  protected FileItemHeaders headers;

  /**
   * Make sure to call init after creating a new instance.
   */
  public FormParameterStore() {
  }

  public void init(InputStream dataSource, String paramName,
      String contentType, FileItemHeaders header) throws IOException {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    StreamUtil.copyStream(dataSource, os);
    this.dataBuffer = os.toByteArray();
    this.parameterName = paramName;
    this.contentType = contentType;
    initialized = true;
    this.headers = header;
  }
  
  public void init(byte[] dataSource, String paramName,
      String contentType, FileItemHeaders header) throws IOException {
    this.dataBuffer = dataSource;
    this.parameterName = paramName;
    this.contentType = contentType;
    initialized = true;
    this.headers = header;
  }

  public void init(FormParameter fp) throws IOException {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    StreamUtil.copyStream(fp.getFormParameterValue(), os);
    this.dataBuffer = os.toByteArray();
    this.parameterName = fp.getFormParameterName();
    this.contentType = fp.getFormParameterContentType();
    if (fp instanceof FormParameterImpl) {
      headers = ((FormParameterImpl) fp).getHeaders();
    } else {
      FileItemHeadersImpl headersImpl = new FileItemHeadersImpl();
      for (Iterator<String> i = fp.getHeaderNames(); i.hasNext();) {
        String headerName = i.next();
        headersImpl.addHeader(headerName, fp.getHeaderValue(headerName));
      }
    }
    initialized = true;
  }

  protected void ensureInitialized() {
    if (!initialized) {
      throw new SLRuntimeException("FormParameterStore not initialized");
    }
  }

  /**
   * Reads all data from the stream and stores it internally. The stream will
   * not be closed.
   * 
   * @param datSource
   * @param formName
   * @param contentType
   */
  @Override
  public String getFormParameterContentType() {
    ensureInitialized();
    return contentType;
  }

  @Override
  public String getFormParameterName() {
    ensureInitialized();
    return parameterName;
  }

  /**
   * May be called more than once.
   */
  @Override
  public InputStream getFormParameterValue() {
    return new ByteArrayInputStream(dataBuffer);
  }

  @Override
  public String getHeaderValue(String name) {
    if (headers == null) {
      return null;
    }
    return headers.getHeader(name);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Iterator<String> getHeaderNames() {
    if (headers == null) {
      return Collections.EMPTY_LIST.iterator();
    }
    return headers.getHeaderNames();
  }
  
  public boolean isEmpty() {
    ensureInitialized();
    return dataBuffer.length == 0;
  }

}
