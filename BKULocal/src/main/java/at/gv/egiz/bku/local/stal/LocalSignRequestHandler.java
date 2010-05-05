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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.bku.smccstal.SignRequestHandler;
import at.gv.egiz.stal.ErrorResponse;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.SignRequest;

/**
 * 
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class LocalSignRequestHandler extends SignRequestHandler {

  private final Logger log = LoggerFactory.getLogger(LocalSignRequestHandler.class);

  public LocalSignRequestHandler(LocalSecureViewer secureViewer) {
    super(secureViewer);
  }

  /**
   * If the request is a SIGN request, it contains a list of DataObjectHashDataInput 
   * providing the pre-digested input stream (that can be obtained repeatedly) if 
   * reference caching is enabled (or null otherwise).
   * @param request
   * @return
   */
  @Override
  public STALResponse handleRequest(STALRequest request) 
          throws InterruptedException {
    
    if (request instanceof SignRequest) {
      SignRequest signReq = (SignRequest) request;
      ((LocalSecureViewer) secureViewer).setDataToBeSigned(signReq.getHashDataInput());
      return super.handleRequest(request);
    } else {
      log.error("Got unexpected STAL request: {}.", request);
      return new ErrorResponse(1000);
    }

    
  }
}
