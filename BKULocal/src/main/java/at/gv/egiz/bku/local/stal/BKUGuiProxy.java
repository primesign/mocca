package at.gv.egiz.bku.local.stal;

import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.List;

import java.util.Locale;
import javax.swing.JDialog;

import at.gv.egiz.bku.gui.BKUGUIFacade;
import at.gv.egiz.smcc.PINSpec;
import at.gv.egiz.stal.HashDataInput;

public class BKUGuiProxy implements BKUGUIFacade {

  private BKUGUIFacade delegate;
  private JDialog dialog;

  public BKUGuiProxy(JDialog dialog, BKUGUIFacade delegate) {
    this.delegate = delegate;
    this.dialog = dialog;
  }

  private void showDialog() {
    dialog.setVisible(true);
    dialog.setAlwaysOnTop(true);
  }

  @Override
  public char[] getPin() {
    return delegate.getPin();
  }

//  @Override
//  public void init(Container contentPane, Locale locale, URL bgImage, ActionListener helpListener) {
//    delegate.init(contentPane, locale, bgImage, helpListener);
//  }

  @Override
  public Locale getLocale() {
    return delegate.getLocale();
  }

  @Override
  public void showCardNotSupportedDialog(ActionListener cancelListener,
      String actionCommand) {
    showDialog();
    delegate.showCardNotSupportedDialog(cancelListener, actionCommand);
  }

  @Override
  public void showCardPINDialog(PINSpec pinSpec, ActionListener okListener,
      String okCommand, ActionListener cancelListener, String cancelCommand) {
    showDialog();
    delegate.showCardPINDialog(pinSpec, okListener, okCommand, cancelListener,
        cancelCommand);
  }

  @Override
  public void showCardPINRetryDialog(PINSpec pinSpec, int numRetries,
      ActionListener okListener, String okCommand,
      ActionListener cancelListener, String cancelCommand) {
    showDialog();
    delegate.showCardPINRetryDialog(pinSpec, numRetries, okListener, okCommand,
        cancelListener, cancelCommand);
  }

  @Override
  public void showErrorDialog(String errorMsgKey, Object[] errorMsgParams, ActionListener okListener,
      String actionCommand) {
    showDialog();
    delegate.showErrorDialog(errorMsgKey, errorMsgParams, okListener, actionCommand);
  }

  @Override
  public void showErrorDialog(String errorMsgKey, Object[] errorMsgParams) {
    showDialog();
    delegate.showErrorDialog(errorMsgKey, errorMsgParams);
  }

  @Override
  public void showInsertCardDialog(ActionListener cancelListener,
      String actionCommand) {
    showDialog();
    delegate.showInsertCardDialog(cancelListener, actionCommand);
  }

  @Override
  public void showSignaturePINDialog(PINSpec pinSpec,
      ActionListener signListener, String signCommand,
      ActionListener cancelListener, String cancelCommand,
      ActionListener hashdataListener, String hashdataCommand) {
    showDialog();
    delegate.showSignaturePINDialog(pinSpec, signListener, signCommand,
        cancelListener, cancelCommand, hashdataListener, hashdataCommand);
  }

  @Override
  public void showSignaturePINRetryDialog(PINSpec pinSpec, int numRetries,
      ActionListener okListener, String okCommand,
      ActionListener cancelListener, String cancelCommand,
      ActionListener hashdataListener, String hashdataCommand) {
    showDialog();
    delegate.showSignaturePINRetryDialog(pinSpec, numRetries, okListener,
        okCommand, cancelListener, cancelCommand, hashdataListener,
        hashdataCommand);
  }

  @Override
  public void showWaitDialog(String waitMessage) {
    showDialog();
    delegate.showWaitDialog(waitMessage);
  }

  @Override
  public void showWelcomeDialog() {
    showDialog();
    delegate.showWelcomeDialog();
  }

  @Override
  public void showHashDataInputDialog(List<HashDataInput> signedReferences, 
          ActionListener okListener, 
          String okCommand) {
    showDialog();
    delegate.showHashDataInputDialog(signedReferences, okListener, okCommand);
  }
}
