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

import at.gv.egiz.bku.gui.viewer.FontProviderException;
import at.gv.egiz.bku.gui.viewer.FontProvider;
import at.gv.egiz.smcc.PINSpec;
import at.gv.egiz.stal.HashDataInput;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



/**
 *
 * @author clemens
 */
public class BKUGUIImpl implements BKUGUIFacade {
    
    protected static final Log log = LogFactory.getLog(BKUGUIImpl.class);

    protected enum PinLabelPosition {
      LEFT, ABOVE
    }

    protected HelpMouseListener helpMouseListener;
    protected HelpKeyListener helpKeyListener;
    protected SwitchFocusFocusListener switchFocusKeyListener;
    protected SecureViewerDialog secureViewer;
    protected FontProvider fontProvider;
    
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
    protected JLabel switchFocusDummyLabel; 
    /** remember the pinfield to return to worker */
    protected JPasswordField pinField;

    protected int buttonSize;
    
    /** gui style config (default 'simple') */
    protected boolean renderHeaderPanel = false;
    protected boolean renderIconPanel = false;
    protected boolean renderCancelButton = false;
    protected boolean shortText = false;
    protected PinLabelPosition pinLabelPos = PinLabelPosition.LEFT;
    protected boolean renderRefId = false;

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
            FontProvider fontProvider,
            ActionListener helpListener,
            SwitchFocusListener switchFocusListener) {
      this.contentPane = contentPane;

      loadMessageBundle(locale);

      if (guiStyle == Style.advanced) {
        renderHeaderPanel = true;
        renderIconPanel = false;
        renderCancelButton = true;
        renderRefId = true;
      } else if (guiStyle == Style.tiny) {
        shortText = true;
        pinLabelPos = PinLabelPosition.ABOVE;
      }

      // ensure that buttons can be fired with enter key too
      UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE);
      
      registerHelpListener(helpListener);
      
      registerSwitchFocusListener(switchFocusListener);

      this.fontProvider = fontProvider;
      createGUI(background);
    }
    
    private void createGUI(final URL background) {

        try {

          log.debug("scheduling gui initialization");
      
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                  
                  log.debug("[" + Thread.currentThread().getName() + "] initializing gui");

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
                            .addContainerGap()
                            .addComponent(iconPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(contentPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addContainerGap());
                    layout.setVerticalGroup(layout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                              .addComponent(iconPanel, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                              .addComponent(contentPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addContainerGap());
                  } else {
                    layout.setHorizontalGroup(layout.createSequentialGroup()
                            // left border
                            .addContainerGap()
                            .addComponent(contentPanel)
                            .addContainerGap());
                    layout.setVerticalGroup(layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(contentPanel)
                            .addContainerGap());
                  }
                }
            });
        } catch (Exception ex) {
            throw new RuntimeException("Failed to init GUI: " + ex.getMessage());
        }
    }
    
    protected void initIconPanel(URL background) {
      if (background == null) {
        background = getClass().getResource(DEFAULT_ICON);
      }
      if ("file".equals(background.getProtocol())) {
        log.warn("file:// background images not permitted: " + background +
                ", loading default background");
        background = getClass().getResource(DEFAULT_ICON);
      }
      log.debug("loading icon panel background " + background);
      
      iconPanel = new JPanel();
      JLabel iconLabel = new JLabel();
      iconLabel.setIcon(new ImageIcon(background));

      GroupLayout iconPanelLayout = new GroupLayout(iconPanel);
      iconPanel.setLayout(iconPanelLayout);
      iconPanelLayout.setHorizontalGroup(
        iconPanelLayout.createSequentialGroup()
          .addComponent(iconLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE));
      iconPanelLayout.setVerticalGroup(
        iconPanelLayout.createSequentialGroup()
          .addComponent(iconLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE));
    }

    protected void initContentPanel(URL background) { 

      if (background == null) {
        log.debug("no background image set");
//        contentPanel = new ImagePanel(getClass().getResource(DEFAULT_BACKGROUND));
        contentPanel = new JPanel();
      } else if ("file".equals(background.getProtocol())) {
          log.warn("file:// background images not permitted: " + background);
          contentPanel = new JPanel();
      } else {
        log.debug("loading background " + background);
        contentPanel = new ImagePanel(background);
      }
      contentPanel.setOpaque(false);
      mainPanel = new JPanel();
      mainPanel.setOpaque(false);
      buttonPanel = new JPanel(); 
      buttonPanel.setOpaque(false);
      
      helpLabel = new JLabel();
      helpLabel.setIcon(new ImageIcon(getClass().getResource(HELP_IMG))); 
      helpLabel.getAccessibleContext().setAccessibleName(getMessage(ALT_HELP));
      helpLabel.setFocusable(true);
      helpLabel.addMouseListener(helpMouseListener);
      helpLabel.addKeyListener(helpKeyListener);
      helpLabel.addFocusListener(new FocusAdapter() {
    	 
    	  @Override
    	  public void focusGained(FocusEvent e) {
    		     		 
    		  helpLabel.setIcon(new ImageIcon(getClass().getResource(HELP_IMG_FOCUS)));
    	  }
    	  
    	  @Override
    	  public void focusLost(FocusEvent e) {
    		 
    		  helpLabel.setIcon(new ImageIcon(getClass().getResource(HELP_IMG)));
    	  }
    	  
    	  
      });
      helpLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      
      switchFocusDummyLabel = new JLabel();
      switchFocusDummyLabel.setText("");
      switchFocusDummyLabel.setFocusable(true);
      switchFocusDummyLabel.addFocusListener(switchFocusKeyListener);
    
      buttonSize = initButtonSize();
        
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
            .addComponent(switchFocusDummyLabel)
            .addComponent(helpLabel)
            );
        headerPanelLayout.setVerticalGroup(
          headerPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(titleLabel, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
            .addComponent(switchFocusDummyLabel)
            .addComponent(helpLabel)
            );
      }

      GroupLayout contentPanelLayout = new GroupLayout(contentPanel);
      contentPanel.setLayout(contentPanelLayout);

      // align header, main and button to the right
      GroupLayout.ParallelGroup horizontalContent =
              contentPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING); //LEADING);
      GroupLayout.SequentialGroup verticalContent =
              contentPanelLayout.createSequentialGroup();

      if (renderHeaderPanel) {
        horizontalContent
                .addComponent(headerPanel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        verticalContent
                .addComponent(headerPanel, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);

      }
      horizontalContent
              .addComponent(mainPanel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
              .addComponent(buttonPanel, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE); //Short.MAX_VALUE);
      verticalContent
              .addComponent(mainPanel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
              .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
              .addComponent(buttonPanel, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);

      contentPanelLayout.setHorizontalGroup(horizontalContent); //Outer);
      contentPanelLayout.setVerticalGroup(verticalContent);
    }

    /**
     * BKUWorker inits signaturecard with locale
     * @return
     */
    @Override
    public Locale getLocale() {
      return messages.getLocale();
    }

    /**
     * to be overridden by subclasses providing additional resource messages
     * @param key
     * @return
     */
    protected String getMessage(String key) {
      return messages.getString(key);
    }

    /**
     * to be overridden by subclasses providing additional resource messages
     * @param key
     * @return
     */
    protected boolean hasMessage(String key) {
      return messages.containsKey(key);
    }

//    @Override
//    public void showWelcomeDialog() {
//
//      log.debug("scheduling welcome dialog");
//
//        SwingUtilities.invokeLater(new Runnable() {
//
//            @Override
//            public void run() {
//
//              log.debug("show welcome dialog");
//
//                mainPanel.removeAll();
//                buttonPanel.removeAll();
//
//                helpListener.setHelpTopic(HELP_WELCOME);
//
//                JLabel welcomeMsgLabel = new JLabel();
//                welcomeMsgLabel.setFont(welcomeMsgLabel.getFont().deriveFont(welcomeMsgLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
//
//                if (renderHeaderPanel) {
//                  titleLabel.setText(getMessage(TITLE_WELCOME));
//                  welcomeMsgLabel.setText(getMessage(MESSAGE_WAIT));
//                } else {
//                  welcomeMsgLabel.setText(getMessage(TITLE_WELCOME));
//                }
//
//                GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
//                mainPanel.setLayout(mainPanelLayout);
//
//                GroupLayout.SequentialGroup messageHorizontal = mainPanelLayout.createSequentialGroup()
//                        .addComponent(welcomeMsgLabel);
//                GroupLayout.Group messageVertical = mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                        .addComponent(welcomeMsgLabel);
//                if (!renderHeaderPanel) {
//                  messageHorizontal
//                          .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 0, Short.MAX_VALUE)
//                          .addComponent(helpLabel);
//                  messageVertical
//                          .addComponent(helpLabel);
//                }
//
//                mainPanelLayout.setHorizontalGroup(messageHorizontal);
//                mainPanelLayout.setVerticalGroup(messageVertical);
//
//                contentPanel.validate();
//
//            }
//        });
//    }

//    @Override
//    public void showInsertCardDialog(
//            final ActionListener cancelListener, final String cancelCommand) {
//
//      log.debug("scheduling insert card dialog");
//
//      SwingUtilities.invokeLater(new Runnable() {
//
//            @Override
//            public void run() {
//
//              log.debug("show insert card dialog");
//
//                mainPanel.removeAll();
//                buttonPanel.removeAll();
//
//                if (renderHeaderPanel) {
//                  titleLabel.setText(getMessage(TITLE_INSERTCARD));
//                }
//
//                helpListener.setHelpTopic(HELP_INSERTCARD);
//
//                JLabel insertCardMsgLabel = new JLabel();
//                insertCardMsgLabel.setFont(insertCardMsgLabel.getFont().deriveFont(insertCardMsgLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
//                insertCardMsgLabel.setText(getMessage(MESSAGE_INSERTCARD));
//
//                GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
//                mainPanel.setLayout(mainPanelLayout);
//
//                GroupLayout.SequentialGroup messageHorizontal = mainPanelLayout.createSequentialGroup()
//                        .addComponent(insertCardMsgLabel);
//                GroupLayout.ParallelGroup messageVertical = mainPanelLayout.createParallelGroup()
//                        .addComponent(insertCardMsgLabel);
//
//                if (!renderHeaderPanel) {
//                  messageHorizontal
//                          .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 0, Short.MAX_VALUE)
//                          .addComponent(helpLabel);
//                  messageVertical
//                          .addComponent(helpLabel);
//                }
//
//                mainPanelLayout.setHorizontalGroup(messageHorizontal);
//                mainPanelLayout.setVerticalGroup(messageVertical);
//
//                if (renderCancelButton) {
//                  JButton cancelButton = new JButton();
//                  cancelButton.setFont(cancelButton.getFont().deriveFont(cancelButton.getFont().getStyle() & ~java.awt.Font.BOLD));
//                  cancelButton.setText(getMessage(BUTTON_CANCEL));
//                  cancelButton.addActionListener(cancelListener);
//                  cancelButton.setActionCommand(cancelCommand);
//
//                  GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
//                  buttonPanel.setLayout(buttonPanelLayout);
//
//                  buttonPanelLayout.setHorizontalGroup(
//                    buttonPanelLayout.createSequentialGroup()
//                          .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                          .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, buttonSize, GroupLayout.PREFERRED_SIZE));
//                  buttonPanelLayout.setVerticalGroup(
//                    buttonPanelLayout.createSequentialGroup()
//                      .addComponent(cancelButton));
//                }
//
//                contentPanel.validate();
//            }
//        });
//    }

    /**
     * only difference to showInsertCard: title text: card not supported
     * @param cancelListener
     * @param cancelCommand
     */
//    @Override
//    public void showCardNotSupportedDialog(final ActionListener cancelListener, final String cancelCommand) {
//
//      log.debug("scheduling card not supported dialog");
//
//      SwingUtilities.invokeLater(new Runnable() {
//
//            @Override
//            public void run() {
//
//              log.debug("show card not supported dialog");
//
//              mainPanel.removeAll();
//              buttonPanel.removeAll();
//
//              JLabel insertCardMsgLabel = new JLabel();
//              insertCardMsgLabel.setFont(insertCardMsgLabel.getFont().deriveFont(insertCardMsgLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
//
//              if (renderHeaderPanel) {
//                titleLabel.setText(getMessage(TITLE_CARD_NOT_SUPPORTED));
//                insertCardMsgLabel.setText(getMessage(MESSAGE_INSERTCARD));
//              } else {
//                insertCardMsgLabel.setText(getMessage(TITLE_CARD_NOT_SUPPORTED));
//              }
//
//              helpListener.setHelpTopic(HELP_CARDNOTSUPPORTED);
//
//              GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
//              mainPanel.setLayout(mainPanelLayout);
//
//              GroupLayout.SequentialGroup messageHorizontal = mainPanelLayout.createSequentialGroup()
//                      .addComponent(insertCardMsgLabel);
//              GroupLayout.Group messageVertical = mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                      .addComponent(insertCardMsgLabel);
//              if (!renderHeaderPanel) {
//                messageHorizontal
//                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 0, Short.MAX_VALUE)
//                        .addComponent(helpLabel);
//                messageVertical
//                        .addComponent(helpLabel);
//              }
//
//              mainPanelLayout.setHorizontalGroup(messageHorizontal);
//              mainPanelLayout.setVerticalGroup(messageVertical);
//
//              if (renderCancelButton) {
//                JButton cancelButton = new JButton();
//                cancelButton.setFont(cancelButton.getFont().deriveFont(cancelButton.getFont().getStyle() & ~java.awt.Font.BOLD));
//                cancelButton.setText(getMessage(BUTTON_CANCEL));
//                cancelButton.addActionListener(cancelListener);
//                cancelButton.setActionCommand(cancelCommand);
//
//                GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
//                buttonPanel.setLayout(buttonPanelLayout);
//
//                buttonPanelLayout.setHorizontalGroup(
//                  buttonPanelLayout.createSequentialGroup()
//                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                        .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, buttonSize, GroupLayout.PREFERRED_SIZE));
//                buttonPanelLayout.setVerticalGroup(
//                  buttonPanelLayout.createSequentialGroup()
//                    .addComponent(cancelButton));
//              }
//
//              contentPanel.validate();
//            }
//        });
//    }

  @Override
  public void showCardPINDialog(final PINSpec pinSpec, final int numRetries,
          final ActionListener okListener, final String okCommand,
          final ActionListener cancelListener, final String cancelCommand) {
        
      log.debug("scheduling card-pin dialog");
      
      SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {

              log.debug("[" + Thread.currentThread().getName() + "] show card-pin dialog");
      
                mainPanel.removeAll();
                buttonPanel.removeAll();

                if (renderHeaderPanel) {
                  if (numRetries < 0) {
                      String cardpinTitle = getMessage(TITLE_CARDPIN);
                      titleLabel.setText(MessageFormat.format(cardpinTitle, new Object[]{pinSpec.getLocalizedName()}));
                  } else {
                      titleLabel.setText(getMessage(TITLE_RETRY));
                  }
                }

                JButton okButton = new JButton();
                okButton.setFont(okButton.getFont().deriveFont(okButton.getFont().getStyle() & ~java.awt.Font.BOLD));
                okButton.setText(getMessage(BUTTON_OK));
                okButton.setEnabled(pinSpec.getMinLength() <= 0);
                okButton.setActionCommand(okCommand);
                okButton.addActionListener(okListener);

                JLabel cardPinLabel = new JLabel();
                cardPinLabel.setFont(cardPinLabel.getFont().deriveFont(cardPinLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
                String pinLabel = getMessage(LABEL_PIN);
                cardPinLabel.setText(MessageFormat.format(pinLabel, new Object[]{pinSpec.getLocalizedName()}));

                pinField = new JPasswordField();
                pinField.setText("");
                pinField.setDocument(new PINDocument(pinSpec.getMinLength(), pinSpec.getMaxLength(), pinSpec.getRexepPattern(), okButton));
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
                  String infoPattern = getMessage(MESSAGE_ENTERPIN);
                  if (shortText) {
                    infoLabel.setText(MessageFormat.format(infoPattern, new Object[] {"PIN"}));
                  } else {
                    infoLabel.setText(MessageFormat.format(infoPattern, new Object[] {pinSpec.getLocalizedName()}));
                  }
                  helpMouseListener.setHelpTopic(HELP_CARDPIN);
                  helpKeyListener.setHelpTopic(HELP_CARDPIN);
                } else {
                  String retryPattern;
                  if (numRetries < 2) {
                    retryPattern = getMessage(MESSAGE_LAST_RETRY);
                  } else {
                    retryPattern = getMessage(MESSAGE_RETRIES);
                  }
                  infoLabel.setFont(infoLabel.getFont().deriveFont(infoLabel.getFont().getStyle() | java.awt.Font.BOLD));
                  infoLabel.setText(MessageFormat.format(retryPattern, new Object[]{String.valueOf(numRetries)}));
                  infoLabel.setForeground(ERROR_COLOR);
                  helpMouseListener.setHelpTopic(HELP_RETRY);
                  helpKeyListener.setHelpTopic(HELP_RETRY);
                }
                
                JLabel pinsizeLabel = new JLabel();
                pinsizeLabel.setFont(pinsizeLabel.getFont().deriveFont(pinsizeLabel.getFont().getStyle() & ~java.awt.Font.BOLD, pinsizeLabel.getFont().getSize()-2));
                pinsizeLabel.setText(MessageFormat.format(getMessage(LABEL_PINSIZE), pinSpec.getLocalizedLength()));
                
                GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
                mainPanel.setLayout(mainPanelLayout);

                GroupLayout.SequentialGroup infoHorizontal = mainPanelLayout.createSequentialGroup()
                          .addComponent(infoLabel);
                GroupLayout.ParallelGroup infoVertical = mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                          .addComponent(infoLabel);
                
                if (!renderHeaderPanel) {
                  infoHorizontal
                          .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 0, Short.MAX_VALUE)
                          .addComponent(switchFocusDummyLabel)
                          .addComponent(helpLabel)
                          ;
                  infoVertical
                          .addComponent(switchFocusDummyLabel)
                          .addComponent(helpLabel)
                          ;
                } 

                // align pinfield and pinsize to the right
                GroupLayout.ParallelGroup pinHorizontal = mainPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING);
                GroupLayout.Group pinVertical;

                if (pinLabelPos == PinLabelPosition.ABOVE) {
                  pinHorizontal
                          .addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(cardPinLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(pinField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                          .addComponent(pinsizeLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
                  pinVertical = mainPanelLayout.createSequentialGroup()
                          .addComponent(cardPinLabel)
                          .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                          .addComponent(pinField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
                } else {
                  pinHorizontal
                          .addGroup(mainPanelLayout.createSequentialGroup()
                            .addComponent(cardPinLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(pinField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                          .addComponent(pinsizeLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
                  pinVertical = mainPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                          .addComponent(cardPinLabel)
                          .addComponent(pinField);
                }

                mainPanelLayout.setHorizontalGroup(
                  mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(infoHorizontal)
                    .addGroup(pinHorizontal));

                mainPanelLayout.setVerticalGroup(
                  mainPanelLayout.createSequentialGroup()
                    .addGroup(infoVertical)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(pinVertical)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(pinsizeLabel));


                GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
                buttonPanel.setLayout(buttonPanelLayout);

                GroupLayout.SequentialGroup buttonHorizontal = buttonPanelLayout.createSequentialGroup()
                        .addComponent(okButton, GroupLayout.PREFERRED_SIZE, buttonSize, GroupLayout.PREFERRED_SIZE);
                GroupLayout.Group buttonVertical;
                
                if (renderCancelButton) {
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
                } else {
                  buttonVertical = buttonPanelLayout.createSequentialGroup()
                          .addComponent(okButton);
                }
                
                buttonPanelLayout.setHorizontalGroup(buttonHorizontal);
                buttonPanelLayout.setVerticalGroup(buttonVertical);

//                pinField.requestFocusInWindow();
//                helpLabel.requestFocus();
                pinField.requestFocus();
                contentPanel.validate();

            }
        });
    }

//    @Override
//    public void showCardPINDialog(PINSpec pinSpec, ActionListener okListener, String okCommand, ActionListener cancelListener, String cancelCommand) {
//        showCardPINDialog(pinSpec, -1, okListener, okCommand, cancelListener, cancelCommand);
//    }
//
//    @Override
//    public void showCardPINRetryDialog(PINSpec pinSpec, int numRetries, ActionListener okListener, String okCommand, ActionListener cancelListener, String cancelCommand) {
//        showCardPINDialog(pinSpec, numRetries, okListener, okCommand, cancelListener, cancelCommand);
//    }

//    @Override
//    public void showSignaturePINDialog(PINSpec pinSpec, ActionListener signListener, String signCommand, ActionListener cancelListener, String cancelCommand, ActionListener hashdataListener, String hashdataCommand) {
//        showSignaturePINDialog(pinSpec, -1, signListener, signCommand, cancelListener, cancelCommand, hashdataListener, hashdataCommand);
//    }

    @Override
    public void showPinpadSignaturePINDialog(final PINSpec pinSpec, final int numRetries,
//            final ActionListener cancelListener, final String cancelCommand,
            final ActionListener hashdataListener, final String hashdataCommand) {

        log.debug("scheduling pinpad signature-pin dialog");

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {

              log.debug("[" + Thread.currentThread().getName() + "] show pinpad signature-pin dialog");

                mainPanel.removeAll();
                buttonPanel.removeAll();

                if (renderHeaderPanel) {
                  if (numRetries < 0) {
                      titleLabel.setText(getMessage(TITLE_SIGN));
                  } else {
                      titleLabel.setText(getMessage(TITLE_RETRY));
                  }
                }

                final JLabel infoLabel = new JLabel();
                if (numRetries < 0) {
                  infoLabel.setFont(infoLabel.getFont().deriveFont(infoLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
                  if (shortText) {
                    infoLabel.setText(getMessage(MESSAGE_HASHDATALINK_TINY));
                  } else {
                    infoLabel.setText(getMessage(MESSAGE_HASHDATALINK));
                  }
                  infoLabel.setFocusable(true);
                  infoLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                  infoLabel.setForeground(HYPERLINK_COLOR);
                  infoLabel.addMouseListener(new MouseAdapter() {

                      @Override
                      public void mouseClicked(MouseEvent me) {
                          ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, hashdataCommand);
                          hashdataListener.actionPerformed(e);
                      }
                  });
                  
                  infoLabel.addKeyListener(new KeyAdapter() {
                	  
                 	 @Override
                 	 public void keyPressed(KeyEvent e) {
                 		 
                 		 if(e.getKeyCode() == KeyEvent.VK_ENTER) {                			 
                 			 ActionEvent e1 = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, hashdataCommand);
                              hashdataListener.actionPerformed(e1);
                 		 }
                 	 }
                 	  
                   });  
                  
                  infoLabel.addFocusListener(new FocusAdapter() {
                	  
                	  @Override
                	  public void focusGained(FocusEvent e) {
                		  
                          if (shortText) {
                              infoLabel.setText(getMessage(MESSAGE_HASHDATALINK_TINY_FOCUS));
                            } else {
                              infoLabel.setText(getMessage(MESSAGE_HASHDATALINK_FOCUS));
                            }
                	  }
                	  
                	  @Override
                	  public void focusLost(FocusEvent e) {
                		  
                          if (shortText) {
                              infoLabel.setText(getMessage(MESSAGE_HASHDATALINK_TINY));
                            } else {
                              infoLabel.setText(getMessage(MESSAGE_HASHDATALINK));
                            }
                		  
                	  }                	  
                	  
                  });                  
                  
                  helpMouseListener.setHelpTopic(HELP_SIGNPIN);
                  helpKeyListener.setHelpTopic(HELP_SIGNPIN);
                } else {
                  String retryPattern;
                  if (numRetries < 2) {
                    retryPattern = getMessage(MESSAGE_LAST_RETRY);
                  } else {
                    retryPattern = getMessage(MESSAGE_RETRIES);
                  }
                  infoLabel.setFocusable(true);
                  infoLabel.setText(MessageFormat.format(retryPattern, new Object[]{String.valueOf(numRetries)}));
                  infoLabel.setFont(infoLabel.getFont().deriveFont(infoLabel.getFont().getStyle() | java.awt.Font.BOLD));
                  infoLabel.setForeground(ERROR_COLOR);
                  helpMouseListener.setHelpTopic(HELP_RETRY);
                  helpKeyListener.setHelpTopic(HELP_RETRY);
                }

                String msgPattern = getMessage(MESSAGE_ENTERPIN_PINPAD);
                String msg = MessageFormat.format(msgPattern, new Object[] {
                  pinSpec.getLocalizedName(), pinSpec.getLocalizedLength() });

                JLabel msgLabel = new JLabel();
                msgLabel.setFont(msgLabel.getFont().deriveFont(msgLabel.getFont().getStyle() & ~Font.BOLD));
                msgLabel.setText(msg);

                GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
                mainPanel.setLayout(mainPanelLayout);

                GroupLayout.SequentialGroup infoHorizontal = mainPanelLayout.createSequentialGroup()
                        .addComponent(infoLabel);
                GroupLayout.ParallelGroup infoVertical = mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(infoLabel);

                if (!renderHeaderPanel) {
                  infoHorizontal
                          .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 0, Short.MAX_VALUE)
                          .addComponent(switchFocusDummyLabel)
                          .addComponent(helpLabel)
                          ;
                  infoVertical
                          .addComponent(switchFocusDummyLabel)
                          .addComponent(helpLabel)
                          ;
                }

                mainPanelLayout.setHorizontalGroup(
                  mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(infoHorizontal)
                    .addComponent(msgLabel));

                mainPanelLayout.setVerticalGroup(
                  mainPanelLayout.createSequentialGroup()
                    .addGroup(infoVertical)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(msgLabel));

                //no cancel button (cancel via pinpad)
//                if (renderCancelButton) {
//                    JButton cancelButton = new JButton();
//                    cancelButton.setFont(cancelButton.getFont().deriveFont(cancelButton.getFont().getStyle() & ~java.awt.Font.BOLD));
//                    cancelButton.setText(getMessage(BUTTON_CANCEL));
//                    cancelButton.setActionCommand(cancelCommand);
//                    cancelButton.addActionListener(cancelListener);
//
//                    GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
//                    buttonPanel.setLayout(buttonPanelLayout);
//
//                    GroupLayout.SequentialGroup buttonHorizontal = buttonPanelLayout.createSequentialGroup()
//                            .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, buttonSize, GroupLayout.PREFERRED_SIZE);
//                    GroupLayout.SequentialGroup buttonVertical = buttonPanelLayout.createSequentialGroup()
//                            .addComponent(cancelButton);
//
//                    buttonPanelLayout.setHorizontalGroup(buttonHorizontal);
//                    buttonPanelLayout.setVerticalGroup(buttonVertical);
//                }

                contentPanel.validate();
            }
        });
    }

    @Override
    public void showSignaturePINDialog(final PINSpec pinSpec, final int numRetries,
            final ActionListener signListener, final String signCommand,
            final ActionListener cancelListener, final String cancelCommand,
            final ActionListener hashdataListener, final String hashdataCommand) {
//        showSignaturePINDialog(pinSpec, numRetries, okListener, okCommand, cancelListener, cancelCommand, hashdataListener, hashdataCommand);
//    }
//
//    private void showSignaturePINDialog(final PINSpec pinSpec, final int numRetries, final ActionListener signListener, final String signCommand, final ActionListener cancelListener, final String cancelCommand, final ActionListener hashdataListener, final String hashdataCommand) {

      log.debug("scheduling signature-pin dialog");
      
      SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
              
              log.debug("[" + Thread.currentThread().getName() + "] show signature-pin dialog");
      
                mainPanel.removeAll();
                buttonPanel.removeAll();
                
                if (renderHeaderPanel) {
                  if (numRetries < 0) {
                      titleLabel.setText(getMessage(TITLE_SIGN));
                  } else {
                      titleLabel.setText(getMessage(TITLE_RETRY));
                  }
                }

                final JLabel infoLabel = new JLabel();
                if (numRetries < 0) {
                  infoLabel.setFont(infoLabel.getFont().deriveFont(infoLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
                  if (shortText) {
                    infoLabel.setText(getMessage(MESSAGE_HASHDATALINK_TINY));
                  } else {
                    infoLabel.setText(getMessage(MESSAGE_HASHDATALINK));
                  }
                  infoLabel.setFocusable(true);
                  infoLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                  infoLabel.setForeground(HYPERLINK_COLOR);
                  infoLabel.addMouseListener(new MouseAdapter() {

                      @Override
                      public void mouseClicked(MouseEvent me) {
                          ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, hashdataCommand);
                          hashdataListener.actionPerformed(e);
                      }
                  });
                  
                  infoLabel.addKeyListener(new KeyAdapter() {
                	  
                	 @Override
                	 public void keyPressed(KeyEvent e) {
                		 
                		 if(e.getKeyCode() == KeyEvent.VK_ENTER) {                			 
                			 ActionEvent e1 = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, hashdataCommand);
                             hashdataListener.actionPerformed(e1);
                		 }
                	 }
                	  
                  });
                  
                  infoLabel.addFocusListener(new FocusAdapter() {
                	  
                	  @Override
                	  public void focusGained(FocusEvent e) {
                		  
                		  
                          if (shortText) {
                              infoLabel.setText(getMessage(MESSAGE_HASHDATALINK_TINY_FOCUS));
                            } else {
                              infoLabel.setText(getMessage(MESSAGE_HASHDATALINK_FOCUS));
                            }
                	  }
                	  
                	  @Override
                	  public void focusLost(FocusEvent e) {
                		  
                		  
                          if (shortText) {
                              infoLabel.setText(getMessage(MESSAGE_HASHDATALINK_TINY));
                            } else {
                              infoLabel.setText(getMessage(MESSAGE_HASHDATALINK));
                            }
                		  
                	  }                	  
                	  
                  });
                  
                  helpMouseListener.setHelpTopic(HELP_SIGNPIN);
                  helpKeyListener.setHelpTopic(HELP_SIGNPIN);
                } else {
                  String retryPattern;
                  if (numRetries < 2) {
                    retryPattern = getMessage(MESSAGE_LAST_RETRY);
                  } else {
                    retryPattern = getMessage(MESSAGE_RETRIES);
                  }
                  infoLabel.setFocusable(true);
                  infoLabel.setText(MessageFormat.format(retryPattern, new Object[]{String.valueOf(numRetries)}));
                  infoLabel.setFont(infoLabel.getFont().deriveFont(infoLabel.getFont().getStyle() | java.awt.Font.BOLD));
                  infoLabel.setForeground(ERROR_COLOR);
                  helpMouseListener.setHelpTopic(HELP_RETRY);
                  helpKeyListener.setHelpTopic(HELP_RETRY);
                }

                JButton signButton = new JButton();
                signButton.setFont(signButton.getFont().deriveFont(signButton.getFont().getStyle() & ~java.awt.Font.BOLD));
                signButton.setText(getMessage(BUTTON_SIGN));
                signButton.setEnabled(pinSpec.getMinLength() <= 0);
                signButton.setActionCommand(signCommand);
                signButton.addActionListener(signListener);

                JLabel signPinLabel = new JLabel();
                signPinLabel.setFont(signPinLabel.getFont().deriveFont(signPinLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
                String pinLabel = getMessage(LABEL_PIN);
                signPinLabel.setText(MessageFormat.format(pinLabel, new Object[]{pinSpec.getLocalizedName()}));

                pinField = new JPasswordField();
                pinField.setText("");
                pinField.setDocument(new PINDocument(pinSpec.getMinLength(), pinSpec.getMaxLength(), pinSpec.getRexepPattern(), signButton));
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
                pinsizeLabel.setText(MessageFormat.format(getMessage(LABEL_PINSIZE), pinSpec.getLocalizedLength()));

                GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
                mainPanel.setLayout(mainPanelLayout);

                GroupLayout.SequentialGroup infoHorizontal = mainPanelLayout.createSequentialGroup()
                        .addComponent(infoLabel);
                GroupLayout.ParallelGroup infoVertical = mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(infoLabel);

                if (!renderHeaderPanel) {
                  infoHorizontal
                          .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 0, Short.MAX_VALUE)
                          .addComponent(switchFocusDummyLabel)
                          .addComponent(helpLabel)
                          ;
                  infoVertical
                          .addComponent(switchFocusDummyLabel)
                          .addComponent(helpLabel)
                          ;
                }

                // align pinfield and pinsize to the right
                GroupLayout.Group pinHorizontal = mainPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING);
                GroupLayout.SequentialGroup pinVertical = mainPanelLayout.createSequentialGroup();

                if (pinLabelPos == PinLabelPosition.ABOVE) {
                  pinHorizontal
                          .addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(signPinLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(pinField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                          .addComponent(pinsizeLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
                  pinVertical
                          .addComponent(signPinLabel)
                          .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                          .addComponent(pinField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                          .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                          .addComponent(pinsizeLabel);
                } else { // PinLabelPosition.LEFT
                  pinHorizontal
                          .addGroup(mainPanelLayout.createSequentialGroup()
                            .addComponent(signPinLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(pinField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                          .addComponent(pinsizeLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
                  pinVertical
                          .addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(signPinLabel)
                            .addComponent(pinField))
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

                GroupLayout.SequentialGroup buttonHorizontal = buttonPanelLayout.createSequentialGroup();
                GroupLayout.Group buttonVertical;

                if (renderCancelButton) {
                  JButton cancelButton = new JButton();
                  cancelButton.setFont(cancelButton.getFont().deriveFont(cancelButton.getFont().getStyle() & ~java.awt.Font.BOLD));
                  cancelButton.setText(getMessage(BUTTON_CANCEL));
                  cancelButton.setActionCommand(cancelCommand);
                  cancelButton.addActionListener(cancelListener);

                  buttonHorizontal
                          .addComponent(signButton, GroupLayout.PREFERRED_SIZE, buttonSize, GroupLayout.PREFERRED_SIZE)
                          .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                          .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, buttonSize, GroupLayout.PREFERRED_SIZE)
                          ;
                  buttonVertical = buttonPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                          .addComponent(signButton)
                          .addComponent(cancelButton)
                          ;
                } else {
                  buttonHorizontal
                          .addComponent(signButton, GroupLayout.PREFERRED_SIZE, buttonSize, GroupLayout.PREFERRED_SIZE)
                          ;
                  buttonVertical = buttonPanelLayout.createSequentialGroup()
                          .addComponent(signButton)
                          ;
                }

                buttonPanelLayout.setHorizontalGroup(buttonHorizontal);
                buttonPanelLayout.setVerticalGroup(buttonVertical);

//                pinField.requestFocusInWindow();
//                helpLabel.requestFocus();
                pinField.requestFocus();
                contentPanel.validate();

            }
        });
    }

    @Override
    public void showErrorDialog(
            final String errorMsgKey, final Object[] errorMsgParams,
            final ActionListener okListener, final String okCommand) {
        
      showMessageDialog(TITLE_ERROR, ERROR_COLOR,
              errorMsgKey, errorMsgParams, BUTTON_OK, okListener, okCommand);
    }

    @Override
    public void showErrorDialog(
            final String errorMsgKey, final Object[] errorMsgParams) {

      showMessageDialog(TITLE_ERROR, ERROR_COLOR,
              errorMsgKey, errorMsgParams, null, null, null);
    }

    @Override
    public void showMessageDialog(
            final String titleKey,
            final String msgKey, final Object[] msgParams,
            final String buttonKey,
            final ActionListener okListener, final String okCommand) {

      showMessageDialog(titleKey, null, 
              msgKey, msgParams, buttonKey, okListener, okCommand);
    }

    @Override
    public void showMessageDialog(
            final String titleKey,
            final String msgKey, final Object[] msgParams) {

      showMessageDialog(titleKey, null,
              msgKey, msgParams, null, null, null);
    }

    @Override
    public void showMessageDialog(
            final String titleKey, final String msgKey) {

      showMessageDialog(titleKey, null,
              msgKey, null, null, null, null);
    }

    /**
     *
     * @param buttonKey if null defaults to BUTTON_OK
     */
    private void showMessageDialog(
            final String titleKey, final Color titleColor,
            final String msgKey, final Object[] msgParams,
            final String buttonKey,
            final ActionListener okListener, final String okCommand) {

      log.debug("scheduling message dialog");

      SwingUtilities.invokeLater(new Runnable() {

          @Override
            public void run() {

                log.debug("[" + Thread.currentThread().getName() + "] show message dialog");

                mainPanel.removeAll();
                buttonPanel.removeAll();

                if (renderHeaderPanel) {
                  titleLabel.setText(getMessage(titleKey));
                }

                helpMouseListener.setHelpTopic(msgKey);
                helpKeyListener.setHelpTopic(msgKey);

                String msgPattern = getMessage(msgKey);
                String msg = MessageFormat.format(msgPattern, msgParams);

                JLabel msgLabel = new JLabel();
                msgLabel.setFont(msgLabel.getFont().deriveFont(msgLabel.getFont().getStyle() & ~Font.BOLD));
                msgLabel.setText(msg);

                GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
                mainPanel.setLayout(mainPanelLayout);

                GroupLayout.ParallelGroup mainHorizontal = mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
                GroupLayout.SequentialGroup mainVertical = mainPanelLayout.createSequentialGroup();

                log.debug("focus to contentPanel");
                contentPanel.requestFocus();
                
                if (!renderHeaderPanel) {
                  JLabel titleLabel = new JLabel();
                  titleLabel.setFont(titleLabel.getFont().deriveFont(titleLabel.getFont().getStyle() | Font.BOLD));
                  titleLabel.setText(getMessage(titleKey));
                  if (titleColor != null) {
                    titleLabel.setForeground(titleColor);
                  }

                  mainHorizontal
                          .addGroup(mainPanelLayout.createSequentialGroup()
                            .addComponent(titleLabel)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 0, Short.MAX_VALUE)
                            .addComponent(switchFocusDummyLabel)
                            .addComponent(helpLabel)
                            );
                  mainVertical
                          .addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(titleLabel)
                            .addComponent(switchFocusDummyLabel)
                            .addComponent(helpLabel)
                            );
                  
                  log.debug("focus to helpLabel");
                  helpLabel.requestFocus();
                }

                mainPanelLayout.setHorizontalGroup(mainHorizontal
                        .addComponent(msgLabel));
                mainPanelLayout.setVerticalGroup(mainVertical
                        .addComponent(msgLabel));

                if (okListener != null) {
                	
                  JButton okButton = new JButton();
                  okButton.setFont(okButton.getFont().deriveFont(okButton.getFont().getStyle() & ~java.awt.Font.BOLD));
                  okButton.setText(getMessage((buttonKey != null) ? buttonKey : BUTTON_OK));
                  okButton.setActionCommand(okCommand);
                  okButton.addActionListener(okListener);

                  GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
                  buttonPanel.setLayout(buttonPanelLayout);

                  buttonPanelLayout.setHorizontalGroup(
                    buttonPanelLayout.createSequentialGroup()
                          .addComponent(okButton, GroupLayout.PREFERRED_SIZE, buttonSize, GroupLayout.PREFERRED_SIZE));
                  buttonPanelLayout.setVerticalGroup(
                    buttonPanelLayout.createSequentialGroup()
                      .addComponent(okButton));
                  
                  log.debug("focus to ok-button");
                  okButton.requestFocus();
                }

                contentPanel.validate();
            }
        });
    }

//    @Override
//    public void showWaitDialog(final String waitMessage) {
//
//      log.debug("scheduling wait dialog");
//
//      SwingUtilities.invokeLater(new Runnable() {
//
//            @Override
//            public void run() {
//
//              log.debug("show wait dialog");
//
//                mainPanel.removeAll();
//                buttonPanel.removeAll();
//
//                if (renderHeaderPanel) {
//                  titleLabel.setText(getMessage(TITLE_WAIT));
//                }
//
//                helpListener.setHelpTopic(HELP_WAIT);
//
//                JLabel waitMsgLabel = new JLabel();
//                waitMsgLabel.setFont(waitMsgLabel.getFont().deriveFont(waitMsgLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
//                if (waitMessage != null) {
//                    waitMsgLabel.setText("<html>" + waitMessage + "</html>");
//                } else {
//                    waitMsgLabel.setText(getMessage(MESSAGE_WAIT));
//                }
//
//                GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
//                mainPanel.setLayout(mainPanelLayout);
//
//                GroupLayout.SequentialGroup messageHorizontal = mainPanelLayout.createSequentialGroup()
//                        .addComponent(waitMsgLabel);
//                GroupLayout.ParallelGroup messageVertical = mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                        .addComponent(waitMsgLabel);
//
//                if (!renderHeaderPanel) {
//                  messageHorizontal
//                          .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 0, Short.MAX_VALUE)
//                          .addComponent(helpLabel);
//                  messageVertical
//                          .addComponent(helpLabel);
//                }
//                mainPanelLayout.setHorizontalGroup(messageHorizontal);
//                mainPanelLayout.setVerticalGroup(messageVertical);
//
//                contentPanel.validate();
//            }
//        });
//    }

    @Override
    public char[] getPin() {
        if (pinField != null) {
          char[] pin = pinField.getPassword();
          pinField = null;
          return pin;
        }
        return null;
    }


    ////////////////////////////////////////////////////////////////////////////
    // SECURE VIEWER
    ////////////////////////////////////////////////////////////////////////////

    
    /**
     * @param signedReferences
     * @param backListener gets notified if pin-dialog has to be redrawn
     * (signedRefencesList returns via BACK button)
     * @param okCommand
     */
    @Override
    public void showSecureViewer(final List<HashDataInput> dataToBeSigned,
            final ActionListener backListener, final String backCommand) {
      
      if (dataToBeSigned == null) {
        showErrorDialog(getMessage(ERR_NO_HASHDATA),
                new Object[] {"no signature data provided"},
                backListener, backCommand);
      } else if (dataToBeSigned.size() == 1) {
        try {
          log.debug("[" + Thread.currentThread().getName() + "] scheduling secure viewer");

          SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
              try {
                showSecureViewer(dataToBeSigned.get(0));
              } catch (FontProviderException ex) {
                log.error("failed to display secure viewer", ex);
                showErrorDialog(ERR_VIEWER, new Object[] {ex.getMessage()}, backListener, backCommand);
              }
            }
          });
       
        } catch (Exception ex) { //InterruptedException InvocationTargetException
          log.error("Failed to display secure viewer: " + ex.getMessage());
          log.trace(ex);
          showErrorDialog(ERR_UNKNOWN, null, backListener, backCommand);
        }
      } else {
        showSignedReferencesListDialog(dataToBeSigned, backListener, backCommand);
      }
    }
    
    /**
     * has to be called from event dispatcher thread
     * This method blocks until the dialog's close button is pressed.
     * @param hashDataText
     * @param saveListener
     * @param saveCommand
     */
    private void showSecureViewer(HashDataInput dataToBeSigned) throws FontProviderException {
      
      log.debug("[" + Thread.currentThread().getName() + "] show secure viewer");
      if (secureViewer == null) {
        secureViewer = new SecureViewerDialog(null, messages,
                fontProvider, helpMouseListener.getActionListener());

        // workaround for [#439]
        // avoid AlwaysOnTop at least in applet, otherwise make secureViewer AlwaysOnTop since MOCCA Dialog (JFrame created in LocalSTALFactory) is always on top.
        Window window = SwingUtilities.getWindowAncestor(contentPane);
        if (window != null && window.isAlwaysOnTop()) {
          log.debug("make secureViewer alwaysOnTop");
          secureViewer.setAlwaysOnTop(true);
        }
      }
      secureViewer.setContent(dataToBeSigned);
      log.trace("show secure viewer returned");
    }
    
    private void showSignedReferencesListDialog(final List<HashDataInput> signedReferences,
            final ActionListener backListener, final String backCommand) {
      
      log.debug("[" + Thread.currentThread().getName() + "] scheduling signed references list dialog");
      
      SwingUtilities.invokeLater(new Runnable() {

        @Override
        public void run() {
          
          log.debug("[" + Thread.currentThread().getName() + "] show signed references list dialog");
          
          mainPanel.removeAll();
          buttonPanel.removeAll();

          if (renderHeaderPanel) {
            titleLabel.setText(getMessage(TITLE_HASHDATA));
          }
          
          helpMouseListener.setHelpTopic(HELP_HASHDATALIST);
          helpKeyListener.setHelpTopic(HELP_HASHDATALIST);
          
          JLabel refIdLabel = new JLabel();
          refIdLabel.setFont(refIdLabel.getFont().deriveFont(refIdLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
          String refIdLabelPattern = getMessage(MESSAGE_HASHDATALIST);
          refIdLabel.setText(MessageFormat.format(refIdLabelPattern, new Object[]{signedReferences.size()}));

          HashDataTableModel tableModel = new HashDataTableModel(signedReferences, renderRefId);
          final JTable hashDataTable = new JTable(tableModel);
          hashDataTable.setDefaultRenderer(HashDataInput.class, new HyperlinkRenderer(renderRefId));
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
                    try {
                      showSecureViewer(selection);
                    } catch (FontProviderException ex) {
                      log.error("failed to display secure viewer", ex);
                      showErrorDialog(ERR_VIEWER, new Object[] {ex.getMessage()}, backListener, backCommand);
                    }
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
                    .addComponent(switchFocusDummyLabel)
                    .addComponent(helpLabel)
                    ;
            messageVertical                    
                    .addComponent(switchFocusDummyLabel)
                    .addComponent(helpLabel)
                    ;
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
          backButton.setText(getMessage(BUTTON_BACK));
          backButton.setActionCommand(backCommand);
          backButton.addActionListener(backListener);

          GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
          buttonPanel.setLayout(buttonPanelLayout);

          buttonPanelLayout.setHorizontalGroup(buttonPanelLayout.createSequentialGroup()
                  .addComponent(backButton, GroupLayout.PREFERRED_SIZE, buttonSize, GroupLayout.PREFERRED_SIZE));
          buttonPanelLayout.setVerticalGroup(buttonPanelLayout.createSequentialGroup()
                  .addComponent(backButton));

          contentPanel.validate();
        }
      });
    }
    
    /**
     * @param okListener may be null
     */
//    private void showSaveDialog(final List<HashDataInput> signedRefs,
//            final ActionListener okListener, final String okCommand) {
//
//      log.debug("scheduling save dialog");
//
//      SwingUtilities.invokeLater(new Runnable() {
//
//        @Override
//        public void run() {
//
//          log.debug("show save dialog");
//
//          String userHome = System.getProperty("user.home");
//
//          JFileChooser fileDialog = new JFileChooser(userHome);
//          fileDialog.setMultiSelectionEnabled(false);
//          fileDialog.setDialogType(JFileChooser.SAVE_DIALOG);
//          fileDialog.setFileHidingEnabled(true);
//          if (signedRefs.size() == 1) {
//            fileDialog.setDialogTitle(getMessage(WINDOWTITLE_SAVE));
//            fileDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
//            String mimeType = signedRefs.get(0).getMimeType();
//            MimeFilter mimeFilter = new MimeFilter(mimeType, messages);
//            fileDialog.setFileFilter(mimeFilter);
//            String filename = getMessage(SAVE_HASHDATAINPUT_PREFIX) + MimeFilter.getExtension(mimeType);
//            fileDialog.setSelectedFile(new File(userHome, filename));
//          } else {
//            fileDialog.setDialogTitle(getMessage(WINDOWTITLE_SAVEDIR));
//            fileDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//          }
//
//          //parent contentPane -> placed over applet
//          switch (fileDialog.showSaveDialog(fileDialog)) {
//            case JFileChooser.APPROVE_OPTION:
//              File f = fileDialog.getSelectedFile();
//              for (HashDataInput hashDataInput : signedRefs) {
//                String mimeType = hashDataInput.getMimeType();
//                String id = hashDataInput.getReferenceId();
//                File file;
//                if (f.isDirectory()) {
//                  String filename = getMessage(SAVE_HASHDATAINPUT_PREFIX) + '_' + id + MimeFilter.getExtension(mimeType);
//                  file = new File(f, filename);
//                } else {
//                  file = f;
//                }
//                if (file.exists()) {
//                  String ovrwrt = getMessage(MESSAGE_OVERWRITE);
//                  int overwrite = JOptionPane.showConfirmDialog(fileDialog, MessageFormat.format(ovrwrt, file), getMessage(WINDOWTITLE_OVERWRITE), JOptionPane.OK_CANCEL_OPTION);
//                  if (overwrite != JOptionPane.OK_OPTION) {
//                    continue;
//                  }
//                }
//                if (log.isDebugEnabled()) {
//                    log.debug("writing hashdata input " + id + " (" + mimeType + ") to file " + file);
//                }
//                FileOutputStream fos = null;
//                try {
//                    fos = new FileOutputStream(file);
//                    BufferedOutputStream bos = new BufferedOutputStream(fos);
//                    InputStream hdi = hashDataInput.getHashDataInput();
//                    int b;
//                    while ((b = hdi.read()) != -1) {
//                        bos.write(b);
//                    }
//                    bos.flush();
//                    bos.close();
//                } catch (IOException ex) {
//                    log.error("Failed to write " + file + ": " + ex.getMessage());
//                    showErrorDialog(ERR_WRITE_HASHDATA, new Object[] {ex.getMessage()}, null, null);
//                    ex.printStackTrace();
//                } finally {
//                    try {
//                        fos.close();
//                    } catch (IOException ex) {
//                    }
//                }
//              }
//              break;
//            case JFileChooser.CANCEL_OPTION :
//              log.debug("cancelled save dialog");
//              break;
//          }
//          if (okListener != null) {
//            okListener.actionPerformed(new ActionEvent(fileDialog, ActionEvent.ACTION_PERFORMED, okCommand));
//          }
//        }
//      });
//    }

    ////////////////////////////////////////////////////////////////////////////
    // UTILITY METHODS
    ////////////////////////////////////////////////////////////////////////////

    private void registerHelpListener(ActionListener helpListener) {
      if (helpListener != null) {
        this.helpMouseListener = new HelpMouseListener(helpListener);
        this.helpKeyListener = new HelpKeyListener(helpListener);
      } else {
        log.error("no help listener provided, will not be able to display help");
        this.helpMouseListener = new HelpMouseListener(new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent e) {
            log.error("no help listener registered (requested help topic: " + e.getActionCommand() + ")");
          }
        });
        this.helpKeyListener = new HelpKeyListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
              log.error("no help listener registered (requested help topic: " + e.getActionCommand() + ")");
            }
          });
      }
    }

    private void registerSwitchFocusListener(ActionListener switchFocusListener) {
        if (switchFocusListener != null) {
          this.switchFocusKeyListener = new SwitchFocusFocusListener(switchFocusListener);
          
        } else {
   
          this.switchFocusKeyListener = new SwitchFocusFocusListener(new ActionListener() {

              @Override
              public void actionPerformed(ActionEvent e) {
                log.warn("no switch focus listener registered");
              }
            });          
        }
      }

    ////////////////////////////////////////////////////////////////////////////
    // INITIALIZERS (MAY BE OVERRIDDEN BY SUBCLASSES)
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Load applet messages bundle. Note that getBundle looks for classes based
     * on the default Locale before it selects the base class!
     * 
     * Called from constructor.
     * Subclasses may override this method to ensure the message bundle is loaded
     * once initButtonSize (called from constructor as well) is called.
     * (Only relevant if initButtonSize is overridden as well)
     * @param locale
     */
    protected void loadMessageBundle(Locale locale) {
      if (locale != null) {
        // see [#378] Ignoring post parameter 'locale': bundle resolve-order not correct?!
        Locale lang = new Locale(locale.getLanguage().substring(0, 2));
        log.debug("loading applet resources for language: " + lang.toString());
        messages = ResourceBundle.getBundle(MESSAGES_BUNDLE, lang);
      } else {
        log.debug("loading default language applet resources");
        messages = ResourceBundle.getBundle(MESSAGES_BUNDLE);
      }
      // how the f*** you know the default Messages.properties is de?!
      log.debug("applet messages loaded: " + messages.getLocale());
    }

    protected int initButtonSize() {
      int bs = 0;

      JButton b = new JButton();
      b.setText(getMessage(BUTTON_OK));
      if (b.getPreferredSize().width > bs) {
        bs = b.getPreferredSize().width;
      }
      // need cancel button for message dialog,
      // even if renderCancelButton == false
      b.setText(getMessage(BUTTON_CANCEL));
      if (b.getPreferredSize().width > bs) {
        bs = b.getPreferredSize().width;
      }
      b.setText(getMessage(BUTTON_SIGN));
      if (b.getPreferredSize().width > bs) {
        bs = b.getPreferredSize().width;
      }
      b.setText(getMessage(BUTTON_BACK));
      if (b.getPreferredSize().width > bs) {
        bs = b.getPreferredSize().width;
      }
      b.setText(getMessage(BUTTON_SAVE));
      if (b.getPreferredSize().width > bs) {
        bs = b.getPreferredSize().width;
      }
      return bs;
    }

	@Override
	public void getFocusFromBrowser() {
		
		// This method puts the focus to the helpLabel as this 
		// element is supposed to appear in each dialogue. 			
		helpLabel.requestFocus();
		
	}
}
