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
package at.gv.egiz.bku.binding;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

  private static Log log = LogFactory
      .getLog(MultiPartFormDataInputDecoder.class);

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

    private FileItemIterator fileItemIterator;

    public IteratorDelegator(FileItemIterator fit) {
      fileItemIterator = fit;
    }

    @Override
    public boolean hasNext() {
      try {
        return fileItemIterator.hasNext();
      } catch (FileUploadException e) {
        log.error(e);
        throw new SLRuntimeException(e);
      } catch (IOException e) {
        log.error(e);
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
        log.error(e);
        throw new SLRuntimeException(e);
      } catch (IOException e) {
        log.error(e);
        throw new SLRuntimeException(e);
      }
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("Remove not supported");
    }
  }
}
