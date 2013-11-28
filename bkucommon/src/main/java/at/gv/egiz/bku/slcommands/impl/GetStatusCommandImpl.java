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



package at.gv.egiz.bku.slcommands.impl;

import at.buergerkarte.namespaces.securitylayer._1_2_3.GetStatusRequestType;
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
