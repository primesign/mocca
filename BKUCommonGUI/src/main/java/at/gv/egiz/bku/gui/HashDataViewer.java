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
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class HashDataViewer extends JDialog
        implements ActionListener {

  public static final String PLAINTEXT_FONT = "Monospaced";
  protected static final Log log = LogFactory.getLog(HashDataViewer.class);

  private static HashDataViewer dialog;
  
  protected ResourceBundle messages;
  
  /**
   * 
   * @param signedReferences currently, only one hashdata input (the first in the list) is displayed
   */
  public static void showHashDataInput(List<HashDataInput> hashDataInputs,
          ResourceBundle messages,
          ActionListener saveListener,
          String saveCommand,
          ActionListener helpListener) {
    showHashDataInput(null, hashDataInputs, messages, saveListener, saveCommand, helpListener);
  }
  
  /**
   * 
   * @param frameComp owner
   */
  public static void showHashDataInput(Component frameComp,
          List<HashDataInput> hashDataInputs,
          ResourceBundle messages,
          ActionListener saveListener,
          String saveCommand,
          ActionListener helpListener) {
    
    Frame frame = null;
    if (frameComp != null) {
      JOptionPane.getFrameForComponent(frameComp);
    }
    dialog = new HashDataViewer(frame, 
            messages,
            hashDataInputs, 
            saveListener, 
            saveCommand, 
            helpListener);
    dialog.setVisible(true);
  }

  private HashDataViewer(Frame frame,
          ResourceBundle messages,
          List<HashDataInput> hashDataInputs,
          ActionListener saveListener,
          String saveCommand,
          ActionListener helpListener) {
    super(frame, messages.getString(BKUGUIFacade.WINDOWTITLE_VIEWER), true);
    this.messages = messages;

    HashDataInput hashData = hashDataInputs.get(0);
    
    Charset cs;
    if (hashData.getEncoding() == null) {
      cs = Charset.forName("UTF-8");
    } else {
      try {
        cs = Charset.forName(hashData.getEncoding());
      } catch (Exception ex) {
        log.debug("charset " + hashData.getEncoding() + " not supported, assuming UTF-8: " + ex.getMessage());
        cs = Charset.forName("UTF-8");
      }  
    }
    
    
    InputStreamReader isr = new InputStreamReader(hashData.getHashDataInput(), cs);
    Reader content = new BufferedReader(isr);
  
    JPanel hashDataPanel = createViewerPanel(
            messages.getString(BKUGUIFacade.MESSAGE_HASHDATA), 
            content, 
            hashData.getMimeType(), 
            helpListener);
    JPanel buttonPanel = createButtonPanel(saveListener, saveCommand);
    initContentPane(new Dimension(600, 400), hashDataPanel, buttonPanel);

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
  private JPanel createViewerPanel(String viewerLabelText, 
          Reader content, 
          String mimeType, 
          final ActionListener helpListener) {
    log.debug("viewer dialog: " + mimeType);

    if (mimeType == null) {
      mimeType = "text/plain";
    } else if ("application/xhtml+xml".equals(mimeType)) {
      mimeType = "text/html";
    }

    JEditorPane viewer = new JEditorPane();
    
    if ("text/plain".equals(mimeType)) {
      viewer.setFont(new Font(PLAINTEXT_FONT, viewer.getFont().getStyle(), viewer.getFont().getSize()));
//    } else if ("text/html".equals(mimeType)) {
//      viewer.setEditorKitForContentType("text/html", new RestrictedHTMLEditorKit());
    }
    viewer.setEditable(false);
    viewer.setContentType(mimeType);

    EditorKit editorKit = viewer.getEditorKit();
    Document document = editorKit.createDefaultDocument();
//    document.putProperty("IgnoreCharsetDirective", new Boolean(true));
    
    try {
        viewer.read(content, document);
        content.close();
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
      helpLabel.setIcon(new ImageIcon(getClass().getResource(BKUGUIFacade.HELP_IMG)));
      helpLabel.getAccessibleContext().setAccessibleName(messages.getString(BKUGUIFacade.ALT_HELP));
      helpLabel.addMouseListener(new MouseAdapter() {

        @Override
        public void mouseClicked(MouseEvent arg0) {
            ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, BKUGUIFacade.HELP_HASHDATAVIEWER);
            helpListener.actionPerformed(e);
        }
      });
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
    HashDataViewer.dialog.setVisible(false);
  }
}
