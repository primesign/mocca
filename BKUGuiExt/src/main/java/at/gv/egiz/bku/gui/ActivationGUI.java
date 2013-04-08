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
import java.awt.Container;
import java.awt.Cursor;
import java.awt.event.ActionListener;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Locale;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.LayoutStyle;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class ActivationGUI extends CardMgmtGUI implements ActivationGUIFacade {
  
  public static final String TITLE_ACTIVATION = "title.activation";
  public static final String LABEL_ACTIVATION = "label.activation";
  public static final String LABEL_ACTIVATION_STEP = "label.activation.step";
  public static final String LABEL_ACTIVATION_IDLE = "label.activation.idle";

  public static final String HELP_ACTIVATION = "help.activation";

  private final Logger log = LoggerFactory.getLogger(ActivationGUI.class);
  
  protected JProgressBar progressBar;
  
  public ActivationGUI(Container contentPane,
          Locale locale,
          Style guiStyle,
          URL backgroundImgURL,
          FontProvider fontProvider,
          HelpListener helpListener) {
    super(contentPane, locale, guiStyle, backgroundImgURL, fontProvider, helpListener);

    progressBar = new JProgressBar();
  }

  @Override
  public void showActivationProgressDialog(final int currentStep, final int maxProgress, final ActionListener cancelListener, final String cancelCommand) {

    log.debug("Scheduling activation progress dialog (step {}).", currentStep);

    SwingUtilities.invokeLater(new Runnable() {
      
      @Override
      public void run() {

        log.debug("Show activation progress dialog (step {}).", currentStep);

        mainPanel.removeAll();
        buttonPanel.removeAll();

        mainPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));


        JLabel infoLabel = new JLabel();
        infoLabel.setFont(infoLabel.getFont().deriveFont(infoLabel.getFont().getStyle() & ~java.awt.Font.BOLD));

        if (renderHeaderPanel) {
          titleLabel.setText(cardmgmtMessages.getString(TITLE_ACTIVATION));
          infoLabel.setText(cardmgmtMessages.getString(LABEL_ACTIVATION));
        } else {
          infoLabel.setText(cardmgmtMessages.getString(TITLE_ACTIVATION));
        }

        helpListener.setHelpTopic(HELP_ACTIVATION);

        progressBar.setIndeterminate(false);
        progressBar.setStringPainted(true);
        progressBar.setString(null); //reset to percentage
        progressBar.setMinimum(0);
        progressBar.setMaximum(maxProgress);

        JLabel stepLabel = new JLabel();
        stepLabel.setFont(stepLabel.getFont().deriveFont(stepLabel.getFont().getStyle() & ~java.awt.Font.BOLD, stepLabel.getFont().getSize()-2));
        String stepPattern = cardmgmtMessages.getString(LABEL_ACTIVATION_STEP);
        stepLabel.setText(MessageFormat.format(stepPattern, new Object[]{ currentStep }));

        GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);

        GroupLayout.SequentialGroup infoHorizontal = mainPanelLayout.createSequentialGroup().addComponent(infoLabel);
        GroupLayout.ParallelGroup infoVertical = mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(infoLabel);

        if (!renderHeaderPanel) {
          infoHorizontal.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 0, Short.MAX_VALUE).addComponent(helpLabel);
          infoVertical.addComponent(helpLabel);
        }

        mainPanelLayout.setHorizontalGroup(
                mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                  .addGroup(infoHorizontal)
                  .addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(stepLabel)
                    .addComponent(progressBar)));

        mainPanelLayout.setVerticalGroup(
                mainPanelLayout.createSequentialGroup()
                  .addGroup(infoVertical)
                  .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                  .addGroup(mainPanelLayout.createSequentialGroup()
                    .addComponent(stepLabel)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(progressBar)));

          JButton cancelButton = new JButton();
          cancelButton.setFont(cancelButton.getFont().deriveFont(cancelButton.getFont().getStyle() & ~java.awt.Font.BOLD));
          cancelButton.setText(messages.getString(BUTTON_CANCEL));
          cancelButton.addActionListener(cancelListener);
          cancelButton.setActionCommand(cancelCommand);

          GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
          buttonPanel.setLayout(buttonPanelLayout);

          buttonPanelLayout.setHorizontalGroup(
            buttonPanelLayout.createSequentialGroup()
                  .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                  .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, buttonSize, GroupLayout.PREFERRED_SIZE));
          buttonPanelLayout.setVerticalGroup(
            buttonPanelLayout.createSequentialGroup()
              .addComponent(cancelButton));

        contentPanel.validate();

      }
    });

  }

  @Override
  public void incrementProgress() {
    SwingUtilities.invokeLater(new Runnable() {

      @Override
      public void run() {
        progressBar.setValue(progressBar.getValue() + 1);
      }
    });
    
  }

  @Override
  public void showIdleDialog(final ActionListener cancelListener, final String cancelCommand) {
    log.debug("Scheduling idle dialog.");

    SwingUtilities.invokeLater(new Runnable() {

      @Override
      public void run() {

        log.debug("Show idle dialog.");

        mainPanel.removeAll();
        buttonPanel.removeAll();

        mainPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));


        JLabel infoLabel = new JLabel();
        infoLabel.setFont(infoLabel.getFont().deriveFont(infoLabel.getFont().getStyle() & ~java.awt.Font.BOLD));

        if (renderHeaderPanel) {
          titleLabel.setText(cardmgmtMessages.getString(TITLE_ACTIVATION));
          infoLabel.setText(cardmgmtMessages.getString(LABEL_ACTIVATION));
        } else {
          infoLabel.setText(cardmgmtMessages.getString(TITLE_ACTIVATION));
        }

        helpListener.setHelpTopic(HELP_ACTIVATION);

        progressBar.setIndeterminate(true);
        progressBar.setStringPainted(true);
        progressBar.setString(""); //not string painted progressbar is smaller

        JLabel stepLabel = new JLabel();
        stepLabel.setFont(stepLabel.getFont().deriveFont(stepLabel.getFont().getStyle() & ~java.awt.Font.BOLD, stepLabel.getFont().getSize()-2));
        stepLabel.setText(cardmgmtMessages.getString(LABEL_ACTIVATION_IDLE));

        GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);

        GroupLayout.SequentialGroup infoHorizontal = mainPanelLayout.createSequentialGroup().addComponent(infoLabel);
        GroupLayout.ParallelGroup infoVertical = mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(infoLabel);

        if (!renderHeaderPanel) {
          infoHorizontal.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 0, Short.MAX_VALUE).addComponent(helpLabel);
          infoVertical.addComponent(helpLabel);
        }

        mainPanelLayout.setHorizontalGroup(
                mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                  .addGroup(infoHorizontal)
                  .addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(stepLabel)
                    .addComponent(progressBar)));

        mainPanelLayout.setVerticalGroup(
                mainPanelLayout.createSequentialGroup()
                  .addGroup(infoVertical)
                  .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                  .addGroup(mainPanelLayout.createSequentialGroup()
                    .addComponent(stepLabel)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(progressBar)));

          JButton cancelButton = new JButton();
          cancelButton.setFont(cancelButton.getFont().deriveFont(cancelButton.getFont().getStyle() & ~java.awt.Font.BOLD));
          cancelButton.setText(messages.getString(BUTTON_CANCEL));
          cancelButton.addActionListener(cancelListener);
          cancelButton.setActionCommand(cancelCommand);

          GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
          buttonPanel.setLayout(buttonPanelLayout);

          buttonPanelLayout.setHorizontalGroup(
            buttonPanelLayout.createSequentialGroup()
                  .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                  .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, buttonSize, GroupLayout.PREFERRED_SIZE));
          buttonPanelLayout.setVerticalGroup(
            buttonPanelLayout.createSequentialGroup()
              .addComponent(cancelButton));

        contentPanel.validate();

      }
    });

  }
}
