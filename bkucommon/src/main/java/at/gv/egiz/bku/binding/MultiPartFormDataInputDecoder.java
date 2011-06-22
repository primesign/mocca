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

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.bku.slexceptions.SLRuntimeException;

/**
 * The code to detect the multipart boundary is based on
 * org.apache.commons.fileupload.FileUploadBase of
 * http://commons.apache.org/fileupload/
 * 
 * @author wbauer
 * 
 */
public class MultiPartFormDataInputDecoder implements InputDecoder,
    RequestContext {

  private final Logger log = LoggerFactory.getLogger(MultiPartFormDataInputDecoder.class);

  private String contentType;
  private InputStream stream;

  @Override
  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  @Override
  public String getCharacterEncoding() {
    return null;
  }

  @Override
  public int getContentLength() {
    return 0;
  }

  @Override
  public String getContentType() {
    return contentType;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return stream;
  }

  @Override
  public Iterator<FormParameter> getFormParameterIterator() {
    try {
      FileUpload fup = new FileUpload();
      FileItemIterator fit = fup.getItemIterator(this);
      return new IteratorDelegator(fit);
    } catch (Exception iox) {
      log.error("Cannot decode multipart form data stream " + iox);
      throw new SLRuntimeException(iox);
    }
  }

  @Override
  public void setInputStream(InputStream is) {
    stream = is;
  }

  static class IteratorDelegator implements Iterator<FormParameter> {

    private final Logger log = LoggerFactory.getLogger(MultiPartFormDataInputDecoder.class);
    
    private FileItemIterator fileItemIterator;

    public IteratorDelegator(FileItemIterator fit) {
      fileItemIterator = fit;
    }

    @Override
    public boolean hasNext() {
      try {
        return fileItemIterator.hasNext();
      } catch (FileUploadException e) {
        log.error("Failed to get next file item.", e);
        throw new SLRuntimeException(e);
      } catch (IOException e) {
        log.error("Failed to get next file item.", e);
        throw new SLRuntimeException(e);
      }
    }

    @Override
    public FormParameter next() {
      try {
        FileItemStream item = fileItemIterator.next();
        return new FormParameterImpl(item.getContentType(),
            item.getFieldName(), item.openStream(), item.getHeaders());
      } catch (FileUploadException e) {
        log.error("Failed to get next file item.", e);
        throw new SLRuntimeException(e);
      } catch (IOException e) {
        log.error("Failed to get next file item.", e);
        throw new SLRuntimeException(e);
      }
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("Remove not supported");
    }
  }
}
