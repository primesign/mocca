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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.bku.gui.viewer.FontProvider;

/**
*
* @author Thomas Lenz <thomas.lenz@iaik.tugraz.at>
*/

public class GetHardwareInfoGUI extends CardMgmtGUI implements
		GetHardwareInfoGUIFacade {

	private final Logger log = LoggerFactory.getLogger(GetHardwareInfoGUI.class);
	
	protected JLabel lblType;
	protected JLabel lblAtr;
	protected JLabel lblReaderContent;
	protected JLabel lblTypeContent;
	protected JLabel lblAtrContent;
	protected JPanel cardpanel;
	protected JPanel readerpanel;
	protected TitledBorder readerpanel_border;
	protected TitledBorder cardpanel_border;
	
	
	public GetHardwareInfoGUI(Container contentPane, Locale locale,
			 URL backgroundImgURL, FontProvider fontProvider,
			HelpListener helpListener) {
		super(contentPane, locale, Style.advanced, backgroundImgURL, fontProvider,
				helpListener);	
	}
	
	
	@Override
	public void showHardwareInfoDialog(final ActionListener hardwareinfolistener, final String backcmd, 
			final String showcardreadername, final String showsmartcardname,
			final String showsmartcardATR) {
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
		
				headerPanel.removeAll();
				mainPanel.removeAll();
				buttonPanel.removeAll();
			 
				titleLabel.setText(getMessage(TITLE_HARDWAREINFO));
				
				backButton.setFont(backButton.getFont().deriveFont(backButton.getFont().getStyle()& ~java.awt.Font.BOLD));
				backButton.setText(getMessage(BUTTON_CLOSE));
				backButton.setActionCommand(backcmd);
				backButton.addActionListener(hardwareinfolistener);
				backButton.setEnabled(true);
								
				readerpanel_border = new TitledBorder(null, getMessage(LABEL_CARDREADER), TitledBorder.LEADING, TitledBorder.TOP, null, null);
				if (readerpanel_border.getTitleFont() != null)
					readerpanel_border.setTitleFont(readerpanel_border.getTitleFont().deriveFont(
							readerpanel_border.getTitleFont().getStyle()
							& java.awt.Font.BOLD));
				readerpanel = new JPanel();
				readerpanel.setBorder(readerpanel_border);

				cardpanel_border = new TitledBorder(null, getMessage(LABEL_SMARTCARD), TitledBorder.LEADING, TitledBorder.TOP, null, null); 
				if (cardpanel_border.getTitleFont() != null)
					cardpanel_border.setTitleFont(cardpanel_border.getTitleFont().deriveFont(
							cardpanel_border.getTitleFont().getStyle()
							& java.awt.Font.BOLD));
				cardpanel = new JPanel();
				cardpanel.setBorder(cardpanel_border);
				cardpanel.setFont(cardpanel.getFont().deriveFont(
						cardpanel.getFont().getStyle()
						& java.awt.Font.BOLD));
								
				lblType = new JLabel(getMessage(LABEL_SMARTCARD_TYPE));
				lblType.setFont(lblType.getFont().deriveFont(
						lblType.getFont().getStyle()
						& java.awt.Font.BOLD));
				
				lblAtr = new JLabel(getMessage(LABEL_SMARTCARD_ATR));
				lblAtr.setFont(lblAtr.getFont().deriveFont(
						lblAtr.getFont().getStyle()
						& java.awt.Font.BOLD));
				
				lblTypeContent = new JLabel("<html>" + showsmartcardname + "</html>");
				lblTypeContent.setVerticalAlignment(SwingConstants.CENTER);
				lblTypeContent.setFont(lblTypeContent.getFont().deriveFont(
						lblTypeContent.getFont().getStyle()
						& ~java.awt.Font.BOLD));
					
				lblAtrContent = new JLabel(makeATRString(showsmartcardATR, 40));
				lblAtrContent.setVerticalAlignment(SwingConstants.CENTER);
				lblAtrContent.setFont(lblAtrContent.getFont().deriveFont(
						lblAtrContent.getFont().getStyle()
						& ~java.awt.Font.BOLD));
				
				lblReaderContent = new JLabel("<html>" + showcardreadername+ "</html>");
				lblReaderContent.setVerticalAlignment(SwingConstants.CENTER);
				lblReaderContent.setFont(lblReaderContent.getFont().deriveFont(
						lblReaderContent.getFont().getStyle()
						& ~java.awt.Font.BOLD));
				
				
				if (windowCloseAdapter != null) {
					windowCloseAdapter.registerListener(hardwareinfolistener, backcmd);
				}
				
				updateMethodToRunAtResize("at.gv.egiz.bku.gui.GetHardwareInfoGUI", "renderHardwareInfoFrame");
				
				renderHardwareInfoFrame();
				
				backButton.requestFocus();
				contentPanel.validate();
				
				resize();			
			}
		});
	}
	
	public void renderHardwareInfoFrame() {
		
		GroupLayout gl_contentPane = new GroupLayout(mainPanel);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_contentPane.createParallelGroup(GroupLayout.Alignment.LEADING)
//						.addComponent(cardpanel, GroupLayout.DEFAULT_SIZE, 298, Short.MAX_VALUE)
//						.addComponent(readerpanel, GroupLayout.DEFAULT_SIZE, 298, Short.MAX_VALUE)))
						.addComponent(cardpanel, 0, 298, Short.MAX_VALUE)
						.addComponent(readerpanel, 0, 298, Short.MAX_VALUE)))
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
//					.addComponent(readerpanel, GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE)
//					.addGap(8)
//					.addComponent(cardpanel, GroupLayout.PREFERRED_SIZE, 89, Short.MAX_VALUE)
					.addComponent(readerpanel, 0, 45, Short.MAX_VALUE)
					.addGap(8)
					.addComponent(cardpanel, 0, 89, Short.MAX_VALUE)
					.addContainerGap())
		);
		
		GroupLayout gl_cardpanel = new GroupLayout(cardpanel);
		gl_cardpanel.setHorizontalGroup(
				gl_cardpanel.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addGroup(gl_cardpanel.createSequentialGroup()
						.addContainerGap()
						.addGroup(gl_cardpanel.createParallelGroup(GroupLayout.Alignment.TRAILING)
							.addComponent(lblType, GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE)
							.addComponent(lblAtr, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addGroup(gl_cardpanel.createParallelGroup(GroupLayout.Alignment.LEADING)
							.addComponent(lblTypeContent, GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE)
							.addComponent(lblAtrContent, GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)))
			);
				
		gl_cardpanel.setVerticalGroup(
				gl_cardpanel.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addGroup(gl_cardpanel.createSequentialGroup()
						.addGroup(gl_cardpanel.createParallelGroup(GroupLayout.Alignment.LEADING)
							.addComponent(lblType, GroupLayout.DEFAULT_SIZE, 21, Short.MAX_VALUE)
							.addComponent(lblTypeContent, GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl_cardpanel.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(lblAtrContent, GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
							.addComponent(lblAtr, GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE))
						.addContainerGap())
			);
		
		cardpanel.setLayout(gl_cardpanel);
				
		GroupLayout gl_readerpanel = new GroupLayout(readerpanel);
		gl_readerpanel.setHorizontalGroup(
			gl_readerpanel.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(gl_readerpanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblReaderContent, GroupLayout.PREFERRED_SIZE, 276, Short.MAX_VALUE))
		);
		gl_readerpanel.setVerticalGroup(
			gl_readerpanel.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(lblReaderContent, GroupLayout.DEFAULT_SIZE, 15, Short.MAX_VALUE)
		);
		readerpanel.setLayout(gl_readerpanel);
		mainPanel.setLayout(gl_contentPane);
		
		//------------------------------------------------------------------------------------				
		
		GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
		buttonPanel.setLayout(buttonPanelLayout);
		
		GroupLayout.ParallelGroup buttonHorizontal = buttonPanelLayout
				.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(backButton, GroupLayout.PREFERRED_SIZE,	buttonSize, GroupLayout.PREFERRED_SIZE);

		GroupLayout.Group buttonVertical = buttonPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(backButton);
		
		
		buttonPanelLayout.setHorizontalGroup(buttonHorizontal);
		buttonPanelLayout.setVerticalGroup(buttonVertical);
	}
	
	@Override
	public void resize() {
	
		log.debug("Resizing Hardware-Info Applet ...");

		float factor = getResizeFactor();
		
		
		if (lblType != null) {
			lblType.setFont(lblType.getFont().deriveFont(
					(float) ((baseFontSize - 2)* factor)));
		}
		
		if (lblAtr != null) {
			lblAtr.setFont(lblAtr.getFont().deriveFont(
					(float) ((baseFontSize - 2)* factor)));
		}
		
		if (lblReaderContent != null) {
			lblReaderContent.setFont(lblReaderContent.getFont().deriveFont(
					(float) ((baseFontSize - 2)* factor)));
		}
		
		if (lblTypeContent != null) {
			lblTypeContent.setFont(lblTypeContent.getFont().deriveFont(
					(float) ((baseFontSize - 2)* factor)));
		}
		
		if (lblAtrContent != null) {
			lblAtrContent.setFont(lblAtrContent.getFont().deriveFont(
					(float) ((baseFontSize - 2)* factor)));
		}
		
		if (cardpanel_border != null && cardpanel_border.getTitleFont() != null) {
			cardpanel_border.setTitleFont(cardpanel_border.getTitleFont().deriveFont(
					(float) ((baseFontSize)* factor)));
		}
		
		if (readerpanel_border != null && readerpanel_border.getTitleFont() != null) {
			readerpanel_border.setTitleFont(readerpanel_border.getTitleFont().deriveFont(
					(float) ((baseFontSize)* factor)));
		}
		
		if (backButton != null) {

			backButton.setFont(backButton.getFont().deriveFont(
					(float) (baseFontSize * factor)));

		}
		
		super.resize();
	}
	
	private String makeATRString(String ATR, int width) {
		
		String line = new String();
		
		if (ATR.length() > width) {
					
			line = line.concat("<html><body>");
			line = line.concat(ATR.substring(1, width));
			line = line.concat("<br>");
			line = line.concat(ATR.substring(width,ATR.length()-1));
			line = line.concat("</body></html>");
			
			return line;
		}
		else
			return ATR;
	}
	
}
