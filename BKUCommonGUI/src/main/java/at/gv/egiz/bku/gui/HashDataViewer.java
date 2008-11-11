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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.InputStreamReader;
import java.io.Reader;
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
        implements ActionListener, WindowListener {

  public static final String PLAINTEXT_FONT = "Monospaced";
    
  protected static final Log log = LogFactory.getLog(HashDataViewer.class);
  private static HashDataViewer dialog;

  public static void showDialog(Component frameComp, List<HashDataInput> signedReferences, ResourceBundle messages, ActionListener saveListener, String saveCommand, HelpMouseListener helpListener) {
    
    log.info("******************* SHOW HASHDATA DIALOG");
    
    Frame frame = JOptionPane.getFrameForComponent(frameComp);

    dialog = new HashDataViewer(frame, signedReferences.get(0), messages, saveListener, saveCommand, helpListener);
    dialog.addWindowListener(dialog);
    dialog.setVisible(true);

  }

  private HashDataViewer(Frame frame, HashDataInput hashData, ResourceBundle messages, ActionListener saveListener, String saveCommand, HelpMouseListener helpListener) {
    super(frame, messages.getString(BKUGUIFacade.WINDOWTITLE_VIEWER), true);

    JPanel viewerPanel = createViewerPanel(messages, hashData, helpListener);
    JPanel buttonPanel = createButtonPanel(messages, saveListener, saveCommand);


    Container contentPane = getContentPane();
    contentPane.setPreferredSize(new Dimension(400, 300));

    GroupLayout mainLayout = new GroupLayout(contentPane);
    contentPane.setLayout(mainLayout);

        mainLayout.setHorizontalGroup(
          mainLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(
              mainLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(viewerPanel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonPanel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addContainerGap());
        mainLayout.setVerticalGroup(
          mainLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(viewerPanel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE) 
            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED) 
            .addComponent(buttonPanel, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
            .addContainerGap());

    pack();
    setLocationRelativeTo(frame);
  }

  private JPanel createViewerPanel(ResourceBundle messages, HashDataInput hashData, HelpMouseListener helpListener) {
    String mimeType = hashData.getMimeType();
    String encoding = hashData.getEncoding();

    log.debug("display hashdata: " + mimeType + ";" + encoding);

    if (mimeType == null) {
      mimeType = "text/plain";
    }
    if (encoding == null) {
      encoding = "UTF-8";
    }

    JEditorPane viewer = new JEditorPane();
    viewer.setEditable(false);
    viewer.setContentType(mimeType);
    if ("text/plain".equals(mimeType)) {
      viewer.setFont(new Font(PLAINTEXT_FONT, viewer.getFont().getStyle(), viewer.getFont().getSize()));
    }
    
    EditorKit editorKit = viewer.getEditorKit();
    Document document = editorKit.createDefaultDocument();

    Reader reader;
    try {
      reader = new InputStreamReader(hashData.getHashDataInput(), encoding);
      viewer.read(reader, document);
    } catch (Exception ex) {
      String p = messages.getString(BKUGUIFacade.ERR_DISPLAY_HASHDATA);
      viewer.setText(MessageFormat.format(p, ex.getMessage()));
    }

    JScrollPane scrollPane = new JScrollPane(viewer);
//    scrollPane.setPreferredSize(new Dimension(400, 300));
    scrollPane.setPreferredSize(viewer.getPreferredSize());
    scrollPane.setAlignmentX(LEFT_ALIGNMENT);

    JLabel viewerTitle = new JLabel();
    viewerTitle.setText(messages.getString(BKUGUIFacade.TITLE_HASHDATA));
    viewerTitle.setFont(viewerTitle.getFont().deriveFont(viewerTitle.getFont().getStyle() | java.awt.Font.BOLD));
    viewerTitle.setLabelFor(viewer);
    
    JLabel helpLabel = new JLabel();
    helpListener.setHelpTopic(BKUGUIFacade.HELP_HASHDATAVIEWER);
    helpLabel.setIcon(new ImageIcon(getClass().getResource(BKUGUIFacade.HELP_IMG))); 
    helpLabel.addMouseListener(helpListener);
    helpLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
    JPanel viewerPanel = new JPanel();
    GroupLayout viewerPanelLayout = new GroupLayout(viewerPanel);
    viewerPanel.setLayout(viewerPanelLayout);

    viewerPanelLayout.setHorizontalGroup(
     viewerPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
      .addGroup(viewerPanelLayout.createSequentialGroup()
        .addComponent(viewerTitle)
        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 0, Short.MAX_VALUE)
        .addComponent(helpLabel))
      .addComponent(scrollPane)); //, 0, 0, Short.MAX_VALUE));

    viewerPanelLayout.setVerticalGroup(
      viewerPanelLayout.createSequentialGroup()
        .addGroup(viewerPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
          .addComponent(viewerTitle)
          .addComponent(helpLabel))
        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(scrollPane)); //, 0, 0, Short.MAX_VALUE));
    
//    viewerPanel.setLayout(new BoxLayout(viewerPanel, BoxLayout.PAGE_AXIS));
//    JLabel title = new JLabel(messages.getString(BKUGUIFacade.TITLE_HASHDATA));
//    title.setLabelFor(viewer);
//    viewerPanel.add(title);
//    viewerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
//    viewerPanel.add(scrollPane);
//    viewerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
    return viewerPanel;
  }

  private JPanel createButtonPanel(ResourceBundle messages, ActionListener saveListener, String saveCommand) {
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
    HashDataViewer.dialog.setVisible(false);
//    HashDataViewer.dialog.dispose();
  }

  @Override
  public void windowOpened(WindowEvent e) {
    log.debug("WINDOW OPENED");
  }

  @Override
  public void windowClosing(WindowEvent e) {
    log.debug("WINDOW CLOSING");
  }

  @Override
  public void windowClosed(WindowEvent e) {
    log.debug("WINDOW CLOSED");
  }

  @Override
  public void windowIconified(WindowEvent e) {
    log.debug("WINDOW ICONIFIED");
  }

  @Override
  public void windowDeiconified(WindowEvent e) {
  }

  @Override
  public void windowActivated(WindowEvent e) {
    log.debug("WINDOW ACTIVATED");
  }

  @Override
  public void windowDeactivated(WindowEvent e) {
    log.debug("WINDOW DEACTIVATED");
  }

  
  
 
  
  
  
}
