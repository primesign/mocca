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


package at.gv.egiz.bku.gui.viewer;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.bku.gui.BKUGUIFacade;
import at.gv.egiz.stal.HashDataInput;

public class SecureViewerSaveDialog {

	private static void setFileChooserFont(Component[] comp, Font font) {
		for (int i = 0; i < comp.length; i++) {
			if (comp[i] instanceof Container)
				setFileChooserFont(((Container) comp[i]).getComponents(), font);
			try {
				comp[i].setFont(font);
			} catch (Exception e) {
			    Logger log = LoggerFactory.getLogger(SecureViewerSaveDialog.class);
				log.warn("FileChooser component font could not be set");
			}
		}
	}


  public static void showSaveDialog(final Component parent, final HashDataInput hashDataInput, final ResourceBundle messages,
      final ActionListener okListener, final String okCommand, final int fontSize) {
    
    final Logger log = LoggerFactory.getLogger(SecureViewerSaveDialog.class);
    log.debug("[{}] Scheduling save dialog.", Thread.currentThread().getName());

    SwingUtilities.invokeLater(new Runnable() {

      @Override
      public void run() {

        log.debug("[{}] Show save dialog.", Thread.currentThread().getName());

        String userHome = System.getProperty("user.home");
        
        UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE);

        @SuppressWarnings("serial")
        JFileChooser fileDialog = new JFileChooser(userHome) {
          @Override
          protected JDialog createDialog(Component parent) throws HeadlessException {
            JDialog dialog = super.createDialog(parent);
            dialog.setModal(true);
            dialog.setAlwaysOnTop(true);
            return dialog;
          }
        };

        fileDialog.setMultiSelectionEnabled(false);
        fileDialog.setDialogType(JFileChooser.SAVE_DIALOG);
        fileDialog.setFileHidingEnabled(true);
        fileDialog.setDialogTitle(messages
            .getString(BKUGUIFacade.WINDOWTITLE_SAVE));
        fileDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
        String mimeType = hashDataInput.getMimeType();
        MimeFilter mimeFilter = new MimeFilter(mimeType, messages);
        fileDialog.setFileFilter(mimeFilter);
        String filename = (hashDataInput.getFilename() != null) ?
          hashDataInput.getFilename() :
          messages.getString(BKUGUIFacade.SAVE_HASHDATAINPUT_PREFIX)
            + MimeFilter.getExtension(mimeType);
        fileDialog.setSelectedFile(new File(userHome, filename));

        setFileChooserFont(fileDialog.getComponents(),
            new JLabel().getFont().deriveFont((float) fontSize));

        // parent SecureViewer -> placed over it
        switch (fileDialog.showSaveDialog(parent)) {
        case JFileChooser.APPROVE_OPTION:
          File file = fileDialog.getSelectedFile();
          String id = hashDataInput.getReferenceId();
          if (file.exists()) {
            String msgPattern = messages
                .getString(BKUGUIFacade.MESSAGE_OVERWRITE);
            int overwrite = JOptionPane.showConfirmDialog(parent,
                MessageFormat.format(msgPattern, file), messages
                    .getString(BKUGUIFacade.WINDOWTITLE_OVERWRITE),
                JOptionPane.OK_CANCEL_OPTION);
            if (overwrite != JOptionPane.OK_OPTION) {
              break;
            }
          }
          if (log.isDebugEnabled()) {
            Object[] args = {id, mimeType, file};
            log.debug("Writing hashdata input {} ({}) to file {}.", args);
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
            log.error("Failed to write.", ex);
            String errPattern = messages
                .getString(BKUGUIFacade.ERR_WRITE_HASHDATA);
            JOptionPane.showMessageDialog(parent, MessageFormat.format(
                errPattern, ex.getMessage()), messages
                .getString(BKUGUIFacade.WINDOWTITLE_ERROR),
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
          log.debug("Cancelled save dialog.");
          break;
        }
        if (okListener != null) {
          okListener.actionPerformed(new ActionEvent(fileDialog,
              ActionEvent.ACTION_PERFORMED, okCommand));
        }
      }
    });
  }
}
