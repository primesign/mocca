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
