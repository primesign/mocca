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
 * This PINDocument also checks if the additional (optional) pinDocuments also meet the requirements
 * to enable the OK button. 
 * Checks if (optional) newPIN and confirmPIN correspond
 * 
 * @author clemens
 */
class ExtendedPinDocument extends PlainDocument {

  private static final long serialVersionUID = 1L;
  protected Pattern pinPattern;
  protected int minLength;
  protected int maxLength;
  protected JButton enterButton;
  protected Document newPIN;
  protected Document confirmPIN;

  public ExtendedPinDocument(int minLength, int maxLength, String pattern, JButton enterButton) {
    if (enterButton == null) {
      throw new NullPointerException("OK Button null");
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
   * @param pinSpec
   * @param enterButton
   * @param newPIN, confirmPIN
   */
  public ExtendedPinDocument(int minLength, int maxLength, String pattern, JButton enterButton, Document newPIN, Document confirmPIN) {
    this(minLength, maxLength, pattern, enterButton);
    this.newPIN = newPIN;
    this.confirmPIN = confirmPIN;
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
                && (newPIN == null || newPIN.getLength() >= minLength)
                && (confirmPIN == null || compare()));
      }
    }
  }

  @Override
  public void remove(int offs, int len) throws BadLocationException {
    super.remove(offs, len);
    enterButton.setEnabled(
            getLength() >= minLength
            && (newPIN == null || newPIN.getLength() >= minLength)
            && (confirmPIN == null || compare()));
  }

  /**
   * assume confirmPIN != null
   * @return
   */
  private boolean compare() throws BadLocationException {
    if (newPIN != null) {
      return confirmPIN.getText(0, confirmPIN.getLength()).equals(newPIN.getText(0, newPIN.getLength()));
    }
    return false;
  }
}
