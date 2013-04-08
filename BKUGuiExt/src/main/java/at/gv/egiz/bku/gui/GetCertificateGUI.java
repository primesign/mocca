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
import java.util.Locale;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
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
	protected JLabel lblSimcert;
	protected JLabel lblQualcert;
	
	
	public GetCertificateGUI(Container contentPane, Locale locale,
			 URL backgroundImgURL, FontProvider fontProvider,
			HelpListener helpListener) {
		super(contentPane, locale, Style.advanced, backgroundImgURL, fontProvider,
				helpListener);
		
		
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
				
				helpListener.setHelpTopic(HELP_GETCERT);
				
		        titleLabel.setText(getMessage(TITLE_GETCERTIFICATE));
				
				lblSimcert = new JLabel(getMessage(LABEL_SIM_CERT));
				lblSimcert.setFont(lblSimcert.getFont().deriveFont(
						lblSimcert.getFont().getStyle()
						& ~java.awt.Font.BOLD));
				
				lblQualcert = new JLabel(getMessage(LABEL_QUAL_CERT));
				lblQualcert.setFont(lblQualcert.getFont().deriveFont(
						lblQualcert.getFont().getStyle()
						& ~java.awt.Font.BOLD));
		        
				getSimCertButton = new JButton();
				getSimCertButton.setFont(okButton.getFont().deriveFont(okButton.getFont().getStyle()& ~java.awt.Font.BOLD));
				getSimCertButton.setText(getMessage(BUTTON_SAVE_AS));
				getSimCertButton.setActionCommand(showGetSimCert);
				getSimCertButton.addActionListener(certificateListener);
				getSimCertButton.setEnabled(true);
				
				getQualCertButton = new JButton();
				getQualCertButton.setFont(okButton.getFont().deriveFont(okButton.getFont().getStyle()& ~java.awt.Font.BOLD));
				getQualCertButton.setText(getMessage(BUTTON_SAVE_AS));
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
								
				cancelButton.requestFocus();
				contentPanel.validate();
				
				if (windowCloseAdapter != null) {
					windowCloseAdapter.registerListener(cancelListener, cancelCmd);
				}
				
				resize();	
			}
		});
	}

	public void renderGetCertificateFrame() {
				
//---------------------------------------------------------------------------------------------------------
		GroupLayout gl_panel = new GroupLayout(mainPanel);
		gl_panel.setHorizontalGroup(
				gl_panel.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addGroup(gl_panel.createSequentialGroup()
						.addContainerGap()
						.addGroup(gl_panel.createParallelGroup(GroupLayout.Alignment.LEADING)
							.addComponent(lblSimcert)
							.addComponent(lblQualcert))
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(gl_panel.createParallelGroup(GroupLayout.Alignment.LEADING, false)
							.addComponent(getSimCertButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(getQualCertButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addContainerGap())
			);
			gl_panel.setVerticalGroup(
				gl_panel.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addGroup(gl_panel.createSequentialGroup()
						.addContainerGap()
						.addGroup(gl_panel.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(lblSimcert)
							.addComponent(getSimCertButton))
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(gl_panel.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(getQualCertButton)
							.addComponent(lblQualcert))
						.addContainerGap())
			);
		
			mainPanel.setLayout(gl_panel);
			
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
		
		if (lblSimcert != null) {

			lblSimcert.setFont(lblSimcert.getFont().deriveFont(
					(float) ((baseFontSize ) * factor)));

		}
		
		if (lblQualcert != null) {

			lblQualcert.setFont(lblQualcert.getFont().deriveFont(
					(float) ((baseFontSize ) * factor)));

		}
		
		if (getQualCertButton != null) {

			getQualCertButton.setFont(getQualCertButton.getFont().deriveFont(
					(float) ((baseFontSize ) * factor)));

		}
		
		if (getSimCertButton != null) {

			getSimCertButton.setFont(getSimCertButton.getFont().deriveFont(
					(float) ((baseFontSize ) * factor)));

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
