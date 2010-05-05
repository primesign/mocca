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

import at.gv.egiz.bku.gui.BKUGUIFacade;
import at.gv.egiz.bku.smccstal.AbstractBKUWorker;
import at.gv.egiz.bku.smccstal.PINManagementRequestHandler;
import at.gv.egiz.stal.QuitRequest;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.SignRequest;

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
