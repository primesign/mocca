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

package at.gv.egiz.bku.online.applet;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.bku.gui.BKUGUIFacade;
import at.gv.egiz.bku.gui.GetCertificateGUIFacade;
import at.gv.egiz.bku.smccstal.GetCertificateRequestHandler;
import at.gv.egiz.stal.ErrorResponse;
import at.gv.egiz.stal.InfoboxReadRequest;
import at.gv.egiz.stal.QuitRequest;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.SignRequest;
import at.gv.egiz.stal.ext.GetCertificateRequest;
import at.gv.egiz.stal.ext.GetCertificateResponse;

/**
*
* @author Thomas Lenz <thomas.lenz@iaik.tugraz.at>
*/

public class GetCertificateBKUWorker extends AppletBKUWorker {
	
	private final Logger log = LoggerFactory.getLogger(GetCertificateBKUWorker.class);

	  public GetCertificateBKUWorker(BKUApplet applet, GetCertificateGUIFacade gui) {
	    super(applet, gui);
	    removeRequestHandler(InfoboxReadRequest.class);
	    removeRequestHandler(SignRequest.class);
	    addRequestHandler(GetCertificateRequest.class, new GetCertificateRequestHandler());
	  }

	  @Override
	  public void run() {
	    gui.showMessageDialog(BKUGUIFacade.TITLE_WELCOME,
	            BKUGUIFacade.MESSAGE_WELCOME);

	    try {

	      List<STALResponse> responses = handleRequest(Collections.singletonList(new GetCertificateRequest()));
	      handleRequest(Collections.singletonList(new QuitRequest()));
	      
	      if (responses.size() == 1) {
	        STALResponse response = responses.get(0);
	        if (response instanceof GetCertificateResponse) {
	          log.debug("Get certificate dialog terminated.");
	        } else if (response instanceof ErrorResponse) {
	          log.debug("Get certificate dialog terminated with error.");
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
