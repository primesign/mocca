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

  import at.gv.egiz.stal.HashDataInput;
import at.gv.egiz.smcc.PinInfo;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Locale;

public interface BKUGUIFacade {

  public static final String ERR_UNKNOWN_WITH_PARAM = "error.unknown.param";
  public static final String ERR_UNKNOWN = "error.unknown";
  public static final String ERR_SERVICE_UNREACHABLE = "error.ws.unreachable";
  public static final String ERR_NO_PCSC = "error.pcsc";
  public static final String ERR_NO_CARDTERMINAL = "error.cardterminal";
  public static final String ERR_NO_HASHDATA = "error.no.hashdata";
  public static final String ERR_DISPLAY_HASHDATA = "error.display.hashdata";
  public static final String ERR_WRITE_HASHDATA = "error.write.hashdata";
  public static final String ERR_INVALID_HASH = "error.invalid.hash";
  public static final String ERR_CARD_LOCKED = "error.card.locked";
  public static final String ERR_CARD_NOTACTIVATED = "error.card.notactivated";
  public static final String ERR_PIN_TIMEOUT = "error.pin.timeout";
  public static final String ERR_VIEWER = "error.viewer";
  public static final String ERR_EXTERNAL_LINK = "error.external.link";
  public static final String ERR_CONFIG = "error.config";

  /** no leading slash for Messages resource, leading slash for images */
  public static final String MESSAGES_BUNDLE = "at/gv/egiz/bku/gui/Messages";
  public static final String DEFAULT_BACKGROUND = "/at/gv/egiz/bku/gui/chip32.png";
  public static final String DEFAULT_ICON = "/at/gv/egiz/bku/gui/chiperling105.png";
  public static final String HELP_IMG = "/at/gv/egiz/bku/gui/help.png";
  public static final String HELP_IMG_L = "/at/gv/egiz/bku/gui/help_l.png";
  public static final String HELP_IMG_XL = "/at/gv/egiz/bku/gui/help_xl.png";
  public static final String HELP_IMG_XXL = "/at/gv/egiz/bku/gui/help_xxl.png";
  public static final String HELP_IMG_FOCUS = "/at/gv/egiz/bku/gui/help.png"; //help_focus.png";
  public static final String HASHDATA_FONT = "Monospaced";
  public static final Color ERROR_COLOR = Color.RED;
  public static final Color WARNING_COLOR = Color.ORANGE;
  public static final Color HYPERLINK_COLOR = Color.BLUE;
  public static final Color HELP_COLOR = new Color(70, 148, 169);
  public static final String TITLE_WELCOME = "title.welcome";
  public static final String TITLE_INSERTCARD = "title.insertcard";
  public static final String TITLE_CARD_NOT_SUPPORTED = "title.cardnotsupported";
  public static final String TITLE_VERIFY_PIN = "title.verify.pin";
  public static final String TITLE_SIGN = "title.sign";
  public static final String TITLE_VERIFY_PINPAD = "title.verify.pinpad";
  public static final String TITLE_ERROR = "title.error";
  public static final String TITLE_WARNING = "title.warning";
  public static final String TITLE_ENTRY_TIMEOUT = "title.entry.timeout";
  public static final String TITLE_RETRY = "title.retry";
  public static final String TITLE_WAIT = "title.wait";
  public static final String TITLE_SIGNATURE_DATA = "title.signature.data";
  public static final String WINDOWTITLE_SAVE = "windowtitle.save";
  public static final String WINDOWTITLE_ERROR = "windowtitle.error";
  public static final String WINDOWTITLE_SAVEDIR = "windowtitle.savedir";
  public static final String WINDOWTITLE_OVERWRITE = "windowtitle.overwrite";
  public static final String WINDOWTITLE_VIEWER = "windowtitle.viewer";
  public static final String WINDOWTITLE_HELP = "windowtitle.help";

  // removed message.* prefix to reuse keys as help keys
  public static final String MESSAGE_WELCOME = "welcome";
  public static final String MESSAGE_WAIT = "wait";
  public static final String MESSAGE_INSERTCARD = "insertcard";
  public static final String MESSAGE_CARD_NOT_SUPPORTED = "cardnotsupported";
  public static final String MESSAGE_ENTERPIN = "enterpin";
  public static final String MESSAGE_ENTERPIN_PINPAD = "enterpin.pinpad";
  public static final String MESSAGE_ENTERPIN_PINPAD_DIRECT = "enterpin.pinpad.direct";
  public static final String MESSAGE_HASHDATALINK = "hashdatalink";
  public static final String MESSAGE_HASHDATALINK_TINY = "hashdatalink.tiny";
  public static final String MESSAGE_HASHDATALINK_FOCUS = "hashdatalink.focus";
  public static final String MESSAGE_HASHDATALINK_TINY_FOCUS = "hashdatalink.tiny.focus";
  public static final String MESSAGE_HASHDATALIST = "hashdatalist";
  public static final String MESSAGE_HASHDATA_VIEWER = "hashdata.viewer";
  public static final String MESSAGE_UNSUPPORTED_MIMETYPE = "unsupported.mimetype";
  public static final String MESSAGE_RETRIES = "retries";
  public static final String MESSAGE_LAST_RETRY = "retries.last";
  public static final String MESSAGE_RETRIES_PINPAD = "retries.pinpad";
  public static final String MESSAGE_LAST_RETRY_PINPAD = "retries.pinpad.last";
  public static final String MESSAGE_OVERWRITE = "overwrite";
  public static final String MESSAGE_HELP = "help";

  public static final String WARNING_XHTML = "warning.xhtml";
  public static final String WARNING_CERT_NOTYETVALID = "warning.cert.notyetvalid";
  public static final String WARNING_CERT_EXPIRED = "warning.cert.expired";

  public static final String LABEL_PIN = "label.pin";
  public static final String LABEL_PINSIZE = "label.pinsize";
  public static final String HELP_WELCOME = "help.welcome";
  public static final String HELP_WAIT = "help.wait";
  public static final String HELP_CARDNOTSUPPORTED = "help.cardnotsupported";
  public static final String HELP_INSERTCARD = "help.insertcard";
  public static final String HELP_VERIFY_PIN = "help.cardpin";
  public static final String HELP_SIGNPIN = "help.signpin";
  public static final String HELP_PINPAD = "help.pinpad";
  public static final String HELP_RETRY = "help.retry";
  public static final String HELP_HASHDATA = "help.hashdata";
  public static final String HELP_HASHDATALIST = "help.hashdatalist";
  public static final String HELP_HASHDATAVIEWER = "help.hashdataviewer";
  public static final String BUTTON_OK = "button.ok";
  public static final String BUTTON_CANCEL = "button.cancel";
  public static final String BUTTON_BACK = "button.back";
  public static final String BUTTON_SIGN = "button.sign";
  public static final String BUTTON_SAVE = "button.save";
  public static final String BUTTON_CLOSE = "button.close";
  public static final String SAVE_HASHDATAINPUT_PREFIX = "save.hashdatainput.prefix";
  public static final String ALT_HELP = "alt.help";

  public static final String SIGDATA_TOOLTIPTEXT = "dialog.sigpin.infolabel.sigdata.tooltiptext";
  public static final String SWITCH_FOCUS_DUMMY_LABEL_NAME = "DummyLabel";
  
  public enum DIALOG_TYPE {DIALOGUE_UNDEFINED, DIALOGUE_VERIFY_PIN, DIALOGUE_ENTER_PIN, DIALOGUE_SHOW_SIG_DATA, DIALOGUE_SIGNATURE_PIN, DIALOGUE_MESSAGE};
  
  
  public void showEnterPINDirect(PinInfo pinInfo, int retries);

  public void showEnterPIN(PinInfo pinInfo, int retries);

  public void showSignatureDataDialog(PinInfo pinInfo, ActionListener listener, String string, ActionListener aThis0, String string0, ActionListener aThis1, String string1);

  public void correctionButtonPressed();

  public void allKeysCleared();

  public void validKeyPressed();

  public enum Style { tiny, simple, advanced };
    
  /**
   * BKUWorker needs to init signature card with locale
   * @return
   */
  public Locale getLocale();

  public void showVerifyPINDialog(PinInfo pinSpec, int numRetries,
          ActionListener okListener, String okCommand,
          ActionListener cancelListener, String cancelCommand);

  public void showSignaturePINDialog(PinInfo pinSpec, int numRetries,
          ActionListener signListener, String signCommand,
          ActionListener cancelListener, String cancelCommand,
          ActionListener viewerListener, String viewerCommand);

  public char[] getPin();

  /**
   *
   * @param dataToBeSigned
   * @param backListener if list of references hides pin dialog, backListener
   * receives an action when user hits 'back' button (i.e. whenever the pin-dialog
   * needs to be re-paint)
   * @param backCommand
   */
  public void showSecureViewer(List<HashDataInput> dataToBeSigned,
          ActionListener backListener, String backCommand);

  public void showErrorDialog(String errorMsgKey, Object[] errorMsgParams,
          ActionListener okListener, String okCommand);

  public void showErrorDialog(String errorMsgKey, Object[] errorMsgParams);

  public void showWarningDialog(String warningMsgKey, Object[] warningMsgParams,
          ActionListener okListener, String okCommand);

  public void showWarningDialog(String warningMsgKey, Object[] warningMsgParams);

  public void showMessageDialog(String titleKey, 
          String msgKey, Object[] msgParams,
          String buttonKey,
          ActionListener okListener, String okCommand);

  public void showMessageDialog(String titleKey,
          String msgKey, Object[] msgParams);

  public void showMessageDialog(String titleKey,
          String msgKey);
  
  public void getFocusFromBrowser();
}
