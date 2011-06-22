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


package at.gv.egiz.bku.online.webapp;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.bku.binding.BindingProcessorManager;
import at.gv.egiz.bku.binding.Id;
import at.gv.egiz.bku.binding.IdFactory;

/**
 * Session listener to trigger the removal of the BindingProcessor
 *
 */
public class SessionListener implements HttpSessionListener {
  
  private final Logger log = LoggerFactory.getLogger(SessionListener.class);

  @Override
  public void sessionCreated(HttpSessionEvent event) {
    log.info("Session {} created.", event.getSession().getId());
    event.getSession().setAttribute(TransactionId.TRANSACTION_INDEX, new TransactionId());
  }

  @Override
  public void sessionDestroyed(HttpSessionEvent event) {
    BindingProcessorManager manager = (BindingProcessorManager) event.getSession().getServletContext().getAttribute("bindingProcessorManager");
    TransactionId tidx = (TransactionId) event.getSession().getAttribute(TransactionId.TRANSACTION_INDEX);
    if (tidx != null) {
      IdFactory idFactory = IdFactory.getInstance();
      for (int i = 0; i <= tidx.get(); i++) {
        Id id = idFactory.createId(event.getSession().getId() + "-" + i);
        manager.removeBindingProcessor(id);
      }
    }
    log.info("Session {} destroyed.", event.getSession().getId());
  }

}
