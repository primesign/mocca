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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.gv.egiz.bku.gui;

import at.gv.egiz.smcc.PINSpec;
import at.gv.egiz.stal.HashDataInput;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author clemens
 */
public class BKUGUI implements BKUGUIFacade {

    private static final Log log = LogFactory.getLog(BKUGUI.class);
    public static final String MESSAGES_BUNDLE = "at/gv/egiz/bku/gui/Messages";
    public static final String LOGO_RESOURCE = "/images/logo.png";
//    public static final int MAIN_PANEL_WIDTH = 100;
//    public static final int MAIN_PANEL_HEIGHT = 20;
//    public static final int PREF_SIZE_PINFIELD = 118;
    public static final Color ERROR_COLOR = Color.RED;
    public static final Color HYPERLINK_COLOR = Color.BLUE;
    private static final String TITLE_WELCOME = "title.welcome";
    private static final String TITLE_INSERTCARD = "title.insertcard";
    private static final String TITLE_CARD_NOT_SUPPORTED = "title.cardnotsupported";
    private static final String TITLE_CARDPIN = "title.cardpin";
    private static final String TITLE_SIGN = "title.sign";
    private static final String TITLE_ERROR = "title.error";
    private static final String TITLE_RETRY = "title.retry";
    private static final String TITLE_WAIT = "title.wait";
    private static final String TITLE_HASHDATA = "title.hashdata";
    private static final String WINDOWTITLE_SAVE = "windowtitle.save";
    private static final String WINDOWTITLE_OVERWRITE = "windowtitle.overwrite";
    private static final String MESSAGE_WAIT = "message.wait";
    private static final String MESSAGE_INSERTCARD = "message.insertcard";
    private static final String MESSAGE_HASHDATALINK = "message.hashdatalink";
    private static final String MESSAGE_HASHDATA = "message.hashdata";
    private static final String MESSAGE_HASHDATALIST = "message.hashdatalist";
    private static final String MESSAGE_RETRIES = "message.retries";
    private static final String MESSAGE_OVERWRITE = "message.overwrite";
    private static final String LABEL_PIN = "label.pin";
    private static final String LABEL_PINSIZE = "label.pinsize";
    private static final String ERROR_HASHDATA = "error.hashdata";
    
    private static final String BUTTON_OK = "button.ok";
    private static final String BUTTON_CANCEL = "button.cancel";
    private static final String BUTTON_BACK = "button.back";
    private static final String BUTTON_SIGN = "button.sign";
    private static final String BUTTON_SAVE = "button.save";
    private static final String MIMETYPE_DESC_XML = "mimetype.desc.xml";
    private static final String MIMETYPE_DESC_HTML = "mimetype.desc.html";
    private static final String MIMETYPE_DESC_XHTML = "mimetype.desc.xhtml";
    private static final String MIMETYPE_DESC_TXT = "mimetype.desc.txt";
    private static final String MIMETYPE_DESC_PDF = "mimetype.desc.pdf";
    private static final String MIMETYPE_DESC_BIN = "mimetype.desc.bin";
    private static final String SAVE_HASHDATAINPUT_PREFIX = "save.hashdatainput.prefix";
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
    /** remember the pinfield to return to worker */
    protected JPasswordField pinField;

    protected JButton okButton;
    protected JButton cancelButton;
    protected JButton signButton;
    protected JButton saveButton;
    protected JButton backButton;
    protected int buttonSize;
    
    private static final int CHECKBOX_WIDTH = new JCheckBox().getPreferredSize().width;
    
    /**
     * @param contentPane
     * @param localeString may be null
     */
    @Override
    public void init(final Container contentPane, String localeString) {

        if (localeString != null) {
            messages = ResourceBundle.getBundle(MESSAGES_BUNDLE, new Locale(localeString));
        } else {
            messages = ResourceBundle.getBundle(MESSAGES_BUNDLE);
        }

        this.contentPane = contentPane;

        try {

            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {

                    initIconPanel();
                    initContentPanel();

                    GroupLayout layout = new GroupLayout(contentPane);
                    contentPane.setLayout(layout);
                    layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addComponent(iconPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(contentPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
                    layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(iconPanel, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(contentPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
                }
            });
        } catch (Exception ex) {
            throw new RuntimeException("Failed to init GUI: " + ex.getMessage());
        }
    }

    protected void initIconPanel() {
        iconPanel = new JPanel();
        JLabel iconLabel = new JLabel();
        iconLabel.setIcon(new ImageIcon(getClass().getResource(LOGO_RESOURCE))); // NOI18N

        GroupLayout iconPanelLayout = new GroupLayout(iconPanel);
        iconPanel.setLayout(iconPanelLayout);
        iconPanelLayout.setHorizontalGroup(
          iconPanelLayout.createSequentialGroup()
//          iconPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(iconPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(iconLabel, GroupLayout.PREFERRED_SIZE, iconLabel.getPreferredSize().width, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)); //);
        iconPanelLayout.setVerticalGroup(
          iconPanelLayout.createSequentialGroup()
//          iconPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(iconPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(iconLabel, GroupLayout.PREFERRED_SIZE, iconLabel.getPreferredSize().height, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)); //);
    }

    protected void initContentPanel() {

        contentPanel = new JPanel();

        headerPanel = new JPanel();
        mainPanel = new JPanel();
        buttonPanel = new JPanel();

//        headerPanel.setBorder(new TitledBorder("header"));
//        mainPanel.setBorder(new TitledBorder("main"));
//        buttonPanel.setBorder(new TitledBorder("button"));

        titleLabel = new JLabel();
        titleLabel.setFont(titleLabel.getFont().deriveFont(titleLabel.getFont().getStyle() |
          java.awt.Font.BOLD, titleLabel.getFont().getSize() + 2));
//        titleLabel.setForeground(defaultForground);

        okButton = new JButton();
        okButton.setText(messages.getString(BUTTON_OK));
        cancelButton = new JButton();
        cancelButton.setText(messages.getString(BUTTON_CANCEL));
        signButton = new JButton();
        signButton.setText(messages.getString(BUTTON_SIGN));
        backButton = new JButton();
        backButton.setText(messages.getString(BUTTON_BACK));
        saveButton = new JButton();
        saveButton.setText(messages.getString(BUTTON_SAVE));
//        contentPanelLayout.linkSize(cancelButton, okButton, signButton, backButton, saveButton);        
        
        if (okButton.getPreferredSize().width > buttonSize)
            buttonSize = okButton.getPreferredSize().width;
        if (cancelButton.getPreferredSize().width > buttonSize)
            buttonSize = cancelButton.getPreferredSize().width;
        if (signButton.getPreferredSize().width > buttonSize)
            buttonSize = signButton.getPreferredSize().width;
        if (backButton.getPreferredSize().width > buttonSize)
            buttonSize = backButton.getPreferredSize().width;
        if (saveButton.getPreferredSize().width > buttonSize)
            buttonSize = saveButton.getPreferredSize().width;

        
        GroupLayout headerPanelLayout = new GroupLayout(headerPanel);
        headerPanel.setLayout(headerPanelLayout);

        headerPanelLayout.setHorizontalGroup(
          headerPanelLayout.createSequentialGroup()
//          headerPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(titleLabel, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));
        headerPanelLayout.setVerticalGroup(
          headerPanelLayout.createSequentialGroup()
//          headerPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(titleLabel, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

//        mainPanel.setPreferredSize(new Dimension(MAIN_PANEL_WIDTH, MAIN_PANEL_HEIGHT));

        GroupLayout contentPanelLayout = new GroupLayout(contentPanel);
        contentPanel.setLayout(contentPanelLayout);
        contentPanelLayout.setHorizontalGroup(
          contentPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(headerPanel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(mainPanel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(buttonPanel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)); //));
        contentPanelLayout.setVerticalGroup(
          contentPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(headerPanel, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(mainPanel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE) 
            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED) //, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(buttonPanel, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
            .addContainerGap()); //);
    }

    @Override
    public void showLoginDialog(ActionListener loginListener, String actionCommand) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                mainPanel.removeAll();
                buttonPanel.removeAll();

                titleLabel.setText(messages.getString(TITLE_ERROR));
//                titleLabel.setForeground(defaultForground);

                JLabel waitMsgLabel = new JLabel();
                waitMsgLabel.setFont(waitMsgLabel.getFont().deriveFont(waitMsgLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
                waitMsgLabel.setText("<html>LoginDialog not supported yet.</html>");
                waitMsgLabel.setForeground(ERROR_COLOR);

                GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
                mainPanel.setLayout(mainPanelLayout);

                mainPanelLayout.setHorizontalGroup(
                  mainPanelLayout.createSequentialGroup()
//                  mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                  .addComponent(waitMsgLabel)
                  .addContainerGap()); //, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));
                mainPanelLayout.setVerticalGroup(
                  mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                  .addComponent(waitMsgLabel));

                contentPanel.validate();
            }
        });
    }

    @Override
    public void showWelcomeDialog() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                mainPanel.removeAll();
                buttonPanel.removeAll();

                titleLabel.setText(messages.getString(TITLE_WELCOME));
//                titleLabel.setForeground(defaultForground);

                JLabel waitMsgLabel = new JLabel();
                waitMsgLabel.setFont(waitMsgLabel.getFont().deriveFont(waitMsgLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
                waitMsgLabel.setText(messages.getString(MESSAGE_WAIT));

                GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
                mainPanel.setLayout(mainPanelLayout);

                mainPanelLayout.setHorizontalGroup(
                  mainPanelLayout.createSequentialGroup()
//                  mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                  .addComponent(waitMsgLabel)
                  .addContainerGap()); //, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));
                mainPanelLayout.setVerticalGroup(
                  mainPanelLayout.createSequentialGroup()
//                  mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                  .addComponent(waitMsgLabel));

                contentPanel.validate();

            }
        });
    }

    @Override
    public void showInsertCardDialog(final ActionListener cancelListener, final String cancelCommand) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                mainPanel.removeAll();
                buttonPanel.removeAll();

                titleLabel.setText(messages.getString(TITLE_INSERTCARD));

                cancelButton.addActionListener(cancelListener);
                cancelButton.setActionCommand(cancelCommand);

                GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
                buttonPanel.setLayout(buttonPanelLayout);

//                buttonPanelLayout.linkSize(cancelButton, okButton, signButton, backButton, saveButton);        
                buttonPanelLayout.setHorizontalGroup(
                  buttonPanelLayout.createSequentialGroup()
//                  buttonPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                    .addGroup(GroupLayout.Alignment.TRAILING, buttonPanelLayout.createSequentialGroup()
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, buttonSize, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap()); //);
                buttonPanelLayout.setVerticalGroup(
                  buttonPanelLayout.createSequentialGroup()
//                  buttonPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(cancelButton));

                contentPanel.validate();
            }
        });
    }

    @Override
    public void showCardNotSupportedDialog(final ActionListener cancelListener, final String cancelCommand) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                mainPanel.removeAll();
                buttonPanel.removeAll();

                titleLabel.setText(messages.getString(TITLE_CARD_NOT_SUPPORTED));
//                titleLabel.setForeground(defaultForground);

                JLabel errorMsgLabel = new JLabel();
                errorMsgLabel.setFont(errorMsgLabel.getFont().deriveFont(errorMsgLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
                errorMsgLabel.setText(messages.getString(MESSAGE_INSERTCARD));

                GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
                mainPanel.setLayout(mainPanelLayout);

                mainPanelLayout.setHorizontalGroup(
                  mainPanelLayout.createSequentialGroup()
//                  mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(errorMsgLabel)); //, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));
                mainPanelLayout.setVerticalGroup(
                  mainPanelLayout.createSequentialGroup()
//                  mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(errorMsgLabel));

                cancelButton.addActionListener(cancelListener);
                cancelButton.setActionCommand(cancelCommand);

                GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
                buttonPanel.setLayout(buttonPanelLayout);

//                buttonPanelLayout.linkSize(cancelButton, okButton, signButton, backButton, saveButton);        
                buttonPanelLayout.setHorizontalGroup(
                  buttonPanelLayout.createSequentialGroup()
//                  buttonPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                    .addGroup(GroupLayout.Alignment.TRAILING, buttonPanelLayout.createSequentialGroup()
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, buttonSize, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap()); //);
                buttonPanelLayout.setVerticalGroup(
                  buttonPanelLayout.createSequentialGroup()
//                  buttonPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(cancelButton));
                
                contentPanel.validate();
            }
        });
    }

    private void showCardPINDialog(final PINSpec pinSpec, final int numRetries, final ActionListener okListener, final String okCommand, final ActionListener cancelListener, final String cancelCommand) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {

                mainPanel.removeAll();
                buttonPanel.removeAll();

                if (numRetries < 0) {
                    String cardpinTitle = messages.getString(TITLE_CARDPIN);
                    titleLabel.setText(MessageFormat.format(cardpinTitle, new Object[]{pinSpec.getLocalizedName()}));
                } else {
                    titleLabel.setText(messages.getString(TITLE_RETRY));
                }

                cancelButton.setActionCommand(cancelCommand);
                cancelButton.addActionListener(cancelListener);

                okButton.setEnabled(false);
                okButton.setActionCommand(okCommand);
                okButton.addActionListener(okListener);

                JLabel cardPinLabel = new JLabel();
                cardPinLabel.setFont(cardPinLabel.getFont().deriveFont(cardPinLabel.getFont().getStyle() | java.awt.Font.BOLD));
                String pinLabel = messages.getString(LABEL_PIN);
                cardPinLabel.setText(MessageFormat.format(pinLabel, new Object[]{pinSpec.getLocalizedName()}));

//                JPasswordField cardPINField = new JPasswordField();
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
                    String pinsizePattern = messages.getString(LABEL_PINSIZE);
                    String pinSize = String.valueOf(pinSpec.getMinLength());
                    if (pinSpec.getMinLength() != pinSpec.getMaxLength()) {
                        pinSize += "-" + pinSpec.getMaxLength();
                    }
                    infoLabel.setText(MessageFormat.format(pinsizePattern, new Object[]{pinSize}));
                } else {
                    infoLabel.setText(MessageFormat.format(messages.getString(MESSAGE_RETRIES), new Object[]{String.valueOf(numRetries)}));
                    infoLabel.setForeground(ERROR_COLOR);
                }

//                GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
//                mainPanel.setLayout(mainPanelLayout);
//
//                mainPanelLayout.setHorizontalGroup(
//                  mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                  .addGroup(mainPanelLayout.createSequentialGroup() //                  .addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                  //                  .addComponent(hashDataLabel)
//                  //                  .addGroup(GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
//                  .addComponent(cardPinLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED) //RELATED)
//                  .addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING) //TRAILING)
//                  .addComponent(infoLabel) //, GroupLayout.DEFAULT_SIZE, PREF_SIZE_PINFIELD, Short.MAX_VALUE)
//                  .addComponent(pinField, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)) //))
//                  .addContainerGap()));
//
//                mainPanelLayout.setVerticalGroup(
//                  mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(mainPanelLayout.createSequentialGroup().addContainerGap() //                  .addComponent(hashDataLabel).addGap(14, 14, 14)
//                  .addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE) //, false)
//                  .addComponent(cardPinLabel) //, GroupLayout.PREFERRED_SIZE, 17, GroupLayout.PREFERRED_SIZE)
//                  .addComponent(pinField)) //, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE))
//                  .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(infoLabel).addGap(cardPinLabel.getFont().getSize()))); //10, 10, 10)));
//
//                GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
//                buttonPanel.setLayout(buttonPanelLayout);
//
//                buttonPanelLayout.setHorizontalGroup(
//                  buttonPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(GroupLayout.Alignment.TRAILING, buttonPanelLayout.createSequentialGroup().addContainerGap(15, Short.MAX_VALUE).addComponent(okButton).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(cancelButton).addContainerGap()));
//                buttonPanelLayout.setVerticalGroup(
//                  buttonPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(buttonPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(okButton).addComponent(cancelButton)));

                
                GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
                mainPanel.setLayout(mainPanelLayout);

                mainPanelLayout.setHorizontalGroup(
                  mainPanelLayout.createSequentialGroup()
//                        .addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                            .addComponent(hashDataLabel)
//                        .addGroup(GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                        .addComponent(cardPinLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                            .addComponent(pinField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE) //))
                            .addComponent(infoLabel))
                        .addContainerGap());

                mainPanelLayout.setVerticalGroup(
                  mainPanelLayout.createSequentialGroup()
//                        .addComponent(hashDataLabel)
//                        .addGap(hashDataLabel.getFont().getSize())
                        .addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(cardPinLabel)
                            .addComponent(pinField))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(infoLabel));
//                        .addGap(signPinLabel.getFont().getSize())); 

                GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
                buttonPanel.setLayout(buttonPanelLayout);

//                buttonPanelLayout.linkSize(cancelButton, okButton, signButton, backButton, saveButton);        
                buttonPanelLayout.setHorizontalGroup(
                  buttonPanelLayout.createSequentialGroup()
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(okButton, GroupLayout.PREFERRED_SIZE, buttonSize, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, buttonSize, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap());//);
                buttonPanelLayout.setVerticalGroup(
                  buttonPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE) 
                        .addComponent(okButton)
                        .addComponent(cancelButton)); 

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
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                mainPanel.removeAll();
                buttonPanel.removeAll();

                if (numRetries < 0) {
                    titleLabel.setText(messages.getString(TITLE_SIGN));
                } else {
                    titleLabel.setText(messages.getString(TITLE_RETRY));
                }

                cancelButton.setActionCommand(cancelCommand);
                cancelButton.addActionListener(cancelListener);

                signButton.setEnabled(false);
                signButton.setActionCommand(signCommand);
                signButton.addActionListener(signListener);

                JLabel signPinLabel = new JLabel();
                signPinLabel.setFont(signPinLabel.getFont().deriveFont(signPinLabel.getFont().getStyle() | java.awt.Font.BOLD));
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

                //pinsize or error label
                JLabel infoLabel = new JLabel();
                infoLabel.setFont(infoLabel.getFont().deriveFont(infoLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
                if (numRetries < 0) {
                    String pinsizePattern = messages.getString(LABEL_PINSIZE);
                    String pinSize = String.valueOf(pinSpec.getMinLength());
                    if (pinSpec.getMinLength() != pinSpec.getMaxLength()) {
                        pinSize += "-" + pinSpec.getMaxLength();
                    }
                    infoLabel.setText(MessageFormat.format(pinsizePattern, new Object[]{pinSize}));
                } else {
                    infoLabel.setText(MessageFormat.format(messages.getString(MESSAGE_RETRIES), new Object[]{String.valueOf(numRetries)}));
                    infoLabel.setForeground(ERROR_COLOR);
                }

                JLabel hashDataLabel = new JLabel();
                hashDataLabel.setFont(hashDataLabel.getFont().deriveFont(hashDataLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
                hashDataLabel.setText(messages.getString(MESSAGE_HASHDATALINK));
                hashDataLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                hashDataLabel.setForeground(HYPERLINK_COLOR);
                hashDataLabel.addMouseListener(new MouseAdapter() {

                    @Override
                    public void mouseClicked(MouseEvent me) {
                        ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, hashdataCommand);
                        hashdataListener.actionPerformed(e);
                    }
                });

                GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
                mainPanel.setLayout(mainPanelLayout);

                mainPanelLayout.setHorizontalGroup(
                  mainPanelLayout.createSequentialGroup()
                        .addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(hashDataLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addGroup(GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                                .addComponent(signPinLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                    .addComponent(pinField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(infoLabel))))
                        .addContainerGap());

                mainPanelLayout.setVerticalGroup(
                  mainPanelLayout.createSequentialGroup()
                        .addComponent(hashDataLabel)
                        .addGap(hashDataLabel.getFont().getSize())
                        .addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(signPinLabel)
                            .addComponent(pinField))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(infoLabel));
//                        .addGap(signPinLabel.getFont().getSize())); 

                GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
                buttonPanel.setLayout(buttonPanelLayout);

//                buttonPanelLayout.linkSize(cancelButton, okButton, signButton, backButton, saveButton);        
                buttonPanelLayout.setHorizontalGroup(
                  buttonPanelLayout.createSequentialGroup()
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(signButton, GroupLayout.PREFERRED_SIZE, buttonSize, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, buttonSize, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap());//);
                buttonPanelLayout.setVerticalGroup(
                  buttonPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE) 
                        .addComponent(signButton)
                        .addComponent(cancelButton)); 

                pinField.requestFocusInWindow();
                contentPanel.validate();

            }//            private ParallelGroup createMainGroup(GroupLayout mainPanelLayout, JLabel hashDataLabel, JLabel signPinLabel, JLabel pinSizeLabel, JLabel errorLabel) {
//                ParallelGroup mainGroup = mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
//                mainGroup.addComponent(hashDataLabel);
//                if (errorLabel != null) {
//                    mainGroup.addComponent(errorLabel);
//                }
//                mainGroup.addGroup(GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
//                                                                    .addComponent(signPinLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
//                                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                                                                    .addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
//                                                                                                .addComponent(pinSizeLabel)
//                                                                                                .addComponent(pinField, GroupLayout.DEFAULT_SIZE, PREF_SIZE_PINFIELD, Short.MAX_VALUE)));
//                return mainGroup;
//            }
//            private GroupLayout.SequentialGroup createVerticalMainGroup(GroupLayout mainPanelLayout, JLabel hashDataLabel, JLabel signPinLabel, JLabel pinSizeLabel, JLabel errorLabel) {
//                GroupLayout.SequentialGroup mainGroup = mainPanelLayout.createSequentialGroup();
//                mainGroup.addComponent(hashDataLabel)
//                    .addGap(hashDataLabel.getFont().getSize()); //14, 14, 14)
//                    
//                if (errorLabel != null) {
//                    mainGroup.addComponent(errorLabel)
//                      .addGap(errorLabel.getFont().getSize());
//                }
//                mainGroup.addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE, false)
//                        .addComponent(signPinLabel) //, GroupLayout.PREFERRED_SIZE, 17, GroupLayout.PREFERRED_SIZE)
//                        .addComponent(pinField)) //, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE))
//                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                    .addComponent(pinSizeLabel)
//                    .addGap(signPinLabel.getFont().getSize());
//                
//                return mainGroup;
////                mainPanelLayout.createSequentialGroup()
////                    .addComponent(hashDataLabel)
////                    .addGap(hashDataLabel.getFont().getSize()) //14, 14, 14)
////                    .addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE, false)
////                        .addComponent(signPinLabel) //, GroupLayout.PREFERRED_SIZE, 17, GroupLayout.PREFERRED_SIZE)
////                        .addComponent(pinField)) //, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE))
////                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
////                    .addComponent(pinSizeLabel)
////                    .addGap(signPinLabel.getFont().getSize())
////                    
////                    
//            }
        });
    }

    @Override
    public void showErrorDialog(final String errorMsg, final ActionListener okListener, final String okCommand) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                mainPanel.removeAll();
                buttonPanel.removeAll();

                titleLabel.setText(messages.getString(TITLE_ERROR));
//                titleLabel.setForeground(defaultForground);

                JLabel errorMsgLabel = new JLabel();
                errorMsgLabel.setFont(errorMsgLabel.getFont().deriveFont(errorMsgLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
                errorMsgLabel.setText("<html>" + errorMsg + "</html>");
                errorMsgLabel.setForeground(ERROR_COLOR);

                GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
                mainPanel.setLayout(mainPanelLayout);

                mainPanelLayout.setHorizontalGroup(
                  mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                  .addComponent(errorMsgLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));
                mainPanelLayout.setVerticalGroup(
                  mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                  .addComponent(errorMsgLabel));

                okButton.setActionCommand(okCommand);
                okButton.addActionListener(okListener);

                GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
                buttonPanel.setLayout(buttonPanelLayout);

//                buttonPanelLayout.linkSize(cancelButton, okButton, signButton, backButton, saveButton);        
                buttonPanelLayout.setHorizontalGroup(
                  buttonPanelLayout.createSequentialGroup()
//                  buttonPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                    .addGroup(GroupLayout.Alignment.TRAILING, buttonPanelLayout.createSequentialGroup()
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(okButton, GroupLayout.PREFERRED_SIZE, buttonSize, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap()); //);
                buttonPanelLayout.setVerticalGroup(
                  buttonPanelLayout.createSequentialGroup()
//                  buttonPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(okButton));

                contentPanel.validate();
            }
        });
    }

    @Override
    public void showErrorDialog(final String errorMsg) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                mainPanel.removeAll();
                buttonPanel.removeAll();

                titleLabel.setText(messages.getString(TITLE_ERROR));
//                titleLabel.setForeground(defaultForground);

                JLabel errorMsgLabel = new JLabel();
                errorMsgLabel.setFont(errorMsgLabel.getFont().deriveFont(errorMsgLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
                errorMsgLabel.setText("<html>" + errorMsg + "</html>");
                errorMsgLabel.setForeground(ERROR_COLOR);

                GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
                mainPanel.setLayout(mainPanelLayout);

                mainPanelLayout.setHorizontalGroup(
                  mainPanelLayout.createSequentialGroup()
//                  mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                  .addComponent(errorMsgLabel)
                  .addContainerGap()); //, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));
                mainPanelLayout.setVerticalGroup(
                  mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                  .addComponent(errorMsgLabel));

                contentPanel.validate();
            }
        });
    }

    @Override
    public void showHashDataInputDialog(final List<HashDataInput> signedReferences, final ActionListener okListener, final String okCommand) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {

                if (signedReferences != null && signedReferences.size() == 1) {

                    final HashDataInput signedRef = signedReferences.get(0);
                    if ("text/plain".equals(signedRef.getMimeType())) {
                        //TODO get encoding from mimetype
                        //read directly to byte[] since hashDataIS is backed by byte[] ?
                        ByteArrayOutputStream baos = null;
                        try {
                            String refId = signedRef.getReferenceId();
                            InputStream hashDataIS = signedRef.getHashDataInput();
                            if (hashDataIS == null) {
                                showErrorDialog("Failed to obtain HashDataInput for reference " + refId, okListener, okCommand);
                            } else {
                                baos = new ByteArrayOutputStream(hashDataIS.available());
                                int c;
                                while ((c = hashDataIS.read()) != -1) {
                                    baos.write(c);
                                }
                                String text = baos.toString("UTF-8");

                                ActionListener al = new ActionListener() {

                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        showSaveHashDataInputDialog(signedRef, okListener, okCommand);
                                    }
                                };
                                showPlainTextHashDataInputDialog(text, refId, okListener, okCommand, al, "save");
                            }
                        } catch (IOException ex) {
                            showErrorDialog("Failed to read HashDataInput for reference " + signedRef.getReferenceId() + ": " + ex.getMessage(), okListener, okCommand);
                        } finally {
                            try {
                                baos.close();
                            } catch (IOException ex) {
                            }
                        }
                    } else {
                        showSaveHashDataInputDialog(signedRef, okListener, okCommand);
                    }

                } else if (signedReferences != null && signedReferences.size() > 1) {
                    final HashDataTableModel tableModel = new HashDataTableModel(signedReferences);

                    ActionListener al = new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            List<String> selection = tableModel.getSelectedReferenceIds();
                            StringBuilder sb = new StringBuilder("<html>Not implemented yet. \n");
                            for (String string : selection) {
                                sb.append(string);
                                sb.append('\n');
                            }
                            showErrorDialog(sb.toString(), okListener, okCommand);
//                            showSaveHashDataInputDialog(signedRef, okListener, okCommand);
                        }
                    };
                    showMultipleHashDataInputDialog(tableModel, okListener, okCommand, al, "save");
                } else {
                    showErrorDialog(messages.getString(ERROR_HASHDATA), okListener, okCommand);
                }
            }
        });
    }

    private void showPlainTextHashDataInputDialog(String text, String refId, ActionListener cancelListener, String cancelCommand, ActionListener saveListener, String saveCommand) {
        mainPanel.removeAll();
        buttonPanel.removeAll();

        titleLabel.setText(messages.getString(TITLE_HASHDATA));

        JLabel refIdLabel = new JLabel();
        refIdLabel.setFont(refIdLabel.getFont().deriveFont(refIdLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
        String refIdLabelPattern = messages.getString(MESSAGE_HASHDATA);
        refIdLabel.setText(MessageFormat.format(refIdLabelPattern, new Object[]{refId}));

        JScrollPane hashDataScrollPane = new JScrollPane();
        JTextArea hashDataTextArea = new JTextArea(text);
        hashDataTextArea.setEditable(false);
        hashDataTextArea.setColumns(1);
        hashDataTextArea.setRows(1);
        hashDataScrollPane.setViewportView(hashDataTextArea);

        GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);

        mainPanelLayout.setHorizontalGroup(
          mainPanelLayout.createSequentialGroup()
                .addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(refIdLabel)
                    .addComponent(hashDataScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap());

        mainPanelLayout.setVerticalGroup(
          mainPanelLayout.createSequentialGroup()
                .addComponent(refIdLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(hashDataScrollPane, 0, GroupLayout.PREFERRED_SIZE, hashDataTextArea.getPreferredSize().height+3));

        
        
//        GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
//        mainPanel.setLayout(mainPanelLayout);
//
//        mainPanelLayout.setHorizontalGroup(
//          mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(mainPanelLayout.createSequentialGroup().addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(refIdLabel).addComponent(hashDataScrollPane, GroupLayout.PREFERRED_SIZE, PREF_SIZE_PINFIELD, Short.MAX_VALUE)).addContainerGap()));
//
//        mainPanelLayout.setVerticalGroup(
//          mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(mainPanelLayout.createSequentialGroup().addComponent(refIdLabel).addGap(refIdLabel.getFont().getSize()).addComponent(hashDataScrollPane).addGap(refIdLabel.getFont().getSize())));

        backButton.setActionCommand(cancelCommand);
        backButton.addActionListener(cancelListener);

        saveButton.setActionCommand(saveCommand);
        saveButton.addActionListener(saveListener);
        
        GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
        buttonPanel.setLayout(buttonPanelLayout);

//        buttonPanelLayout.linkSize(cancelButton, okButton, signButton, backButton, saveButton);        
        buttonPanelLayout.setHorizontalGroup(
          buttonPanelLayout.createSequentialGroup()
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(saveButton, GroupLayout.PREFERRED_SIZE, buttonSize, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(backButton, GroupLayout.PREFERRED_SIZE, buttonSize, GroupLayout.PREFERRED_SIZE)
                .addContainerGap());
        buttonPanelLayout.setVerticalGroup(
          buttonPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE) 
                .addComponent(saveButton)
                .addComponent(backButton)); 


//        GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
//        buttonPanel.setLayout(buttonPanelLayout);
//
//        buttonPanelLayout.setHorizontalGroup(
//          buttonPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(GroupLayout.Alignment.TRAILING, buttonPanelLayout.createSequentialGroup().addContainerGap(15, Short.MAX_VALUE).addComponent(saveButton).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(cancelButton).addContainerGap()));
//        buttonPanelLayout.setVerticalGroup(
//          buttonPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(buttonPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(saveButton).addComponent(cancelButton)));

        contentPanel.validate();
    }

    private void showMultipleHashDataInputDialog(final TableModel signedReferences, final ActionListener cancelListener, final String cancelCommand, ActionListener saveListener, String saveCommand) {
        mainPanel.removeAll();
        buttonPanel.removeAll();

        titleLabel.setText(messages.getString(TITLE_HASHDATA));

        JLabel refIdLabel = new JLabel();
        refIdLabel.setFont(refIdLabel.getFont().deriveFont(refIdLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
        String refIdLabelPattern = messages.getString(MESSAGE_HASHDATALIST);
        refIdLabel.setText(MessageFormat.format(refIdLabelPattern, new Object[]{signedReferences.getRowCount()}));

        JTable hashDataTable = new JTable();
        hashDataTable.setModel(signedReferences);
        hashDataTable.setTableHeader(null);
//        hashDataTable.setShowVerticalLines(false);
//        hashDataTable.setRowSelectionAllowed(false);
        TableColumn selectCol = hashDataTable.getColumnModel().getColumn(1);
        selectCol.setMinWidth(CHECKBOX_WIDTH);
        selectCol.setMaxWidth(CHECKBOX_WIDTH);
        
        
        hashDataTable.setPreferredScrollableViewportSize(mainPanel.getPreferredSize());
        
        JScrollPane hashDataScrollPane = new JScrollPane(hashDataTable);
        
        GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);

        mainPanelLayout.setHorizontalGroup(
          mainPanelLayout.createSequentialGroup()
                .addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(refIdLabel)
                    .addComponent(hashDataScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap());

        mainPanelLayout.setVerticalGroup(
          mainPanelLayout.createSequentialGroup()
                .addComponent(refIdLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(hashDataScrollPane, 0, GroupLayout.PREFERRED_SIZE, hashDataTable.getPreferredSize().height+3));
//                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED));

        
        
//        
//        GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
//        mainPanel.setLayout(mainPanelLayout);
//
//        mainPanelLayout.setHorizontalGroup(
//          mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(mainPanelLayout.createSequentialGroup()
//                .addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                    .addComponent(refIdLabel)
//                    .addComponent(hashDataScrollPane, GroupLayout.PREFERRED_SIZE, PREF_SIZE_PINFIELD, Short.MAX_VALUE))
//                    .addContainerGap()));
//
//        mainPanelLayout.setVerticalGroup(
//          mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//          .addGroup(mainPanelLayout.createSequentialGroup()
//            .addComponent(refIdLabel)
//            .addGap(refIdLabel.getFont().getSize())
//            .addComponent(hashDataScrollPane)
//            .addGap(refIdLabel.getFont().getSize())));

        
        
        
        
        
        
        
        
        
        
        
//        GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
//        buttonPanel.setLayout(buttonPanelLayout);
//
//        buttonPanelLayout.setHorizontalGroup(
//          buttonPanelLayout.createSequentialGroup()
////          buttonPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
////            .addGroup(GroupLayout.Alignment.TRAILING, buttonPanelLayout.createSequentialGroup()
//                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                .addComponent(saveButton)
//                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                .addComponent(cancelButton)
//                .addContainerGap()); //);
//        buttonPanelLayout.setVerticalGroup(
//          buttonPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//          .addGroup(GroupLayout.Alignment.TRAILING, buttonPanelLayout.createSequentialGroup()
//            .addGroup(buttonPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                .addComponent(saveButton)
//                .addComponent(cancelButton))
//            .addContainerGap()));

        
        backButton.setActionCommand(cancelCommand);
        backButton.addActionListener(cancelListener);

        saveButton.setActionCommand(saveCommand);
        saveButton.addActionListener(saveListener);
        
        GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
        buttonPanel.setLayout(buttonPanelLayout);

//        buttonPanelLayout.linkSize(cancelButton, okButton, signButton, backButton, saveButton);        
        buttonPanelLayout.setHorizontalGroup(
          buttonPanelLayout.createSequentialGroup()
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(saveButton, GroupLayout.PREFERRED_SIZE, buttonSize, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(backButton, GroupLayout.PREFERRED_SIZE, buttonSize, GroupLayout.PREFERRED_SIZE)
                .addContainerGap());//);
        buttonPanelLayout.setVerticalGroup(
          buttonPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE) 
                .addComponent(saveButton)
                .addComponent(backButton)); 

        contentPanel.validate();
    }

    private void showSaveHashDataInputDialog(HashDataInput signedRef, ActionListener okListener, String okCommand) {
        String dir = System.getProperty("user.home");
        JFileChooser fileDialog = new JFileChooser(dir);
        fileDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileDialog.setMultiSelectionEnabled(false);
        fileDialog.setDialogTitle(messages.getString(WINDOWTITLE_SAVE));
        fileDialog.setDialogType(JFileChooser.SAVE_DIALOG);
        fileDialog.setFileHidingEnabled(true);
        MimeFilter mimeFilter = new MimeFilter(signedRef.getMimeType());
        fileDialog.setFileFilter(mimeFilter);
        String filename = messages.getString(SAVE_HASHDATAINPUT_PREFIX) + mimeFilter.getExtension();
        fileDialog.setSelectedFile(new File(dir, filename));
        switch (fileDialog.showSaveDialog(contentPane)) {
            case JFileChooser.APPROVE_OPTION:
                File f = fileDialog.getSelectedFile();
                if (f.exists()) {
//                                    log.debug("hashDataInput file exists, overwrite?");
                    String ovrwrt = messages.getString(MESSAGE_OVERWRITE);
                    int overwrite = JOptionPane.showConfirmDialog(contentPane, MessageFormat.format(ovrwrt, filename), messages.getString(WINDOWTITLE_OVERWRITE), JOptionPane.OK_CANCEL_OPTION);
                    if (overwrite != JOptionPane.OK_OPTION) {
//                                        log.debug("User canceled overwrite HashDataInput, returning to SignaturePin dialog");
                        okListener.actionPerformed(new ActionEvent(fileDialog, ActionEvent.ACTION_PERFORMED, okCommand));
                        return;
                    }
                }
                if (log.isDebugEnabled()) {
                    log.debug("Writing HashDataInput " + signedRef.getReferenceId() + " (" + signedRef.getMimeType() + ") to file " + f);
                }
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(f);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    InputStream hdi = signedRef.getHashDataInput();
                    int b;
                    while ((b = hdi.read()) != -1) {
                        bos.write(b);
                    }
                    bos.flush();
                    bos.close();
                } catch (IOException ex) {
                    log.error("Failed to write HashDataInput to file " + f + ": " + ex.getMessage());
                    showErrorDialog("Failed to write signed reference to file: " + ex.getMessage(), null, null);
                    ex.printStackTrace();
                } finally {
                    try {
                        fos.close();
                    } catch (IOException ex) {
                    }
                }
        }
        okListener.actionPerformed(new ActionEvent(fileDialog, ActionEvent.ACTION_PERFORMED, okCommand));
    }

    @Override
    public void showWaitDialog(final String waitMessage) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                mainPanel.removeAll();
                buttonPanel.removeAll();

                titleLabel.setText(messages.getString(TITLE_WAIT));

                JLabel waitMsgLabel = new JLabel();
                waitMsgLabel.setFont(waitMsgLabel.getFont().deriveFont(waitMsgLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
                if (waitMessage != null) {
                    waitMsgLabel.setText("<html>" + waitMessage + "</html>");
                } else {
                    waitMsgLabel.setText("<html>" + messages.getString(MESSAGE_WAIT) + "</html>");
                }

                GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
                mainPanel.setLayout(mainPanelLayout);

                mainPanelLayout.setHorizontalGroup(
                  mainPanelLayout.createSequentialGroup()
//                  mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                  .addComponent(waitMsgLabel)
                  .addContainerGap()); //, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));
                mainPanelLayout.setVerticalGroup(
                  mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                  .addComponent(waitMsgLabel));

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

    class MimeFilter extends FileFilter {

        protected String mimeType;

        public MimeFilter(String mimeType) {
            this.mimeType = mimeType;
        }

        @Override
        public boolean accept(File f) {

            if (f.isDirectory()) {
                return true;
            }

            String ext = getExtension(f);
            if ("text/xml".equals(mimeType)) {
                return "xml".equalsIgnoreCase(ext);
            } else if ("text/html".equals(mimeType)) {
                return "html".equalsIgnoreCase(ext) || "htm".equalsIgnoreCase(ext);
            } else if ("application/xhtml+xml".equals(mimeType)) {
                return "xhtml".equalsIgnoreCase(ext);
            } else if ("text/plain".equals(mimeType)) {
                return "txt".equalsIgnoreCase(ext);
            } else if ("application/pdf".equals(mimeType)) {
                return "pdf".equalsIgnoreCase(ext);
            } else {
                return true;
            }
        }

        private String getExtension(File f) {
            String ext = null;
            String s = f.getName();
            int i = s.lastIndexOf('.');

            if (i > 0 && i < s.length() - 1) {
                ext = s.substring(i + 1).toLowerCase();
            }
            return ext;
        }

        @Override
        public String getDescription() {
            if ("text/xml".equals(mimeType)) {
                return messages.getString(MIMETYPE_DESC_XML);
            } else if ("text/html".equals(mimeType)) {
                return messages.getString(MIMETYPE_DESC_HTML);
            } else if ("application/xhtml+xml".equals(mimeType)) {
                return messages.getString(MIMETYPE_DESC_XHTML);
            } else if ("text/plain".equals(mimeType)) {
                return messages.getString(MIMETYPE_DESC_TXT);
            } else if ("application/pdf".equals(mimeType)) {
                return messages.getString(MIMETYPE_DESC_PDF);
            } else {
                return messages.getString(MIMETYPE_DESC_BIN);
            }
        }

        public String getExtension() {
            if ("text/xml".equals(mimeType)) {
                return ".xml";
            } else if ("text/html".equals(mimeType)) {
                return ".html";
            } else if ("application/xhtml+xml".equals(mimeType)) {
                return ".xhtml";
            } else if ("text/plain".equals(mimeType)) {
                return ".txt";
            } else if ("application/pdf".equals(mimeType)) {
                return ".pdf";
            } else {
                return ".bin";
            }
        }
    }

    class HashDataTableModel extends DefaultTableModel {

        Class[] types = new Class[]{
            java.lang.String.class, java.lang.Boolean.class
        };
        
        public HashDataTableModel(List<HashDataInput> signedReferences) {
            super(0, 2);
            for (HashDataInput hashDataInput : signedReferences) {
                addRow(new Object[]{hashDataInput.getReferenceId(), new Boolean(true)});
            }
        }

        @Override
        public Class getColumnClass(int columnIndex) {
            return types [columnIndex];
        }

        public List<String> getSelectedReferenceIds() {
            ArrayList<String> selection = new ArrayList<String>();
            for (Object row : dataVector) {
                if ((Boolean) ((Vector) row).elementAt(1)) {
                    selection.add((String) ((Vector) row).elementAt(0));
                }
            }
            return selection;
        }
        
    }
}
