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
import at.gv.egiz.bku.gui.viewer.SecureViewerSaveDialog;
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle;
import javax.swing.WindowConstants;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.HTMLEditorKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class SecureViewerDialog extends JDialog {

	private static final long serialVersionUID = 1L;

/** don't import BKUFonts in order not to load BKUFonts.jar
   * BKUApplet includes BKUFonts as runtime dependency only, the jar is copied to the applet dir in BKUOnline with dependency-plugin
   * BKUViewer has compile dependency BKUFonts, transitive in BKUOnline and BKULocal
   */
  public static final Dimension VIEWER_DIMENSION = new Dimension(600, 480);
  
  public static final List<String> SUPPORTED_MIME_TYPES = new ArrayList<String>();
  static {
    SUPPORTED_MIME_TYPES.add("text/plain");
    SUPPORTED_MIME_TYPES.add("application/xhtml+xml");
    SUPPORTED_MIME_TYPES.add("text/html");
  }
  private final Logger log = LoggerFactory.getLogger(SecureViewerDialog.class);
  protected ResourceBundle messages;
  protected JEditorPane viewer;
  protected JLabel viewerLabel;
  protected JScrollPane scrollPane;
  protected HashDataInput content; //remember for save dialog and for resizing
  protected FontProvider fontProvider;
  protected HelpListener helpListener;
  
  protected JButton closeButton;
  protected JButton saveButton;
  
  protected int baseFontSize;
  protected int baseButtonSize;
  
  protected float resizeFactor;
  
  protected ActionListener closeListener;
  protected String closeCommand;

  /**
   * Create and display a modal SecureViewer dialog.
   * This method blocks until the dialog's close button is pressed.
   * 
   * @param owner, dialog is positioned relative to its owner
   * (if null, at default location of native windowing system)
   */
  public SecureViewerDialog(Frame owner, ResourceBundle messages,
          ActionListener closeListener, String closeCommand,
          FontProvider fontProvider,
          HelpListener helpListener, float resizeFactor) {
    super(owner, messages.getString(BKUGUIFacade.WINDOWTITLE_VIEWER), false);
    this.setIconImages(BKUIcons.icons);
    this.messages = messages;
    this.fontProvider = fontProvider;
    this.helpListener = helpListener;
    
    this.baseFontSize = new JLabel().getFont().getSize();
    
    this.resizeFactor = 1.0f;
    this.closeListener = closeListener;
    this.closeCommand = closeCommand;
    
    this.resizeFactor = resizeFactor;
    
    
    initContentPane(VIEWER_DIMENSION,
            createViewerPanel(),
            createButtonPanel(closeListener, closeCommand));

    // also leave defaultWindowClosing HIDE_ON_CLOSE
    this.addWindowListener(new WindowCloseListener(closeListener, closeCommand));
    this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

    pack();
    if (owner != null) {
      setLocationRelativeTo(owner);
    } else {
      setLocationByPlatform(true);
    }
    
    
  }

  public void resize(float resizeFactor) {
	  
	  log.debug("Resizing secure viewer ...");
	  this.resizeFactor = resizeFactor;  
	  
	  getContentPane().removeAll();
	  
	  initContentPane(VIEWER_DIMENSION,
	            createViewerPanel(),
	            createButtonPanel(closeListener, closeCommand));
	  
	  this.setContent(content);
	  
	  getContentPane().validate();
	  
	  
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
  private JPanel createViewerPanel() {
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

    if (helpListener.implementsListener()) {
      final JLabel helpLabel = new JLabel();
      helpLabel.setFocusable(true);
      helpLabel.setIcon(new ImageIcon(getClass().getResource(BKUGUIFacade.HELP_IMG)));
      helpLabel.getAccessibleContext().setAccessibleName(messages.getString(BKUGUIFacade.ALT_HELP));
      helpLabel.addMouseListener(helpListener);
      helpLabel.addKeyListener(helpListener);

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

    log.debug("[{}] set viewer content.", Thread.currentThread().getName());

    helpListener.setHelpTopic(BKUGUIFacade.HELP_HASHDATAVIEWER);
    
    this.content = null;
    viewer.setText(null);

    String mimeType = hashDataInput.getMimeType();
    if (mimeType == null) {
      mimeType = "text/plain";
    }
    log.debug("Secure viewer mime type: {}.", mimeType);
    // loads editorkit for text/plain if unrecognized
    viewer.setContentType(mimeType);

    try {
      
      if ("text/plain".equals(mimeType)) {
        viewer.setEditorKit(new StyledEditorKit());
        viewer.setFont(fontProvider.getFont().deriveFont(Font.PLAIN, viewer.getFont().getSize() * resizeFactor));
      } else if ("application/xhtml+xml".equals(mimeType)) {
        viewer.setEditorKit(new HTMLEditorKit());
        //reset font if fontprovider font was set before (TODO also html font from fontprovider)
        viewer.setFont(new Font("Dialog", Font.PLAIN, (int)(viewer.getFont().getSize() * resizeFactor))); //UIManager.getFont("Label.font"));
      }

      EditorKit editorKit = viewer.getEditorKit();
      Document document = editorKit.createDefaultDocument();
  //    document.putProperty("IgnoreCharsetDirective", new Boolean(true));

      Charset cs = (hashDataInput.getEncoding() == null) ? Charset.forName("UTF-8") : Charset.forName(hashDataInput.getEncoding());
      log.debug("Secure viewer encoding: {}.", cs.toString());

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

    if (viewer.getText() != null) {
		viewer.getAccessibleContext().setAccessibleDescription(
				viewer.getText());
	}
    
    scrollPane.setViewportView(viewer);
    scrollPane.setPreferredSize(viewer.getPreferredSize());
    scrollPane.setAlignmentX(LEFT_ALIGNMENT);

    if ("application/xhtml+xml".equals(mimeType)) {
      viewerLabel.setText(messages.getString(BKUGUIFacade.WARNING_XHTML));
    } else {
      viewerLabel.setText("");
    }

    viewer.setFocusable(Boolean.TRUE);
    
    log.debug("VIEWER FONT: {}.", viewer.getFont());
    setVisible(true);
    toFront();
    
    viewer.requestFocus();
    
  }

  private JPanel createButtonPanel(ActionListener closeListener, String closeCommand) {

	closeButton = new JButton();
	
    closeButton.setText(messages.getString(BKUGUIFacade.BUTTON_CLOSE));
    closeButton.setActionCommand(closeCommand);
    closeButton.addActionListener(new CloseButtonListener(closeListener));
	closeButton.setFont(closeButton.getFont().deriveFont(
				(float) (baseFontSize * resizeFactor)));
 
    saveButton = new JButton();
    saveButton.setText(messages.getString(BKUGUIFacade.BUTTON_SAVE));
    saveButton.addActionListener(new SaveButtonListener());
    saveButton.setFont(saveButton.getFont().deriveFont(
			(float) (baseFontSize * resizeFactor)));

    
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

  public class WindowCloseListener extends WindowAdapter {

    ActionListener closeListener;
    String closeCommand;

    public WindowCloseListener(ActionListener closeListener, String closeCommand) {
      this.closeListener = closeListener;
      this.closeCommand = closeCommand;
    }

    @Override
    public void windowClosing(WindowEvent e) {
      log.trace("[{}] closing secure viewer.", Thread.currentThread().getName());
      setVisible(false);
      if (closeListener != null) {
        closeListener.actionPerformed(new ActionEvent(e.getSource(), e.getID(), closeCommand));
      }
    }
  }

  public class CloseButtonListener implements ActionListener {

    ActionListener closeListener;

    public CloseButtonListener(ActionListener closeListener) {
      this.closeListener = closeListener;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      log.trace("[{}] closing secure viewer.", Thread.currentThread().getName());
      setVisible(false);
      if (closeListener != null) {
        closeListener.actionPerformed(e);
    }
    }
  }

  public class SaveButtonListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      log.trace("[{}] display secure viewer save dialog.", Thread.currentThread().getName());
      SecureViewerSaveDialog.showSaveDialog(SecureViewerDialog.this, content, messages, null, null, closeButton.getFont().getSize());
    }
  }
}
