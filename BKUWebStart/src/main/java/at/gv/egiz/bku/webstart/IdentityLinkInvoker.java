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


package at.gv.egiz.bku.webstart;

import java.io.IOException;
import java.net.HttpURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.bku.webstart.gui.StatusNotifier;

/**
 * 
 * @author Andreas Fitzek <andreas.fitzek@iaik.tugraz.at>
 */
public class IdentityLinkInvoker implements Runnable {
	
	private static final Logger log = LoggerFactory.getLogger(IdentityLinkInvoker.class);
	  
	private StatusNotifier status;
	
	public IdentityLinkInvoker(StatusNotifier status) {
	      this.status = status;
	  }

	@Override
	public void run() {
		HttpURLConnection connection = null;
	    try {
	      log.debug("Connecting to: " + Launcher.IDENTITY_LINK_URL);

	      connection = (HttpURLConnection) Launcher.IDENTITY_LINK_URL.openConnection();

	      connection.setRequestMethod("GET");
	      connection.setReadTimeout(0);
	      connection.connect();

	      if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
	        log.debug("identity link dialog returned");
	      } else {
	        log.error("unexpected response from identity link: " + connection.getResponseMessage());
	      }
	    } catch (IOException ex) {
	      log.error("Failed to connect to identity link", ex);
	      status.error(StatusNotifier.ERROR_IDENTITY_LINK);
	    } finally {
	      if (connection != null) {
	        connection.disconnect();
	      }
	    }
	}
}
