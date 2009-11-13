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

import at.gv.egiz.bku.gui.viewer.FontProvider;
import at.gv.egiz.bku.gui.viewer.FontProviderException;
import at.gv.egiz.bku.gui.viewer.SecureViewerSaveDialog;
import at.gv.egiz.stal.HashDataInput;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.HTMLEditorKit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class SecureViewerDialog extends JDialog implements ActionListener {

  /** don't import BKUFonts in order not to load BKUFonts.jar
   * BKUApplet includes BKUFonts as runtime dependency only, the jar is copied to the applet dir in BKUOnline with dependency-plugin
   * BKUViewer has compile dependency BKUFonts, transitive in BKUOnline and BKULocal
   */
  public static final Dimension VIEWER_DIMENSION = new Dimension(600, 400);
  
  public static final List<String> SUPPORTED_MIME_TYPES = new ArrayList<String>();
  static {
    SUPPORTED_MIME_TYPES.add("text/plain");
    SUPPORTED_MIME_TYPES.add("application/xhtml+xml");
  }
  protected static final Log log = LogFactory.getLog(SecureViewerDialog.class);
//  private static SecureViewerDialog dialog;
  protected ResourceBundle messages;
  protected JEditorPane viewer;
  protected JLabel viewerLabel;
  protected JScrollPane scrollPane;
  protected HashDataInput content; //remember for save dialog
  protected FontProvider fontProvider;

  /**
   * Create and display a modal SecureViewer dialog.
   * This method blocks until the dialog's close button is pressed.
   * 
   * @param owner, dialog is positioned relative to its owner
   * (if null, at default location of native windowing system)
   */
//  public static void showDataToBeSigned(HashDataInput dataToBeSigned,
//          ResourceBundle messages,
//          ActionListener saveListener, String saveCommand,
//          ActionListener helpListener) {
//
////      Frame ownerFrame = (owner != null) ?
////        JOptionPane.getFrameForComponent(owner) :
////        null;
//    dialog = new SecureViewerDialog(null, messages,
//            saveListener, saveCommand, helpListener);
//    dialog.setContent(dataToBeSigned);
//    dialog.setVisible(true);
//  }
  public SecureViewerDialog(Frame owner, ResourceBundle messages,
          ActionListener closeListener, String closeCommand,
          FontProvider fontProvider, ActionListener helpListener) {
    super(owner, messages.getString(BKUGUIFacade.WINDOWTITLE_VIEWER), true);
    this.setIconImages(BKUIcons.icons);
    this.messages = messages;
    this.fontProvider = fontProvider;

    initContentPane(VIEWER_DIMENSION,
            createViewerPanel(helpListener),
            createButtonPanel(closeListener, closeCommand));

    pack();
    if (owner != null) {
      setLocationRelativeTo(owner);
    } else {
      setLocationByPlatform(true);
    }
  }

  private void initContentPane(Dimension preferredSize,
          JPanel viewerPanel, JPanel buttonPanel) {
    Container contentPane = getContentPane();
    contentPane.setPreferredSize(preferredSize);

    GroupLayout mainLayout = new GroupLayout(contentPane);
    contentPane.setLayout(mainLayout);

    mainLayout.setHorizontalGroup(
            mainLayout.createSequentialGroup().addContainerGap().addGroup(
            mainLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(viewerPanel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(buttonPanel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addContainerGap());
    mainLayout.setVerticalGroup(
            mainLayout.createSequentialGroup().addContainerGap().addComponent(viewerPanel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addComponent(buttonPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addContainerGap());
  }

  /**
   * @param helpListener may be null
   */
  private JPanel createViewerPanel(final ActionListener helpListener) {
    viewer = new JEditorPane();
    viewer.setEditable(false);
    viewer.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
    
    scrollPane = new JScrollPane();

    JPanel viewerPanel = new JPanel();
    GroupLayout viewerPanelLayout = new GroupLayout(viewerPanel);
    viewerPanel.setLayout(viewerPanelLayout);

    GroupLayout.SequentialGroup infoHorizontal = viewerPanelLayout.createSequentialGroup();
    GroupLayout.ParallelGroup infoVertical = viewerPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING);

    viewerLabel = new JLabel();
    viewerLabel.setFont(viewerLabel.getFont().deriveFont(viewerLabel.getFont().getStyle() | java.awt.Font.BOLD));
//    viewerLabel.setLabelFor(viewer);

    infoHorizontal.addComponent(viewerLabel);
    infoVertical.addComponent(viewerLabel);

    if (helpListener != null) {
      final JLabel helpLabel = new JLabel();
      helpLabel.setFocusable(true);
      helpLabel.setIcon(new ImageIcon(getClass().getResource(BKUGUIFacade.HELP_IMG)));
      helpLabel.getAccessibleContext().setAccessibleName(messages.getString(BKUGUIFacade.ALT_HELP));
      helpLabel.addMouseListener(new MouseAdapter() {

        @Override
        public void mouseClicked(MouseEvent arg0) {
          ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, BKUGUIFacade.HELP_HASHDATAVIEWER);
          helpListener.actionPerformed(e);
        }
      });
      helpLabel.addKeyListener(new KeyAdapter() {

        @Override
        public void keyPressed(KeyEvent arg0) {

          if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
            ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, BKUGUIFacade.HELP_HASHDATAVIEWER);
            helpListener.actionPerformed(e);
          }
        }
      });

      helpLabel.addFocusListener(new FocusAdapter() {

        @Override
        public void focusGained(FocusEvent e) {

          helpLabel.setIcon(new ImageIcon(getClass().getResource(BKUGUIFacade.HELP_IMG_FOCUS)));
        }

        @Override
        public void focusLost(FocusEvent e) {

          helpLabel.setIcon(new ImageIcon(getClass().getResource(BKUGUIFacade.HELP_IMG)));
        }
      });
      helpLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

      infoHorizontal.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 0, Short.MAX_VALUE).addComponent(helpLabel);
      infoVertical.addComponent(helpLabel);
    }

    viewerPanelLayout.setHorizontalGroup(
            viewerPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(infoHorizontal).addComponent(scrollPane));
    viewerPanelLayout.setVerticalGroup(
            viewerPanelLayout.createSequentialGroup().addGroup(infoVertical).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(scrollPane));

    return viewerPanel;
  }

  /**
   * Sets the hashdataInput to be displayed and makes the dialog visible.
   * This method blocks until the dialog's close button is pressed.
   * 
   * @param mimeType defaults to text/plain if null
   * @param encoding must be null if document contains charset declaration (e.g. HTML page), otherwise the parser crashes

   * @param hashDataInput
   */
  public void setContent(HashDataInput hashDataInput) { //throws FontProviderException {

    log.debug("[" + Thread.currentThread().getName() + "] set viewer content");
    
    this.content = null;
    viewer.setText(null);

    String mimeType = hashDataInput.getMimeType();
    if (mimeType == null) {
      mimeType = "text/plain";
    }
    log.debug("secure viewer mime type: " + mimeType);
    // loads editorkit for text/plain if unrecognized
    viewer.setContentType(mimeType);

    try {
      
      if ("text/plain".equals(mimeType)) {
        viewer.setEditorKit(new StyledEditorKit());
        viewer.setFont(fontProvider.getFont().deriveFont(Font.PLAIN, viewer.getFont().getSize()));
      } else if ("application/xhtml+xml".equals(mimeType)) {
        viewer.setEditorKit(new HTMLEditorKit());
        //reset font if fontprovider font was set before (TODO also html font from fontprovider)
        viewer.setFont(new Font("Dialog", Font.PLAIN, viewer.getFont().getSize())); //UIManager.getFont("Label.font"));
      }

      EditorKit editorKit = viewer.getEditorKit();
      Document document = editorKit.createDefaultDocument();
  //    document.putProperty("IgnoreCharsetDirective", new Boolean(true));

      Charset cs = (hashDataInput.getEncoding() == null) ? Charset.forName("UTF-8") : Charset.forName(hashDataInput.getEncoding());
      log.debug("secure viewer encoding: " + cs.toString());

      InputStreamReader isr = new InputStreamReader(hashDataInput.getHashDataInput(), cs);
      Reader contentReader = new BufferedReader(isr);
      viewer.read(contentReader, document);
      contentReader.close();

      this.content = hashDataInput;

//    } catch (Exception ex) // fontProvider
//    } catch (IllegalCharsetNameException ex) {
//    } catch (UnsupportedCharsetException ex) {
//    } catch (FontProviderException ex) {
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
      String p = messages.getString(BKUGUIFacade.ERR_VIEWER);
      viewer.setContentType("text/plain");
      viewer.setText(MessageFormat.format(p, ex.getMessage()));
    }
    viewer.setCaretPosition(0);

    scrollPane.setViewportView(viewer);
    scrollPane.setPreferredSize(viewer.getPreferredSize());
    scrollPane.setAlignmentX(LEFT_ALIGNMENT);

    if ("application/xhtml+xml".equals(mimeType)) {
      viewerLabel.setText(messages.getString(BKUGUIFacade.WARNING_XHTML));
    } else {
      viewerLabel.setText("");
    }

    log.debug("VIEWER FONT: " + viewer.getFont());
    setVisible(true);
    toFront();
  }

  private JPanel createButtonPanel(ActionListener closeListener, String closeCommand) {
    JButton closeButton = new JButton();
    closeButton.setText(messages.getString(BKUGUIFacade.BUTTON_CLOSE));
    closeButton.setActionCommand(closeCommand);
    closeButton.addActionListener(closeListener);
    closeButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        log.trace("[" + Thread.currentThread().getName() + "] closing secure viewer");
        setVisible(false);
      }
    });

    JButton saveButton = new JButton();
    saveButton.setText(messages.getString(BKUGUIFacade.BUTTON_SAVE));
    saveButton.setActionCommand("save"); //TODO ensure unequal to closeCommand 
    saveButton.addActionListener(this);

    int buttonSize = closeButton.getPreferredSize().width;
    if (saveButton.getPreferredSize().width > buttonSize) {
      buttonSize = saveButton.getPreferredSize().width;
    }

    JPanel buttonPanel = new JPanel();
    GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
    buttonPanel.setLayout(buttonPanelLayout);

    buttonPanelLayout.setHorizontalGroup(
            buttonPanelLayout.createSequentialGroup().addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(saveButton, GroupLayout.PREFERRED_SIZE, buttonSize, GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(closeButton, GroupLayout.PREFERRED_SIZE, buttonSize, GroupLayout.PREFERRED_SIZE));
    buttonPanelLayout.setVerticalGroup(
            buttonPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(saveButton).addComponent(closeButton));

    return buttonPanel;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if ("save".equals(e.getActionCommand())) {
      log.trace("[" + Thread.currentThread().getName() + "] display secure viewer save dialog");
      SecureViewerSaveDialog.showSaveDialog(content, messages, null, null);
      log.trace("done secure viewer save");
    } else {
      log.warn("unknown action command " + e.getActionCommand());
    }
  }
  
  
//  //TEST
//  private static SecureViewerDialog secureViewer;
//  public static void showSecureViewerXXX(HashDataInput dataToBeSigned, ResourceBundle messages, FontProvider fontProvider, ActionListener helpListener, boolean alwaysOnTop) throws FontProviderException {
//    
////    ResourceBundle messages = ResourceBundle.getBundle(BKUGUIFacade.MESSAGES_BUNDLE, locale);
//    
//    log.debug("[" + Thread.currentThread().getName() + "] show secure viewer");
//    if (secureViewer == null) {
//      secureViewer = new SecureViewerDialog(null, messages,
//              fontProvider, helpListener); 
//
//      // workaround for [#439]
//      // avoid AlwaysOnTop at least in applet, otherwise make secureViewer AlwaysOnTop since MOCCA Dialog (JFrame created in LocalSTALFactory) is always on top.
////      Window window = SwingUtilities.getWindowAncestor(contentPane);
////      if (window != null && window.isAlwaysOnTop()) {
////        log.debug("make secureViewer alwaysOnTop");
//        secureViewer.setAlwaysOnTop(alwaysOnTop);
////      }
//    }
//    secureViewer.setContent(dataToBeSigned);
//    log.trace("show secure viewer returned");
//  }

}
