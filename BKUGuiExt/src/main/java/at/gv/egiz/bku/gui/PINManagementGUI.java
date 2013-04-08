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

import at.gv.egiz.bku.gui.viewer.FontProvider;
import at.gv.egiz.smcc.PinInfo;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Locale;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO pull out ResourceBundle to common superclass for activationGUI and
 * pinMgmtGUI
 * 
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class PINManagementGUI extends CardMgmtGUI implements
		PINManagementGUIFacade {

  private final Logger log = LoggerFactory.getLogger(PINManagementGUI.class);
  
  /** remember the pinfield to return to worker */
  protected JPasswordField oldPinField;
  /** remember the pinSpec to return to worker */
  protected PinInfo pinSpec;

	protected JButton cancelButton;
	protected JTable pinStatusTable;
	protected JLabel mgmtLabel;
	protected PINStatusRenderer pinStatusRenderer;
//	protected int baseTableRowHeight;
	protected JButton activateButton;

	protected JLabel pinpadLabel;
	protected JLabel oldPinLabel;
	protected JLabel repeatPinLabel;
	protected JLabel pinLabel;
	protected JPasswordField repeatPinField;
	protected JLabel pinsizeLabel;


	public PINManagementGUI(Container contentPane, Locale locale,
			URL backgroundImgURL, FontProvider fontProvider,
			HelpListener helpListener) {
		super(contentPane, locale, Style.advanced, backgroundImgURL, fontProvider,
				helpListener);
		
		cancelButton = new JButton();
		this.pinStatusRenderer = new PINStatusRenderer(cardmgmtMessages);
		this.activateButton = new JButton();

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
  public PinInfo getSelectedPinInfo() {
		return pinSpec;
	}

  @Override
  public void showPINManagementDialog(final PinInfo[] pins,
          final ActionListener activateListener,
          final String activateCmd,
          final String changeCmd,
          final String unblockCmd,
          final String verifyCmd,
          final ActionListener cancelListener,
          final String cancelCmd) {

		log.debug("Scheduling PIN managment dialog.");

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				log.debug("Show PIN management dialog.");

				mainPanel.removeAll();
				buttonPanel.removeAll();

				helpListener.setHelpTopic(HELP_PINMGMT);

				mgmtLabel = new JLabel();
				mgmtLabel.setFont(mgmtLabel.getFont().deriveFont(
						mgmtLabel.getFont().getStyle() & ~java.awt.Font.BOLD));

        if (renderHeaderPanel) {
          titleLabel.setText(getMessage(TITLE_PINMGMT));
          String infoPattern = getMessage(MESSAGE_PINMGMT);
          mgmtLabel.setText(MessageFormat.format(infoPattern, pins.length));
        } else {
          mgmtLabel.setText(getMessage(TITLE_PINMGMT));
        }

        final PINStatusTableModel tableModel = new PINStatusTableModel(pins);
        pinStatusTable = new JTable(tableModel);
//                pinStatusTable.setDefaultRenderer(PINSpec.class, new PINSpecRenderer());
                pinStatusTable.setDefaultRenderer(PinInfo.class, pinStatusRenderer);
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

				primaryFocusHolder = pinStatusTable;

				activateButton.setFont(activateButton.getFont().deriveFont(
						activateButton.getFont().getStyle()
								& ~java.awt.Font.BOLD));
				activateButton.addActionListener(activateListener);

				pinStatusTable
						.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				pinStatusTable.getSelectionModel().addListSelectionListener(
						new ListSelectionListener() {

							@Override
							public void valueChanged(final ListSelectionEvent e) {
								// invoke later to allow thread to paint
								// selection background
								SwingUtilities.invokeLater(new Runnable() {

                  @Override
                      public void run() {
                        ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                        int selectionIdx = lsm.getMinSelectionIndex();
                        if (selectionIdx >= 0) {
                          pinSpec = (PinInfo) tableModel.getValueAt(selectionIdx, 1);
                          PinInfo.STATE status = pinSpec.getState();

                          if (status == PinInfo.STATE.NOT_ACTIV) {
                            activateButton
                                    .setText(getMessage(BUTTON_ACTIVATE));
                            activateButton.setEnabled(true);
                            activateButton
                                    .setActionCommand(activateCmd);
                          } else if (status == PinInfo.STATE.BLOCKED) {
                            activateButton
                                    .setText(getMessage(BUTTON_UNBLOCK));
                            activateButton.setEnabled(true);
                            activateButton
                                    .setActionCommand(unblockCmd);
                          } else if (status == PinInfo.STATE.ACTIV) {
                            activateButton
                                    .setText(getMessage(BUTTON_CHANGE));
                            activateButton.setEnabled(true);
                            activateButton
                                    .setActionCommand(changeCmd);
                          } else if (status == PinInfo.STATE.UNKNOWN) {
                            activateButton
                                    .setText(getMessage(BUTTON_VERIFY));
                            activateButton.setEnabled(true);
                            activateButton
                                    .setActionCommand(verifyCmd);
                          }
                        }
                      }
                    });
                  }
                });

				// select first entry
				pinStatusTable.getSelectionModel().setSelectionInterval(0, 0);

				// JScrollPane pinStatusScrollPane = new
				// JScrollPane(pinStatusTable);
				//
				// GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
				// mainPanel.setLayout(mainPanelLayout);
				//
				// GroupLayout.SequentialGroup messageHorizontal =
				// mainPanelLayout.createSequentialGroup()
				// .addComponent(mgmtLabel);
				// GroupLayout.Group messageVertical =
				// mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				// .addComponent(mgmtLabel);
				// if (!renderHeaderPanel) {
				// messageHorizontal
				// .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 0,
				// Short.MAX_VALUE)
				// .addComponent(helpLabel);
				// messageVertical
				// .addComponent(helpLabel);
				// }
				//
				// mainPanelLayout.setHorizontalGroup(
				// mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				// .addGroup(messageHorizontal)
				// .addComponent(pinStatusScrollPane, 0, 0, Short.MAX_VALUE));
				//
				// mainPanelLayout.setVerticalGroup(
				// mainPanelLayout.createSequentialGroup()
				// .addGroup(messageVertical)
				// .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				// .addComponent(pinStatusScrollPane, 0, 0,
				// pinStatusTable.getPreferredSize().height+3));
				//
				// // JButton cancelButton = new JButton();
				cancelButton.setFont(cancelButton.getFont()
						.deriveFont(
								cancelButton.getFont().getStyle()
										& ~java.awt.Font.BOLD));
				cancelButton.setText(getMessage(BUTTON_CLOSE));
				cancelButton.setActionCommand(cancelCmd);
				cancelButton.addActionListener(cancelListener);

				updateMethodToRunAtResize("at.gv.egiz.bku.gui.PINManagementGUI", "renderPINManagmentTableAndButtons");
				
				renderPINManagmentTableAndButtons();

				pinStatusTable.requestFocus();
				contentPanel.validate();

        if (windowCloseAdapter != null) {
          windowCloseAdapter.registerListener(cancelListener, cancelCmd);
        }
        
				resize();

			}
		});
	}

	public void renderPINManagmentTableAndButtons() {

		// It is necessary to remove old components in order to ensure
		// the correct rendering of the status table and the button panel
		mainPanel.removeAll();
		buttonPanel.removeAll();

		JScrollPane pinStatusScrollPane = new JScrollPane(pinStatusTable);

		GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
		mainPanel.setLayout(mainPanelLayout);

		GroupLayout.SequentialGroup messageHorizontal = mainPanelLayout
				.createSequentialGroup().addComponent(mgmtLabel);
		GroupLayout.Group messageVertical = mainPanelLayout
				.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(mgmtLabel);
		if (!renderHeaderPanel) {
			messageHorizontal.addPreferredGap(
					LayoutStyle.ComponentPlacement.UNRELATED, 0,
					Short.MAX_VALUE).addComponent(helpLabel);
			messageVertical.addComponent(helpLabel);
		}

		mainPanelLayout.setHorizontalGroup(mainPanelLayout.createParallelGroup(
				GroupLayout.Alignment.LEADING).addGroup(messageHorizontal)
				.addComponent(pinStatusScrollPane, 0, 0, Short.MAX_VALUE));

		mainPanelLayout.setVerticalGroup(mainPanelLayout
				.createSequentialGroup().addGroup(messageVertical)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(pinStatusScrollPane, 0, 0,
						pinStatusTable.getPreferredSize().height + 3));

		GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
		buttonPanel.setLayout(buttonPanelLayout);

		GroupLayout.SequentialGroup buttonHorizontal = buttonPanelLayout
				.createSequentialGroup().addContainerGap(
						GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(activateButton, GroupLayout.PREFERRED_SIZE,
						buttonSize, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(cancelButton, GroupLayout.PREFERRED_SIZE,
						buttonSize, GroupLayout.PREFERRED_SIZE);

		GroupLayout.Group buttonVertical = buttonPanelLayout
				.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(activateButton).addComponent(cancelButton);

		buttonPanelLayout.setHorizontalGroup(buttonHorizontal);
		buttonPanelLayout.setVerticalGroup(buttonVertical);

	}

@Override
  public void showModifyPINDirect(DIALOG type, PinInfo pinSpec, int retries) {
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
        log.debug("Show change pin dialog.");
        title = TITLE_CHANGE_PIN;
        msg = MESSAGE_CHANGE_PINPAD_DIREKT;
      } else if (type == DIALOG.ACTIVATE) {
        log.debug("Show activate pin dialog.");
        title = TITLE_ACTIVATE_PIN;
        msg = MESSAGE_ACTIVATE_PINPAD_DIREKT;
      } else if (type == DIALOG.VERIFY) {
        log.debug("Show verify pin dialog.");
        title = TITLE_VERIFY_PINPAD;
        msg = MESSAGE_ENTERPIN_PINPAD_DIRECT;
      } else {
        log.debug("Show unblock pin dialog.");
        title = TITLE_UNBLOCK_PIN;
        msg = MESSAGE_UNBLOCK_PINPAD_DIREKT;
      }

		} else {
			log.debug("Show retry pin dialog.");
			title = TITLE_RETRY;
			msg = (retries < 2) ? MESSAGE_LAST_RETRY : MESSAGE_RETRIES;
			params = new Object[] { String.valueOf(retries) };
		}

		showMessageDialog(title, msg, params);
	}

  @Override
  public void showPINDialog(DIALOG type, PinInfo pinSpec, int retries,
          ActionListener okListener, String okCommand,
          ActionListener cancelListener, String cancelCommand) {
    showPINDialog(type, pinSpec, retries, false, okListener, okCommand,
            cancelListener, cancelCommand);
  }

  private void showPINDialog(final DIALOG type, final PinInfo pinSpec,
          final int retries, final boolean pinpad,
          final ActionListener okListener, final String okCommand,
          final ActionListener cancelListener, final String cancelCommand) {

		log.debug("Scheduling pin dialog.");

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				String HELP_TOPIC, TITLE, MESSAGE_MGMT, MESSAGE_MGMT_PARAM;
				HELP_TOPIC = HELP_PINMGMT;

				if (retries < 0) {
					if (type == DIALOG.CHANGE) {
						log.debug("Show change pin dialog.");
						TITLE = TITLE_CHANGE_PIN;
						MESSAGE_MGMT = MESSAGE_CHANGE_PIN;
					} else if (type == DIALOG.ACTIVATE) {
						log.debug("Show activate pin dialog.");
						TITLE = TITLE_ACTIVATE_PIN;
						MESSAGE_MGMT = MESSAGE_ACTIVATE_PIN;
						oldPinField = null;
					} else if (type == DIALOG.VERIFY) {
						log.debug("Show verify pin dialog.");
						TITLE = TITLE_VERIFY_PIN;
						MESSAGE_MGMT = MESSAGE_ENTERPIN;
					} else {
						log.debug("Show unblock pin dialog.");
						TITLE = TITLE_UNBLOCK_PIN;
						MESSAGE_MGMT = MESSAGE_UNBLOCK_PIN;
					}
					if (shortText) {
						MESSAGE_MGMT_PARAM = "PIN";
					} else {
						MESSAGE_MGMT_PARAM = pinSpec.getLocalizedName();
					}
				} else {
					log.debug("Show retry pin dialog.");
					TITLE = TITLE_RETRY;
					MESSAGE_MGMT = (retries < 2) ? MESSAGE_LAST_RETRY
							: MESSAGE_RETRIES;
					MESSAGE_MGMT_PARAM = String.valueOf(retries);
				}

				mainPanel.removeAll();
				buttonPanel.removeAll();

				helpListener.setHelpTopic(HELP_TOPIC);

				mgmtLabel = new JLabel();
				if (retries < 0) {
					mgmtLabel.setFont(mgmtLabel.getFont().deriveFont(
							mgmtLabel.getFont().getStyle() & ~Font.BOLD));
				} else {
					mgmtLabel.setFont(mgmtLabel.getFont().deriveFont(
							mgmtLabel.getFont().getStyle() | Font.BOLD));
					mgmtLabel.setForeground(ERROR_COLOR);
					helpListener.setHelpTopic(HELP_RETRY);
				}

				if (renderHeaderPanel) {
					titleLabel.setText(getMessage(TITLE));
					String mgmtPattern = getMessage(MESSAGE_MGMT);
					mgmtLabel.setText(MessageFormat.format(mgmtPattern,
							MESSAGE_MGMT_PARAM));
				} else {
					mgmtLabel.setText(getMessage(TITLE));
				}

				// //////////////////////////////////////////////////////////////
				// COMMON LAYOUT SECTION
				// //////////////////////////////////////////////////////////////

				GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
				mainPanel.setLayout(mainPanelLayout);

				GroupLayout.SequentialGroup infoHorizontal = mainPanelLayout
						.createSequentialGroup().addComponent(mgmtLabel);
				GroupLayout.ParallelGroup infoVertical = mainPanelLayout
						.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(mgmtLabel);

				if (!renderHeaderPanel) {
					infoHorizontal.addPreferredGap(
							LayoutStyle.ComponentPlacement.UNRELATED, 0,
							Short.MAX_VALUE).addComponent(helpLabel);
					infoVertical.addComponent(helpLabel);
				}

				GroupLayout.ParallelGroup pinHorizontal;
				GroupLayout.SequentialGroup pinVertical;

				if (pinpad) {
					pinpadLabel = new JLabel();
					pinpadLabel.setFont(mgmtLabel.getFont().deriveFont(
							mgmtLabel.getFont().getStyle() & ~Font.BOLD));
					String pinpadPattern = getMessage(MESSAGE_ENTERPIN_PINPAD);
					pinpadLabel.setText(MessageFormat.format(pinpadPattern,
							new Object[] { pinSpec.getLocalizedName(),
									pinSpec.getLocalizedLength() }));

					pinHorizontal = mainPanelLayout.createParallelGroup(
							GroupLayout.Alignment.LEADING).addComponent(
							pinpadLabel);
					pinVertical = mainPanelLayout.createSequentialGroup()
							.addComponent(pinpadLabel);
				} else {

					okButton = new JButton();
					okButton.setFont(okButton.getFont().deriveFont(
							okButton.getFont().getStyle() & ~Font.BOLD));
					okButton.setText(getMessage(BUTTON_OK));
					okButton.setEnabled(pinSpec.getMinLength() <= 0);
					okButton.setActionCommand(okCommand);
					okButton.addActionListener(okListener);

					pinLabel = new JLabel();
					pinLabel.setFont(pinLabel.getFont().deriveFont(
							pinLabel.getFont().getStyle() & ~Font.BOLD));
					String pinLabelPattern = (type == DIALOG.CHANGE || type == DIALOG.UNBLOCK) ? getMessage(LABEL_NEW_PIN)
							: getMessage(LABEL_PIN);
					pinLabel.setText(MessageFormat.format(pinLabelPattern,
							new Object[] { pinSpec.getLocalizedName() }));

					repeatPinField = new JPasswordField();
					pinField = new JPasswordField();
					pinField.setText("");
					pinField.setActionCommand(okCommand);
					pinField.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							if (pinField.getPassword().length >= pinSpec
									.getMinLength()) {
								if (type == DIALOG.VERIFY) {
									okListener.actionPerformed(e);
								} else {
									repeatPinField.requestFocusInWindow();
								}
							}
						}
					});

					if (type != DIALOG.VERIFY) {
						repeatPinLabel = new JLabel();
						repeatPinLabel.setFont(pinLabel.getFont());
						String repeatPinLabelPattern = getMessage(LABEL_REPEAT_PIN);
						repeatPinLabel.setText(MessageFormat.format(
								repeatPinLabelPattern, new Object[] { pinSpec
										.getLocalizedName() }));

						repeatPinField.setText("");
						repeatPinField.setActionCommand(okCommand);
						repeatPinField.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e) {
								if (okButton.isEnabled()) {
									okListener.actionPerformed(e);
								}
							}
						});

						if (type == DIALOG.CHANGE || type == DIALOG.UNBLOCK) {
							oldPinLabel = new JLabel();
							oldPinLabel.setFont(oldPinLabel.getFont()
									.deriveFont(
											oldPinLabel.getFont().getStyle()
													& ~java.awt.Font.BOLD));
							String oldPinLabelPattern = getMessage((type == DIALOG.CHANGE) ? LABEL_OLD_PIN
									: LABEL_PUK);
							oldPinLabel.setText(MessageFormat.format(
									oldPinLabelPattern, new Object[] { pinSpec
											.getLocalizedName() }));

							oldPinField = new JPasswordField();
							oldPinField.setText("");
							oldPinField.setActionCommand(okCommand);
							oldPinField.addActionListener(new ActionListener() {

								@Override
								public void actionPerformed(ActionEvent e) {
									if (oldPinField.getPassword().length >= pinSpec
											.getMinLength()) {
										pinField.requestFocusInWindow();
									}
								}
							});

							ExtendedPinDocument oldPinDocument = new ExtendedPinDocument(
									pinSpec.getMinLength(), pinSpec
											.getMaxLength(), pinSpec
											.getRegexpPattern(), okButton);
							ComparePinDocument newPinDocument = new ComparePinDocument(
									pinSpec.getRecMinLength(), pinSpec
											.getRecMaxLength(), pinSpec
											.getRegexpPattern(), okButton);
							ComparePinDocument confirmPinDocument = new ComparePinDocument(
									pinSpec.getRecMinLength(), pinSpec
											.getRecMaxLength(), pinSpec
											.getRegexpPattern(), okButton);

							oldPinDocument.newPIN = newPinDocument;
							oldPinDocument.confirmPIN = confirmPinDocument;

							newPinDocument.compareTo = confirmPinDocument;
							newPinDocument.currentPIN = oldPinDocument;
							confirmPinDocument.compareTo = newPinDocument;
							confirmPinDocument.currentPIN = oldPinDocument;

							oldPinField.setDocument(oldPinDocument);
							pinField.setDocument(newPinDocument);
							repeatPinField.setDocument(confirmPinDocument);

							primaryFocusHolder = oldPinField;
							
						} else {
							// else -> ACTIVATE (not verify, not change)
							ComparePinDocument newPinDocument = new ComparePinDocument(
									pinSpec.getRecMinLength(), pinSpec
											.getRecMaxLength(), pinSpec
											.getRegexpPattern(), okButton);
							ComparePinDocument confirmPinDocument = new ComparePinDocument(
									pinSpec.getRecMinLength(), pinSpec
											.getRecMaxLength(), pinSpec
											.getRegexpPattern(), okButton);

							newPinDocument.compareTo = confirmPinDocument;
							confirmPinDocument.compareTo = newPinDocument;

							pinField.setDocument(newPinDocument);
							repeatPinField.setDocument(confirmPinDocument);
							
							primaryFocusHolder = pinField;
						}
					} else {
						// VERIFY
						pinField.setDocument(new PINDocument(pinSpec
								.getMinLength(), pinSpec.getMaxLength(),
								pinSpec.getRegexpPattern(), okButton));
						
						primaryFocusHolder = pinField;
					}

					pinsizeLabel = new JLabel();
					pinsizeLabel.setFont(pinsizeLabel.getFont().deriveFont(
							pinsizeLabel.getFont().getStyle() & ~Font.BOLD,
							pinsizeLabel.getFont().getSize() - 2));
					String pinsizePattern = getMessage(LABEL_PINSIZE);
					pinsizeLabel.setText(MessageFormat.format(pinsizePattern,
							pinSpec.getLocalizedLength()));

					// //////////////////////////////////////////////////////////////
					// NON-PINPAD SPECIFIC LAYOUT SECTION
					// //////////////////////////////////////////////////////////////

					pinHorizontal = mainPanelLayout
							.createParallelGroup(GroupLayout.Alignment.LEADING);
					pinVertical = mainPanelLayout.createSequentialGroup();

					// if (pinLabelPos == PinLabelPosition.ABOVE) {
					// if (changePin) {
					// pinHorizontal
					// .addComponent(oldPinLabel, GroupLayout.PREFERRED_SIZE,
					// GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					// .addComponent(oldPinField, GroupLayout.PREFERRED_SIZE,
					// GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
					// pinVertical
					// .addComponent(oldPinLabel)
					// .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
					// .addComponent(oldPinField, GroupLayout.PREFERRED_SIZE,
					// GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					// .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
					// }
					// pinHorizontal
					// .addComponent(pinLabel, GroupLayout.PREFERRED_SIZE,
					// GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					// .addComponent(pinField, GroupLayout.PREFERRED_SIZE,
					// GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					// .addComponent(repeatPinLabel, GroupLayout.PREFERRED_SIZE,
					// GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					// .addComponent(repeatPinField, GroupLayout.PREFERRED_SIZE,
					// GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					// .addGroup(mainPanelLayout.createSequentialGroup()
					// .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED,
					// 0, Short.MAX_VALUE)
					// .addComponent(pinsizeLabel, GroupLayout.PREFERRED_SIZE,
					// GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE));
					// pinVertical
					// .addComponent(pinLabel)
					// .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
					// .addComponent(pinField, GroupLayout.PREFERRED_SIZE,
					// GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					// .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
					// .addComponent(repeatPinLabel)
					// .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
					// .addComponent(repeatPinField, GroupLayout.PREFERRED_SIZE,
					// GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					// .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
					// .addComponent(pinsizeLabel);
					// } else {

//<<<<<<< .mine
//                if (windowCloseAdapter != null) {
//                  windowCloseAdapter.registerListener(cancelListener, cancelCommand);
//                }
//=======
					if (type == DIALOG.CHANGE || type == DIALOG.UNBLOCK) {
						pinHorizontal
								.addGroup(mainPanelLayout
										.createSequentialGroup()
										.addGroup(
												mainPanelLayout
														.createParallelGroup(
																GroupLayout.Alignment.LEADING)
														.addComponent(
																oldPinLabel,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE)
														.addComponent(
																pinLabel,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE)
														.addComponent(
																repeatPinLabel,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												mainPanelLayout
														.createParallelGroup(
																GroupLayout.Alignment.LEADING)
														.addComponent(
																oldPinField,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)
														.addComponent(
																pinField,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)
														.addComponent(
																repeatPinField,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)));
//>>>>>>> .r684

						pinVertical.addGroup(
								mainPanelLayout.createParallelGroup(
										GroupLayout.Alignment.BASELINE)
										.addComponent(oldPinLabel)
										.addComponent(oldPinField))
								.addPreferredGap(
										LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(
										mainPanelLayout.createParallelGroup(
												GroupLayout.Alignment.BASELINE)
												.addComponent(pinLabel)
												.addComponent(pinField))
								.addPreferredGap(
										LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(
										mainPanelLayout.createParallelGroup(
												GroupLayout.Alignment.BASELINE)
												.addComponent(repeatPinLabel)
												.addComponent(repeatPinField))
								.addPreferredGap(
										LayoutStyle.ComponentPlacement.RELATED);
					} else if (type == DIALOG.ACTIVATE) {
						pinHorizontal
								.addGroup(mainPanelLayout
										.createSequentialGroup()
										.addGroup(
												mainPanelLayout
														.createParallelGroup(
																GroupLayout.Alignment.LEADING)
														.addComponent(
																pinLabel,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE)
														.addComponent(
																repeatPinLabel,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												mainPanelLayout
														.createParallelGroup(
																GroupLayout.Alignment.LEADING)
														.addComponent(
																pinField,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)
														.addComponent(
																repeatPinField,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)));

						pinVertical.addGroup(
								mainPanelLayout.createParallelGroup(
										GroupLayout.Alignment.BASELINE)
										.addComponent(pinLabel).addComponent(
												pinField)).addPreferredGap(
								LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(
										mainPanelLayout.createParallelGroup(
												GroupLayout.Alignment.BASELINE)
												.addComponent(repeatPinLabel)
												.addComponent(repeatPinField))
								.addPreferredGap(
										LayoutStyle.ComponentPlacement.RELATED);
					} else { // VERIFY
						pinHorizontal.addGroup(mainPanelLayout
								.createSequentialGroup().addComponent(pinLabel,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(
										LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(pinField,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE));

						pinVertical.addGroup(
								mainPanelLayout.createParallelGroup(
										GroupLayout.Alignment.BASELINE)
										.addComponent(pinLabel).addComponent(
												pinField)).addPreferredGap(
								LayoutStyle.ComponentPlacement.RELATED);
					}
					pinHorizontal.addGroup(mainPanelLayout
							.createSequentialGroup().addPreferredGap(
									LayoutStyle.ComponentPlacement.UNRELATED,
									0, Short.MAX_VALUE).addComponent(
									pinsizeLabel, GroupLayout.PREFERRED_SIZE,
									GroupLayout.DEFAULT_SIZE,
									GroupLayout.PREFERRED_SIZE));
					pinVertical.addComponent(pinsizeLabel);

					cancelButton = new JButton();
					cancelButton.setFont(cancelButton.getFont().deriveFont(
							cancelButton.getFont().getStyle()
									& ~java.awt.Font.BOLD));
					cancelButton.setText(getMessage(BUTTON_CANCEL));
					cancelButton.setActionCommand(cancelCommand);
					cancelButton.addActionListener(cancelListener);
	
					
					updateMethodToRunAtResize("at.gv.egiz.bku.gui.PINManagementGUI", "renderPINDialogButtonPanel");
					
					renderPINDialogButtonPanel();

					if (oldPinField != null) {
						oldPinField.requestFocusInWindow();
					} else {
						pinField.requestFocusInWindow();
					}

				} // END NON-PINPAD SECTION

				mainPanelLayout.setHorizontalGroup(mainPanelLayout
						.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(infoHorizontal).addGroup(pinHorizontal));

				mainPanelLayout
						.setVerticalGroup(mainPanelLayout
								.createSequentialGroup().addGroup(infoVertical)
								.addPreferredGap(
										LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(pinVertical));

				contentPanel.validate();

        if (windowCloseAdapter != null) {
          windowCloseAdapter.registerListener(cancelListener, cancelCommand);
        }
        
				resize();

			}
		});
	}
  
  @Override
  public void showPUKDialog(DIALOG type, PinInfo pinSpec, PinInfo pukSpec, int retries,
          ActionListener okListener, String okCommand,
          ActionListener cancelListener, String cancelCommand) {
	  showPUKDialog(type, pinSpec, pukSpec, retries, false, okListener, okCommand,
            cancelListener, cancelCommand);
  }

  private void showPUKDialog(final DIALOG type, final PinInfo pinSpec, final PinInfo pukSpec,
          final int retries, final boolean pinpad,
          final ActionListener okListener, final String okCommand,
          final ActionListener cancelListener, final String cancelCommand) {

		log.debug("Scheduling puk dialog.");

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				String HELP_TOPIC, TITLE, MESSAGE_MGMT, MESSAGE_MGMT_PARAM;
				HELP_TOPIC = HELP_PINMGMT;

				if (retries < 0) {
					if (type == DIALOG.CHANGE) {
						log.debug("Show change pin dialog.");
						TITLE = TITLE_CHANGE_PIN;
						MESSAGE_MGMT = MESSAGE_UNBLOCK_PIN;
					} else if(type == DIALOG.UNBLOCK) {
						log.debug("Show unblock pin dialog.");
						TITLE = TITLE_UNBLOCK_PIN;
						MESSAGE_MGMT = MESSAGE_UNBLOCK_PIN;
					} else {
						log.info("PUK Dialog may only be used for change and unblocking!");
						return;
					}
					if (shortText) {
						MESSAGE_MGMT_PARAM = "PIN";
					} else {
						MESSAGE_MGMT_PARAM = pinSpec.getLocalizedName();
					}
				} else {
					log.debug("Show retry pin dialog.");
					TITLE = TITLE_RETRY;
					MESSAGE_MGMT = (retries < 2) ? MESSAGE_LAST_RETRY
							: MESSAGE_RETRIES;
					MESSAGE_MGMT_PARAM = String.valueOf(retries);
				}

				mainPanel.removeAll();
				buttonPanel.removeAll();

				helpListener.setHelpTopic(HELP_TOPIC);

				mgmtLabel = new JLabel();
				if (retries < 0) {
					mgmtLabel.setFont(mgmtLabel.getFont().deriveFont(
							mgmtLabel.getFont().getStyle() & ~Font.BOLD));
				} else {
					mgmtLabel.setFont(mgmtLabel.getFont().deriveFont(
							mgmtLabel.getFont().getStyle() | Font.BOLD));
					mgmtLabel.setForeground(ERROR_COLOR);
					helpListener.setHelpTopic(HELP_RETRY);
				}

				if (renderHeaderPanel) {
					titleLabel.setText(getMessage(TITLE));
					String mgmtPattern = getMessage(MESSAGE_MGMT);
					mgmtLabel.setText(MessageFormat.format(mgmtPattern,
							MESSAGE_MGMT_PARAM));
				} else {
					mgmtLabel.setText(getMessage(TITLE));
				}

				// //////////////////////////////////////////////////////////////
				// COMMON LAYOUT SECTION
				// //////////////////////////////////////////////////////////////

				GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
				mainPanel.setLayout(mainPanelLayout);

				GroupLayout.SequentialGroup infoHorizontal = mainPanelLayout
						.createSequentialGroup().addComponent(mgmtLabel);
				GroupLayout.ParallelGroup infoVertical = mainPanelLayout
						.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(mgmtLabel);

				if (!renderHeaderPanel) {
					infoHorizontal.addPreferredGap(
							LayoutStyle.ComponentPlacement.UNRELATED, 0,
							Short.MAX_VALUE).addComponent(helpLabel);
					infoVertical.addComponent(helpLabel);
				}

				GroupLayout.ParallelGroup pinHorizontal;
				GroupLayout.SequentialGroup pinVertical;
/* Currently not supporting pinpads!!
				if (pinpad) {
					pinpadLabel = new JLabel();
					pinpadLabel.setFont(mgmtLabel.getFont().deriveFont(
							mgmtLabel.getFont().getStyle() & ~Font.BOLD));
					String pinpadPattern = getMessage(MESSAGE_ENTERPIN_PINPAD);
					pinpadLabel.setText(MessageFormat.format(pinpadPattern,
							new Object[] { pinSpec.getLocalizedName(),
									pinSpec.getLocalizedLength() }));

					pinHorizontal = mainPanelLayout.createParallelGroup(
							GroupLayout.Alignment.LEADING).addComponent(
							pinpadLabel);
					pinVertical = mainPanelLayout.createSequentialGroup()
							.addComponent(pinpadLabel);
				} else {
*/
					okButton = new JButton();
					okButton.setFont(okButton.getFont().deriveFont(
							okButton.getFont().getStyle() & ~Font.BOLD));
					okButton.setText(getMessage(BUTTON_OK));
					okButton.setEnabled(pinSpec.getMinLength() <= 0);
					okButton.setActionCommand(okCommand);
					okButton.addActionListener(okListener);

					pinLabel = new JLabel();
					pinLabel.setFont(pinLabel.getFont().deriveFont(
							pinLabel.getFont().getStyle() & ~Font.BOLD));
					String pinLabelPattern = getMessage(LABEL_NEW_PIN);
					
					pinLabel.setText(MessageFormat.format(pinLabelPattern,
							new Object[] { pinSpec.getLocalizedName() }));

					repeatPinField = new JPasswordField();
					pinField = new JPasswordField();
					pinField.setText("");
					pinField.setActionCommand(okCommand);
					pinField.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							if (pinField.getPassword().length >= pinSpec
									.getMinLength()) {
								if (type == DIALOG.VERIFY) {
									okListener.actionPerformed(e);
								} else {
									repeatPinField.requestFocusInWindow();
								}
							}
						}
					});

						repeatPinLabel = new JLabel();
						repeatPinLabel.setFont(pinLabel.getFont());
						String repeatPinLabelPattern = getMessage(LABEL_REPEAT_PIN);
						repeatPinLabel.setText(MessageFormat.format(
								repeatPinLabelPattern, new Object[] { pinSpec
										.getLocalizedName() }));

						repeatPinField.setText("");
						repeatPinField.setActionCommand(okCommand);
						repeatPinField.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e) {
								if (okButton.isEnabled()) {
									okListener.actionPerformed(e);
								}
							}
						});

							oldPinLabel = new JLabel();
							oldPinLabel.setFont(oldPinLabel.getFont()
									.deriveFont(
											oldPinLabel.getFont().getStyle()
													& ~java.awt.Font.BOLD));
							String oldPinLabelPattern = getMessage(LABEL_PUK);
							oldPinLabel.setText(MessageFormat.format(
									oldPinLabelPattern, new Object[] { pinSpec
											.getLocalizedName() }));

							oldPinField = new JPasswordField();
							oldPinField.setText("");
							oldPinField.setActionCommand(okCommand);
							oldPinField.addActionListener(new ActionListener() {

								@Override
								public void actionPerformed(ActionEvent e) {
									if (oldPinField.getPassword().length >= pinSpec
											.getMinLength()) {
										pinField.requestFocusInWindow();
									}
								}
							});

							ExtendedPinDocument oldPinDocument = new ExtendedPinDocument(
									pukSpec.getMinLength(), pukSpec
											.getMaxLength(), pukSpec
											.getRegexpPattern(), okButton);
							ComparePinDocument newPinDocument = new ComparePinDocument(
									pinSpec.getRecMinLength(), pinSpec
											.getRecMaxLength(), pinSpec
											.getRegexpPattern(), okButton);
							ComparePinDocument confirmPinDocument = new ComparePinDocument(
									pinSpec.getRecMinLength(), pinSpec
											.getRecMaxLength(), pinSpec
											.getRegexpPattern(), okButton);

							oldPinDocument.newPIN = newPinDocument;
							oldPinDocument.confirmPIN = confirmPinDocument;

							newPinDocument.compareTo = confirmPinDocument;
							newPinDocument.currentPIN = oldPinDocument;
							confirmPinDocument.compareTo = newPinDocument;
							confirmPinDocument.currentPIN = oldPinDocument;

							oldPinField.setDocument(oldPinDocument);
							pinField.setDocument(newPinDocument);
							repeatPinField.setDocument(confirmPinDocument);

							primaryFocusHolder = oldPinField;
							
						


					pinsizeLabel = new JLabel();
					pinsizeLabel.setFont(pinsizeLabel.getFont().deriveFont(
							pinsizeLabel.getFont().getStyle() & ~Font.BOLD,
							pinsizeLabel.getFont().getSize() - 2));
					String pinsizePattern = getMessage(LABEL_PINSIZE);
					pinsizeLabel.setText(MessageFormat.format(pinsizePattern,
							pinSpec.getLocalizedLength()));

					// //////////////////////////////////////////////////////////////
					// NON-PINPAD SPECIFIC LAYOUT SECTION
					// //////////////////////////////////////////////////////////////

					pinHorizontal = mainPanelLayout
							.createParallelGroup(GroupLayout.Alignment.LEADING);
					pinVertical = mainPanelLayout.createSequentialGroup();

					if (type == DIALOG.CHANGE || type == DIALOG.UNBLOCK) {
						pinHorizontal
								.addGroup(mainPanelLayout
										.createSequentialGroup()
										.addGroup(
												mainPanelLayout
														.createParallelGroup(
																GroupLayout.Alignment.LEADING)
														.addComponent(
																oldPinLabel,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE)
														.addComponent(
																pinLabel,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE)
														.addComponent(
																repeatPinLabel,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												mainPanelLayout
														.createParallelGroup(
																GroupLayout.Alignment.LEADING)
														.addComponent(
																oldPinField,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)
														.addComponent(
																pinField,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)
														.addComponent(
																repeatPinField,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)));
//>>>>>>> .r684

						pinVertical.addGroup(
								mainPanelLayout.createParallelGroup(
										GroupLayout.Alignment.BASELINE)
										.addComponent(oldPinLabel)
										.addComponent(oldPinField))
								.addPreferredGap(
										LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(
										mainPanelLayout.createParallelGroup(
												GroupLayout.Alignment.BASELINE)
												.addComponent(pinLabel)
												.addComponent(pinField))
								.addPreferredGap(
										LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(
										mainPanelLayout.createParallelGroup(
												GroupLayout.Alignment.BASELINE)
												.addComponent(repeatPinLabel)
												.addComponent(repeatPinField))
								.addPreferredGap(
										LayoutStyle.ComponentPlacement.RELATED);
					} else if (type == DIALOG.ACTIVATE) {
						pinHorizontal
								.addGroup(mainPanelLayout
										.createSequentialGroup()
										.addGroup(
												mainPanelLayout
														.createParallelGroup(
																GroupLayout.Alignment.LEADING)
														.addComponent(
																pinLabel,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE)
														.addComponent(
																repeatPinLabel,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												mainPanelLayout
														.createParallelGroup(
																GroupLayout.Alignment.LEADING)
														.addComponent(
																pinField,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)
														.addComponent(
																repeatPinField,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)));

						pinVertical.addGroup(
								mainPanelLayout.createParallelGroup(
										GroupLayout.Alignment.BASELINE)
										.addComponent(pinLabel).addComponent(
												pinField)).addPreferredGap(
								LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(
										mainPanelLayout.createParallelGroup(
												GroupLayout.Alignment.BASELINE)
												.addComponent(repeatPinLabel)
												.addComponent(repeatPinField))
								.addPreferredGap(
										LayoutStyle.ComponentPlacement.RELATED);
					} else { // VERIFY
						pinHorizontal.addGroup(mainPanelLayout
								.createSequentialGroup().addComponent(pinLabel,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(
										LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(pinField,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE));

						pinVertical.addGroup(
								mainPanelLayout.createParallelGroup(
										GroupLayout.Alignment.BASELINE)
										.addComponent(pinLabel).addComponent(
												pinField)).addPreferredGap(
								LayoutStyle.ComponentPlacement.RELATED);
					}
					pinHorizontal.addGroup(mainPanelLayout
							.createSequentialGroup().addPreferredGap(
									LayoutStyle.ComponentPlacement.UNRELATED,
									0, Short.MAX_VALUE).addComponent(
									pinsizeLabel, GroupLayout.PREFERRED_SIZE,
									GroupLayout.DEFAULT_SIZE,
									GroupLayout.PREFERRED_SIZE));
					pinVertical.addComponent(pinsizeLabel);

					cancelButton = new JButton();
					cancelButton.setFont(cancelButton.getFont().deriveFont(
							cancelButton.getFont().getStyle()
									& ~java.awt.Font.BOLD));
					cancelButton.setText(getMessage(BUTTON_CANCEL));
					cancelButton.setActionCommand(cancelCommand);
					cancelButton.addActionListener(cancelListener);
	
					
					updateMethodToRunAtResize("at.gv.egiz.bku.gui.PINManagementGUI", "renderPINDialogButtonPanel");
					
					renderPINDialogButtonPanel();

					if (oldPinField != null) {
						oldPinField.requestFocusInWindow();
					} else {
						pinField.requestFocusInWindow();
					}

				//} // END NON-PINPAD SECTION

				mainPanelLayout.setHorizontalGroup(mainPanelLayout
						.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(infoHorizontal).addGroup(pinHorizontal));

				mainPanelLayout
						.setVerticalGroup(mainPanelLayout
								.createSequentialGroup().addGroup(infoVertical)
								.addPreferredGap(
										LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(pinVertical));

				contentPanel.validate();

        if (windowCloseAdapter != null) {
          windowCloseAdapter.registerListener(cancelListener, cancelCommand);
        }
        
				resize();

			}
		});
	}
  
  
	public void renderPINDialogButtonPanel() {

		GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
		buttonPanel.setLayout(buttonPanelLayout);

		GroupLayout.SequentialGroup buttonHorizontal = buttonPanelLayout
				.createSequentialGroup().addContainerGap(
						GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(okButton, GroupLayout.PREFERRED_SIZE, buttonSize,
						GroupLayout.PREFERRED_SIZE);
		GroupLayout.Group buttonVertical;


		buttonHorizontal
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(cancelButton, GroupLayout.PREFERRED_SIZE,
						buttonSize, GroupLayout.PREFERRED_SIZE);
		buttonVertical = buttonPanelLayout.createParallelGroup(
				GroupLayout.Alignment.BASELINE).addComponent(okButton)
				.addComponent(cancelButton);

		buttonPanelLayout.setHorizontalGroup(buttonHorizontal);
		buttonPanelLayout.setVerticalGroup(buttonVertical);

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

  @Override
  public void showEnterCurrentPIN(DIALOG type, PinInfo pinSpec, int retries) {
    String title, message;
//    Object[] params = null;

    if (type == PINManagementGUIFacade.DIALOG.VERIFY) {
      title = PINManagementGUIFacade.TITLE_VERIFY_PINPAD;
      message = BKUGUIFacade.MESSAGE_ENTERPIN_PINPAD;
    } else if (type == PINManagementGUIFacade.DIALOG.ACTIVATE) {
      title = PINManagementGUIFacade.TITLE_ACTIVATE_PIN;
      message = PINManagementGUIFacade.MESSAGE_ACTIVATE_PINPAD_CURRENT;
    } else if (type == PINManagementGUIFacade.DIALOG.CHANGE) {
      title = PINManagementGUIFacade.TITLE_CHANGE_PIN;
      message = PINManagementGUIFacade.MESSAGE_CHANGE_PINPAD_CURRENT;
    } else { //if (type == DIALOG.UNBLOCK) {
      title = PINManagementGUIFacade.TITLE_UNBLOCK_PIN;
      message = PINManagementGUIFacade.MESSAGE_UNBLOCK_PINPAD_CURRENT;
    }
    showEnterPIN(pinSpec, retries, title, message, null);
  }

  @Override
  public void showEnterNewPIN(DIALOG type, PinInfo pinSpec) {
    String title, message;
    if (type == PINManagementGUIFacade.DIALOG.ACTIVATE) {
      title = PINManagementGUIFacade.TITLE_ACTIVATE_PIN;
      message = PINManagementGUIFacade.MESSAGE_ACTIVATE_PINPAD_NEW;
    } else if (type == PINManagementGUIFacade.DIALOG.CHANGE) {
      title = PINManagementGUIFacade.TITLE_CHANGE_PIN;
      message = PINManagementGUIFacade.MESSAGE_CHANGE_PINPAD_NEW;
    } else if (type == DIALOG.UNBLOCK) {
      title = PINManagementGUIFacade.TITLE_UNBLOCK_PIN;
      message = PINManagementGUIFacade.MESSAGE_UNBLOCK_PINPAD_NEW;
    } else {
      log.error("EnterNewPIN not supported for dialog type {}.", type);
      showErrorDialog(ERR_UNKNOWN, null);
      return;
    }
    showEnterPIN(pinSpec, -1, title, message, null);
  }

  @Override
  public void showConfirmNewPIN(DIALOG type, PinInfo pinSpec) {
    String title, message;
    if (type == PINManagementGUIFacade.DIALOG.ACTIVATE) {
      title = PINManagementGUIFacade.TITLE_ACTIVATE_PIN;
      message = PINManagementGUIFacade.MESSAGE_ACTIVATE_PINPAD_CONFIRM;
    } else if (type == PINManagementGUIFacade.DIALOG.CHANGE) {
      title = PINManagementGUIFacade.TITLE_CHANGE_PIN;
      message = PINManagementGUIFacade.MESSAGE_CHANGE_PINPAD_CONFIRM;
    } else if (type == DIALOG.UNBLOCK) {
      title = PINManagementGUIFacade.TITLE_UNBLOCK_PIN;
      message = PINManagementGUIFacade.MESSAGE_UNBLOCK_PINPAD_CONFIRM;
    } else {
      log.error("EnterNewPIN not supported for dialog type {}.", type);
      showErrorDialog(ERR_UNKNOWN, null);
      return;
    }
    showEnterPIN(pinSpec, -1, title, message, null);
  }

	@Override
	public void resize() {

		log.debug("Resizing PINManagementApplet ...");

		float factor = getResizeFactor();

		if (mgmtLabel != null) {

			mgmtLabel.setFont(mgmtLabel.getFont().deriveFont(
					(float) (baseFontSize * factor)));
		}

		if (pinStatusRenderer != null) {

			pinStatusRenderer.setFontSize((int) (baseFontSize * factor));
		}

		if (pinStatusTable != null) {

			pinStatusTable.setRowHeight((int) (baseTableRowHeight * factor));
      pinStatusTable.setFont(pinStatusTable.getFont().deriveFont(
					(float) (baseFontSize * factor)));

		}

		if (activateButton != null) {

			activateButton.setFont(activateButton.getFont().deriveFont(
					(float) (baseFontSize * factor)));

		}

		if (cancelButton != null) {

			cancelButton.setFont(cancelButton.getFont().deriveFont(
					(float) (baseFontSize * factor)));

		}


		if (pinpadLabel != null) {
			pinpadLabel.setFont(pinpadLabel.getFont().deriveFont(
					(float) (baseFontSize * factor)));
		}

		if (okButton != null) {
			okButton.setFont(okButton.getFont().deriveFont(
					(float) (baseFontSize * factor)));
		}

		if (pinLabel != null) {
			pinLabel.setFont(pinLabel.getFont().deriveFont(
					(float) (baseFontSize * factor)));
		}

		if (repeatPinLabel != null) {
			repeatPinLabel.setFont(repeatPinLabel.getFont().deriveFont(
					(float) (baseFontSize * factor)));
		}

		if (oldPinLabel != null) {
			oldPinLabel.setFont(oldPinLabel.getFont().deriveFont(
					(float) (baseFontSize * factor)));
		}

		if (pinField != null) {
			pinField.setFont(pinField.getFont().deriveFont(
					(float) (baseFontSize * factor)));
		}

		if (repeatPinField != null) {

			repeatPinField.setFont(repeatPinField.getFont().deriveFont(
					(float) (baseFontSize * factor)));
		}

		if (oldPinField != null) {

			oldPinField.setFont(oldPinField.getFont().deriveFont(
					(float) (baseFontSize * factor)));
		}

		if (pinsizeLabel != null) {
			pinsizeLabel.setFont(pinsizeLabel.getFont().deriveFont(
					(float) ((baseFontSize-2) * factor)));
		}

		super.resize();


	}

}
