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

import java.applet.AppletContext;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class HelpViewer extends JDialog
        implements ActionListener {

  public static final String MESSAGE_BUNDLE = "at/gv/egiz/bku/gui/Messages";

  private static final long serialVersionUID = 1L;

  private final Logger log = LoggerFactory.getLogger(HelpViewer.class);
  
  private static HelpViewer dialog;
  protected ResourceBundle messages;
  protected AppletContext ctx;

  /**
   * 
   * @param ctx external links are opened via ctx.showDocument()
   * @param helpURL
   * @param helpTopic
   * @param messages
   */
  public static void showHelpDialog(AppletContext ctx,
          URL helpURL, ResourceBundle messages) {
    showHelpDialog(null, ctx, helpURL, messages); 
  }

  public static void showHelpDialog(URL helpURL, ResourceBundle messages) {
    showHelpDialog(null, null, helpURL, messages);
  }

  public static void showHelpDialog(Component owner,
          AppletContext ctx,
          URL helpURL,
          ResourceBundle messages) {

    Frame frame = null;
    if (owner != null) {
      JOptionPane.getFrameForComponent(owner);
    }
    dialog = new HelpViewer(frame, messages, ctx, helpURL); 
    dialog.setVisible(true);
    dialog.toFront();
  }

  private HelpViewer(Frame frame,
          ResourceBundle messages,
          AppletContext ctx,
          URL helpURL) {
    super(frame, messages.getString(BKUGUIFacade.WINDOWTITLE_HELP), true);
    this.messages = messages;
    this.ctx = ctx;

    log.trace("init help viewer for locale ", messages.getLocale());

    JPanel helpPanel = createViewerPanel(helpURL); //viewerLabel, helpURL);
    JPanel buttonPanel = createButtonPanel();

    initContentPane(new Dimension(600, 600), helpPanel, buttonPanel);
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
            mainLayout.createSequentialGroup().addContainerGap().addComponent(viewerPanel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addComponent(buttonPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addContainerGap());
  }

  private JPanel createViewerPanel(URL helpURL) {  //String viewerLabelText, 
    log.debug("Viewer dialog: {}.", helpURL.toString());

    final JEditorPane viewer = new JEditorPane();
    viewer.setEditable(false);
    try {
      viewer.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      viewer.setPage(helpURL);

      HelpLinkFocusManager editorFocusManager = new HelpLinkFocusManager (viewer);
      viewer.addKeyListener(editorFocusManager );
      
      viewer.addHyperlinkListener(new HyperlinkListener() {

        @Override
        public void hyperlinkUpdate(HyperlinkEvent e) {
          final URL url = e.getURL();
          if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            if (ctx != null) {
              log.debug("Open external link in help viewer: {}.", url);
              ctx.showDocument(url, "_blank");
            } else {
              SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                  String msgP = messages.getString(BKUGUIFacade.ERR_EXTERNAL_LINK);
                  String msg = MessageFormat.format(msgP, url);
                  String title = messages.getString(BKUGUIFacade.TITLE_ERROR);
                  JOptionPane.showMessageDialog(rootPane, msg, title, JOptionPane.ERROR_MESSAGE);
                }
              });
            }
          }
        }
      });
    } catch (IOException ex) {
      String p = messages.getString(BKUGUIFacade.ERR_VIEWER);
      viewer.setText(MessageFormat.format(p, new Object[]{ex.getMessage()}));
    }
    viewer.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    JScrollPane scrollPane = new JScrollPane(viewer);
    scrollPane.setPreferredSize(viewer.getPreferredSize());
    scrollPane.setAlignmentX(LEFT_ALIGNMENT);
    viewer.setCaretPosition(0);

//    JLabel viewerLabel = new JLabel();
//    viewerLabel.setText(viewerLabelText);
//    viewerLabel.setFont(viewerLabel.getFont().deriveFont(viewerLabel.getFont().getStyle() | java.awt.Font.BOLD));
//    viewerLabel.setLabelFor(viewer);

    JPanel viewerPanel = new JPanel();
    GroupLayout viewerPanelLayout = new GroupLayout(viewerPanel);
    viewerPanel.setLayout(viewerPanelLayout);
    
    viewerPanelLayout.setHorizontalGroup(
            viewerPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//              .addComponent(viewerLabel)
              .addComponent(scrollPane));
    viewerPanelLayout.setVerticalGroup(
            viewerPanelLayout.createSequentialGroup()
//            .addComponent(viewerLabel)
//            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(scrollPane));

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
            buttonPanelLayout.createSequentialGroup().addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(closeButton));
    buttonPanelLayout.setVerticalGroup(
            buttonPanelLayout.createSequentialGroup().addComponent(closeButton));
    return buttonPanel;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    HelpViewer.dialog.setVisible(false);
  }
}
