/*
* Copyright 2009 Federal Chancellery Austria and
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
        return Collections.singletonList((STALResponse) response);
      }
    } catch (InterruptedException e) {
      // interrupted
      ErrorResponse response = new ErrorResponse(6000);
      return Collections.singletonList((STALResponse) response);
    }
    
  }

}
