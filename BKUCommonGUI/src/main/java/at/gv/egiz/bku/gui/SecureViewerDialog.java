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
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class SecureViewerDialog extends JDialog implements ActionListener {

  public static final String PLAINTEXT_FONT = "Monospaced";
  public static final Dimension VIEWER_DIMENSION = new Dimension(600, 400);
  protected static final Log log = LogFactory.getLog(SecureViewerDialog.class);

//  private static SecureViewerDialog dialog;
  protected ResourceBundle messages;
  protected JEditorPane viewer;
  protected JLabel viewerLabel;
  protected JScrollPane scrollPane;
  protected HashDataInput content; //remember for save dialog

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
//          ActionListener saveListener, String saveCommand,
          ActionListener helpListener) {
    super(owner, messages.getString(BKUGUIFacade.WINDOWTITLE_VIEWER), true);
    this.messages = messages;

    initContentPane(VIEWER_DIMENSION,
            createViewerPanel(helpListener),
            createButtonPanel()); //saveListener, saveCommand));

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
        	  
        	  if(arg0.getKeyCode() == KeyEvent.VK_ENTER) {
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
  public void setContent(HashDataInput hashDataInput) {

    this.content = null;

    String mimeType = hashDataInput.getMimeType();
    if (mimeType == null) {
      mimeType = "text/plain";
    }
    log.debug("secure viewer mime type: " + mimeType);
    // loads editorkit for text/plain if unrecognized
    viewer.setContentType(mimeType);

    if ("text/plain".equals(mimeType)) {
      viewer.setEditorKit(new StyledEditorKit());
      viewer.setFont(new Font(PLAINTEXT_FONT, viewer.getFont().getStyle(), viewer.getFont().getSize()));
//    } else if ("text/html".equals(mimeType)) {
//      viewer.setEditorKit(new RestrictedHTMLEditorKit());
    } else if ("application/xhtml+xml".equals(mimeType)) {
      viewer.setContentType("text/html");
    }

    EditorKit editorKit = viewer.getEditorKit();
    Document document = editorKit.createDefaultDocument();
//    document.putProperty("IgnoreCharsetDirective", new Boolean(true));

    try {
      Charset cs = (hashDataInput.getEncoding() == null) ? Charset.forName("UTF-8") : Charset.forName(hashDataInput.getEncoding());
      log.debug("secure viewer encoding: " + cs.toString());

      InputStreamReader isr = new InputStreamReader(hashDataInput.getHashDataInput(), cs);
      Reader contentReader = new BufferedReader(isr);
      viewer.read(contentReader, document);
      contentReader.close();

      this.content = hashDataInput;

//    } catch (IllegalCharsetNameException ex) {
//    } catch (UnsupportedCharsetException ex) {
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
      String p = messages.getString(BKUGUIFacade.ERR_VIEWER);
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

    setVisible(true);
    toFront();
  }

  private JPanel createButtonPanel() { //ActionListener saveListener, String saveCommand) {
    JButton closeButton = new JButton();
    closeButton.setText(messages.getString(BKUGUIFacade.BUTTON_CLOSE));
    closeButton.setActionCommand("close");
    closeButton.addActionListener(this);
    
    JButton saveButton = new JButton();
    saveButton.setText(messages.getString(BKUGUIFacade.BUTTON_SAVE));
    saveButton.setActionCommand("save");
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
    if ("close".equals(e.getActionCommand())) {
//    SecureViewerDialog.dialog.setVisible(false);
      log.trace("closing secure viewer");
      setVisible(false);
      log.trace("secure viewer closed");
    } else if ("save".equals(e.getActionCommand())) {
      log.trace("display secure viewer save dialog");
      showSaveDialog(content, null, null);
      log.trace("done secure viewer save");
    } else {
      log.warn("unknown action command " + e.getActionCommand());
    }
  }

  private void showSaveDialog(final HashDataInput hashDataInput,
          final ActionListener okListener, final String okCommand) {

    log.debug("scheduling save dialog [" + Thread.currentThread().getName() + "]");

    SwingUtilities.invokeLater(new Runnable() {

      @Override
      public void run() {

        log.debug("show save dialog [" + Thread.currentThread().getName() + "]");

        String userHome = System.getProperty("user.home");

        JFileChooser fileDialog = new JFileChooser(userHome);
        fileDialog.setMultiSelectionEnabled(false);
        fileDialog.setDialogType(JFileChooser.SAVE_DIALOG);
        fileDialog.setFileHidingEnabled(true);
        fileDialog.setDialogTitle(messages.getString(BKUGUIFacade.WINDOWTITLE_SAVE));
        fileDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
        String mimeType = hashDataInput.getMimeType();
        MimeFilter mimeFilter = new MimeFilter(mimeType, messages);
        fileDialog.setFileFilter(mimeFilter);
        String filename = messages.getString(BKUGUIFacade.SAVE_HASHDATAINPUT_PREFIX) +
                MimeFilter.getExtension(mimeType);
        fileDialog.setSelectedFile(new File(userHome, filename));

        //parent contentPane -> placed over applet
        switch (fileDialog.showSaveDialog(fileDialog)) {
          case JFileChooser.APPROVE_OPTION:
            File file = fileDialog.getSelectedFile();
            String id = hashDataInput.getReferenceId();
            if (file.exists()) {
              String msgPattern = messages.getString(BKUGUIFacade.MESSAGE_OVERWRITE);
              int overwrite = JOptionPane.showConfirmDialog(fileDialog,
                      MessageFormat.format(msgPattern, file),
                      messages.getString(BKUGUIFacade.WINDOWTITLE_OVERWRITE),
                      JOptionPane.OK_CANCEL_OPTION);
              if (overwrite != JOptionPane.OK_OPTION) {
                return;
              }
            }
            if (log.isDebugEnabled()) {
              log.debug("writing hashdata input " + id + " (" + mimeType + ") to file " + file);
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
              log.error("Failed to write " + file + ": " + ex.getMessage());
              log.debug(ex);
              String errPattern = messages.getString(BKUGUIFacade.ERR_WRITE_HASHDATA);
              JOptionPane.showMessageDialog(fileDialog,
                      MessageFormat.format(errPattern, ex.getMessage()),
                      messages.getString(BKUGUIFacade.WINDOWTITLE_ERROR),
                      JOptionPane.ERROR_MESSAGE);
            } finally {
              try {
                if (fos != null) {
                  fos.close();
                }
              } catch (IOException ex) {
              }
            }
            break;
          case JFileChooser.CANCEL_OPTION:
            log.debug("cancelled save dialog");
            break;
        }
        if (okListener != null) {
          okListener.actionPerformed(new ActionEvent(fileDialog, ActionEvent.ACTION_PERFORMED, okCommand));
        }
      }
    });
  }
}
