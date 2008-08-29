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
package at.gv.egiz.bku.local.stal;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.bku.gui.BKUGUIFacade;
import at.gv.egiz.bku.local.ui.TrayIconDialog;
import at.gv.egiz.bku.smccstal.AbstractRequestHandler;
import at.gv.egiz.bku.smccstal.AbstractSMCCSTAL;
import at.gv.egiz.bku.smccstal.STALMessageConsumer;
import at.gv.egiz.smcc.PINProvider;
import at.gv.egiz.smcc.util.SMCCHelper;
import at.gv.egiz.stal.QuitRequest;

public class SMCCSTAL extends AbstractSMCCSTAL implements STALMessageConsumer {
  private static Log log = LogFactory.getLog(SMCCSTAL.class);

  protected PINProvider pinProvider = new SwingPINProvider();
  protected SwingInsertCardDialog insertCard = new SwingInsertCardDialog();
  private boolean canceled = false;

  static {
    addRequestHandler(QuitRequest.class, new QuitRequestHandler());
  }

  public SMCCSTAL() {
    AbstractRequestHandler.setMessageConsumer(this);
  }

  /**
   * 
   * @return if the user canceled
   */
  protected boolean waitForCard() {
    canceled = false;
    while ((smccHelper.getResultCode() != SMCCHelper.CARD_FOUND) && (!canceled)) {
      insertCard.setVisible(true);
      insertCard.setAlwaysOnTop(true);
      insertCard.addCanceledListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          canceled = true;
        }
      });
      try {
        smccHelper.update(1000);
      } catch (Exception ex) {
        log.info(ex);
      }
    }
    insertCard.setVisible(false);
    signatureCard = smccHelper.getSignatureCard(locale);
    return canceled;
  }

  @Override
  public void setLocale(Locale locale) {
    super.setLocale(locale);
    if (pinProvider instanceof SwingPINProvider) {
      ((SwingPINProvider) pinProvider).setLocale(locale);
    }
  }

  @Override
  public void consumeNewSTALMessage(String captionId, String messageId) {
    TrayIconDialog.getInstance().displayInfo(captionId, messageId);
  }

  @Override
  protected BKUGUIFacade getGUI() {
    // TODO Auto-generated method stub
    //FIXME
    return null;
  }
}
