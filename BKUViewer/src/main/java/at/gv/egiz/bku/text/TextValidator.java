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


package at.gv.egiz.bku.text;

import at.gv.egiz.bku.gui.viewer.FontProviderException;
import at.gv.egiz.bku.viewer.ResourceFontLoader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.bku.viewer.ValidationException;
import at.gv.egiz.bku.viewer.Validator;
import java.awt.Font;

public class TextValidator implements Validator {

  /**
   * Logging facility.
   */
  protected static Logger log = LoggerFactory.getLogger(TextValidator.class);

  protected Font viewerFont;

  public TextValidator() throws FontProviderException {
    viewerFont = new ResourceFontLoader().getFont();
  }

  private void invalid(char c) throws ValidationException {
    log.info("Invalid character (0x{}) found.", Integer.toHexString(c));
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
        log.info("Charset '{}' not supported.", charset, e);
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
