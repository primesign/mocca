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

import at.gv.egiz.smcc.PINSpec;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 *
 * @author clemens
 */
class PINDocument extends PlainDocument {

        private PINSpec pinSpec;
        private Pattern pinPattern;
        private JButton enterButton;

        public PINDocument(PINSpec pinSpec, JButton enterButton) {
            this.pinSpec = pinSpec;
            if (pinSpec.getRexepPattern() != null) {
                pinPattern = Pattern.compile(pinSpec.getRexepPattern());
            } else {
                pinPattern = Pattern.compile(".");
            }
            this.enterButton = enterButton;
        }

        @Override
        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
            if (pinSpec.getMaxLength() >= (getLength() + str.length())) {
                boolean matches = true;
                for (int i = 0; i < str.length(); i++) {
                    Matcher m = pinPattern.matcher(str.substring(i, i + 1));
                    if (!m.matches()) {
                        matches = false;
                    }
                }
                if (matches) {
                    super.insertString(offs, str, a);
                }
            }
            enterButton.setEnabled(getLength() >= pinSpec.getMinLength());
        }

        @Override
        public void remove(int offs, int len) throws BadLocationException {
            super.remove(offs, len);
            enterButton.setEnabled(getLength() >= pinSpec.getMinLength());
        }
    }