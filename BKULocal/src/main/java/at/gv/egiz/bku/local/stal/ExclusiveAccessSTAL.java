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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.stal.ErrorResponse;
import at.gv.egiz.stal.STAL;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;

public class ExclusiveAccessSTAL implements STAL {
  
  private final Logger log = LoggerFactory.getLogger(ExclusiveAccessSTAL.class);
  
  private Lock lock = new ReentrantLock(true);
  
  private long timeout = 30;
  
  private STAL stal;
  
  public ExclusiveAccessSTAL(STAL stal) {
    if (stal == null) {
      throw new NullPointerException("Argument 'stal' must not be null.");
    }
    this.stal = stal;
  }

  @Override
  public List<STALResponse> handleRequest(
      List<? extends STALRequest> aRequestList) {

    try {
      if (lock.tryLock(timeout, TimeUnit.SECONDS)) {
        try {
          return stal.handleRequest(aRequestList);
        } finally {
          lock.unlock();
        }
      } else {
        // time out
        log.info("Timeout while waiting for exclusive access to STAL.");
        ErrorResponse response = new ErrorResponse(6000);
        response.setErrorMessage("Timeout while waiting for exclusive access to STAL.");
        return Collections.singletonList((STALResponse) response);
      }
    } catch (InterruptedException e) {
      // interrupted
      ErrorResponse response = new ErrorResponse(6000);
      response.setErrorMessage("Interrupted: " + e);
      return Collections.singletonList((STALResponse) response);
    }
    
  }

}
