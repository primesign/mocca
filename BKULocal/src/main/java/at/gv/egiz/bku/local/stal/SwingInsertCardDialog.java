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
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JDialog;

import at.gv.egiz.bku.utils.StreamUtil;

public class SwingInsertCardDialog extends JDialog {

  private javax.swing.JButton cancelButton;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private Locale locale = Locale.getDefault();

  public SwingInsertCardDialog() {
    super((java.awt.Frame) null, false);
    initComponents();
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  private void initComponents() {
    ResourceBundle rb = ResourceBundle.getBundle(
        "at/gv/egiz/bku/local/Userdialog", locale);
    setTitle(rb.getString("Insert.Header"));
    jLabel1 = new javax.swing.JLabel();
    jLabel2 = new javax.swing.JLabel();
    cancelButton = new javax.swing.JButton();

    setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
    setName("Form"); // NOI18N
    setUndecorated(true);

    jLabel1.setFont(new Font("Tahoma", Font.BOLD, 14));
    jLabel1.setText(rb.getString("Insert.Text")); // NOI18N
    jLabel1.setName("text"); // NOI18N

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    InputStream is = getClass().getClassLoader().getResourceAsStream(
        "at/gv/egiz/bku/local/logo.png");
    try {
      StreamUtil.copyStream(is, os);
      jLabel2.setIcon(new ImageIcon(os.toByteArray())); // NOI18N
    } catch (IOException e) {
      jLabel2.setText("Chipperling image missing"); // NOI18N
    }
    jLabel2.setName("jLabel2"); // NOI18N
    cancelButton.setText(rb.getString("Insert.Button.Cancel")); // NOI18N
    cancelButton.setName("jButton1"); // NOI18N

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(
        getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(layout.createParallelGroup(
        javax.swing.GroupLayout.Alignment.LEADING).addGroup(
        layout.createSequentialGroup().addContainerGap().addComponent(jLabel2)
            .addGroup(
                layout.createParallelGroup(
                    javax.swing.GroupLayout.Alignment.TRAILING).addGroup(
                    layout.createSequentialGroup().addGap(35, 35, 35)
                        .addComponent(jLabel1,
                            javax.swing.GroupLayout.DEFAULT_SIZE,
                            javax.swing.GroupLayout.DEFAULT_SIZE,
                            Short.MAX_VALUE)).addGroup(
                    layout.createSequentialGroup().addPreferredGap(
                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton))).addGap(29, 29, 29)));
    layout.setVerticalGroup(layout.createParallelGroup(
        javax.swing.GroupLayout.Alignment.LEADING).addGroup(
        javax.swing.GroupLayout.Alignment.TRAILING,
        layout.createSequentialGroup().addContainerGap().addGroup(
            layout.createParallelGroup(
                javax.swing.GroupLayout.Alignment.TRAILING).addComponent(
                jLabel2).addGroup(
                layout.createSequentialGroup().addComponent(jLabel1,
                    javax.swing.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE)
                    .addGap(35, 35, 35).addComponent(cancelButton).addGap(9, 9,
                        9))).addContainerGap()));

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

  public void addCanceledListener(ActionListener al) {
    cancelButton.addActionListener(al);
  }

  /**
   * @param args
   *          the command line arguments
   */
  public static void main(String args[]) {
    java.awt.EventQueue.invokeLater(new Runnable() {
      public void run() {
        SwingInsertCardDialog dialog = new SwingInsertCardDialog();
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
          public void windowClosing(java.awt.event.WindowEvent e) {
            System.exit(0);
          }
        });
        //
      }
    });
  }

}
