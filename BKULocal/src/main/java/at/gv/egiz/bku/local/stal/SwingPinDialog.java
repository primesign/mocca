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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPasswordField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import at.gv.egiz.bku.utils.StreamUtil;
import at.gv.egiz.smcc.PINSpec;

public class SwingPinDialog extends javax.swing.JDialog implements
    ActionListener {

  private javax.swing.JButton okButton;
  private javax.swing.JButton cancelButton;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JPasswordField password;

  private PINSpec pinSpec;
  private String pinString;
  private boolean finished = false;
  private int retries = -1;
  private Locale locale = Locale.getDefault();
  private boolean setUp = false;

  class PinDocument extends PlainDocument {
    private Pattern pattern;

    public PinDocument() {
      if ((pinSpec != null) && (pinSpec.getRexepPattern() != null)) {
        pattern = Pattern.compile(pinSpec.getRexepPattern());
      } else {
        pattern = Pattern.compile(".");
      }
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

  /**
   * Make sure to call initComponents
   * 
   * @param parent
   * @param modal
   */
  public SwingPinDialog(java.awt.Frame parent, boolean modal) {
    super(parent, modal);
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  public void setPinSpec(PINSpec pinSpec) {
    this.pinSpec = pinSpec;
  }

  public void setRetries(int retries) {
    this.retries = retries;
  }

  public void initComponents() {
    ResourceBundle rb = ResourceBundle.getBundle(
        "at/gv/egiz/bku/local/Userdialog", locale);
    okButton = new javax.swing.JButton();
    cancelButton = new javax.swing.JButton();
    password = new javax.swing.JPasswordField();
    jLabel1 = new javax.swing.JLabel();
    jLabel2 = new javax.swing.JLabel();

    setTitle(rb.getString("Pin.Header"));
    setName("Form");
    setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

    okButton.setText(rb.getString("Pin.Button.OK"));
    okButton.setName("okButton");
    okButton.setEnabled(false);
    okButton.addActionListener(this);

    cancelButton.setText(rb.getString("Pin.Button.Cancel"));
    cancelButton.setName("cancelButton");
    cancelButton.addActionListener(this);

    password.setText("");
    password.setDocument(new PinDocument());
    password.setName("password");
    password.addActionListener(this);
    password.setDocument(new PinDocument());
    password.setRequestFocusEnabled(true);
    password.requestFocus();

    jLabel1.setFont(new Font("Tahoma", Font.BOLD, 14));
    String text = null;
    Object[] args;
    if (retries > 0) {
      text = rb.getString("Pin.Text.Retries");
      args = new Object[2];
      args[0] = pinSpec.getLocalizedName();
      args[1] = new Integer(retries);
    } else {
      text = rb.getString("Pin.Text.NoRetries");
      args = new Object[1];
      args[0] = pinSpec.getLocalizedName();
    }
    text = MessageFormat.format(text, args);
    jLabel1.setText(text); // NOI18N
    jLabel1.setName("jLabel1"); // NOI18N

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    InputStream is = getClass().getClassLoader().getResourceAsStream(
        "at/gv/egiz/bku/local/logo.png");
    try {
      StreamUtil.copyStream(is, os);
      jLabel2.setIcon(new ImageIcon(os.toByteArray())); // NOI18N
    } catch (Exception e) {
      jLabel2.setText("Chipperling image missing"); // NOI18N
    }
    jLabel2.setName("jLabel2"); // NOI18N

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(
        getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(layout.createParallelGroup(
        javax.swing.GroupLayout.Alignment.LEADING).addGroup(
        layout.createSequentialGroup().addContainerGap().addComponent(jLabel2)
            .addGap(73, 73, 73).addGroup(
                layout.createParallelGroup(
                    javax.swing.GroupLayout.Alignment.LEADING).addComponent(
                    jLabel1).addGroup(
                    layout.createParallelGroup(
                        javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(password,
                            javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(
                            javax.swing.GroupLayout.Alignment.LEADING,
                            layout.createSequentialGroup().addComponent(
                                cancelButton).addGap(18, 18, 18).addComponent(
                                okButton)))).addContainerGap(31,
                Short.MAX_VALUE)));
    layout.setVerticalGroup(layout.createParallelGroup(
        javax.swing.GroupLayout.Alignment.LEADING).addGroup(
        layout.createSequentialGroup().addContainerGap().addGroup(
            layout.createParallelGroup(
                javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jLabel2).addGroup(
                    layout.createSequentialGroup().addPreferredGap(
                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel1,
                            javax.swing.GroupLayout.PREFERRED_SIZE, 33,
                            javax.swing.GroupLayout.PREFERRED_SIZE).addGap(18,
                            18, 18).addComponent(password,
                            javax.swing.GroupLayout.PREFERRED_SIZE,
                            javax.swing.GroupLayout.DEFAULT_SIZE,
                            javax.swing.GroupLayout.PREFERRED_SIZE).addGap(20,
                            20, 20).addGroup(
                            layout.createParallelGroup(
                                javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(cancelButton).addComponent(
                                    okButton)))).addGap(36, 36, 36)));
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = getSize();
    if (frameSize.height > screenSize.height) {
      frameSize.height = screenSize.height;
    }
    if (frameSize.width > screenSize.width) {
      frameSize.width = screenSize.width;
    }
    setLocation((screenSize.width - frameSize.width) / 2,
        (screenSize.height - frameSize.height) / 2);
    setUndecorated(false);
    pack();
  }

  public String getPIN() {
    return pinString;
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
