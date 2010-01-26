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
package at.gv.egiz.bku.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.CharBuffer;
import java.util.BitSet;

/**
 * An URLEncoding <a
 * href="http://tools.ietf.org/html/rfc3986#section-2.1">RFC3986, Section 2.1</a>
 * OutputStream.
 * 
 * @author mcentner
 */
public class URLEncodingOutputStream extends OutputStream {
  
  private static final int MAX_BUFFER_SIZE = 512;

  private static final BitSet UNRESERVED = new BitSet(256);
  
  static {
    for (int i = '0'; i <= '9'; i++) {
      UNRESERVED.set(i);
    }
    for (int i = 'a'; i <= 'z'; i++) {
      UNRESERVED.set(i);
    }
    for (int i = 'A'; i <= 'Z'; i++) {
      UNRESERVED.set(i);
    }
    UNRESERVED.set('-');
    UNRESERVED.set('_');
    UNRESERVED.set('.');
    UNRESERVED.set('*');
    UNRESERVED.set(' ');
  }

  private static final char[] HEX = new char[] {
    '0', '1', '2', '3', '4', '5', '6', '7', 
    '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
  };
  
  private char[] buf;
  
  protected Appendable out;

  /**
   * Creates a new instance of this URLEncodingOutputStream that writes to the
   * given Appendable.
   * <p>
   * Note: According to
   * http://www.w3.org/TR/html40/appendix/notes.html#non-ascii-chars the input
   * for the {@link #write()} methods should be the UTF-8.
   * </p>
   * 
   * @param out
   */
  public URLEncodingOutputStream(Appendable out) {
    this.out = out;
  }

  /* (non-Javadoc)
   * @see java.io.OutputStream#write(int)
   */
  @Override
  public void write(int b) throws IOException {
    b &= 0xFF;
    if (UNRESERVED.get(b)) {
      if (b == ' ') {
        out.append('+');
      } else {
        out.append((char) b);
      }
    } else {
      out.append('%').append(HEX[b >>> 4]).append(HEX[b & 0xF]);
    }
    
  }

  /* (non-Javadoc)
   * @see java.io.OutputStream#write(byte[], int, int)
   */
  @Override
  public void write(byte[] b, int off, int len) throws IOException {
  
    // ensure a buffer at least double the size of end - start + 1
    // but max 
    int sz = Math.min(len + 1, MAX_BUFFER_SIZE);
    if (buf == null || buf.length < sz) {
      buf = new char[sz];
    }
  
    int bPos = 0;
    for (int i = 0; i < len; i++) {
      if (bPos + 3 > buf.length) {
        // flush buffer
        out.append(CharBuffer.wrap(buf, 0, bPos));
        bPos = 0;
      }
      int c = 0xFF & b[off + i];
      if (UNRESERVED.get(c)) {
        if (c == ' ') {
          buf[bPos++] = '+';
        } else {
          buf[bPos++] = (char) c;
        }
      } else {
        buf[bPos++] = '%';
        buf[bPos++] = HEX[c >>> 4];
        buf[bPos++] = HEX[c & 0xF];
      }
    }
    out.append(CharBuffer.wrap(buf, 0, bPos));
    
  }


}
