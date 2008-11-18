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

import at.gv.egiz.bku.slcommands.impl.DataObjectHashDataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.bku.smccstal.SignRequestHandler;
import at.gv.egiz.stal.HashDataInput;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.SignRequest;
import at.gv.egiz.stal.impl.ByteArrayHashDataInput;
import at.gv.egiz.stal.signedinfo.ReferenceType;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * 
 * @author clemens
 */
public class LocalSignRequestHandler extends SignRequestHandler {

  private static final Log log = LogFactory.getLog(LocalSignRequestHandler.class);
  private List<HashDataInput> hashDataInputs = Collections.EMPTY_LIST;

  /**
   * If the request is a SIGN request, it contains a list of DataObjectHashDataInput 
   * providing the pre-digested input stream (that can be obtained repeatedly) if 
   * reference caching is enabled (or null otherwise).
   * @param request
   * @return
   */
  @SuppressWarnings("unchecked")
  @Override
  public STALResponse handleRequest(STALRequest request) throws InterruptedException {
    if (request instanceof SignRequest) {
      SignRequest signReq = (SignRequest) request;
      hashDataInputs = signReq.getHashDataInput();
    }
    return super.handleRequest(request);
  }

  /**
   * 
   * @param dsigReferences
   * @throws java.lang.Exception
   */
  @Override
  public void displayHashDataInputs(List<ReferenceType> dsigReferences) throws Exception {
    if (dsigReferences == null || dsigReferences.size() < 1) {
      log.error("No hashdata input selected to be displayed: null");
      throw new Exception("No HashData Input selected to be displayed");
    }

    ArrayList<HashDataInput> selectedHashDataInputs = new ArrayList<HashDataInput>();
    for (ReferenceType dsigRef : dsigReferences) {
      // don't get Manifest, QualifyingProperties, ...
      if (dsigRef.getType() == null) {
        String dsigRefId = dsigRef.getId();
        if (dsigRefId != null) {
          boolean hdiAvailable = false;
          for (HashDataInput hashDataInput : hashDataInputs) {
            if (dsigRefId.equals(hashDataInput.getReferenceId())) {
              log.debug("display hashdata input for dsig:SignedReference " + dsigRefId);
              if (!(hashDataInput instanceof DataObjectHashDataInput)) {
                log.warn(
                  "expected DataObjectHashDataInput for LocalSignRequestHandler, got " + hashDataInput.getClass().getName());
                hashDataInput = getByteArrayHashDataInput(hashDataInput);
              }
              selectedHashDataInputs.add(hashDataInput);
              hdiAvailable = true;
              break;
            }
          }
          if (!hdiAvailable) {
            log.error("no hashdata input for dsig:SignedReference " + dsigRefId);
            throw new Exception(
              "No HashDataInput available for dsig:SignedReference " + dsigRefId);
          }
        } else {
          throw new Exception(
            "Cannot get HashDataInput for dsig:Reference without Id attribute");
        }
      }
    }

    if (selectedHashDataInputs.size() < 1) {
      log.error("dsig:SignedInfo does not contain a data reference");
      throw new Exception("dsig:SignedInfo does not contain a data reference");
    }
    gui.showHashDataInputDialog(selectedHashDataInputs, this, "hashDataDone");
  }

  private ByteArrayHashDataInput getByteArrayHashDataInput(HashDataInput hashDataInput) throws IOException {

    InputStream hdIs = hashDataInput.getHashDataInput();
    ByteArrayOutputStream baos = new ByteArrayOutputStream(hdIs.available());
    int b;
    while ((b = hdIs.read()) != -1) {
      baos.write(b);
    }
    ByteArrayHashDataInput hdi = new ByteArrayHashDataInput(baos.toByteArray(), hashDataInput.getReferenceId(), hashDataInput.getMimeType(), hashDataInput.getEncoding());

    return hdi;
  }
}
