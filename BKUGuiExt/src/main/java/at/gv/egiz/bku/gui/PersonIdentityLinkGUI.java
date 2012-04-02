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
import java.text.MessageFormat;
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
 * Implements GUI for the person identity link informations
 * 
 * @author Andreas Fitzek <andreas.fitzek@iaik.tugraz.at>
 */
public class PersonIdentityLinkGUI extends CardMgmtGUI implements
		PersonIdentityLinkGUIFacade {

	private final Logger log = LoggerFactory
			.getLogger(PersonIdentityLinkGUI.class);

	protected JLabel mgmtLabel;
	
	protected JLabel firstNameLabel_description;
	protected JLabel lastNameLabel_description;
	protected JLabel birthdateLabel_description;
	
	protected JLabel firstNameLabel;
	protected JLabel lastNameLabel;
	protected JLabel birthdateLabel;
	
	protected JButton activateButton;

	public PersonIdentityLinkGUI(Container contentPane, Locale locale, URL backgroundImgURL, FontProvider fontProvider,
			HelpListener helpListener, SwitchFocusListener switchFocusListener) {
		super(contentPane, locale, Style.advanced, backgroundImgURL, fontProvider,
				helpListener, switchFocusListener);
		// TODO Auto-generated constructor stub
		this.activateButton = new JButton();
	}

	@Override
	public void showPersonIdentityLinkInformationDialog(
			final ActionListener activateListener,
			final String actionCommand,
			final String firstName,
			final String surName,
			final String birthdate) {
		log.debug("Scheduling Person Identity Link dialog.");

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				log.debug("Show Person Identity Link dialog.");

				mainPanel.removeAll();
				buttonPanel.removeAll();

				mgmtLabel = new JLabel();
				mgmtLabel.setFont(mgmtLabel.getFont().deriveFont(
						mgmtLabel.getFont().getStyle() & ~java.awt.Font.BOLD));

				if (renderHeaderPanel) {
					titleLabel.setText(getMessage(PersonIdentityLinkGUIFacade.TITLE_IDENITY));
					mgmtLabel.setText(getMessage(PersonIdentityLinkGUIFacade.MESSAGE_IDENITY));
				} else {
					mgmtLabel.setText(getMessage(PersonIdentityLinkGUIFacade.TITLE_IDENITY));
				}
				
				activateButton.setFont(activateButton.getFont().deriveFont(
						activateButton.getFont().getStyle()
								& ~java.awt.Font.BOLD));
				activateButton.addActionListener(activateListener);
				activateButton.setActionCommand(actionCommand);
				
				activateButton.setText(getMessage(PersonIdentityLinkGUIFacade.IDENTIFY_BUTTON));
				
				firstNameLabel_description = new JLabel();
				firstNameLabel_description.setText(getMessage(PersonIdentityLinkGUIFacade.FIRSTNAME));
				lastNameLabel_description = new JLabel();
				lastNameLabel_description.setText(getMessage(PersonIdentityLinkGUIFacade.LASTNAME));
				
				birthdateLabel_description = new JLabel();
				birthdateLabel_description.setText(getMessage(PersonIdentityLinkGUIFacade.DATEOFBIRTH));
				
				
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
				
				updateMethodToRunAtResize("at.gv.egiz.bku.gui.PersonIdentityLinkGUI", "renderContentAndButtons");
				
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
					Short.MAX_VALUE).addComponent(switchFocusDummyLabel)
					.addComponent(helpLabel);
			messageVertical.addComponent(switchFocusDummyLabel).addComponent(
					helpLabel);
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
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(mainPanelLayout.createParallelGroup(Alignment.LEADING)
									.addComponent(firstNameLabel)
									.addComponent(lastNameLabel)
									.addComponent(birthdateLabel)))))
			);
		mainPanelLayout.setVerticalGroup(
				mainPanelLayout.createParallelGroup(Alignment.LEADING)
					.addGroup(mainPanelLayout.createSequentialGroup()
						//.addContainerGap()
						.addGroup(messageVertical)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addGroup(mainPanelLayout.createParallelGroup(Alignment.BASELINE)
							.addComponent(firstNameLabel_description)
							.addComponent(firstNameLabel))
						//.addPreferredGap(ComponentPlacement.UNRELATED)
						.addGroup(mainPanelLayout.createParallelGroup(Alignment.BASELINE)
							.addComponent(lastNameLabel_description)
							.addComponent(lastNameLabel))
						//.addPreferredGap(ComponentPlacement.UNRELATED)
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
}
