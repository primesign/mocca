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
