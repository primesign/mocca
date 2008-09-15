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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.bku.smccstal.SMCCSTALRequestHandler;
import at.gv.egiz.bku.smccstal.SignRequestHandler;
import at.gv.egiz.stal.HashDataInput;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.SignRequest;
import at.gv.egiz.stal.signedinfo.ReferenceType;

/**
 * 
 * @author clemens
 */
public class LocalSignRequestHandler extends SignRequestHandler {

  private static final Log log = LogFactory
      .getLog(LocalSignRequestHandler.class);
  private List<HashDataInput> hashDataInput = Collections.EMPTY_LIST;

  public LocalSignRequestHandler() {
  }

  @SuppressWarnings("unchecked")
  @Override
  public STALResponse handleRequest(STALRequest request) {
    if (request instanceof SignRequest) {
      SignRequest signReq = (SignRequest) request;
      hashDataInput = signReq.getHashDataInput();
    }
    return super.handleRequest(request);
  }

  @Override
  protected List<HashDataInput> getHashDataInputs(
      List<ReferenceType> dsigReferences) throws Exception {
    ArrayList<HashDataInput> result = new ArrayList<HashDataInput>();
    for (ReferenceType dsigRef : dsigReferences) {
      // don't get Manifest, QualifyingProperties, ...
      if (dsigRef.getType() == null) {
        String dsigRefId = dsigRef.getId();
        if (dsigRefId != null) {
          for (HashDataInput hdi : hashDataInput) {
            if (hdi.getReferenceId().equals(dsigRefId)) {
              result.add(hdi);
            }
          }
        } else {
          throw new Exception(
              "Cannot get HashDataInput for dsig:Reference without Id attribute");
        }
      }
    }
    return result;
  }

  @Override
  public SMCCSTALRequestHandler newInstance() {
    return new LocalSignRequestHandler();
  }
}
