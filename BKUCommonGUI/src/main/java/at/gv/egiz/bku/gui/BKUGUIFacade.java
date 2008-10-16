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
import java.awt.Container;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.List;

public interface BKUGUIFacade {
  
  public static final String ERR_UNKNOWN = "error.unknown";
  public static final String ERR_SERVICE_UNREACHABLE = "error.ws.unreachable";
  public static final String ERR_NO_PCSC = "error.pcsc";
  public static final String ERR_NO_CARDTERMINAL = "error.cardterminal";
  public static final String ERR_NO_HASHDATA = "error.no.hashdata";
  public static final String ERR_DISPLAY_HASHDATA = "error.display.hashdata";
  public static final String ERR_WRITE_HASHDATA = "error.write.hashdata";
  public static final String ERR_INVALID_HASH = "error.invalid.hash";
  
    public static final String MESSAGES_BUNDLE = "at/gv/egiz/bku/gui/Messages";
    public static final String DEFAULT_BACKGROUND = "/images/BackgroundChipperling.png";
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
    public static final String MESSAGE_WAIT = "message.wait";
    public static final String MESSAGE_INSERTCARD = "message.insertcard";
    public static final String MESSAGE_ENTERPIN = "message.enterpin";
    public static final String MESSAGE_HASHDATALINK = "message.hashdatalink";
    public static final String MESSAGE_HASHDATA = "message.hashdata";
    public static final String MESSAGE_HASHDATALIST = "message.hashdatalist";
    public static final String MESSAGE_RETRIES = "message.retries";
    public static final String MESSAGE_OVERWRITE = "message.overwrite";
    public static final String LABEL_PIN = "label.pin";
    public static final String LABEL_PINSIZE = "label.pinsize";
//    public static final String ERROR_NO_HASHDATA = "error.no.hashdata";
    
    public static final String BUTTON_OK = "button.ok";
    public static final String BUTTON_CANCEL = "button.cancel";
    public static final String BUTTON_BACK = "button.back";
    public static final String BUTTON_SIGN = "button.sign";
    public static final String BUTTON_SAVE = "button.save";
    public static final String SAVE_HASHDATAINPUT_PREFIX = "save.hashdatainput.prefix";
    
    
    public void init(Container contentPane, String localeString, URL background);
    
    public void showWelcomeDialog(); 
    
    /**
     * MOA-ID only
     * @param loginListener
     */
    public void showLoginDialog(ActionListener loginListener, String actionCommand);

   /** optional wait message */
    public void showWaitDialog(String waitMessage);

    public void showInsertCardDialog(ActionListener cancelListener, String actionCommand);
    
    public void showCardNotSupportedDialog(ActionListener cancelListener, String actionCommand);
    
    public void showCardPINDialog(PINSpec pinSpec, ActionListener okListener, String okCommand, ActionListener cancelListener, String cancelCommand);
    
    public void showCardPINRetryDialog(PINSpec pinSpec, int numRetries, ActionListener okListener, String okCommand, ActionListener cancelListener, String cancelCommand);
    
    public void showSignaturePINDialog(PINSpec pinSpec, ActionListener signListener, String signCommand, ActionListener cancelListener, String cancelCommand, ActionListener hashdataListener, String hashdataCommand);
    
    public void showSignaturePINRetryDialog(PINSpec pinSpec, int numRetries, ActionListener okListener, String okCommand, ActionListener cancelListener, String cancelCommand, ActionListener hashdataListener, String hashdataCommand);
    
    public char[] getPin();
    
    public void showHashDataInputDialog(List<HashDataInput> signedReferences, ActionListener okListener, String actionCommand);
    
//    public void showPlainTextHashDataInputDialog(String text, ActionListener saveListener, String saveCommand, ActionListener cancelListener, String cancelCommand);
    
    public void showErrorDialog(String errorMsgKey, Object[] errorMsgParams, ActionListener okListener, String actionCommand);
    
    public void showErrorDialog(String errorMsgKey, Object[] errorMsgParams);
}
