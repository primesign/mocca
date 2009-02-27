/*
 * Copyright 2008 Federal Chancellery Austria and
 * Graz University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.gv.egiz.bku.gui;

import at.gv.egiz.stal.HashDataInput;
import at.gv.egiz.smcc.PINSpec;
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
  public static final String ERR_VIEWER = "error.viewer";
  public static final String ERR_EXTERNAL_LINK = "error.external.link";
  public static final String ERR_CONFIG = "error.config";
  
  public static final String MESSAGES_BUNDLE = "at/gv/egiz/bku/gui/Messages";
  public static final String DEFAULT_BACKGROUND = "/images/BackgroundChipperling.png";
  public static final String HELP_IMG = "/images/help.png";
  public static final String HASHDATA_FONT = "Monospaced";
  public static final Color ERROR_COLOR = Color.RED;
  public static final Color HYPERLINK_COLOR = Color.BLUE;
  public static final String TITLE_WELCOME = "title.welcome";
  public static final String TITLE_INSERTCARD = "title.insertcard";
  public static final String TITLE_CARD_NOT_SUPPORTED = "title.cardnotsupported";
  public static final String TITLE_CARDPIN = "title.cardpin";
  public static final String TITLE_SIGN = "title.sign";
  public static final String TITLE_ERROR = "title.error";
  public static final String TITLE_RETRY = "title.retry";
  public static final String TITLE_WAIT = "title.wait";
  public static final String TITLE_HASHDATA = "title.hashdata";
  public static final String WINDOWTITLE_SAVE = "windowtitle.save";
  public static final String WINDOWTITLE_SAVEDIR = "windowtitle.savedir";
  public static final String WINDOWTITLE_OVERWRITE = "windowtitle.overwrite";
  public static final String WINDOWTITLE_VIEWER = "windowtitle.viewer";
  public static final String WINDOWTITLE_HELP = "windowtitle.help";
  public static final String MESSAGE_WAIT = "message.wait";
  public static final String MESSAGE_INSERTCARD = "message.insertcard";
  public static final String MESSAGE_ENTERPIN = "message.enterpin";
  public static final String MESSAGE_HASHDATALINK = "message.hashdatalink";
  public static final String MESSAGE_HASHDATALINK_TINY = "message.hashdatalink.tiny";
//  public static final String MESSAGE_HASHDATA = "message.hashdata";
  public static final String MESSAGE_HASHDATALIST = "message.hashdatalist";
  public static final String MESSAGE_RETRIES = "message.retries";
  public static final String MESSAGE_LAST_RETRY = "message.retries.last";
  public static final String MESSAGE_OVERWRITE = "message.overwrite";
  public static final String MESSAGE_HELP = "message.help";
  public static final String WARNING_XHTML = "warning.xhtml";
  public static final String LABEL_PIN = "label.pin";
  public static final String LABEL_PINSIZE = "label.pinsize";
  public static final String HELP_WELCOME = "help.welcome";
  public static final String HELP_WAIT = "help.wait";
  public static final String HELP_CARDNOTSUPPORTED = "help.cardnotsupported";
  public static final String HELP_INSERTCARD = "help.insertcard";
  public static final String HELP_CARDPIN = "help.cardpin";
  public static final String HELP_SIGNPIN = "help.signpin";
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

  public enum Style { tiny, simple, advanced };
    
//  public void init(Container contentPane, Locale locale, Style guiStyle, URL background, ActionListener helpListener);

  /**
   * BKUWorker needs to init signature card with locale
   * @return
   */
  public Locale getLocale();

  public void showWelcomeDialog();

  /**
   * 
   * @param waitMessage if null, a simple 'please wait' text is displayed
   */
  public void showWaitDialog(String waitMessage);

  public void showInsertCardDialog(ActionListener cancelListener, String actionCommand);

  public void showCardNotSupportedDialog(ActionListener cancelListener, String actionCommand);

  public void showCardPINDialog(PINSpec pinSpec, ActionListener okListener, String okCommand, ActionListener cancelListener, String cancelCommand);

  public void showCardPINRetryDialog(PINSpec pinSpec, int numRetries, ActionListener okListener, String okCommand, ActionListener cancelListener, String cancelCommand);

  public void showSignaturePINDialog(PINSpec pinSpec, ActionListener signListener, String signCommand, ActionListener cancelListener, String cancelCommand, ActionListener hashdataListener, String hashdataCommand);

  public void showSignaturePINRetryDialog(PINSpec pinSpec, int numRetries, ActionListener okListener, String okCommand, ActionListener cancelListener, String cancelCommand, ActionListener hashdataListener, String hashdataCommand);

  public char[] getPin();

  public void showHashDataInputDialog(List<HashDataInput> signedReferences, ActionListener okListener, String okCommand);

  public void showErrorDialog(String errorMsgKey, Object[] errorMsgParams, ActionListener okListener, String actionCommand);

  public void showErrorDialog(String errorMsgKey, Object[] errorMsgParams);
}
