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
package at.gv.egiz.bku.local.stal;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JPasswordField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import at.gv.egiz.smcc.PINSpec;

public class PINDialog extends javax.swing.JDialog implements ActionListener {

  // Variables declaration - do not modify
  private javax.swing.JButton okButton;
  private javax.swing.JButton cancelButton;
  private javax.swing.JLabel label;
  private javax.swing.JPasswordField password;
  // End of variables declaration

  private PINSpec pinSpec;
  private String pinString;
  private boolean finished = false;

  class PinDocument extends PlainDocument {
    private Pattern pattern;

    public PinDocument() {
      pattern = Pattern.compile(pinSpec.getRexepPattern());
    }

    public void insertString(int offs, String str, AttributeSet a)
        throws BadLocationException {
      if (pinSpec.getMaxLength() >= (getLength() + str.length())) {
        Matcher matcher = pattern.matcher(str);
        if (matcher.matches()) {
          super.insertString(offs, str, a);
        }
      }
      okButton.setEnabled(getLength() >= pinSpec.getMinLength());
    }

    @Override
    public void remove(int offs, int len) throws BadLocationException {
      super.remove(offs, len);
      okButton.setEnabled(getLength() >= pinSpec.getMinLength());
    }
  }

  public PINDialog() {
  }

  private synchronized void finished(boolean ok) {
    if (ok) {
      pinString = password.getText();
    } else {
      pinString = null;
    }
    finished = true;
    notifyAll();
  }

  public synchronized void waitFinished() {
    while (!finished) {
      try {
        wait();
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  public String getPIN() {
    return pinString;
  }

  /** Creates new form NewJDialog */
  public PINDialog(java.awt.Frame parent, boolean modal, PINSpec pinSpec,
      int retries) {
    super(parent, modal);
    this.pinSpec = pinSpec;
    initComponents();
  }

  private void initComponents() {
    okButton = new javax.swing.JButton();
    cancelButton = new javax.swing.JButton();
    password = new javax.swing.JPasswordField();
    label = new javax.swing.JLabel();
    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

    setTitle("PIN Dialog"); // NOI18N
    setName("Form"); // NOI18N

    okButton.setText("OK"); // NOI18N
    okButton.setName("okButton"); // NOI18N
    okButton.setEnabled(false);
    okButton.addActionListener(this);

    cancelButton.setText("Cancel"); // NOI18N
    cancelButton.setName("cancelButton"); // NOI18N
    cancelButton.addActionListener(this);

    password.setText(""); // NOI18N
    password.setName("password"); // NOI18N
    password.addActionListener(this);
    password.setDocument(new PinDocument());

    label.setText("PIN: "); // NOI18N
    label.setName("jLabel1"); // NOI18N

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(
        getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(layout.createParallelGroup(
        javax.swing.GroupLayout.Alignment.LEADING).addGroup(
        layout.createSequentialGroup().addContainerGap().addGroup(
            layout.createParallelGroup(
                javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                layout.createSequentialGroup().addComponent(label,
                    javax.swing.GroupLayout.PREFERRED_SIZE, 61,
                    javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(
                    javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                    javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(password,
                        javax.swing.GroupLayout.PREFERRED_SIZE, 127,
                        javax.swing.GroupLayout.PREFERRED_SIZE)).addGroup(
                javax.swing.GroupLayout.Alignment.TRAILING,
                layout.createSequentialGroup().addComponent(cancelButton)
                    .addPreferredGap(
                        javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(okButton))).addContainerGap()));
    layout.setVerticalGroup(layout.createParallelGroup(
        javax.swing.GroupLayout.Alignment.LEADING).addGroup(
        layout.createSequentialGroup().addContainerGap().addGroup(
            layout.createParallelGroup(
                javax.swing.GroupLayout.Alignment.BASELINE).addComponent(label,
                javax.swing.GroupLayout.PREFERRED_SIZE, 33,
                javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(password,
                javax.swing.GroupLayout.PREFERRED_SIZE,
                javax.swing.GroupLayout.DEFAULT_SIZE,
                javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(
            javax.swing.LayoutStyle.ComponentPlacement.RELATED, 14,
            Short.MAX_VALUE).addGroup(
            layout.createParallelGroup(
                javax.swing.GroupLayout.Alignment.BASELINE).addComponent(
                okButton).addComponent(cancelButton)).addContainerGap()));

    pack();
  }

  /**
   * @param args
   *          the command line arguments
   */
  public static void main(String args[]) {
    java.awt.EventQueue.invokeLater(new Runnable() {
      public void run() {
        PINDialog dialog = new PINDialog(new javax.swing.JFrame(), true,
            new PINSpec(1, 5, "[0-9]*", "Hansi"), 10);
        dialog.setResizable(false);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
          public void windowClosing(java.awt.event.WindowEvent e) {
            System.exit(0);
          }
        });
        dialog.setVisible(true);
      }
    });
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() instanceof JButton) {
      JButton pressed = (JButton) e.getSource();
      if (pressed.getName().equals("okButton")) {
        finished(true);
      } else if (pressed.getName().equals("cancelButton")) {
        finished(false);
      }
    } else if (e.getSource() instanceof JPasswordField) {
      JPasswordField pwf = (JPasswordField) e.getSource();
      if (pwf.getName().equals("password")) {
        if (password.getPassword().length >= pinSpec.getMinLength()) {
          finished(true);
        }
      }
    }
  }

}
