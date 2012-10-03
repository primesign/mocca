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
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class XWWWFormUrlInputIterator implements Iterator<FormParameter> {
  
  public static final byte NAME_VALUE_SEP = '=';
  
  public static final byte PARAM_SEP = '&';
  
  public static final Charset UTF_8 = Charset.forName("UTF-8");

  /**
   * The default buffer size.
   */
  protected static final int DEFAULT_BUFFER_SIZE = 4096;
  
  /**
   * Are we done with parsing the input.
   */
  protected boolean done = false;

  /**
   * The x-www-formdata-urlencoded input stream to be parsed.
   */
  protected final InputStream in;
  
  /**
   * The buffer size.
   */
  protected int bufferSize = DEFAULT_BUFFER_SIZE;
  
  /**
   * The read buffer.
   */
  protected final byte[] buf = new byte[bufferSize];
  
  /**
   * The read position.
   */
  protected int pos;
  
  /**
   * The number of valid bytes in the buffer;
   */
  protected int count;
  
  /**
   * The parameter returned by the last call of {@link #next()};
   */
  protected XWWWFormUrlEncodedParameter currentParameter;
  
  /**
   * An IOException that cannot be reported immediately.
   */
  protected IOException deferredIOException;
  
  /**
   * Creates a new instance of this x-www-formdata-urlencoded input iterator
   * with the given InputStream <code>in</code> to be parsed.
   * 
   * @param in the InputStream to be parsed
   */
  public XWWWFormUrlInputIterator(InputStream in) {
    this.in = in;
  }

  /* (non-Javadoc)
   * @see java.util.Iterator#hasNext()
   */
  @Override
  public boolean hasNext() {
    if (done) {
      return false;
    }
    try {
      if (currentParameter != null) {
        // we have to disconnect the current parameter
        // to look for further parameters
        currentParameter.formParameterValue.disconnect();
      }
      // fill buffer if empty
      if (pos >= count) {
        if ((count = in.read(buf)) == -1) {
          // done
          done = true;
          return false;
        }
        pos = 0;
      }
    } catch (IOException e) {
      deferredIOException = e;
      // return true to be able to report error
      return true;
    }
    return true;
  }

  @Override
  public FormParameter next() {
    if (hasNext()) {
      // skip separator
      if (buf[pos] == PARAM_SEP) {
        pos++;
      }
      currentParameter = new XWWWFormUrlEncodedParameter();
      return currentParameter;
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
  
  public class XWWWFormUrlEncodedParameter implements FormParameter {

    /**
     * The list of header names. 
     */
    // x-www-form-urlencoded parameters do not provide headers
    protected final List<String> headers = Collections.emptyList();
    
    /**
     * The name of the form parameter.
     */
    protected String formParameterName;
    
    /**
     * The value of the form parameter.
     */
    protected URLDecodingInputStream formParameterValue;

    public XWWWFormUrlEncodedParameter() {
      // parse parameter name
      URLDecodingInputStream urldec = new URLDecodingInputStream(in, NAME_VALUE_SEP);
      InputStreamReader reader = new InputStreamReader(urldec, UTF_8);
      try {
        StringBuilder sb = new StringBuilder();
        char[] b = new char[128];
        for (int l = 0; (l = reader.read(b)) != -1;) {
          sb.append(b, 0, l);
        }
        formParameterName = sb.toString();
        // fill buffer if empty
        if (pos >= count) {
          if ((count = in.read(buf)) == -1) {
            urldec.close();
            throw new IOException("Invalid URL encoding.");
          }
          pos = 0;
        }
        // skip separator
        pos++;
      } catch (IOException e) {
        deferredIOException = e;
        formParameterName = "";
      }
      formParameterValue = new URLDecodingInputStream(in, PARAM_SEP);
    }

    @Override
    public String getFormParameterContentType() {
      // x-www-form-urlencoded parameters do not specify a content type
      return null;
    }

    @Override
    public String getFormParameterName() {
      return formParameterName;
    }

    @Override
    public InputStream getFormParameterValue() {
      if (deferredIOException != null) {
        final IOException e = deferredIOException;
        deferredIOException = null;
        return new InputStream() {
          @Override
          public int read() throws IOException {
            throw e;
          }
        };
      } else {
        return formParameterValue;
      }
    }

    @Override
    public Iterator<String> getHeaderNames() {
      return headers.iterator();
    }

    @Override
    public String getHeaderValue(String headerName) {
      return null;
    }
    
  }
  
  public class URLDecodingInputStream extends FilterInputStream {
    
    /**
     * Has this stream already been closed.
     */
    private boolean closed = false;
    
    /**
     * Has this stream been disconnected.
     */
    private boolean disconnected = false;
    
    /**
     * Read until this byte occurs.
     */
    protected final byte term;

    /**
     * Creates a new instance of this URLDecodingInputStream.
     * 
     * @param in
     * @param separator
     */
    protected URLDecodingInputStream(InputStream in, byte separator) {
      super(in);
      this.term = separator;
    }
    
    /* (non-Javadoc)
     * @see java.io.FilterInputStream#read()
     */
    @Override
    public int read() throws IOException {
      if (closed) {
        throw new IOException("The stream has already been closed.");
      }
      if (disconnected) {
        return in.read();
      }
      
      if (pos >= count) {
        if ((count = in.read(buf)) == -1) {
          return -1;
        }
        pos = 0;
      } if (buf[pos] == term) {
        return -1;
      } else if (buf[pos] == '+') {
        pos++;
        return ' ';
      } else if (buf[pos] == '%') {
        if (++pos == count) {
          if ((count = in.read(buf)) == -1) {
            throw new IOException("Invalid URL encoding.");
          }
          pos = 0;
        }
        int c1 = Character.digit(buf[pos], 16);
        if (++pos == count) {
          if ((count = in.read(buf)) == -1) {
            throw new IOException("Invalid URL encoding.");
          }
          pos = 0;
        }
        int c2 = Character.digit(buf[pos], 16);
        pos++;
        return ((c1 << 4) | c2);
      } else {
        return buf[pos++];
      }
    }

    /* (non-Javadoc)
     * @see java.io.FilterInputStream#read(byte[], int, int)
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
      if (closed) {
        throw new IOException("The stream has already been closed.");
      }
      if (disconnected) {
        return in.read(b, off, len);
      }
      
      if ((off | len | (off + len) | (b.length - (off + len))) < 0) {
        throw new IndexOutOfBoundsException();
      } else if (len == 0) {
        return 0;
      }

      if (pos >= count) {
        if ((count = in.read(buf)) == -1) {
          return -1;
        }
        pos = 0;
      }
      if (buf[pos] == term) {
        return -1;
      }

      int l = 0;
      for (;;) {
        while (pos < count) {
          if (l == len || buf[pos] == term) {
            return l;
          } else if (buf[pos] == '+') {
            b[off] = ' ';
          } else if (buf[pos] == '%') {
            if (++pos == count) {
              if ((count = in.read(buf)) == -1) {
                throw new IOException("Invalid URL encoding.");
              }
              pos = 0;
            }
            int c1 = Character.digit(buf[pos], 16);
            if (++pos == count) {
              if ((count = in.read(buf)) == -1) {
                throw new IOException("Invalid URL encoding.");
              }
              pos = 0;
            }
            int c2 = Character.digit(buf[pos], 16);
            b[off] = (byte) ((c1 << 4) | c2);
          } else {
            b[off] = buf[pos];
          }
          pos++;
          off++;
          l++;
        }
        if ((count = in.read(buf)) == -1) {
          return l;
        }
        pos = 0;
      }
    }

    /**
     * Disconnect from the InputStream and buffer all remaining data.
     * 
     * @throws IOException
     */
    public void disconnect() throws IOException {
      if (!disconnected) {
        // don't waste space for a buffer if end of stream has already been
        // reached
        byte[] b = new byte[1];
        if ((read(b)) != -1) {
          ByteArrayOutputStream os = new ByteArrayOutputStream();
          os.write(b);
          b = new byte[1024];
          for (int l; (l = read(b, 0, b.length)) != -1;) {
            os.write(b, 0, l);
          }
          super.in = new ByteArrayInputStream(os.toByteArray());
        }
        disconnected = true;
      }
    }
    
    /* (non-Javadoc)
     * @see java.io.FilterInputStream#close()
     */
    @Override
    public void close() throws IOException {
      if (!hasNext()) {
        // don't close the underlying stream until all parts are read
        super.close();
      }
      disconnect();
      closed = true;
    }
    
  }

}
