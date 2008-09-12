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
package at.gv.egiz.bku.text;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.bku.viewer.ValidationException;
import at.gv.egiz.bku.viewer.Validator;

public class TextValidator implements Validator {

  /**
   * Logging facility.
   */
  protected static Log log = LogFactory.getLog(TextValidator.class);
  
  private void invalid(char c) throws ValidationException {
    log.info("Invalid character (0x" + Integer.toHexString(c) + ") found.");
    // TODO: localize
    throw new ValidationException();
  }
  
  @Override
  public void validate(InputStream is, String charset)
      throws ValidationException {
    
    InputStreamReader reader;
    if (charset != null) {
      try {
        reader = new InputStreamReader(is, charset);
      } catch (UnsupportedEncodingException e) {
        log.info("Charset '" + charset + "' not supported.", e);
        // TODO: localize
        throw new ValidationException(e);
      }
    } else {
      reader = new InputStreamReader(is, Charset.forName("UTF-8"));
    }
    
    try {
      char c;
      CharBuffer cb = CharBuffer.allocate(256);
      for (int l; (l = reader.read(cb)) != -1;) {
        cb.flip();
        for (int i = 0; i < l; i++) {
          c = cb.get();
          if (c < '\u0020') {
            // C0 Controls and Basic Latin (0x000C-0x000D)
            if (c > '\r') invalid(c); if (c >= '\u000C') continue;
            // C0 Controls and Basic Latin (0x0009-0x000A)
            if (c > '\n') invalid(c); if (c >= '\t') continue;
            invalid(c);
          } else {
            // C0 Controls and Basic Latin (0x0020-0x007E)
            if (c <= '\u007E') continue;
            // C1 Controls and Latin-1 Supplement (0x00A1-0x00FF)
            if (c < '\u00A1') invalid(c); if (c <= '\u00FF') continue;
            // Latin Extended-A (0x0100-0x017F)
            if (c < '\u0100') invalid(c); if (c <= '\u017F') continue;
            // EURO Sign
            if (c == '\u20AC') continue;
            // Spacing Modifier Letters
            if (c == '\u02C7') continue;
            if (c == '\u02D8') continue;
            if (c == '\u02D9') continue;
            if (c == '\u02DB') continue;
            if (c == '\u02DD') continue;
            if (c == '\u2015') continue;
            invalid(c);
          }
        }
      }
      cb.clear();
    } catch (IOException e) {
      // TODO: localize
      throw new ValidationException(e);
    }
    
    
    
  }

}
