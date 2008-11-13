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

import at.gv.egiz.bku.gui.*;
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
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class HelpViewer extends JDialog
        implements ActionListener {

  protected static final Log log = LogFactory.getLog(HelpViewer.class);
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
          URL helpURL,
          String helpTopic,
          ResourceBundle messages) {
    showHelpDialog(null, ctx, helpURL, helpTopic, messages);
  }

  public static void showHelpDialog(URL helpURL,
          String helpTopic,
          ResourceBundle messages) {
    showHelpDialog(null, null, helpURL, helpTopic, messages);
  }

  public static void showHelpDialog(Component owner,
          AppletContext ctx,
          URL helpURL,
          String helpTopic,
          ResourceBundle messages) {

    Frame frame = null;
    if (owner != null) {
      JOptionPane.getFrameForComponent(owner);
    }
    dialog = new HelpViewer(frame, messages, ctx, helpURL, helpTopic);
    dialog.setVisible(true);
  }

  private HelpViewer(Frame frame,
          ResourceBundle messages,
          AppletContext ctx,
          URL helpURL,
          String helpTopic) {
    super(frame, messages.getString(BKUGUIFacade.WINDOWTITLE_HELP), true);
    this.messages = messages;
    this.ctx = ctx;

    String p = messages.getString(BKUGUIFacade.MESSAGE_HELP);
    String helpItem = messages.getString(helpTopic);
    String viewerLabel = MessageFormat.format(p, new Object[]{helpItem});

    JPanel helpPanel = createViewerPanel(viewerLabel, helpURL);
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
            mainLayout.createSequentialGroup().addContainerGap().addComponent(viewerPanel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addComponent(buttonPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addContainerGap());
  }

  private JPanel createViewerPanel(String viewerLabelText, URL helpURL) {
    log.debug("viewer dialog: " + helpURL.toString());

    final JEditorPane viewer = new JEditorPane();
    viewer.setEditable(false);
    try {
      viewer.setPage(helpURL);
      viewer.addHyperlinkListener(new HyperlinkListener() {

        @Override
        public void hyperlinkUpdate(HyperlinkEvent e) {
          final URL url = e.getURL();
          if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            if (ctx != null) {
              log.debug("open external link in help viewer: " + url);
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
    
    viewerPanelLayout.setHorizontalGroup(
            viewerPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(viewerLabel).addComponent(scrollPane));
    viewerPanelLayout.setVerticalGroup(
            viewerPanelLayout.createSequentialGroup().addComponent(viewerLabel).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(scrollPane));

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
