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


package at.gv.egiz.bku.local.stal;

import at.gv.egiz.bku.gui.BKUGUIFacade;
import at.gv.egiz.bku.smccstal.AbstractBKUWorker;
import at.gv.egiz.bku.smccstal.GetCertificateRequestHandler;
import at.gv.egiz.bku.smccstal.GetHardwareInfoRequestHandler;
import at.gv.egiz.bku.smccstal.PINManagementRequestHandler;
import at.gv.egiz.bku.smccstal.IdentityLinkRequestHandler;
import at.gv.egiz.stal.QuitRequest;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.SignRequest;

import at.gv.egiz.stal.ext.IdentityLinkRequest;
import at.gv.egiz.stal.ext.GetCertificateRequest;
import at.gv.egiz.stal.ext.GetHardwareInfoRequest;
import at.gv.egiz.stal.ext.PINManagementRequest;
import java.util.List;
import javax.swing.JFrame;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class LocalBKUWorker extends AbstractBKUWorker {

  private JFrame container;

  public LocalBKUWorker(BKUGUIFacade gui, JFrame container) {
    super(gui);
    this.container = container;
    addRequestHandler(SignRequest.class, 
            new LocalSignRequestHandler(new LocalSecureViewer(gui)));
    addRequestHandler(PINManagementRequest.class, new PINManagementRequestHandler());
    addRequestHandler(IdentityLinkRequest.class, new IdentityLinkRequestHandler());
    addRequestHandler(GetCertificateRequest.class, new GetCertificateRequestHandler());
    addRequestHandler(GetHardwareInfoRequest.class, new GetHardwareInfoRequestHandler());
  }

  /** does not change container's visibility (use quit request to close) */
  @Override
  public List<STALResponse> handleRequest(List<? extends STALRequest> requestList) {
    signatureCard = null;
    List<STALResponse> responses = super.handleRequest(requestList);
      container.setVisible(false);
    return responses;
  }

  /** overrides handle quit from abstract bku worker, make container invisible */
  @Override
  public STALResponse handleRequest(STALRequest request) {
    if (request instanceof QuitRequest) {
      container.setVisible(false);
    }
    return null;
  }
}
