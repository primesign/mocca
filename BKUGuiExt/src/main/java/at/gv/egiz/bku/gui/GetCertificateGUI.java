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
import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Locale;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.LayoutStyle;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.bku.gui.viewer.FontProvider;

/**
*
* @author Thomas Lenz <thomas.lenz@iaik.tugraz.at>
*/

public class GetCertificateGUI extends CardMgmtGUI implements
		GetCertificateGUIFacade {

	private final Logger log = LoggerFactory.getLogger(GetCertificateGUI.class);
	
	protected JButton getSimCertButton;
	protected JButton getQualCertButton;
	
	public GetCertificateGUI(Container contentPane, Locale locale,
			 URL backgroundImgURL, FontProvider fontProvider,
			HelpListener helpListener, SwitchFocusListener switchFocusListener) {
		super(contentPane, locale, Style.advanced, backgroundImgURL, fontProvider,
				helpListener, switchFocusListener);
		
		
	}

	@Override
	public void showGetCertificateDialog(final ActionListener certificateListener,
			final String showGetQualCert, final String showGetSimCert,
			final ActionListener cancelListener, final String cancelCmd) {
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				
				mainPanel.removeAll();
				buttonPanel.removeAll();
								
		        titleLabel.setText(getMessage(TITLE_GETCERTIFICATE));
				
				getSimCertButton = new JButton();
				getSimCertButton.setFont(okButton.getFont().deriveFont(okButton.getFont().getStyle()& ~java.awt.Font.BOLD));
				getSimCertButton.setText(getMessage(BUTTON_SIM_CERT));
				getSimCertButton.setActionCommand(showGetSimCert);
				getSimCertButton.addActionListener(certificateListener);
				getSimCertButton.setEnabled(true);
				
				getQualCertButton = new JButton();
				getQualCertButton.setFont(okButton.getFont().deriveFont(okButton.getFont().getStyle()& ~java.awt.Font.BOLD));
				getQualCertButton.setText(getMessage(BUTTON_QUAL_CERT));
				getQualCertButton.setActionCommand(showGetQualCert);
				getQualCertButton.addActionListener(certificateListener);
				getQualCertButton.setEnabled(true);
								
				cancelButton.setFont(cancelButton.getFont().deriveFont(cancelButton.getFont().getStyle()& ~java.awt.Font.BOLD));
				cancelButton.setText(getMessage(BUTTON_CLOSE));
				cancelButton.setActionCommand(cancelCmd);
				cancelButton.addActionListener(cancelListener);
				cancelButton.setEnabled(true);
							
				updateMethodToRunAtResize("at.gv.egiz.bku.gui.GetCertificateGUI", "renderGetCertificateFrame");
				
				renderGetCertificateFrame();
				
				if (windowCloseAdapter != null) {
					windowCloseAdapter.registerListener(cancelListener, cancelCmd);
				}
				
				
				cancelButton.requestFocus();
				contentPanel.validate();
				
				resize();	
			}
		});
	}

	public void renderGetCertificateFrame() {
				
//---------------------------------------------------------------------------------------------------------
		
		GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
		mainPanelLayout.setHorizontalGroup(
				mainPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addGroup(mainPanelLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
							.addComponent(getQualCertButton, 0, 
									GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addGap(12))
						.addGroup(GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
							.addGap(1)
							.addComponent(getSimCertButton, 0, 
									getQualCertButton.getSize().width, Short.MAX_VALUE)
							.addContainerGap())))
		);
		mainPanelLayout.setVerticalGroup(
				mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(mainPanelLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(getSimCertButton, GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
					.addComponent(getQualCertButton, GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
					));
		
		mainPanel.setLayout(mainPanelLayout);
//---------------------------------------------------------------------------------------------------------
		
		GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
		
		GroupLayout.ParallelGroup buttonHorizontal = buttonPanelLayout
				.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(cancelButton, GroupLayout.PREFERRED_SIZE,	buttonSize, GroupLayout.PREFERRED_SIZE);

		GroupLayout.Group buttonVertical = buttonPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(cancelButton);
		
		buttonPanelLayout.setHorizontalGroup(buttonHorizontal);
		buttonPanelLayout.setVerticalGroup(buttonVertical);
		
		buttonPanel.setLayout(buttonPanelLayout);
	}
	
	@Override
	public void resize() {
		
		log.debug("Resizing Get-Certificate Applet ...");

		float factor = getResizeFactor();
		
		if (getQualCertButton != null) {

			getQualCertButton.setFont(getQualCertButton.getFont().deriveFont(
					(float) (baseFontSize * factor)));

		}
		
		if (getSimCertButton != null) {

			getSimCertButton.setFont(getSimCertButton.getFont().deriveFont(
					(float) (baseFontSize * factor)));

		}
		
		if (cancelButton != null) {

			cancelButton.setFont(cancelButton.getFont().deriveFont(
					(float) (baseFontSize * factor)));

		}
		
		super.resize();
	}
	
	public File showSaveDialog(String defaultfilename)  {
		
	
		JFileChooser filechooser = new JFileChooser();
	
		filechooser.setMultiSelectionEnabled(false);
		filechooser.setDialogTitle(getMessage(TITEL_FILESAVE));
		filechooser.setSelectedFile(new File(defaultfilename));
		filechooser.setFileFilter( new FileFilter()
	    {
	      @Override public boolean accept( File f )
	      {
	        return f.isDirectory() ||
	          f.getName().toLowerCase().endsWith( ".cer" );
	      }
	      @Override public String getDescription()
	      {
	        return getMessage(FILE_TYPE_NAME);
	      }
	    } );
		
		int state = filechooser.showSaveDialog(contentPane);
		
		if (state == JFileChooser.APPROVE_OPTION) {
			return filechooser.getSelectedFile();

		} else {
			log.info("Save certificate dialog canceled");
			return null;
		}	
	}
	
}
