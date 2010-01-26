package at.gv.egiz.bku.gui.viewer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.bku.gui.BKUGUIFacade;
import at.gv.egiz.stal.HashDataInput;

public class SecureViewerSaveDialog {

  protected static final Log log = LogFactory.getLog(SecureViewerSaveDialog.class);
    
  public static void showSaveDialog(final HashDataInput hashDataInput, final ResourceBundle messages,
      final ActionListener okListener, final String okCommand) {

    log.debug("[" + Thread.currentThread().getName()
        + "] scheduling save dialog");

    SwingUtilities.invokeLater(new Runnable() {

      @Override
      public void run() {

        log
            .debug("[" + Thread.currentThread().getName()
                + "] show save dialog");

        String userHome = System.getProperty("user.home");

        JFileChooser fileDialog = new JFileChooser(userHome);
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

        // parent contentPane -> placed over applet
        switch (fileDialog.showSaveDialog(fileDialog)) {
        case JFileChooser.APPROVE_OPTION:
          File file = fileDialog.getSelectedFile();
          String id = hashDataInput.getReferenceId();
          if (file.exists()) {
            String msgPattern = messages
                .getString(BKUGUIFacade.MESSAGE_OVERWRITE);
            int overwrite = JOptionPane.showConfirmDialog(fileDialog,
                MessageFormat.format(msgPattern, file), messages
                    .getString(BKUGUIFacade.WINDOWTITLE_OVERWRITE),
                JOptionPane.OK_CANCEL_OPTION);
            if (overwrite != JOptionPane.OK_OPTION) {
              break;
            }
          }
          if (log.isDebugEnabled()) {
            log.debug("writing hashdata input " + id + " (" + mimeType
                + ") to file " + file);
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
            log.error("Failed to write " + file + ": " + ex.getMessage());
            log.debug(ex);
            String errPattern = messages
                .getString(BKUGUIFacade.ERR_WRITE_HASHDATA);
            JOptionPane.showMessageDialog(fileDialog, MessageFormat.format(
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
          log.debug("cancelled save dialog");
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
