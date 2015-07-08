/*
 * Copyright 2015 Datentechnik Innovation GmbH and Prime Sign GmbH, Austria
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.bku.smccstal.BulkSignRequestHandler;

import at.gv.egiz.stal.BulkSignRequest;
import at.gv.egiz.stal.ErrorResponse;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;

/**
 * 
 * @author szoescher
 */
public class LocalBulkSignRequestHandler extends BulkSignRequestHandler {

  private final Logger log = LoggerFactory.getLogger(LocalBulkSignRequestHandler.class);

  public LocalBulkSignRequestHandler(LocalSecureViewer secureViewer) {
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
  public STALResponse handleRequest(STALRequest request) throws InterruptedException {

    if (request instanceof BulkSignRequest) {

      
      // TODO set hashDataInput
      // ((LocalSecureViewer) secureViewer).setDataToBeSigned(signReq.getHashDataInput());
      return super.handleRequest(request);
    } else {
      log.error("Got unexpected STAL request: {}.", request);
      ErrorResponse err = new ErrorResponse(1000);
      err.setErrorMessage("Got unexpected STAL request: " + request);
      return err;
    }
  }
}
