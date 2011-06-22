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


package at.gv.egiz.bku.smccstal;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.bku.gui.BKUGUIFacade;
import at.gv.egiz.smcc.SignatureCard;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;

public abstract class AbstractRequestHandler implements SMCCSTALRequestHandler,
    ActionListener {
  private final Logger log = LoggerFactory.getLogger(AbstractRequestHandler.class);

  protected SignatureCard card;
  protected BKUGUIFacade gui;
  protected String actionCommand;
  protected boolean actionPerformed = false;

  @Override
  public abstract STALResponse handleRequest(STALRequest request) throws InterruptedException;
  
  @Override
  public void init(SignatureCard sc, BKUGUIFacade gui) {
    if ((sc == null) || (gui == null)) {
      throw new NullPointerException("Parameter must not be set to null");
    }
    this.card = sc;
    this.gui = gui;
  }
  
  protected synchronized void waitForAction() throws InterruptedException {
    try {
      while (!actionPerformed) {
        wait();
      }
    } catch (InterruptedException e) {
      log.error("interrupt in waitForAction");
      throw e;
    }
    actionPerformed = false;
  }

  private synchronized void actionPerformed() {
    actionPerformed = true;
    notifyAll();
  }
  
  @Override
  public void actionPerformed(ActionEvent e) {
    actionCommand = e.getActionCommand();
    actionPerformed();
  }

}
