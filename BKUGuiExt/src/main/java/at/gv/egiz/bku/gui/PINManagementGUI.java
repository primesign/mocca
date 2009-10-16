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
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TODO pull out ResourceBundle to common superclass for activationGUI and pinMgmtGUI
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class PINManagementGUI extends CardMgmtGUI implements PINManagementGUIFacade {

  protected static final Log log = LogFactory.getLog(PINManagementGUI.class);
  
  /** remember the pinfield to return to worker */
  protected JPasswordField oldPinField;
  /** remember the pinSpec to return to worker */
  protected PINSpec pinSpec;

  public PINManagementGUI(Container contentPane,
          Locale locale,
          Style guiStyle,
          URL backgroundImgURL,
          AbstractHelpListener helpListener,
          SwitchFocusListener switchFocusListener) {
    super(contentPane, locale, guiStyle, backgroundImgURL, helpListener, switchFocusListener);
  }

  @Override
  public char[] getOldPin() {
    if (oldPinField != null) {
      char[] pin = oldPinField.getPassword();
      oldPinField = null;
      return pin;
    }
    return null;
  }

  @Override
  public PINSpec getSelectedPINSpec() {
    return pinSpec;
  }

  @Override
  public void showPINManagementDialog(final Map<PINSpec, STATUS> pins, 
          final ActionListener activateListener,
          final String activateCmd,
          final String changeCmd,
          final String unblockCmd,
          final String verifyCmd,
          final ActionListener cancelListener,
          final String cancelCmd) {

      log.debug("scheduling PIN managment dialog");
    
      SwingUtilities.invokeLater(new Runnable() {

        @Override
        public void run() {
          log.debug("show PIN management dialog");

                mainPanel.removeAll();
                buttonPanel.removeAll();

                helpMouseListener.setHelpTopic(HELP_PINMGMT);
                helpKeyListener.setHelpTopic(HELP_PINMGMT);


                JLabel mgmtLabel = new JLabel();
                mgmtLabel.setFont(mgmtLabel.getFont().deriveFont(mgmtLabel.getFont().getStyle() & ~java.awt.Font.BOLD));

                if (renderHeaderPanel) {
                  titleLabel.setText(getMessage(TITLE_PINMGMT));
                  String infoPattern = getMessage(MESSAGE_PINMGMT);
                  mgmtLabel.setText(MessageFormat.format(infoPattern, pins.size()));
                } else {
                  mgmtLabel.setText(getMessage(TITLE_PINMGMT));
                }

                final PINStatusTableModel tableModel = new PINStatusTableModel(pins);
                final JTable pinStatusTable = new JTable(tableModel);
                pinStatusTable.setDefaultRenderer(PINSpec.class, new PINSpecRenderer());
                pinStatusTable.setDefaultRenderer(STATUS.class, new PINStatusRenderer(cardmgmtMessages));
                pinStatusTable.setTableHeader(null);
                pinStatusTable.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//                pinStatusTable.addMouseMotionListener(new MouseMotionAdapter() {
//
//                  @Override
//                  public void mouseMoved(MouseEvent e) {
//                    if (pinStatusTable.columnAtPoint(e.getPoint()) == 0) {
//                      pinStatusTable.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//                    } else {
//                      pinStatusTable.setCursor(Cursor.getDefaultCursor());
//                    }
//                  }
//                });

                final JButton activateButton = new JButton();
                activateButton.setFont(activateButton.getFont().deriveFont(activateButton.getFont().getStyle() & ~java.awt.Font.BOLD));
                activateButton.addActionListener(activateListener);

                pinStatusTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                pinStatusTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

                  @Override
                  public void valueChanged(final ListSelectionEvent e) {
                    //invoke later to allow thread to paint selection background
                    SwingUtilities.invokeLater(new Runnable() {

                      @Override
                      public void run() {
                        ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                        int selectionIdx = lsm.getMinSelectionIndex();
                        if (selectionIdx >= 0) {
                          pinSpec = (PINSpec) tableModel.getValueAt(selectionIdx, 0);
                          STATUS status = (STATUS) tableModel.getValueAt(selectionIdx, 1);

                          if (status == STATUS.NOT_ACTIV) {
                            activateButton.setText(getMessage(BUTTON_ACTIVATE));
                            activateButton.setEnabled(true);
                            activateButton.setActionCommand(activateCmd);
                          } else if (status == STATUS.BLOCKED) {
                            activateButton.setText(getMessage(BUTTON_UNBLOCK));
                            activateButton.setEnabled(true);
                            activateButton.setActionCommand(unblockCmd);
                          } else if (status == STATUS.ACTIV) {
                            activateButton.setText(getMessage(BUTTON_CHANGE));
                            activateButton.setEnabled(true);
                            activateButton.setActionCommand(changeCmd);
                          } else if (status == STATUS.UNKNOWN) {
                            activateButton.setText(getMessage(BUTTON_VERIFY));
                            activateButton.setEnabled(true);
                            activateButton.setActionCommand(verifyCmd);
                          }
                        }
                      }
                    });
                  }
                });

                //select first entry
                pinStatusTable.getSelectionModel().setSelectionInterval(0, 0);

                JScrollPane pinStatusScrollPane = new JScrollPane(pinStatusTable);

                GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
                mainPanel.setLayout(mainPanelLayout);

                GroupLayout.SequentialGroup messageHorizontal = mainPanelLayout.createSequentialGroup()
                        .addComponent(mgmtLabel);
                GroupLayout.Group messageVertical = mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(mgmtLabel);
                if (!renderHeaderPanel) {
                  messageHorizontal
                          .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 0, Short.MAX_VALUE)
                          .addComponent(switchFocusDummyLabel)
                          .addComponent(helpLabel);
                  messageVertical
                  		  .addComponent(switchFocusDummyLabel)
                          .addComponent(helpLabel);
                }

                mainPanelLayout.setHorizontalGroup(
                mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                  .addGroup(messageHorizontal)
                  .addComponent(pinStatusScrollPane, 0, 0, Short.MAX_VALUE));

                mainPanelLayout.setVerticalGroup(
                  mainPanelLayout.createSequentialGroup()
                    .addGroup(messageVertical)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(pinStatusScrollPane, 0, 0, pinStatusTable.getPreferredSize().height+3));

                JButton cancelButton = new JButton();
                cancelButton.setFont(cancelButton.getFont().deriveFont(cancelButton.getFont().getStyle() & ~java.awt.Font.BOLD));
                cancelButton.setText(getMessage(BUTTON_CLOSE));
                cancelButton.setActionCommand(cancelCmd);
                cancelButton.addActionListener(cancelListener);

                GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
                buttonPanel.setLayout(buttonPanelLayout);

                GroupLayout.SequentialGroup buttonHorizontal = buttonPanelLayout.createSequentialGroup()
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(activateButton, GroupLayout.PREFERRED_SIZE, buttonSize, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, buttonSize, GroupLayout.PREFERRED_SIZE);

                GroupLayout.Group buttonVertical = buttonPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                          .addComponent(activateButton)
                          .addComponent(cancelButton);

                buttonPanelLayout.setHorizontalGroup(buttonHorizontal);
                buttonPanelLayout.setVerticalGroup(buttonVertical);

                helpLabel.requestFocus();
                contentPanel.validate();
        }
      });
  }

  @Override
  public void showPINDialog(DIALOG type, PINSpec pinSpec,
          ActionListener okListener, String okCommand,
          ActionListener cancelListener, String cancelCommand) {
    showPINDialog(type, pinSpec, -1, false,
            okListener, okCommand, cancelListener, cancelCommand);
  }

  @Override
  public void showPINDialog(DIALOG type, PINSpec pinSpec, int retries,
          ActionListener okListener, String okCommand,
          ActionListener cancelListener, String cancelCommand) {
    showPINDialog(type, pinSpec, retries, false,
            okListener, okCommand, cancelListener, cancelCommand);
  }

  @Override
  public void showPinpadPINDialog(DIALOG type, PINSpec pinSpec, int retries) {
    String title, msg;
    Object[] params;
    if (retries < 0) {
      params = new Object[2];
      if (shortText) {
        params[0] = "PIN";
      } else {
        params[0] = pinSpec.getLocalizedName();
      }
      params[1] = pinSpec.getLocalizedLength();
      if (type == DIALOG.CHANGE) {
        log.debug("show change pin dialog");
        title = TITLE_CHANGE_PIN;
        msg = MESSAGE_CHANGEPIN_PINPAD;
      } else if (type == DIALOG.ACTIVATE) {
        log.debug("show activate pin dialog");
        title = TITLE_ACTIVATE_PIN;
        msg = MESSAGE_ENTERPIN_PINPAD;
      } else if (type == DIALOG.VERIFY) {
        log.debug("show verify pin dialog");
        title = TITLE_VERIFY_PIN;
        msg = MESSAGE_ENTERPIN_PINPAD;
      } else {
        log.debug("show unblock pin dialog");
        title = TITLE_UNBLOCK_PIN;
        msg = MESSAGE_ENTERPIN_PINPAD;
      }

    } else {
      log.debug("show retry pin dialog");
      title = TITLE_RETRY;
      msg = (retries < 2) ?
        MESSAGE_LAST_RETRY : MESSAGE_RETRIES;
      params = new Object[] {String.valueOf(retries)};
    }
    showMessageDialog(title, msg, params);
  }

  private void showPINDialog(final DIALOG type, final PINSpec pinSpec,
          final int retries, final boolean pinpad,
          final ActionListener okListener, final String okCommand,
          final ActionListener cancelListener, final String cancelCommand) {

    log.debug("scheduling pin dialog");

      SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {

              String HELP_TOPIC, TITLE, MESSAGE_MGMT, MESSAGE_MGMT_PARAM;
              HELP_TOPIC = HELP_PINMGMT;

              if (retries < 0) {
                if (type == DIALOG.CHANGE) {
                  log.debug("show change pin dialog");
                  TITLE = TITLE_CHANGE_PIN;
                  MESSAGE_MGMT = MESSAGE_CHANGE_PIN;
                } else if (type == DIALOG.ACTIVATE) {
                  log.debug("show activate pin dialog");
                  TITLE = TITLE_ACTIVATE_PIN;
                  MESSAGE_MGMT = MESSAGE_ACTIVATE_PIN;
                  oldPinField = null;
                } else if (type == DIALOG.VERIFY) {
                  log.debug("show verify pin dialog");
                  TITLE = TITLE_VERIFY_PIN;
                  MESSAGE_MGMT = MESSAGE_VERIFY_PIN;
                } else {
                  log.debug("show unblock pin dialog");
                  TITLE = TITLE_UNBLOCK_PIN;
                  MESSAGE_MGMT = MESSAGE_UNBLOCK_PIN;
                }
                if (shortText) {
                  MESSAGE_MGMT_PARAM = "PIN";
                } else {
                  MESSAGE_MGMT_PARAM = pinSpec.getLocalizedName();
                }
              } else {
                log.debug("show retry pin dialog");
                TITLE = TITLE_RETRY;
                MESSAGE_MGMT = (retries < 2) ?
                  MESSAGE_LAST_RETRY : MESSAGE_RETRIES;
                MESSAGE_MGMT_PARAM = String.valueOf(retries);
              }

                mainPanel.removeAll();
                buttonPanel.removeAll();

                helpMouseListener.setHelpTopic(HELP_TOPIC);
                helpKeyListener.setHelpTopic(HELP_TOPIC);

                JLabel mgmtLabel = new JLabel();
                if (retries < 0) {
                  mgmtLabel.setFont(mgmtLabel.getFont().deriveFont(mgmtLabel.getFont().getStyle() & ~Font.BOLD));
                } else {
                  mgmtLabel.setFont(mgmtLabel.getFont().deriveFont(mgmtLabel.getFont().getStyle() | Font.BOLD));
                  mgmtLabel.setForeground(ERROR_COLOR);
                  helpMouseListener.setHelpTopic(HELP_RETRY);
                  helpKeyListener.setHelpTopic(HELP_RETRY);
                }

                if (renderHeaderPanel) {
                  titleLabel.setText(getMessage(TITLE));
                  String mgmtPattern = getMessage(MESSAGE_MGMT);
                  mgmtLabel.setText(MessageFormat.format(mgmtPattern, MESSAGE_MGMT_PARAM));
                } else {
                  mgmtLabel.setText(getMessage(TITLE));
                }

                ////////////////////////////////////////////////////////////////
                // COMMON LAYOUT SECTION
                ////////////////////////////////////////////////////////////////

                GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
                mainPanel.setLayout(mainPanelLayout);

                GroupLayout.SequentialGroup infoHorizontal = mainPanelLayout.createSequentialGroup()
                          .addComponent(mgmtLabel);
                GroupLayout.ParallelGroup infoVertical = mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                          .addComponent(mgmtLabel);

                if (!renderHeaderPanel) {
                  infoHorizontal
                          .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 0, Short.MAX_VALUE)
                          .addComponent(switchFocusDummyLabel)
                          .addComponent(helpLabel);
                  infoVertical
                  		  .addComponent(switchFocusDummyLabel)
                          .addComponent(helpLabel);
                }

                GroupLayout.ParallelGroup pinHorizontal;
                GroupLayout.SequentialGroup pinVertical;

                if (pinpad) {
                  JLabel pinpadLabel = new JLabel();
                  pinpadLabel.setFont(mgmtLabel.getFont().deriveFont(mgmtLabel.getFont().getStyle() & ~Font.BOLD));
                  String pinpadPattern = getMessage(MESSAGE_VERIFYPIN_PINPAD);
                  pinpadLabel.setText(MessageFormat.format(pinpadPattern,
                          new Object[] { pinSpec.getLocalizedName(), pinSpec.getLocalizedLength() }));
                  
                  pinHorizontal = mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                          .addComponent(pinpadLabel);
                  pinVertical = mainPanelLayout.createSequentialGroup()
                          .addComponent(pinpadLabel);
                } else {

                JButton okButton = new JButton();
                okButton.setFont(okButton.getFont().deriveFont(okButton.getFont().getStyle() & ~Font.BOLD));
                okButton.setText(getMessage(BUTTON_OK));
                okButton.setEnabled(pinSpec.getMinLength() <= 0);
                okButton.setActionCommand(okCommand);
                okButton.addActionListener(okListener);

                JLabel oldPinLabel = null;
                JLabel repeatPinLabel = null;
                JLabel pinLabel = new JLabel();
                pinLabel.setFont(pinLabel.getFont().deriveFont(pinLabel.getFont().getStyle() & ~Font.BOLD));
                String pinLabelPattern = (type == DIALOG.CHANGE) ? getMessage(LABEL_NEW_PIN) : getMessage(LABEL_PIN);
                pinLabel.setText(MessageFormat.format(pinLabelPattern, new Object[]{pinSpec.getLocalizedName()}));

                final JPasswordField repeatPinField = new JPasswordField();
                pinField = new JPasswordField();
                pinField.setText("");
                pinField.setActionCommand(okCommand);
                pinField.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (pinField.getPassword().length >= pinSpec.getMinLength()) {
                          if (type == DIALOG.VERIFY) {
                            okListener.actionPerformed(e);
                          } else {
                            repeatPinField.requestFocusInWindow();
                          }
                        }
                    }
                });

                if (type != DIALOG.VERIFY) {
                  pinField.setDocument(new PINDocument(pinSpec, null));
                  repeatPinLabel = new JLabel();
                  repeatPinLabel.setFont(pinLabel.getFont());
                  String repeatPinLabelPattern = getMessage(LABEL_REPEAT_PIN);
                  repeatPinLabel.setText(MessageFormat.format(repeatPinLabelPattern, new Object[]{pinSpec.getLocalizedName()}));

                  repeatPinField.setText("");
//                  repeatPinField.setDocument(new PINDocument(pinSpec, okButton, pinField.getDocument()));
                  repeatPinField.setActionCommand(okCommand);
                  repeatPinField.addActionListener(new ActionListener() {

                      @Override
                      public void actionPerformed(ActionEvent e) {
                          if (pinField.getPassword().length >= pinSpec.getMinLength()) {
                              okListener.actionPerformed(e);
                          }
                      }
                  });

                  if (type == DIALOG.CHANGE) {
                    oldPinLabel = new JLabel();
                    oldPinLabel.setFont(oldPinLabel.getFont().deriveFont(oldPinLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
                    String oldPinLabelPattern = getMessage(LABEL_OLD_PIN);
                    oldPinLabel.setText(MessageFormat.format(oldPinLabelPattern, new Object[]{pinSpec.getLocalizedName()}));

                    oldPinField = new JPasswordField();
                    oldPinField.setText("");
                    oldPinField.setDocument(new PINDocument(pinSpec, null));
                    oldPinField.setActionCommand(okCommand);
                    oldPinField.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (oldPinField.getPassword().length >= pinSpec.getMinLength()) {
                              pinField.requestFocusInWindow();
                            }
                        }
                    });

                    repeatPinField.setDocument(new PINDocument(
                            pinSpec, okButton,
                            pinField.getDocument(), oldPinField.getDocument()));
                  } else {
                    // else -> ACTIVATE (not verify, not change)
                    repeatPinField.setDocument(new PINDocument(
                            pinSpec, okButton, pinField.getDocument()));
                  }
                } else {
                  pinField.setDocument(new PINDocument(pinSpec, okButton));
                }

                JLabel pinsizeLabel = new JLabel();
                pinsizeLabel.setFont(pinsizeLabel.getFont().deriveFont(pinsizeLabel.getFont().getStyle() & ~Font.BOLD, pinsizeLabel.getFont().getSize()-2));
                String pinsizePattern = getMessage(LABEL_PINSIZE);
                pinsizeLabel.setText(MessageFormat.format(pinsizePattern, pinSpec.getLocalizedLength()));

                ////////////////////////////////////////////////////////////////
                // NON-PINPAD SPECIFIC LAYOUT SECTION
                ////////////////////////////////////////////////////////////////

                pinHorizontal = mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
                pinVertical = mainPanelLayout.createSequentialGroup();

//                if (pinLabelPos == PinLabelPosition.ABOVE) {
//                  if (changePin) {
//                      pinHorizontal
//                              .addComponent(oldPinLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                              .addComponent(oldPinField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
//                      pinVertical
//                              .addComponent(oldPinLabel)
//                              .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                              .addComponent(oldPinField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                              .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
//                  }
//                  pinHorizontal
//                          .addComponent(pinLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                          .addComponent(pinField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                          .addComponent(repeatPinLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                          .addComponent(repeatPinField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                          .addGroup(mainPanelLayout.createSequentialGroup()
//                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 0, Short.MAX_VALUE)
//                            .addComponent(pinsizeLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE));
//                  pinVertical
//                          .addComponent(pinLabel)
//                          .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                          .addComponent(pinField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                          .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                          .addComponent(repeatPinLabel)
//                          .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                          .addComponent(repeatPinField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                          .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                          .addComponent(pinsizeLabel);
//                } else {


                  if (type == DIALOG.CHANGE) {
                    pinHorizontal
                          .addGroup(mainPanelLayout.createSequentialGroup()
                            .addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                              .addComponent(oldPinLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                              .addComponent(pinLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                              .addComponent(repeatPinLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                              .addComponent(oldPinField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                              .addComponent(pinField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                              .addComponent(repeatPinField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

                    pinVertical
                          .addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(oldPinLabel)
                            .addComponent(oldPinField))
                          .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                          .addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(pinLabel)
                            .addComponent(pinField))
                          .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                          .addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(repeatPinLabel)
                            .addComponent(repeatPinField))
                          .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
                  } else if (type == DIALOG.ACTIVATE) {
                    pinHorizontal
                          .addGroup(mainPanelLayout.createSequentialGroup()
                            .addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                              .addComponent(pinLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                              .addComponent(repeatPinLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                              .addComponent(pinField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                              .addComponent(repeatPinField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

                    pinVertical
                          .addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(pinLabel)
                            .addComponent(pinField))
                          .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                          .addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(repeatPinLabel)
                            .addComponent(repeatPinField))
                          .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
                  } else { // VERIFY
                    pinHorizontal
                          .addGroup(mainPanelLayout.createSequentialGroup()
                            .addComponent(pinLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(pinField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));

                    pinVertical
                          .addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(pinLabel)
                            .addComponent(pinField))
                          .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
                  }
                  pinHorizontal
                          .addGroup(mainPanelLayout.createSequentialGroup()
                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 0, Short.MAX_VALUE)
                            .addComponent(pinsizeLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE));
                  pinVertical
                          .addComponent(pinsizeLabel);

                  GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
                  buttonPanel.setLayout(buttonPanelLayout);

                  GroupLayout.SequentialGroup buttonHorizontal = buttonPanelLayout.createSequentialGroup()
                          .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                          .addComponent(okButton, GroupLayout.PREFERRED_SIZE, buttonSize, GroupLayout.PREFERRED_SIZE);
                  GroupLayout.Group buttonVertical;

                  JButton cancelButton = new JButton();
                  cancelButton.setFont(cancelButton.getFont().deriveFont(cancelButton.getFont().getStyle() & ~java.awt.Font.BOLD));
                  cancelButton.setText(getMessage(BUTTON_CANCEL));
                  cancelButton.setActionCommand(cancelCommand);
                  cancelButton.addActionListener(cancelListener);

                  buttonHorizontal
                          .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                          .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, buttonSize, GroupLayout.PREFERRED_SIZE);
                  buttonVertical = buttonPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                          .addComponent(okButton)
                          .addComponent(cancelButton);

                  buttonPanelLayout.setHorizontalGroup(buttonHorizontal);
                  buttonPanelLayout.setVerticalGroup(buttonVertical);

                  if (oldPinField != null) {
                    oldPinField.requestFocusInWindow();
                  } else {
                    pinField.requestFocusInWindow();
                  }

                } // END NON-PINPAD SECTION

                mainPanelLayout.setHorizontalGroup(
                  mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(infoHorizontal)
                    .addGroup(pinHorizontal));

                mainPanelLayout.setVerticalGroup(
                  mainPanelLayout.createSequentialGroup()
                    .addGroup(infoVertical)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(pinVertical));

                helpLabel.requestFocus();
                contentPanel.validate();

            }
        });
  }

  @Override
  protected int initButtonSize() {
    int bs = super.initButtonSize();

    JButton b = new JButton();
    b.setText(getMessage(BUTTON_ACTIVATE));
    if (b.getPreferredSize().width > bs) {
      bs = b.getPreferredSize().width;
    }
    b.setText(getMessage(BUTTON_CHANGE));
    if (b.getPreferredSize().width > bs) {
      bs = b.getPreferredSize().width;
    }
    b.setText(getMessage(BUTTON_UNBLOCK));
    if (b.getPreferredSize().width > bs) {
      bs = b.getPreferredSize().width;
    }
    b.setText(getMessage(BUTTON_CANCEL));
    if (b.getPreferredSize().width > bs) {
      bs = b.getPreferredSize().width;
    }

    return bs;
  }

}
