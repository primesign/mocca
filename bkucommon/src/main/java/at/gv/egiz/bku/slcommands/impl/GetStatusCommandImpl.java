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

package at.gv.egiz.bku.slcommands.impl;

import at.buergerkarte.namespaces.securitylayer._1.GetStatusRequestType;
import at.gv.egiz.bku.slcommands.GetStatusCommand;
import at.gv.egiz.bku.slcommands.SLCommandContext;
import at.gv.egiz.bku.slcommands.SLResult;
import at.gv.egiz.bku.slexceptions.SLCommandException;
import at.gv.egiz.stal.ErrorResponse;
import at.gv.egiz.stal.STAL;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.StatusRequest;
import at.gv.egiz.stal.StatusResponse;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class GetStatusCommandImpl extends SLCommandImpl<GetStatusRequestType> implements GetStatusCommand {

  protected final Logger log = LoggerFactory.getLogger(GetStatusCommandImpl.class);

  @Override
  public String getName() {
    return "GetStatusRequest";
  }

  @Override
  public SLResult execute(SLCommandContext commandContext) {

    //ignore maxDelay and TokenStatus

    log.debug("Execute GetStatusRequest.");

    StatusRequest stalRequest = new StatusRequest();

    STAL stal = commandContext.getSTAL();

    List<STALResponse> responses = stal.handleRequest(Collections.singletonList(stalRequest));
    
    if (responses != null && responses.size() == 1) {
      STALResponse stalResponse = responses.get(0);
      if (stalResponse instanceof StatusResponse) {
        boolean ready = ((StatusResponse) stalResponse).isCardReady();
        log.trace("Received status response cardReady: {}.", ready);
        return new GetStatusResultImpl(ready);
      } else if (stalResponse instanceof ErrorResponse) {
        log.debug("Received error response.");
        SLCommandException ex = new SLCommandException(((ErrorResponse) stalResponse).getErrorCode());
        return new ErrorResultImpl(ex, commandContext.getLocale());
      }
    }
    log.error("Received unexpected responses.");
    return new ErrorResultImpl(new SLCommandException(4000), commandContext.getLocale());

  }
}
