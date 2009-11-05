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

import at.gv.egiz.bku.gui.viewer.FontProviderException;
import at.gv.egiz.bku.viewer.ResourceFontLoader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.bku.viewer.ValidationException;
import at.gv.egiz.bku.viewer.Validator;
import java.awt.Font;

public class TextValidator implements Validator {

  /**
   * Logging facility.
   */
  protected static Log log = LogFactory.getLog(TextValidator.class);

  protected Font viewerFont;

  public TextValidator() throws FontProviderException {
    viewerFont = new ResourceFontLoader().getFont();
  }

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
          if (!viewerFont.canDisplay(c)) invalid(c);
        }
      }
      cb.clear();
    } catch (IOException e) {
      // TODO: localize
      throw new ValidationException(e);
    }
    
    
    
  }

}
