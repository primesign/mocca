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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
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

/**
 * TODO pull out ResourceBundle to common superclass for activationGUI and pinMgmtGUI
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class PINManagementGUI extends ActivationGUI implements PINManagementGUIFacade {

  /** remember the pinfield to return to worker */
  protected JPasswordField oldPinField;
  /** remember the pinSpec to return to worker */
  protected PINSpec pinSpec;

  public PINManagementGUI(Container contentPane,
          Locale locale,
          Style guiStyle,
          URL backgroundImgURL,
          AbstractHelpListener helpListener) {
    super(contentPane, locale, guiStyle, backgroundImgURL, helpListener);
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
  public PINSpec getSelectedPIN() {
    return pinSpec;
  }

  @Override
  public void showPINManagementDialog(final Map<PINSpec, STATUS> pins, 
          final ActionListener activateListener,
          final String activateCmd,
          final String changeCmd,
          final String unblockCmd,
          final ActionListener cancelListener,
          final String cancelCmd) {

      log.debug("scheduling PIN managment dialog");
    
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
                  String infoPattern = cardmgmtMessages.getString(MESSAGE_PINMGMT);
                  mgmtLabel.setText(MessageFormat.format(infoPattern, pins.size()));
                } else {
                  mgmtLabel.setText(cardmgmtMessages.getString(TITLE_PINMGMT));
                }

                final PINStatusTableModel tableModel = new PINStatusTableModel(pins);
                final JTable pinStatusTable = new JTable(tableModel);
                pinStatusTable.setDefaultRenderer(PINSpec.class, new PINSpecRenderer());
                pinStatusTable.setDefaultRenderer(STATUS.class, new PINStatusRenderer(cardmgmtMessages));
                pinStatusTable.setTableHeader(null);

                pinStatusTable.addMouseMotionListener(new MouseMotionAdapter() {

                  @Override
                  public void mouseMoved(MouseEvent e) {
                    if (pinStatusTable.columnAtPoint(e.getPoint()) == 0) {
                      pinStatusTable.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    } else {
                      pinStatusTable.setCursor(Cursor.getDefaultCursor());
                    }
                  }
                });

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
                            activateButton.setText(cardmgmtMessages.getString(BUTTON_ACTIVATE));
                            activateButton.setEnabled(true);
                            activateButton.setActionCommand(activateCmd);
                          } else if (status == STATUS.BLOCKED) {
                            activateButton.setText(cardmgmtMessages.getString(BUTTON_UNBLOCK));
                            activateButton.setEnabled(true);
                            activateButton.setActionCommand(unblockCmd);
                          } else if (status == STATUS.ACTIV) {
                            activateButton.setText(cardmgmtMessages.getString(BUTTON_CHANGE));
                            activateButton.setEnabled(true);
                            activateButton.setActionCommand(changeCmd);
                          } else {
                            activateButton.setText(cardmgmtMessages.getString(BUTTON_ACTIVATE));
                            activateButton.setEnabled(false);
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
                          .addComponent(helpLabel);
                  messageVertical
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
                cancelButton.setText(messages.getString(BUTTON_CANCEL));
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

                contentPanel.validate();
        }
      });
  }

  @Override
  public void showActivatePINDialog(final PINSpec pin,
          final ActionListener okListener, final String okCommand,
          final ActionListener cancelListener, final String cancelCommand) {
    log.debug("scheduling activate pin dialog");
    showPINDialog(false, pin, okListener, okCommand, cancelListener, cancelCommand);
  }


  private void showPINDialog(final boolean changePin, final PINSpec pinSpec,
          final ActionListener okListener, final String okCommand,
          final ActionListener cancelListener, final String cancelCommand) {

      SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {

              String HELP_TOPIC, TITLE, MESSAGE_MGMT;
              if (changePin) {
                log.debug("show change pin dialog");
                HELP_TOPIC = HELP_PINMGMT;
                TITLE = TITLE_CHANGE_PIN;
                MESSAGE_MGMT = MESSAGE_CHANGE_PIN;
              } else {
                log.debug("show activate pin dialog");
                HELP_TOPIC = HELP_PINMGMT;
                TITLE = TITLE_ACTIVATE_PIN;
                MESSAGE_MGMT = MESSAGE_ACTIVATE_PIN;
                oldPinField = null;
              }

                mainPanel.removeAll();
                buttonPanel.removeAll();

                helpListener.setHelpTopic(HELP_TOPIC);

                JLabel mgmtLabel = new JLabel();
                mgmtLabel.setFont(mgmtLabel.getFont().deriveFont(mgmtLabel.getFont().getStyle() & ~java.awt.Font.BOLD));

                if (renderHeaderPanel) {
                  titleLabel.setText(cardmgmtMessages.getString(TITLE));
                  String mgmtPattern = cardmgmtMessages.getString(MESSAGE_MGMT);
                  if (shortText) {
                    mgmtLabel.setText(MessageFormat.format(mgmtPattern, "PIN"));
                  } else {
                    mgmtLabel.setText(MessageFormat.format(mgmtPattern, pinSpec.getLocalizedName()));
                  }
                } else {
                  mgmtLabel.setText(cardmgmtMessages.getString(TITLE));
                }

                JButton okButton = new JButton();
                okButton.setFont(okButton.getFont().deriveFont(okButton.getFont().getStyle() & ~java.awt.Font.BOLD));
                okButton.setText(messages.getString(BUTTON_OK));
                okButton.setEnabled(false);
                okButton.setActionCommand(okCommand);
                okButton.addActionListener(okListener);

                JLabel pinLabel = new JLabel();
                pinLabel.setFont(pinLabel.getFont().deriveFont(pinLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
                String pinLabelPattern = (changePin) ? cardmgmtMessages.getString(LABEL_NEW_PIN) : messages.getString(LABEL_PIN);
                pinLabel.setText(MessageFormat.format(pinLabelPattern, new Object[]{pinSpec.getLocalizedName()}));

                final JPasswordField repeatPinField = new JPasswordField();
                pinField = new JPasswordField();
                pinField.setText("");
                pinField.setDocument(new PINDocument(pinSpec, null));
                pinField.setActionCommand(okCommand);
                pinField.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (pinField.getPassword().length >= pinSpec.getMinLength()) {
                          repeatPinField.requestFocusInWindow();
                        }
                    }
                });
                JLabel repeatPinLabel = new JLabel();
                repeatPinLabel.setFont(pinLabel.getFont());
                String repeatPinLabelPattern = cardmgmtMessages.getString(LABEL_REPEAT_PIN);
                repeatPinLabel.setText(MessageFormat.format(repeatPinLabelPattern, new Object[]{pinSpec.getLocalizedName()}));

                repeatPinField.setText("");
                repeatPinField.setDocument(new PINDocument(pinSpec, okButton, pinField.getDocument()));
                repeatPinField.setActionCommand(okCommand);
                repeatPinField.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (pinField.getPassword().length >= pinSpec.getMinLength()) {
                            okListener.actionPerformed(e);
                        }
                    }
                });

                JLabel oldPinLabel = null;
                if (changePin) {
                  oldPinLabel = new JLabel();
                  oldPinLabel.setFont(oldPinLabel.getFont().deriveFont(oldPinLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
                  String oldPinLabelPattern = cardmgmtMessages.getString(LABEL_OLD_PIN);
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
                }
                
                JLabel pinsizeLabel = new JLabel();
                pinsizeLabel.setFont(pinsizeLabel.getFont().deriveFont(pinsizeLabel.getFont().getStyle() & ~java.awt.Font.BOLD, pinsizeLabel.getFont().getSize()-2));
                String pinsizePattern = messages.getString(LABEL_PINSIZE);
                String pinSize = String.valueOf(pinSpec.getMinLength());
                if (pinSpec.getMinLength() != pinSpec.getMaxLength()) {
                    pinSize += "-" + pinSpec.getMaxLength();
                }
                pinsizeLabel.setText(MessageFormat.format(pinsizePattern, new Object[]{pinSize}));

                GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
                mainPanel.setLayout(mainPanelLayout);

                GroupLayout.SequentialGroup infoHorizontal = mainPanelLayout.createSequentialGroup()
                          .addComponent(mgmtLabel);
                GroupLayout.ParallelGroup infoVertical = mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                          .addComponent(mgmtLabel);

                if (!renderHeaderPanel) {
                  infoHorizontal
                          .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 0, Short.MAX_VALUE)
                          .addComponent(helpLabel);
                  infoVertical
                          .addComponent(helpLabel);
                }

                GroupLayout.ParallelGroup pinHorizontal = mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
                GroupLayout.SequentialGroup pinVertical = mainPanelLayout.createSequentialGroup();

                if (pinLabelPos == PinLabelPosition.ABOVE) {
                  if (changePin) {
                    pinHorizontal
                            .addComponent(oldPinLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(oldPinField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
                    pinVertical
                            .addComponent(oldPinLabel)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(oldPinField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
                  }
                  pinHorizontal 
                          .addComponent(pinLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                          .addComponent(pinField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                          .addComponent(repeatPinLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                          .addComponent(repeatPinField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                          .addGroup(mainPanelLayout.createSequentialGroup()
                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 0, Short.MAX_VALUE)
                            .addComponent(pinsizeLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE));
                  pinVertical
                          .addComponent(pinLabel)
                          .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                          .addComponent(pinField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                          .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                          .addComponent(repeatPinLabel)
                          .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                          .addComponent(repeatPinField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                          .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                          .addComponent(pinsizeLabel);
                } else {
                  if (changePin) {
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
                          .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
                  } else {
                    pinHorizontal
                          .addGroup(mainPanelLayout.createSequentialGroup()
                            .addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                              .addComponent(pinLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                              .addComponent(repeatPinLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                              .addComponent(pinField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                              .addComponent(repeatPinField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

                  }
                  pinHorizontal
                          .addGroup(mainPanelLayout.createSequentialGroup()
                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 0, Short.MAX_VALUE)
                            .addComponent(pinsizeLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE));
                  pinVertical
                          .addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(pinLabel)
                            .addComponent(pinField))
                          .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                          .addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(repeatPinLabel)
                            .addComponent(repeatPinField))
                          .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                          .addComponent(pinsizeLabel);
                }

                mainPanelLayout.setHorizontalGroup(
                  mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(infoHorizontal)
                    .addGroup(pinHorizontal));

                mainPanelLayout.setVerticalGroup(
                  mainPanelLayout.createSequentialGroup()
                    .addGroup(infoVertical)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(pinVertical));

                GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
                buttonPanel.setLayout(buttonPanelLayout);

                GroupLayout.SequentialGroup buttonHorizontal = buttonPanelLayout.createSequentialGroup()
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(okButton, GroupLayout.PREFERRED_SIZE, buttonSize, GroupLayout.PREFERRED_SIZE);
                GroupLayout.Group buttonVertical;

                JButton cancelButton = new JButton();
                cancelButton.setFont(cancelButton.getFont().deriveFont(cancelButton.getFont().getStyle() & ~java.awt.Font.BOLD));
                cancelButton.setText(messages.getString(BUTTON_CANCEL));
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
                contentPanel.validate();

            }
        });
  }

  @Override
  public void showChangePINDialog(final PINSpec pin,
          final ActionListener okListener, final String okCommand,
          final ActionListener cancelListener, final String cancelCommand) {
    
      log.debug("scheduling change pin dialog");
      showPINDialog(true, pin, okListener, okCommand, cancelListener, cancelCommand);
  }

  @Override
  public void showUnblockPINDialog(final PINSpec pin,
          final ActionListener okListener, final String okCommand,
          final ActionListener cancelListener, final String cancelCommand) {

      log.debug("scheduling unblock PIN dialog");

      SwingUtilities.invokeLater(new Runnable() {

          @Override
            public void run() {

                log.debug("show unblock PIN dialog");

                log.error("unblock pin not supported");

                mainPanel.removeAll();
                buttonPanel.removeAll();

                if (renderHeaderPanel) {
                  titleLabel.setText(messages.getString(TITLE_ERROR));
                }

                helpListener.setHelpTopic(HELP_PINMGMT);

                String errorMsgPattern = cardmgmtMessages.getString(ERR_UNBLOCK);
                String errorMsg = MessageFormat.format(errorMsgPattern, pin.getLocalizedName());

                JLabel errorMsgLabel = new JLabel();
                errorMsgLabel.setFont(errorMsgLabel.getFont().deriveFont(errorMsgLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
                errorMsgLabel.setText(errorMsg);

                GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
                mainPanel.setLayout(mainPanelLayout);

                GroupLayout.ParallelGroup mainHorizontal = mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
                GroupLayout.SequentialGroup mainVertical = mainPanelLayout.createSequentialGroup();

                if (!renderHeaderPanel) {
                  JLabel errorTitleLabel = new JLabel();
                  errorTitleLabel.setFont(errorTitleLabel.getFont().deriveFont(errorTitleLabel.getFont().getStyle() | java.awt.Font.BOLD));
                  errorTitleLabel.setText(messages.getString(TITLE_ERROR));
                  errorTitleLabel.setForeground(ERROR_COLOR);

                  mainHorizontal
                          .addGroup(mainPanelLayout.createSequentialGroup()
                            .addComponent(errorTitleLabel)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 0, Short.MAX_VALUE)
                            .addComponent(helpLabel));
                  mainVertical
                          .addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(errorTitleLabel)
                            .addComponent(helpLabel));
                }

                mainPanelLayout.setHorizontalGroup(mainHorizontal
                        .addComponent(errorMsgLabel));
                mainPanelLayout.setVerticalGroup(mainVertical
                        .addComponent(errorMsgLabel));

                JButton okButton = new JButton();
                okButton.setFont(okButton.getFont().deriveFont(okButton.getFont().getStyle() & ~java.awt.Font.BOLD));
                okButton.setText(messages.getString(BUTTON_OK));
                okButton.setActionCommand(cancelCommand);
                okButton.addActionListener(cancelListener);

                GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
                buttonPanel.setLayout(buttonPanelLayout);

                buttonPanelLayout.setHorizontalGroup(
                  buttonPanelLayout.createSequentialGroup()
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(okButton, GroupLayout.PREFERRED_SIZE, buttonSize, GroupLayout.PREFERRED_SIZE));
                buttonPanelLayout.setVerticalGroup(
                  buttonPanelLayout.createSequentialGroup()
                    .addComponent(okButton));

                contentPanel.validate();
            }
        });
  }


}
