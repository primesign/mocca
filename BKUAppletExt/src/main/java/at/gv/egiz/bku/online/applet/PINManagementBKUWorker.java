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
package at.gv.egiz.bku.online.applet;

import at.gv.egiz.bku.gui.BKUGUIFacade;
import at.gv.egiz.bku.gui.PINManagementGUIFacade;
import at.gv.egiz.bku.smccstal.ext.PINMgmtRequestHandler;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.ext.ActivatePINRequest;
import at.gv.egiz.stal.ext.ChangePINRequest;
import at.gv.egiz.stal.ext.UnblockPINRequest;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class PINManagementBKUWorker extends AppletBKUWorker {

  protected PINMgmtRequestHandler handler = new PINMgmtRequestHandler();
  protected PINManagementActionListener listener = new PINManagementActionListener();

  public PINManagementBKUWorker(BKUApplet applet, BKUGUIFacade gui) {
    super(applet, gui);
    handlerMap.clear();
//    PINMgmtRequestHandler handler = new PINMgmtRequestHandler();
//    addRequestHandler(ActivatePINRequest.class, handler);
//    addRequestHandler(ChangePINRequest.class, handler);
//    addRequestHandler(UnblockPINRequest.class, handler);
  }

  @Override
  public void run() {
    gui.showWelcomeDialog();

    try {

      if (waitForCard()) {
        gui.showErrorDialog("no card, canceled PIN mgmt dialog", null);
      }

      actionCommandList.clear();
      actionCommandList.add("cancel");

      ((PINManagementGUIFacade) gui).showPINManagementDialog(handler,
              listener, "activate",
              listener, "change",
              listener, "unblock",
              this, "cancel");

      waitForAction();

    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
      showErrorDialog(BKUGUIFacade.ERR_UNKNOWN_WITH_PARAM, ex);
    } finally {
      if (signatureCard != null) {
        signatureCard.disconnect(false);
      }
    }

    applet.sendRedirect(sessionId);
  }

  protected class PINManagementActionListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      try {
        String cmd = e.getActionCommand();
        if ("activate".equals(cmd)) {
          //create STAL request, call handle(req)
          ActivatePINRequest stalReq = new ActivatePINRequest();
          STALResponse stalResp = handler.handleRequest(stalReq);
          gui.showErrorDialog(BKUGUIFacade.ERR_UNKNOWN_WITH_PARAM, new Object[]{"debug"}, this, "back");
        } else if ("change".equals(cmd)) {
        } else if ("unblock".equals(cmd)) {
        } else if ("back".equals(cmd)) {

          ((PINManagementGUIFacade) gui).showPINManagementDialog(handler,
                  this, "activate",
                  this, "change",
                  this, "unblock",
                  PINManagementBKUWorker.this, "cancel");

        }
      } catch (InterruptedException ex) {
        log.fatal(ex);
      }
    }
  }
}
