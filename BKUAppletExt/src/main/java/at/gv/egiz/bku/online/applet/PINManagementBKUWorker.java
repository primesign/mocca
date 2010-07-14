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
import at.gv.egiz.bku.smccstal.PINManagementRequestHandler;
import at.gv.egiz.stal.ErrorResponse;
import at.gv.egiz.stal.InfoboxReadRequest;
import at.gv.egiz.stal.QuitRequest;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.SignRequest;
import at.gv.egiz.stal.ext.PINManagementRequest;
import at.gv.egiz.stal.ext.PINManagementResponse;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This BKU Worker does not connect to STAL webservice
 * (no Internet connection permitted while activating PINs).
 * 
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class PINManagementBKUWorker extends AppletBKUWorker {
  
  private final Logger log = LoggerFactory.getLogger(PINManagementBKUWorker.class);

  public PINManagementBKUWorker(BKUApplet applet, PINManagementGUIFacade gui) {
    super(applet, gui);
    removeRequestHandler(InfoboxReadRequest.class);
    removeRequestHandler(SignRequest.class);
    addRequestHandler(PINManagementRequest.class, new PINManagementRequestHandler());
  }

  @Override
  public void run() {
    gui.showMessageDialog(BKUGUIFacade.TITLE_WELCOME,
            BKUGUIFacade.MESSAGE_WELCOME);

    try {

      List<STALResponse> responses = handleRequest(Collections.singletonList(new PINManagementRequest()));
      handleRequest(Collections.singletonList(new QuitRequest()));
      
      if (responses.size() == 1) {
        STALResponse response = responses.get(0);
        if (response instanceof PINManagementResponse) {
          log.debug("PIN management dialog terminated.");
        } else if (response instanceof ErrorResponse) {
          log.debug("PIN management dialog terminated with error.");
        } else {
          throw new RuntimeException("Invalid STAL response: " + response.getClass().getName());
        }
      } else {
        throw new RuntimeException("invalid number of STAL responses: " + responses.size());
      }

    } catch (RuntimeException ex) {
      log.error(ex.getMessage());
      Throwable cause = ex.getCause();
      if (cause != null) { // && cause instanceof InterruptedException) {
        log.info(cause.getMessage());
      }
      showErrorDialog(BKUGUIFacade.ERR_UNKNOWN, null);
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
      showErrorDialog(BKUGUIFacade.ERR_UNKNOWN_WITH_PARAM, ex);
    } finally {
      if (signatureCard != null) {
        signatureCard.disconnect(false);
      }
    }

    applet.sendRedirect();
  }

  }
