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
import at.gv.egiz.stal.HashDataInput;
import at.gv.egiz.stal.impl.ByteArrayHashDataInput;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.CellRendererPane;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author clemens
 */
public class BKUGUIImpl implements BKUGUIFacade {
    
    private static final Log log = LogFactory.getLog(BKUGUIImpl.class);

    protected HelpMouseListener helpListener;
    
    protected Container contentPane;
    protected ResourceBundle messages;
    /** left and right side main panels */
    protected JPanel iconPanel;
    protected JPanel contentPanel;
    /** right side content panels and layouts */
    protected JPanel headerPanel;
    protected JPanel mainPanel;
    protected JPanel buttonPanel;
    /** right side fixed labels  */
    protected JLabel titleLabel;
    protected JLabel helpLabel;
    /** remember the pinfield to return to worker */
    protected JPasswordField pinField;

    protected int buttonSize;
    
    /** gui style config (default 'simple') */
    protected boolean renderHeaderPanel = false;
    protected boolean renderIconPanel = false;
    protected boolean renderCancelButton = false;

    /**
     * set contentPane
     * init message bundle
     * configure the style 
     * register the help listener
     * create GUI (on event-dispatching thread)
     * 
     * @param contentPane
     * @param locale
     * @param guiStyle
     * @param background
     * @param helpListener
     */
    public BKUGUIImpl(Container contentPane, 
            Locale locale, 
            Style guiStyle, 
            URL background, 
            ActionListener helpListener) {
      this.contentPane = contentPane;

      if (locale != null) {
          messages = ResourceBundle.getBundle(MESSAGES_BUNDLE, locale);
      } else {
          messages = ResourceBundle.getBundle(MESSAGES_BUNDLE);
      }

      if (guiStyle == Style.advanced) {
        renderHeaderPanel = true;
        renderIconPanel = false;
        renderCancelButton = true;
      }

      registerHelpListener(helpListener);
      
      createGUI(background);
    }
    
    private void createGUI(final URL background) {

        try {

          log.debug("scheduling gui initialization");
      
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                  
                  log.debug("initializing gui");

                  if (renderIconPanel) {
                    initIconPanel(background);
                    initContentPanel(null);
                  } else {
                    initContentPanel(background);
                  }
                  
                  GroupLayout layout = new GroupLayout(contentPane);
                  contentPane.setLayout(layout);
                  
                  if (renderIconPanel) {
                    layout.setHorizontalGroup(layout.createSequentialGroup()
                        .addComponent(iconPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(contentPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
                    layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                      .addComponent(iconPanel, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                      .addComponent(contentPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
                  } else {
                    layout.setHorizontalGroup(layout.createSequentialGroup().addComponent(contentPanel));
                    layout.setVerticalGroup(layout.createSequentialGroup().addComponent(contentPanel));
                  }
                }
            });
        } catch (Exception ex) {
            throw new RuntimeException("Failed to init GUI: " + ex.getMessage());
        }
    }
    
    protected void initIconPanel(URL background) {
      if (background == null) {
        background = getClass().getResource(DEFAULT_BACKGROUND);
      }
      if ("file".equals(background.getProtocol())) {
        log.warn("file:// background images not permitted: " + background);
        background = getClass().getResource(DEFAULT_BACKGROUND);
      }
      log.debug("loading icon panel background " + background);
      
      iconPanel = new JPanel();
      JLabel iconLabel = new JLabel();
      iconLabel.setIcon(new ImageIcon(background));

      GroupLayout iconPanelLayout = new GroupLayout(iconPanel);
      iconPanel.setLayout(iconPanelLayout);
      iconPanelLayout.setHorizontalGroup(
        iconPanelLayout.createSequentialGroup()
          .addContainerGap()
          .addComponent(iconLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE));
          // no gap here (contentPanel has containerGap)
      iconPanelLayout.setVerticalGroup(
        iconPanelLayout.createSequentialGroup()
          .addContainerGap()
          .addComponent(iconLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
          .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
    }

    protected void initContentPanel(URL background) { 

      if (background == null) {
        background = getClass().getResource(DEFAULT_BACKGROUND);
      }
      if ("file".equals(background.getProtocol())) {
        log.warn("file:// background images not permitted: " + background);
        background = getClass().getResource(DEFAULT_BACKGROUND);
      }
      log.debug("loading background " + background);
      contentPanel = new ImagePanel(background);

      mainPanel = new JPanel();
      mainPanel.setOpaque(false);
      buttonPanel = new JPanel(); 
      buttonPanel.setOpaque(false);

      helpLabel = new JLabel();
      helpLabel.setIcon(new ImageIcon(getClass().getResource(HELP_IMG))); 
      helpLabel.getAccessibleContext().setAccessibleName(messages.getString(ALT_HELP));
      helpLabel.addMouseListener(helpListener);
      helpLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    
      buttonSize = initButtonSize(messages, renderCancelButton);
        
      if (renderHeaderPanel) {
        headerPanel = new JPanel();
        headerPanel.setOpaque(false);

        titleLabel = new JLabel();
        titleLabel.setFont(titleLabel.getFont().deriveFont(titleLabel.getFont().getStyle() |
          java.awt.Font.BOLD, titleLabel.getFont().getSize() + 2));

        GroupLayout headerPanelLayout = new GroupLayout(headerPanel);
        headerPanel.setLayout(headerPanelLayout);

        headerPanelLayout.setHorizontalGroup(
          headerPanelLayout.createSequentialGroup()
            .addComponent(titleLabel, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 0, Short.MAX_VALUE)
            .addComponent(helpLabel));
        headerPanelLayout.setVerticalGroup(
          headerPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(titleLabel, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
            .addComponent(helpLabel));
      }

      GroupLayout contentPanelLayout = new GroupLayout(contentPanel);
      contentPanel.setLayout(contentPanelLayout);

      GroupLayout.ParallelGroup horizontalContentInner = contentPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
      if (renderHeaderPanel) {
        horizontalContentInner
                .addComponent(headerPanel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
      }
      horizontalContentInner
              .addComponent(mainPanel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
              .addComponent(buttonPanel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
      GroupLayout.SequentialGroup horizontalContentOuter = contentPanelLayout.createSequentialGroup();
      if (!renderIconPanel) {
        horizontalContentOuter
                .addContainerGap();
      }
      horizontalContentOuter
              .addGroup(horizontalContentInner)
              .addContainerGap();
      contentPanelLayout.setHorizontalGroup(horizontalContentOuter);
      
      GroupLayout.SequentialGroup verticalContent = contentPanelLayout.createSequentialGroup();
      verticalContent.addContainerGap();
      if (renderHeaderPanel) {
        verticalContent.addComponent(headerPanel, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
          .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
      }
      verticalContent.addComponent(mainPanel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE) 
        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(buttonPanel, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        .addContainerGap();
      contentPanelLayout.setVerticalGroup(verticalContent);
    }

    @Override
    public Locale getLocale() {
      return messages.getLocale();
    }

    @Override
    public void showWelcomeDialog() {
      
      log.debug("scheduling welcome dialog");
      
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
              
              log.debug("show welcome dialog");
              
                mainPanel.removeAll();
                buttonPanel.removeAll();

                helpListener.setHelpTopic(HELP_WELCOME);
                
                JLabel welcomeMsgLabel = new JLabel();
                welcomeMsgLabel.setFont(welcomeMsgLabel.getFont().deriveFont(welcomeMsgLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
                
                if (renderHeaderPanel) {
                  titleLabel.setText(messages.getString(TITLE_WELCOME));
                  welcomeMsgLabel.setText(messages.getString(MESSAGE_WAIT));
                } else {
                  welcomeMsgLabel.setText(messages.getString(TITLE_WELCOME));   
                }
                
                GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
                mainPanel.setLayout(mainPanelLayout);

                GroupLayout.SequentialGroup messageHorizontal = mainPanelLayout.createSequentialGroup()
                        .addComponent(welcomeMsgLabel);
                GroupLayout.Group messageVertical = mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(welcomeMsgLabel);
                if (!renderHeaderPanel) {
                  messageHorizontal
                          .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 0, Short.MAX_VALUE)
                          .addComponent(helpLabel); 
                  messageVertical
                          .addComponent(helpLabel);
                }

                mainPanelLayout.setHorizontalGroup(messageHorizontal);
                mainPanelLayout.setVerticalGroup(messageVertical);
                
                contentPanel.validate();

            }
        });
    }

    @Override
    public void showInsertCardDialog(final ActionListener cancelListener, final String cancelCommand) {
      
      log.debug("scheduling insert card dialog");
      
      SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
              
              log.debug("show insert card dialog");
      
                mainPanel.removeAll();
                buttonPanel.removeAll();

                if (renderHeaderPanel) {
                  titleLabel.setText(messages.getString(TITLE_INSERTCARD));
                }
                
                helpListener.setHelpTopic(HELP_INSERTCARD);

                JLabel insertCardMsgLabel = new JLabel();
                insertCardMsgLabel.setFont(insertCardMsgLabel.getFont().deriveFont(insertCardMsgLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
                insertCardMsgLabel.setText(messages.getString(MESSAGE_INSERTCARD));

                GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
                mainPanel.setLayout(mainPanelLayout);

                GroupLayout.SequentialGroup messageHorizontal = mainPanelLayout.createSequentialGroup()
                        .addComponent(insertCardMsgLabel);
                GroupLayout.ParallelGroup messageVertical = mainPanelLayout.createParallelGroup()
                        .addComponent(insertCardMsgLabel);
                
                if (!renderHeaderPanel) {
                  messageHorizontal
                          .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 0, Short.MAX_VALUE)
                          .addComponent(helpLabel);    
                  messageVertical
                          .addComponent(helpLabel);
                }
                
                mainPanelLayout.setHorizontalGroup(messageHorizontal);
                mainPanelLayout.setVerticalGroup(messageVertical);
                
                if (renderCancelButton) {
                  JButton cancelButton = new JButton();
                  cancelButton.setFont(cancelButton.getFont().deriveFont(cancelButton.getFont().getStyle() & ~java.awt.Font.BOLD));
                  cancelButton.setText(messages.getString(BUTTON_CANCEL));
                  cancelButton.addActionListener(cancelListener);
                  cancelButton.setActionCommand(cancelCommand);

                  GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
                  buttonPanel.setLayout(buttonPanelLayout);

                  buttonPanelLayout.setHorizontalGroup(
                    buttonPanelLayout.createSequentialGroup()
                          .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                          .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, buttonSize, GroupLayout.PREFERRED_SIZE));
                  buttonPanelLayout.setVerticalGroup(
                    buttonPanelLayout.createSequentialGroup()
                      .addComponent(cancelButton));
                }

                contentPanel.validate();
            }
        });
    }

    /**
     * only difference to showInsertCard: title text: card not supported
     * @param cancelListener
     * @param cancelCommand
     */
    @Override
    public void showCardNotSupportedDialog(final ActionListener cancelListener, final String cancelCommand) {
        
      log.debug("scheduling card not supported dialog");
      
      SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                
              log.debug("show card not supported dialog");
                
              mainPanel.removeAll();
              buttonPanel.removeAll();

              JLabel insertCardMsgLabel = new JLabel();
              insertCardMsgLabel.setFont(insertCardMsgLabel.getFont().deriveFont(insertCardMsgLabel.getFont().getStyle() & ~java.awt.Font.BOLD));

              if (renderHeaderPanel) {
                titleLabel.setText(messages.getString(TITLE_CARD_NOT_SUPPORTED));
                insertCardMsgLabel.setText(messages.getString(MESSAGE_INSERTCARD));
              } else {
                insertCardMsgLabel.setText(messages.getString(TITLE_CARD_NOT_SUPPORTED)); 
              }

              helpListener.setHelpTopic(HELP_CARDNOTSUPPORTED);
              
              GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
              mainPanel.setLayout(mainPanelLayout);

              GroupLayout.SequentialGroup messageHorizontal = mainPanelLayout.createSequentialGroup()
                      .addComponent(insertCardMsgLabel);
              GroupLayout.Group messageVertical = mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                      .addComponent(insertCardMsgLabel);
              if (!renderHeaderPanel) {
                messageHorizontal
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 0, Short.MAX_VALUE)
                        .addComponent(helpLabel);
                messageVertical
                        .addComponent(helpLabel);
              } 
              
              mainPanelLayout.setHorizontalGroup(messageHorizontal);
              mainPanelLayout.setVerticalGroup(messageVertical);
                
              if (renderCancelButton) {
                JButton cancelButton = new JButton();
                cancelButton.setFont(cancelButton.getFont().deriveFont(cancelButton.getFont().getStyle() & ~java.awt.Font.BOLD));
                cancelButton.setText(messages.getString(BUTTON_CANCEL));
                cancelButton.addActionListener(cancelListener);
                cancelButton.setActionCommand(cancelCommand);

                GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
                buttonPanel.setLayout(buttonPanelLayout);

                buttonPanelLayout.setHorizontalGroup(
                  buttonPanelLayout.createSequentialGroup()
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, buttonSize, GroupLayout.PREFERRED_SIZE));
                buttonPanelLayout.setVerticalGroup(
                  buttonPanelLayout.createSequentialGroup()
                    .addComponent(cancelButton));
              }
              
              contentPanel.validate();
            }
        });
    }

    private void showCardPINDialog(final PINSpec pinSpec, final int numRetries, final ActionListener okListener, final String okCommand, final ActionListener cancelListener, final String cancelCommand) {
        
      log.debug("scheduling card-pin dialog");
      
      SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {

              log.debug("show card-pin dialog");
      
                mainPanel.removeAll();
                buttonPanel.removeAll();

                if (renderHeaderPanel) {
                  if (numRetries < 0) {
                      String cardpinTitle = messages.getString(TITLE_CARDPIN);
                      titleLabel.setText(MessageFormat.format(cardpinTitle, new Object[]{pinSpec.getLocalizedName()}));
                  } else {
                      titleLabel.setText(messages.getString(TITLE_RETRY));
                  }
                }

                JButton okButton = new JButton();
                okButton.setFont(okButton.getFont().deriveFont(okButton.getFont().getStyle() & ~java.awt.Font.BOLD));
                okButton.setText(messages.getString(BUTTON_OK));
                okButton.setEnabled(false);
                okButton.setActionCommand(okCommand);
                okButton.addActionListener(okListener);

                JLabel cardPinLabel = new JLabel();
                cardPinLabel.setFont(cardPinLabel.getFont().deriveFont(cardPinLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
                String pinLabel = messages.getString(LABEL_PIN);
                cardPinLabel.setText(MessageFormat.format(pinLabel, new Object[]{pinSpec.getLocalizedName()}));

                pinField = new JPasswordField();
                pinField.setText("");
                pinField.setDocument(new PINDocument(pinSpec, okButton));
                pinField.setActionCommand(okCommand);
                pinField.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (pinField.getPassword().length >= pinSpec.getMinLength()) {
                            okListener.actionPerformed(e);
                        }
                    }
                });

                JLabel infoLabel = new JLabel();
                if (numRetries < 0) {
                  infoLabel.setFont(infoLabel.getFont().deriveFont(infoLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
                  String infoPattern = messages.getString(MESSAGE_ENTERPIN);
                  infoLabel.setText(MessageFormat.format(infoPattern, new Object[] {pinSpec.getLocalizedName()}));
                  helpListener.setHelpTopic(HELP_CARDPIN);
                } else {
                  infoLabel.setFont(infoLabel.getFont().deriveFont(infoLabel.getFont().getStyle() | java.awt.Font.BOLD));
                  infoLabel.setText(MessageFormat.format(messages.getString(MESSAGE_RETRIES), new Object[]{String.valueOf(numRetries)}));
                  infoLabel.setForeground(ERROR_COLOR);
                  helpListener.setHelpTopic(HELP_RETRY);
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
                          .addComponent(infoLabel);
                GroupLayout.ParallelGroup infoVertical = mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                          .addComponent(infoLabel);
                
                if (!renderHeaderPanel) {
                  infoHorizontal
                          .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 0, Short.MAX_VALUE)
                          .addComponent(helpLabel);
                  infoVertical
                          .addComponent(helpLabel);
                } 
                
                mainPanelLayout.setHorizontalGroup(
                        mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                          .addGroup(infoHorizontal)
                          .addGroup(mainPanelLayout.createSequentialGroup()
                            .addComponent(cardPinLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                              .addComponent(pinField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE) //))
                              .addComponent(pinsizeLabel))));
                mainPanelLayout.setVerticalGroup(
                        mainPanelLayout.createSequentialGroup()
                          .addGroup(infoVertical)
                          .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                          .addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(cardPinLabel)
                            .addComponent(pinField))
                          .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                          .addComponent(pinsizeLabel));
                
                
                GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
                buttonPanel.setLayout(buttonPanelLayout);

                GroupLayout.SequentialGroup buttonHorizontal = buttonPanelLayout.createSequentialGroup()
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(okButton, GroupLayout.PREFERRED_SIZE, buttonSize, GroupLayout.PREFERRED_SIZE);
                GroupLayout.Group buttonVertical;
                
                if (renderCancelButton) {
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
                } else {
                  buttonVertical = buttonPanelLayout.createSequentialGroup()
                          .addComponent(okButton);
                }
                
                buttonPanelLayout.setHorizontalGroup(buttonHorizontal);
                buttonPanelLayout.setVerticalGroup(buttonVertical);

                pinField.requestFocusInWindow();
                contentPanel.validate();

            }
        });
    }

    @Override
    public void showCardPINDialog(PINSpec pinSpec, ActionListener okListener, String okCommand, ActionListener cancelListener, String cancelCommand) {
        showCardPINDialog(pinSpec, -1, okListener, okCommand, cancelListener, cancelCommand);
    }

    @Override
    public void showCardPINRetryDialog(PINSpec pinSpec, int numRetries, ActionListener okListener, String okCommand, ActionListener cancelListener, String cancelCommand) {
        showCardPINDialog(pinSpec, numRetries, okListener, okCommand, cancelListener, cancelCommand);
    }

    @Override
    public void showSignaturePINDialog(PINSpec pinSpec, ActionListener signListener, String signCommand, ActionListener cancelListener, String cancelCommand, ActionListener hashdataListener, String hashdataCommand) {
        showSignaturePINDialog(pinSpec, -1, signListener, signCommand, cancelListener, cancelCommand, hashdataListener, hashdataCommand);
    }

    @Override
    public void showSignaturePINRetryDialog(PINSpec pinSpec, int numRetries, ActionListener okListener, String okCommand, ActionListener cancelListener, String cancelCommand, ActionListener hashdataListener, String hashdataCommand) {
        showSignaturePINDialog(pinSpec, numRetries, okListener, okCommand, cancelListener, cancelCommand, hashdataListener, hashdataCommand);
    }

    private void showSignaturePINDialog(final PINSpec pinSpec, final int numRetries, final ActionListener signListener, final String signCommand, final ActionListener cancelListener, final String cancelCommand, final ActionListener hashdataListener, final String hashdataCommand) {

      log.debug("scheduling signature-pin dialog");
      
      SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
              
              log.debug("show signature-pin dialog");
      
                mainPanel.removeAll();
                buttonPanel.removeAll();
                
                if (renderHeaderPanel) {
                  if (numRetries < 0) {
                      titleLabel.setText(messages.getString(TITLE_SIGN));
                  } else {
                      titleLabel.setText(messages.getString(TITLE_RETRY));
                  }
                }

                JButton signButton = new JButton();
                signButton.setFont(signButton.getFont().deriveFont(signButton.getFont().getStyle() & ~java.awt.Font.BOLD));
                signButton.setText(messages.getString(BUTTON_SIGN));
                signButton.setEnabled(false);
                signButton.setActionCommand(signCommand);
                signButton.addActionListener(signListener);

                JLabel signPinLabel = new JLabel();
                signPinLabel.setFont(signPinLabel.getFont().deriveFont(signPinLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
                String pinLabel = messages.getString(LABEL_PIN);
                signPinLabel.setText(MessageFormat.format(pinLabel, new Object[]{pinSpec.getLocalizedName()}));

                pinField = new JPasswordField();
                pinField.setText("");
                pinField.setDocument(new PINDocument(pinSpec, signButton));
                pinField.setActionCommand(signCommand);
                pinField.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (pinField.getPassword().length >= pinSpec.getMinLength()) {
                            signListener.actionPerformed(e);
                        }
                    }
                });

                JLabel pinsizeLabel = new JLabel();
                pinsizeLabel.setFont(pinsizeLabel.getFont().deriveFont(pinsizeLabel.getFont().getStyle() & ~java.awt.Font.BOLD, pinsizeLabel.getFont().getSize()-2));
                String pinsizePattern = messages.getString(LABEL_PINSIZE);
                String pinSize = String.valueOf(pinSpec.getMinLength());
                if (pinSpec.getMinLength() != pinSpec.getMaxLength()) {
                    pinSize += "-" + pinSpec.getMaxLength();
                }
                pinsizeLabel.setText(MessageFormat.format(pinsizePattern, new Object[]{pinSize}));

                JLabel infoLabel = new JLabel();
                if (numRetries < 0) {
                  infoLabel.setFont(infoLabel.getFont().deriveFont(infoLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
                  infoLabel.setText(messages.getString(MESSAGE_HASHDATALINK));
                  infoLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                  infoLabel.setForeground(HYPERLINK_COLOR);
                  infoLabel.addMouseListener(new MouseAdapter() {

                      @Override
                      public void mouseClicked(MouseEvent me) {
                          ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, hashdataCommand);
                          hashdataListener.actionPerformed(e);
                      }
                  });
                  helpListener.setHelpTopic(HELP_SIGNPIN);
                } else {
                  infoLabel.setFont(infoLabel.getFont().deriveFont(infoLabel.getFont().getStyle() | java.awt.Font.BOLD));
                  infoLabel.setText(MessageFormat.format(messages.getString(MESSAGE_RETRIES), new Object[]{String.valueOf(numRetries)}));
                  infoLabel.setForeground(ERROR_COLOR);
                  helpListener.setHelpTopic(HELP_RETRY);
                }

                GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
                mainPanel.setLayout(mainPanelLayout);
                
                GroupLayout.SequentialGroup infoHorizontal = mainPanelLayout.createSequentialGroup()
                        .addComponent(infoLabel);
                GroupLayout.ParallelGroup infoVertical = mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(infoLabel);
                       
                if (!renderHeaderPanel) {
                  infoHorizontal
                          .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 0, Short.MAX_VALUE)
                          .addComponent(helpLabel); //, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
                  infoVertical
                          .addComponent(helpLabel);
                }

                mainPanelLayout.setHorizontalGroup(
                  mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(infoHorizontal)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(signPinLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                          .addComponent(pinField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                          .addComponent(pinsizeLabel))));

                mainPanelLayout.setVerticalGroup(
                  mainPanelLayout.createSequentialGroup()
                    .addGroup(infoVertical)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(signPinLabel)
                        .addComponent(pinField))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(pinsizeLabel));

                GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
                buttonPanel.setLayout(buttonPanelLayout);

                GroupLayout.SequentialGroup buttonHorizontal = buttonPanelLayout.createSequentialGroup()
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(signButton, GroupLayout.PREFERRED_SIZE, buttonSize, GroupLayout.PREFERRED_SIZE);
                GroupLayout.Group buttonVertical;
                
                if (renderCancelButton) {
                  JButton cancelButton = new JButton();
                  cancelButton.setFont(cancelButton.getFont().deriveFont(cancelButton.getFont().getStyle() & ~java.awt.Font.BOLD));
                  cancelButton.setText(messages.getString(BUTTON_CANCEL));
                  cancelButton.setActionCommand(cancelCommand);
                  cancelButton.addActionListener(cancelListener);

                  buttonHorizontal
                          .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                          .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, buttonSize, GroupLayout.PREFERRED_SIZE);
                  
                  buttonVertical = buttonPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                          .addComponent(signButton)
                          .addComponent(cancelButton); 
                } else {
                  buttonVertical = buttonPanelLayout.createSequentialGroup()
                          .addComponent(signButton);
                }
                
                buttonPanelLayout.setHorizontalGroup(buttonHorizontal);
                buttonPanelLayout.setVerticalGroup(buttonVertical);

                pinField.requestFocusInWindow();
                contentPanel.validate();

            }
        });
    }

    @Override
    public void showErrorDialog(final String errorMsgKey, final Object[] errorMsgParams, final ActionListener okListener, final String okCommand) {
        
      log.debug("scheduling error dialog");
      
      SwingUtilities.invokeLater(new Runnable() {

          @Override
            public void run() {

                log.debug("show error dialog");
                
                mainPanel.removeAll();
                buttonPanel.removeAll();

                if (renderHeaderPanel) {
                  titleLabel.setText(messages.getString(TITLE_ERROR));
                }

                helpListener.setHelpTopic(errorMsgKey);
                
                String errorMsgPattern = messages.getString(errorMsgKey);
                String errorMsg = MessageFormat.format(errorMsgPattern, errorMsgParams);
                
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
                okButton.setActionCommand(okCommand);
                okButton.addActionListener(okListener);

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

    @Override
    public void showErrorDialog(final String errorMsgKey, final Object[] errorMsgParams) {
      
      log.debug("scheduling error  dialog");
      
      SwingUtilities.invokeLater(new Runnable() {

        @Override
        public void run() {
          
          log.debug("show error dialog");
      
          mainPanel.removeAll();
          buttonPanel.removeAll();

          if (renderHeaderPanel) {
            titleLabel.setText(messages.getString(TITLE_ERROR));
          }

          helpListener.setHelpTopic(errorMsgKey);
          
          String errorMsgPattern = messages.getString(errorMsgKey);
          String errorMsg = MessageFormat.format(errorMsgPattern, errorMsgParams);

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
          
          contentPanel.validate();
        }
      });
    }

    @Override
    public void showWaitDialog(final String waitMessage) {
        
      log.debug("scheduling wait dialog");
      
      SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
        
              log.debug("show wait dialog");
              
                mainPanel.removeAll();
                buttonPanel.removeAll();

                if (renderHeaderPanel) {
                  titleLabel.setText(messages.getString(TITLE_WAIT));
                }
                
                helpListener.setHelpTopic(HELP_WAIT);
                
                JLabel waitMsgLabel = new JLabel();
                waitMsgLabel.setFont(waitMsgLabel.getFont().deriveFont(waitMsgLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
                if (waitMessage != null) {
                    waitMsgLabel.setText("<html>" + waitMessage + "</html>");
                } else {
                    waitMsgLabel.setText(messages.getString(MESSAGE_WAIT));
                }

                GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
                mainPanel.setLayout(mainPanelLayout);

                GroupLayout.SequentialGroup messageHorizontal = mainPanelLayout.createSequentialGroup()
                        .addComponent(waitMsgLabel);
                GroupLayout.ParallelGroup messageVertical = mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(waitMsgLabel);
                
                if (!renderHeaderPanel) {
                  messageHorizontal
                          .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 0, Short.MAX_VALUE)
                          .addComponent(helpLabel);
                  messageVertical
                          .addComponent(helpLabel);
                }
                mainPanelLayout.setHorizontalGroup(messageHorizontal);
                mainPanelLayout.setVerticalGroup(messageVertical);
                
                contentPanel.validate();
            }
        });
    }

    @Override
    public char[] getPin() {
        if (pinField != null) {
            return pinField.getPassword();
        }
        return null;
    }
    
    /**
     * TODO handle multiple references in HashDataViewer
     * @param signedReferences
     * @param okListener
     * @param okCommand
     */
    @Override
    public void showHashDataInputDialog(final List<HashDataInput> signedReferences, 
            final ActionListener okListener, 
            final String okCommand) {
      
      if (signedReferences == null) {
        showErrorDialog(messages.getString(ERR_NO_HASHDATA), new Object[] {"No SignedReferences provided"}, okListener, okCommand);
        return;
      }
      
      if (signedReferences.size() == 1) {
        try {
          log.debug("scheduling hashdata viewer");

          SwingUtilities.invokeAndWait(new Runnable() {

            @Override
            public void run() {
              ActionListener saveHashDataListener = new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                  HashDataInput hdi = signedReferences.get(0);
                  showSaveHashDataInputDialog(Collections.singletonList(hdi), okListener, okCommand);
                }
              };
              showHashDataViewer(signedReferences.get(0), saveHashDataListener, "save");
            }
          });
       
        } catch (InterruptedException ex) {
          log.error("Failed to display HashDataViewer: " + ex.getMessage());
        } catch (InvocationTargetException ex) {
          log.error("Failed to display HashDataViewer: " + ex.getMessage());
        }
      } else {
        showSignedReferencesListDialog(signedReferences, okListener, okCommand);
      }
    }
    
    /**
     * has to be called from event dispatcher thread
     * @param hashDataText
     * @param saveListener
     * @param saveCommand
     */
    private void showHashDataViewer(final HashDataInput hashDataInput, final ActionListener saveListener, final String saveCommand) {
      
      log.debug("show hashdata viewer");

      ActionListener l = helpListener.getActionListener();
      HashDataViewer.showHashDataInput(contentPane, hashDataInput, messages, saveListener, saveCommand, l);
    }
    
    private void showSignedReferencesListDialog(final List<HashDataInput> signedReferences, final ActionListener backListener, final String backCommand) {
      
      log.debug("scheduling signed references list dialog");
      
      SwingUtilities.invokeLater(new Runnable() {

        @Override
        public void run() {
          
          log.debug("show signed references list dialog");
          
          mainPanel.removeAll();
          buttonPanel.removeAll();

          if (renderHeaderPanel) {
            titleLabel.setText(messages.getString(TITLE_HASHDATA));
          }
          
          helpListener.setHelpTopic(HELP_HASHDATALIST);
          
          JLabel refIdLabel = new JLabel();
          refIdLabel.setFont(refIdLabel.getFont().deriveFont(refIdLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
          String refIdLabelPattern = messages.getString(MESSAGE_HASHDATALIST);
          refIdLabel.setText(MessageFormat.format(refIdLabelPattern, new Object[]{signedReferences.size()}));

          HashDataTableModel tableModel = new HashDataTableModel(signedReferences);
          final JTable hashDataTable = new JTable(tableModel);
          hashDataTable.setDefaultRenderer(HashDataInput.class, new HyperlinkRenderer());
          hashDataTable.setTableHeader(null);
          
          // not possible to add mouse listener to TableCellRenderer
          hashDataTable.addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseMoved(MouseEvent e) {
              if (hashDataTable.columnAtPoint(e.getPoint()) == 0) {
                hashDataTable.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
              } else {
                hashDataTable.setCursor(Cursor.getDefaultCursor());
              }
            }
          });
          
          hashDataTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
          hashDataTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(final ListSelectionEvent e) {
              //invoke later to allow thread to paint selection background
              SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                  ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                  int selectionIdx = lsm.getMinSelectionIndex();
                  if (selectionIdx >= 0) {
                    final HashDataInput selection = signedReferences.get(selectionIdx);
                    showHashDataViewer(selection, new ActionListener() {

                      @Override
                      public void actionPerformed(ActionEvent e) {
                        showSaveHashDataInputDialog(Collections.singletonList(selection), null, null);
                      }
                    }, "save");
                  }
                }
              });
            }
          });
          
          JScrollPane hashDataScrollPane = new JScrollPane(hashDataTable);

          GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
          mainPanel.setLayout(mainPanelLayout);
          
          GroupLayout.SequentialGroup messageHorizontal = mainPanelLayout.createSequentialGroup()
                  .addComponent(refIdLabel);
          
          GroupLayout.ParallelGroup messageVertical = mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                  .addComponent(refIdLabel);
          
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
              .addComponent(hashDataScrollPane, 0, 0, Short.MAX_VALUE));

          mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createSequentialGroup()
              .addGroup(messageVertical)
              .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
              .addComponent(hashDataScrollPane, 0, 0, hashDataTable.getPreferredSize().height+3));

          JButton backButton = new JButton();
          backButton.setFont(backButton.getFont().deriveFont(backButton.getFont().getStyle() & ~java.awt.Font.BOLD));
          backButton.setText(messages.getString(BUTTON_BACK));
          backButton.setActionCommand(backCommand);
          backButton.addActionListener(backListener);

          GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
          buttonPanel.setLayout(buttonPanelLayout);

          buttonPanelLayout.setHorizontalGroup(buttonPanelLayout.createSequentialGroup()
                  .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                  .addComponent(backButton, GroupLayout.PREFERRED_SIZE, buttonSize, GroupLayout.PREFERRED_SIZE));
          buttonPanelLayout.setVerticalGroup(buttonPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                  .addComponent(backButton));

          contentPanel.validate();
        }
      });
    }
    
    /**
     * 
     * @param signedRefs
     * @param okListener may be null
     * @param okCommand
     */
    private void showSaveHashDataInputDialog(final List<HashDataInput> signedRefs, final ActionListener okListener, final String okCommand) {
      
      log.debug("scheduling save hashdatainput dialog");
      
      SwingUtilities.invokeLater(new Runnable() {

        @Override
        public void run() {
          
          log.debug("show save hashdatainput dialog");
      
          String userHome = System.getProperty("user.home");
          
          JFileChooser fileDialog = new JFileChooser(userHome); 
          fileDialog.setMultiSelectionEnabled(false);
          fileDialog.setDialogType(JFileChooser.SAVE_DIALOG);
          fileDialog.setFileHidingEnabled(true);
          if (signedRefs.size() == 1) {
            fileDialog.setDialogTitle(messages.getString(WINDOWTITLE_SAVE));
            fileDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
            String mimeType = signedRefs.get(0).getMimeType();
            MimeFilter mimeFilter = new MimeFilter(mimeType, messages);
            fileDialog.setFileFilter(mimeFilter);
            String filename = messages.getString(SAVE_HASHDATAINPUT_PREFIX) + MimeFilter.getExtension(mimeType);
            fileDialog.setSelectedFile(new File(userHome, filename));
          } else {
            fileDialog.setDialogTitle(messages.getString(WINDOWTITLE_SAVEDIR));
            fileDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
          }
          
          //parent contentPane -> placed over applet
          switch (fileDialog.showSaveDialog(fileDialog)) {
            case JFileChooser.APPROVE_OPTION:
              File f = fileDialog.getSelectedFile();
              for (HashDataInput hashDataInput : signedRefs) {
                String mimeType = hashDataInput.getMimeType();
                String id = hashDataInput.getReferenceId();
                File file;
                if (f.isDirectory()) {
                  String filename = messages.getString(SAVE_HASHDATAINPUT_PREFIX) + '_' + id + MimeFilter.getExtension(mimeType);
                  file = new File(f, filename);
                } else {
                  file = f;
                }
                if (file.exists()) {
                  String ovrwrt = messages.getString(MESSAGE_OVERWRITE);
                  int overwrite = JOptionPane.showConfirmDialog(fileDialog, MessageFormat.format(ovrwrt, file), messages.getString(WINDOWTITLE_OVERWRITE), JOptionPane.OK_CANCEL_OPTION);
                  if (overwrite != JOptionPane.OK_OPTION) {
                    continue;
                  }
                }
                if (log.isDebugEnabled()) {
                    log.debug("Writing HashDataInput " + id + " (" + mimeType + ") to file " + file);
                }
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(file);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    InputStream hdi = hashDataInput.getHashDataInput();
                    int b;
                    while ((b = hdi.read()) != -1) {
                        bos.write(b);
                    }
                    bos.flush();
                    bos.close();
                } catch (IOException ex) {
                    log.error("Failed to write HashDataInput to file " + file + ": " + ex.getMessage());
                    showErrorDialog(ERR_WRITE_HASHDATA, new Object[] {ex.getMessage()}, null, null);
                    ex.printStackTrace();
                } finally {
                    try {
                        fos.close();
                    } catch (IOException ex) {
                    }
                }    
              }  
          }
          log.debug("done saving hashdatainput");
          if (okListener != null) {
            okListener.actionPerformed(new ActionEvent(fileDialog, ActionEvent.ACTION_PERFORMED, okCommand));
          }
        }
      });
    }
    
    private void registerHelpListener(ActionListener helpListener) {
      if (helpListener != null) {
        this.helpListener = new HelpMouseListener(helpListener);
      } else {
        log.error("no help listener provided, will not be able to display help");
        this.helpListener = new HelpMouseListener(new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent e) {
            log.error("no help listener registered (requested help topic: " + e.getActionCommand() + ")");
          }
        });
      }
    }
    
  private static int initButtonSize(ResourceBundle messages, boolean renderCancelButton) {

    int buttonSize = 0;
    
    JButton b = new JButton();
    b.setText(messages.getString(BUTTON_OK));
    if (b.getPreferredSize().width > buttonSize) {
      buttonSize = b.getPreferredSize().width;
    }
    b.setText(messages.getString(BUTTON_SIGN));
    if (b.getPreferredSize().width > buttonSize) {
      buttonSize = b.getPreferredSize().width;
    }
    b.setText(messages.getString(BUTTON_BACK));
    if (b.getPreferredSize().width > buttonSize) {
      buttonSize = b.getPreferredSize().width;
    }
    b.setText(messages.getString(BUTTON_SAVE));
    if (b.getPreferredSize().width > buttonSize) {
      buttonSize = b.getPreferredSize().width;
    }
    if (renderCancelButton) {
      b.setText(messages.getString(BUTTON_CANCEL));
      if (b.getPreferredSize().width > buttonSize) {
        buttonSize = b.getPreferredSize().width;
      }
    }
    return buttonSize;
  }
}
