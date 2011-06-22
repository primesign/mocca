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


package at.gv.egiz.bku.gui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

/**
 * Checks if the pin confirmation (compareTo) corresponds to this pin.
 * Additionally, checks if currentPIN (optional) meets the requirements before enabling the OK button.
 * @author clemens
 */
class ComparePinDocument extends PlainDocument {

  private static final long serialVersionUID = 1L;
  protected Pattern pinPattern;
  protected int minLength;
  protected int maxLength;
  protected JButton enterButton;
  protected Document compareTo;
  protected Document currentPIN;

  /**
   * Constructor without compareTo Document parameter (allow null and set later to avoid cyclic dependencies)
   */
  public ComparePinDocument(int minLength, int maxLength, String pattern, JButton enterButton) {
    if (enterButton == null) {
      throw new NullPointerException("OK button null");
    }
    if (pattern != null) {
      pinPattern = Pattern.compile(pattern);
    } else {
      pinPattern = Pattern.compile(".");
    }
    this.minLength = minLength;
    this.maxLength = maxLength;
    this.enterButton = enterButton;
  }

  /**
   * @param compareTo should not be null (allow null and set later to avoid cyclic dependencies)
   */
  public ComparePinDocument(int minLength, int maxLength, String pattern,
          JButton enterButton, Document compareTo) {
    this(minLength, maxLength, pattern, enterButton);
    this.compareTo = compareTo;
  }
  
  public ComparePinDocument(int minLength, int maxLength, String pattern,
          JButton enterButton, Document compareTo, Document currentPIN) {
    this(minLength, maxLength, pattern, enterButton, compareTo);
    this.currentPIN = currentPIN;
  }

  @Override
  public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
    if (maxLength < 0 || maxLength >= (getLength() + str.length())) {
      boolean matches = true;
      for (int i = 0; i < str.length(); i++) {
        Matcher m = pinPattern.matcher(str.substring(i, i + 1));
        if (!m.matches()) {
          matches = false;
        }
      }
      if (matches) {
        super.insertString(offs, str, a);
        enterButton.setEnabled(
                getLength() >= minLength
                && (currentPIN == null || currentPIN.getLength() >= minLength)
                && compareTo.getText(0, compareTo.getLength()).equals(getText(0, getLength())));
      }
    }
  }

  @Override
  public void remove(int offs, int len) throws BadLocationException {
    super.remove(offs, len);
    enterButton.setEnabled(
            getLength() >= minLength
            && (currentPIN == null || currentPIN.getLength() >= minLength)
            && compareTo.getText(0, compareTo.getLength()).equals(getText(0, getLength())));
  }
}
