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

import java.awt.Container;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Locale;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.LayoutStyle;
import javax.swing.SwingUtilities;

/**
 * TODO pull out ResourceBundle to common superclass for activationGUI and pinMgmtGUI
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class PINManagementGUI extends ActivationGUI implements PINManagementGUIFacade {

  public static final String BUTTON_ACTIVATE = "button.activate";
  public static final String BUTTON_UNBLOCK = "button.unblock";
  public static final String BUTTON_CHANGE = "button.change";

  public PINManagementGUI(Container contentPane,
          Locale locale,
          Style guiStyle,
          URL backgroundImgURL,
          AbstractHelpListener helpListener) {
    super(contentPane, locale, guiStyle, backgroundImgURL, helpListener);
  }

  @Override
  public void showPINManagementDialog(final PINStatusProvider pinStatusProvider,
          final ActionListener activateListener, final String activateCmd,
          final ActionListener changeListener, final String changeCmd,
          final ActionListener unblockListener, final String unblockCmd,
          final ActionListener cancelListener, final String cancelCmd) {
//    try {
      SwingUtilities.invokeLater(new Runnable() {

        @Override
        public void run() {
          log.debug("show PIN management dialog");

                mainPanel.removeAll();
                buttonPanel.removeAll();

                helpListener.setHelpTopic(HELP_PINMGMT);

                JLabel mgmtLabel = new JLabel();
                mgmtLabel.setFont(mgmtLabel.getFont().deriveFont(mgmtLabel.getFont().getStyle() & ~java.awt.Font.BOLD));

                if (renderHeaderPanel) {
                  titleLabel.setText(cardmgmtMessages.getString(TITLE_PINMGMT));
                  mgmtLabel.setText(cardmgmtMessages.getString(MESSAGE_PINMGMT));
                } else {
                  mgmtLabel.setText(cardmgmtMessages.getString(TITLE_PINMGMT));
                }


                

                GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
                mainPanel.setLayout(mainPanelLayout);

                GroupLayout.SequentialGroup messageHorizontal = mainPanelLayout.createSequentialGroup()
                        .addComponent(mgmtLabel);
                GroupLayout.Group messageVertical = mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(mgmtLabel);
                if (!renderHeaderPanel) {
                  messageHorizontal
                          .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 0, Short.MAX_VALUE)
                          .addComponent(helpLabel);
                  messageVertical
                          .addComponent(helpLabel);
                }

                mainPanelLayout.setHorizontalGroup(messageHorizontal);
                mainPanelLayout.setVerticalGroup(messageVertical);


                JButton activateButton = new JButton();
                activateButton.setFont(activateButton.getFont().deriveFont(activateButton.getFont().getStyle() & ~java.awt.Font.BOLD));
                activateButton.setText(cardmgmtMessages.getString(BUTTON_ACTIVATE));
                activateButton.setEnabled(true);//false);
                activateButton.setActionCommand(activateCmd);
                activateButton.addActionListener(activateListener);

                JButton changeButton = new JButton();
                changeButton.setFont(activateButton.getFont().deriveFont(activateButton.getFont().getStyle() & ~java.awt.Font.BOLD));
                changeButton.setText(cardmgmtMessages.getString(BUTTON_CHANGE));
                changeButton.setEnabled(false);
                changeButton.setActionCommand(changeCmd);
                changeButton.addActionListener(changeListener);

                JButton unblockButton = new JButton();
                unblockButton.setFont(activateButton.getFont().deriveFont(activateButton.getFont().getStyle() & ~java.awt.Font.BOLD));
                unblockButton.setText(cardmgmtMessages.getString(BUTTON_UNBLOCK));
                unblockButton.setEnabled(false);
                unblockButton.setActionCommand(unblockCmd);
                unblockButton.addActionListener(unblockListener);

                JButton cancelButton = new JButton();
                cancelButton.setFont(cancelButton.getFont().deriveFont(cancelButton.getFont().getStyle() & ~java.awt.Font.BOLD));
                cancelButton.setText(messages.getString(BUTTON_CANCEL));
                cancelButton.setActionCommand(cancelCmd);
                cancelButton.addActionListener(cancelListener);

                GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
                buttonPanel.setLayout(buttonPanelLayout);

                GroupLayout.SequentialGroup buttonHorizontal = buttonPanelLayout.createSequentialGroup()
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(activateButton, GroupLayout.PREFERRED_SIZE, buttonSize, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(changeButton, GroupLayout.PREFERRED_SIZE, buttonSize, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(unblockButton, GroupLayout.PREFERRED_SIZE, buttonSize, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, buttonSize, GroupLayout.PREFERRED_SIZE);

                GroupLayout.Group buttonVertical = buttonPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                          .addComponent(activateButton)
                          .addComponent(changeButton)
                          .addComponent(unblockButton)
                          .addComponent(cancelButton);

                buttonPanelLayout.setHorizontalGroup(buttonHorizontal);
                buttonPanelLayout.setVerticalGroup(buttonVertical);

                contentPanel.validate();

        }
      });

//    } catch (Exception ex) {
//      log.error(ex.getMessage(), ex);
//      showErrorDialog(ERR_UNKNOWN_WITH_PARAM, new Object[] {ex.getMessage()});
//    }
  }


}
