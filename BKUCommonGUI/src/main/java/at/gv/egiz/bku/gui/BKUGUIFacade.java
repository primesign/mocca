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
import java.awt.Container;
import java.awt.event.ActionListener;
import java.util.List;

public interface BKUGUIFacade {
    
    public void init(Container contentPane, String localeString);
    
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
    
    public void showErrorDialog(String errorMsg, ActionListener okListener, String actionCommand);
    
    public void showErrorDialog(String errorMsg);
}
