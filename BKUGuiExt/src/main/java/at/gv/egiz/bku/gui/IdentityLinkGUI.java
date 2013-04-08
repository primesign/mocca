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

import java.awt.Container;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Locale;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.LayoutStyle;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.bku.gui.viewer.FontProvider;

/**
 * Implements GUI for the identity link informations
 * 
 * @author Andreas Fitzek <andreas.fitzek@iaik.tugraz.at>
 */
public class IdentityLinkGUI extends CardMgmtGUI implements
		IdentityLinkGUIFacade {

	private final Logger log = LoggerFactory
			.getLogger(IdentityLinkGUI.class);

	protected JLabel mgmtLabel;
	
	protected JLabel firstNameLabel_description;
	protected JLabel lastNameLabel_description;
	protected JLabel birthdateLabel_description;
	
	protected JLabel firstNameLabel;
	protected JLabel lastNameLabel;
	protected JLabel birthdateLabel;
	
	protected JButton activateButton;

	public IdentityLinkGUI(Container contentPane, Locale locale, URL backgroundImgURL, FontProvider fontProvider,
			HelpListener helpListener) {
		super(contentPane, locale, Style.advanced, backgroundImgURL, fontProvider,
				helpListener);
		// TODO Auto-generated constructor stub
		this.activateButton = new JButton();
	}

	@Override
	public void showIdentityLinkInformationDialog(
			final ActionListener activateListener,
			final String actionCommand,
			final String firstName,
			final String surName,
			final String birthdate) {
		log.debug("Scheduling Identity Link dialog.");

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				log.debug("Show Identity Link dialog.");

				helpListener.setHelpTopic(HELP_IDENTITYLINK);
				
				mainPanel.removeAll();
				buttonPanel.removeAll();

				mgmtLabel = new JLabel();
				mgmtLabel.setFont(mgmtLabel.getFont().deriveFont(
						mgmtLabel.getFont().getStyle() & ~java.awt.Font.BOLD));

				if (renderHeaderPanel) {
					titleLabel.setText(getMessage(IdentityLinkGUIFacade.TITLE_IDENITY));
					mgmtLabel.setText(getMessage(IdentityLinkGUIFacade.MESSAGE_IDENITY));
				} else {
					mgmtLabel.setText(getMessage(IdentityLinkGUIFacade.TITLE_IDENITY));
				}
				
				activateButton.setFont(activateButton.getFont().deriveFont(
						activateButton.getFont().getStyle()
								& ~java.awt.Font.BOLD));
				activateButton.addActionListener(activateListener);
				activateButton.setActionCommand(actionCommand);
				
				activateButton.setText(getMessage(BUTTON_CLOSE));
				
				firstNameLabel_description = new JLabel();
				firstNameLabel_description.setText(getMessage(IdentityLinkGUIFacade.FIRSTNAME));
				lastNameLabel_description = new JLabel();
				lastNameLabel_description.setText(getMessage(IdentityLinkGUIFacade.LASTNAME));
				
				birthdateLabel_description = new JLabel();
				birthdateLabel_description.setText(getMessage(IdentityLinkGUIFacade.DATEOFBIRTH));
				
				
				firstNameLabel = new JLabel();
				firstNameLabel.setText(firstName);

				firstNameLabel.setFont(firstNameLabel.getFont().deriveFont(
						firstNameLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
				
				lastNameLabel = new JLabel();
				lastNameLabel.setText(surName);
				
				lastNameLabel.setFont(lastNameLabel.getFont().deriveFont(
						lastNameLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
				
				birthdateLabel = new JLabel();
				birthdateLabel.setText(birthdate);

				birthdateLabel.setFont(birthdateLabel.getFont().deriveFont(
						birthdateLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
				
				updateMethodToRunAtResize("at.gv.egiz.bku.gui.IdentityLinkGUI", "renderContentAndButtons");
				
				renderContentAndButtons();
				
				cancelButton.requestFocus();
				contentPanel.validate();
				
				if(windowCloseAdapter != null)
				{
					windowCloseAdapter.registerListener(activateListener, actionCommand);
				}
				
				
				
				resize();
			}

		});
	}
	
	public void renderContentAndButtons() {

		// It is necessary to remove old components in order to ensure
		// the correct rendering of the status table and the button panel
		mainPanel.removeAll();
		buttonPanel.removeAll();

		//JScrollPane pinStatusScrollPane = new JScrollPane(pinStatusTable);

		GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
		mainPanel.setLayout(mainPanelLayout);

		GroupLayout.SequentialGroup messageHorizontal = mainPanelLayout
				.createSequentialGroup().addComponent(mgmtLabel);
		GroupLayout.Group messageVertical = mainPanelLayout
				.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(mgmtLabel);
		
		if (!renderHeaderPanel) {
			
			
			messageHorizontal.addPreferredGap(
					LayoutStyle.ComponentPlacement.UNRELATED, 0,
					Short.MAX_VALUE).addComponent(helpLabel);
			messageVertical.addComponent(helpLabel);
		}
		
		mainPanelLayout.setHorizontalGroup(
				mainPanelLayout.createParallelGroup(Alignment.LEADING)
					.addGroup(mainPanelLayout.createSequentialGroup()
						//.addContainerGap()
						.addGroup(mainPanelLayout.createParallelGroup(Alignment.LEADING)
							.addGroup(messageHorizontal)
							.addGroup(mainPanelLayout.createSequentialGroup()
								.addGroup(mainPanelLayout.createParallelGroup(Alignment.LEADING)
									.addComponent(firstNameLabel_description)
									.addComponent(lastNameLabel_description)
									.addComponent(birthdateLabel_description))
								.addPreferredGap(ComponentPlacement.RELATED, 5, 5)
								.addGroup(mainPanelLayout.createParallelGroup(Alignment.LEADING)
									.addComponent(firstNameLabel)
									.addComponent(lastNameLabel)
									.addComponent(birthdateLabel))
								.addPreferredGap(ComponentPlacement.RELATED)
								.addContainerGap(0, Integer.MAX_VALUE))))
			);
		mainPanelLayout.setVerticalGroup(
				mainPanelLayout.createParallelGroup(Alignment.LEADING)
					.addGroup(mainPanelLayout.createSequentialGroup()
						//.addContainerGap()
						.addGroup(messageVertical)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(mainPanelLayout.createParallelGroup(Alignment.BASELINE)
							.addComponent(firstNameLabel_description)
							.addComponent(firstNameLabel))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(mainPanelLayout.createParallelGroup(Alignment.BASELINE)
							.addComponent(lastNameLabel_description)
							.addComponent(lastNameLabel))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(mainPanelLayout.createParallelGroup(Alignment.BASELINE)
							.addComponent(birthdateLabel_description)
							.addComponent(birthdateLabel)))
			);
		
		GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
		buttonPanel.setLayout(buttonPanelLayout);

		GroupLayout.SequentialGroup buttonHorizontal = buttonPanelLayout
				.createSequentialGroup().addContainerGap(
						GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(activateButton, GroupLayout.PREFERRED_SIZE,
						buttonSize, GroupLayout.PREFERRED_SIZE);

		GroupLayout.Group buttonVertical = buttonPanelLayout
				.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(activateButton);

		buttonPanelLayout.setHorizontalGroup(buttonHorizontal);
		buttonPanelLayout.setVerticalGroup(buttonVertical);

	}
	
	@Override
	public void resize() {
	
		log.debug("Resizing Personal IdentityLink Applet ...");

		float factor = getResizeFactor();
		
		
		if (mgmtLabel != null) {
			mgmtLabel.setFont(mgmtLabel.getFont().deriveFont(
					(float) ((baseFontSize)* factor)));
		}
		
		if (activateButton != null) {
			activateButton.setFont(activateButton.getFont().deriveFont(
					(float) ((baseFontSize)* factor)));
		}
		
		if (firstNameLabel_description != null) {
			firstNameLabel_description.setFont(firstNameLabel_description.getFont().deriveFont(
					(float) ((baseFontSize)* factor)));
		}
		
		if (lastNameLabel_description != null) {
			lastNameLabel_description.setFont(lastNameLabel_description.getFont().deriveFont(
					(float) ((baseFontSize)* factor)));
		}
		
		if (birthdateLabel_description != null) {
			birthdateLabel_description.setFont(birthdateLabel_description.getFont().deriveFont(
					(float) ((baseFontSize)* factor)));
		}

		if (firstNameLabel != null) {
			firstNameLabel.setFont(firstNameLabel.getFont().deriveFont(
					(float) ((baseFontSize)* factor)));
		}

		if (lastNameLabel != null) {
			lastNameLabel.setFont(lastNameLabel.getFont().deriveFont(
					(float) ((baseFontSize)* factor)));
		}

		if (birthdateLabel != null) {
			birthdateLabel.setFont(birthdateLabel.getFont().deriveFont(
					(float) ((baseFontSize)* factor)));
		}

		
		if (activateButton != null) {

			activateButton.setFont(activateButton.getFont().deriveFont(
					(float) (baseFontSize * factor)));

		}
		
		super.resize();
	}
}
