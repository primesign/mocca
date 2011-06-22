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


/**
 * 
 */
package at.gv.egiz.bku.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.CharBuffer;

/**
 * @author mcentner
 *
 */
public class URLEncodingInputStream extends InputStream {

  private char[] buffer = new char[1];
  
  private CharBuffer charBuffer = CharBuffer.wrap(buffer); 

  protected Readable in;
  
  /**
   * @param in
   */
  public URLEncodingInputStream(Readable in) {
    this.in = in;
  }
  
  /* (non-Javadoc)
   * @see java.io.InputStream#read()
   */
  @Override
  public int read() throws IOException {
    charBuffer.rewind();
    if (in.read(charBuffer) == -1) {
      return -1;
    }
    if (buffer[0] == '+') {
      return ' ';
    } else if (buffer[0] == '%') {
      charBuffer.rewind();
      if (in.read(charBuffer) == -1) {
        throw new IOException("Invalid URL encoding.");
      }
      int c1 = Character.digit(buffer[0], 16);
      charBuffer.rewind();
      if (in.read(charBuffer) == -1) {
        throw new IOException("Invalid URL encoding.");
      }
      int c2 = Character.digit(buffer[0], 16);
      if (c1 == -1 || c2 == -1) {
        throw new IOException("Invalid URL encoding.");
      }
      return ((c1 << 4) | c2);
    } else {
      return buffer[0];
    }
  }
  
  

}
