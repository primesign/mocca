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

import at.gv.egiz.stal.HashDataInput;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class ViewerDialog extends JDialog
        implements ActionListener {

  public static final String PLAINTEXT_FONT = "Monospaced";
  protected static final Log log = LogFactory.getLog(ViewerDialog.class);
//  private ViewerDialog dialog;
  
  protected ResourceBundle messages;
  
  /**
   * 
   * @param frameComp
   * @param signedReferences currently, only one hashdata input (the first in the list) is displayed
   * @param messages
   * @param saveListener
   * @param saveCommand
   * @param helpListener
   */
  public static void showHashDataInput(Component frameComp,
          List<HashDataInput> hashDataInputs,
          ResourceBundle messages,
          ActionListener saveListener,
          String saveCommand,
          HelpMouseListener helpListener) {
    
    Frame frame = null;
    if (frameComp != null) {
      JOptionPane.getFrameForComponent(frameComp);
    }
    ViewerDialog viewer = new ViewerDialog(frame, 
            messages,
            hashDataInputs, 
            saveListener, 
            saveCommand, 
            helpListener);
    viewer.setVisible(true);
  }

  public static void showHelp(Component frameComp,
          String helpTopic,
//          Reader helpDocument,
          InputStream helpDocument,
          String mimeType,
          ResourceBundle messages) {
    
    Frame frame = null;
    if (frameComp != null) {
      JOptionPane.getFrameForComponent(frameComp);
    }
    ViewerDialog viewer = new ViewerDialog(frame, messages, helpTopic, helpDocument, mimeType);
    viewer.setVisible(true);
  }

  /**
   * TODO make encoding aware!
   * @param frame
   * @param title
   * @param messages
   * @param hashDataInputs
   * @param saveListener
   * @param saveCommand
   * @param helpListener
   */
  private ViewerDialog(Frame frame,
          ResourceBundle messages,
          List<HashDataInput> hashDataInputs,
          ActionListener saveListener,
          String saveCommand,
          HelpMouseListener helpListener) {
    super(frame, messages.getString(BKUGUIFacade.WINDOWTITLE_VIEWER), true);
    this.messages = messages;

    HashDataInput hashData = hashDataInputs.get(0);
    
//    Charset cs;
//    if (hashData.getEncoding() == null) {
//      cs = Charset.forName("UTF-8");
//    } else {
//      try {
//        cs = Charset.forName(hashData.getEncoding());
//      } catch (Exception ex) {
//        log.debug("charset " + hashData.getEncoding() + " not supported, assuming UTF-8: " + ex.getMessage());
//        cs = Charset.forName("UTF-8");
//      }  
//    }
    
//    InputStreamReader isr = new InputStreamReader(hashData.getHashDataInput(), cs);
//    Reader content = new BufferedReader(isr);
    InputStream content = hashData.getHashDataInput();
    String mimeType = hashData.getMimeType();
    String encoding = hashData.getEncoding();
      
    JPanel hashDataPanel = createViewerPanel(messages.getString(BKUGUIFacade.MESSAGE_HASHDATA), content, mimeType, encoding, helpListener);
    JPanel buttonPanel = createButtonPanel(saveListener, saveCommand);
    initContentPane(new Dimension(600, 400), hashDataPanel, buttonPanel);

    pack();
    if (frame != null) {
      setLocationRelativeTo(frame);
    } else {
      setLocationByPlatform(true);
    }
  }

  private ViewerDialog(Frame frame,
          ResourceBundle messages,
          String helpTopic,
//          Reader helpDocument,
          InputStream helpDocument,
          String mimeType) {
    super(frame, messages.getString(BKUGUIFacade.WINDOWTITLE_HELP), true);
    this.messages = messages;
    
    String p = messages.getString(BKUGUIFacade.MESSAGE_HELP);
    String helpItem = messages.getString(helpTopic);
    String viewerLabel = MessageFormat.format(p, new Object[] {helpItem});
    
    JPanel helpPanel = createViewerPanel(viewerLabel, helpDocument, mimeType, null, null);
    JPanel buttonPanel = createButtonPanel();
    
    initContentPane(new Dimension(600, 400), helpPanel, buttonPanel);
    pack();
    if (frame != null) {
      setLocationRelativeTo(frame);
    } else {
      setLocationByPlatform(true);
    }
  }
  
  private void initContentPane(Dimension preferredSize, JPanel viewerPanel, JPanel buttonPanel) {
    Container contentPane = getContentPane();
    contentPane.setPreferredSize(preferredSize);

    GroupLayout mainLayout = new GroupLayout(contentPane);
    contentPane.setLayout(mainLayout);

    mainLayout.setHorizontalGroup(
            mainLayout.createSequentialGroup().addContainerGap().addGroup(
            mainLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(viewerPanel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(buttonPanel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addContainerGap());
    mainLayout.setVerticalGroup(
            mainLayout.createSequentialGroup()
              .addContainerGap()
              .addComponent(viewerPanel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
              .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
              .addComponent(buttonPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
              .addContainerGap());
  }

  /**
   * 
   * @param messages
   * @param content
   * @param mimeType defaults to text/plain if null 
   * @param encoding must be null if document contains charset declaration (e.g. HTML page), otherwise the parser crashes
   * @param helpListener may be null
   * @return
   */
  private JPanel createViewerPanel(String viewerLabelText, InputStream content, String mimeType, String encoding, HelpMouseListener helpListener) {
    log.debug("viewer dialog: " + mimeType);

    if (mimeType == null) {
      mimeType = "text/plain";
    } else if ("application/xhtml+xml".equals(mimeType)) {
      mimeType = "text/html";
    }

    JEditorPane viewer = new JEditorPane();
    viewer.setEditable(false);
    viewer.setContentType(mimeType);
    if ("text/plain".equals(mimeType)) {
      viewer.setFont(new Font(PLAINTEXT_FONT, viewer.getFont().getStyle(), viewer.getFont().getSize()));
    }

    EditorKit editorKit = viewer.getEditorKit();
    Document document = editorKit.createDefaultDocument();
//    document.putProperty("IgnoreCharsetDirective", new Boolean(true));
    
    try {
      if (encoding != null) {
        BufferedReader contentReader = new BufferedReader(new InputStreamReader(content, encoding));
        viewer.read(contentReader, document);
        contentReader.close();
      } else {
        // charset declaration in content
        viewer.read(content, document);
        content.close();
      }
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
      String p = messages.getString(BKUGUIFacade.ERR_VIEWER);
      viewer.setText(MessageFormat.format(p, ex.getMessage()));
    }

    JScrollPane scrollPane = new JScrollPane(viewer);
    scrollPane.setPreferredSize(viewer.getPreferredSize());
    scrollPane.setAlignmentX(LEFT_ALIGNMENT);
    viewer.setCaretPosition(0);

    JLabel viewerLabel = new JLabel();
    viewerLabel.setText(viewerLabelText);
    viewerLabel.setFont(viewerLabel.getFont().deriveFont(viewerLabel.getFont().getStyle() | java.awt.Font.BOLD));
    viewerLabel.setLabelFor(viewer);

    JPanel viewerPanel = new JPanel();
    GroupLayout viewerPanelLayout = new GroupLayout(viewerPanel);
    viewerPanel.setLayout(viewerPanelLayout);

    if (helpListener != null) {
      JLabel helpLabel = new JLabel();
      helpListener.setHelpTopic(BKUGUIFacade.HELP_HASHDATAVIEWER);
      helpLabel.setIcon(new ImageIcon(getClass().getResource(BKUGUIFacade.HELP_IMG)));
      helpLabel.addMouseListener(helpListener);
      helpLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      viewerPanelLayout.setHorizontalGroup(
            viewerPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(viewerPanelLayout.createSequentialGroup().addComponent(viewerLabel).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 0, Short.MAX_VALUE).addComponent(helpLabel)).addComponent(scrollPane)); //, 0, 0, Short.MAX_VALUE));
      viewerPanelLayout.setVerticalGroup(
            viewerPanelLayout.createSequentialGroup()
              .addGroup(viewerPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(viewerLabel)
                .addComponent(helpLabel))
              .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
              .addComponent(scrollPane)); 
    } else {
      viewerPanelLayout.setHorizontalGroup(
            viewerPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
              .addComponent(viewerLabel)
              .addComponent(scrollPane)); 
      viewerPanelLayout.setVerticalGroup(
            viewerPanelLayout.createSequentialGroup()
              .addComponent(viewerLabel)
              .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
              .addComponent(scrollPane)); 

    }
    
    return viewerPanel;
  }

  private JPanel createButtonPanel() {
    JButton closeButton = new JButton();
    closeButton.setText(messages.getString(BKUGUIFacade.BUTTON_CLOSE));
    closeButton.addActionListener(this);

    JPanel buttonPanel = new JPanel();
    GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
    buttonPanel.setLayout(buttonPanelLayout);

    buttonPanelLayout.setHorizontalGroup(
            buttonPanelLayout.createSequentialGroup()
              .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
              .addComponent(closeButton));
    buttonPanelLayout.setVerticalGroup(
            buttonPanelLayout.createSequentialGroup()
              .addComponent(closeButton));
    return buttonPanel;
  }
  
  private JPanel createButtonPanel(ActionListener saveListener, String saveCommand) {
    JButton closeButton = new JButton();
    closeButton.setText(messages.getString(BKUGUIFacade.BUTTON_CLOSE));
    closeButton.addActionListener(this);

    JButton saveButton = new JButton();
    saveButton.setText(messages.getString(BKUGUIFacade.BUTTON_SAVE));
    saveButton.setActionCommand(saveCommand);
    saveButton.addActionListener(saveListener);

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
//    if ("close".equals(e.getActionCommand())) {
//    ViewerDialog.dialog.setVisible(false);
//    HashDataViewer.dialog.dispose();
    this.setVisible(false);
  }
}
