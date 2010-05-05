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

package at.gv.egiz.mocca.id;

import at.gv.egiz.bku.slcommands.SLCommand;
import at.gv.egiz.bku.slcommands.SLCommandContext;
import at.gv.egiz.bku.slcommands.SLResult;
import at.gv.egiz.bku.slcommands.impl.ErrorResultImpl;
import at.gv.egiz.bku.slexceptions.SLCommandException;

public class SLCommandBrocker {
  
  private Sync<SLCommand> commandSync = new Sync<SLCommand>();
  
  private Sync<SLResult> resultSync = new Sync<SLResult>();

  public SLResult execute(SLCommand command, SLCommandContext context, long timeout) throws InterruptedException {
    try {
      commandSync.put(command, timeout);
      if (command != null) {
        return resultSync.get(timeout);
      } else {
        return null;
      }
    } catch (SLCommandException e) {
      return new ErrorResultImpl(e, context.getLocale());
    }
  }
  
  public SLCommand nextCommand(SLResult result, long timeout) throws SLCommandException, InterruptedException {
    if (result != null) {
      resultSync.put(result, timeout);
    }
    return commandSync.get(timeout);
  }
  
  public class Sync<R> {
    
    private boolean available;
    
    private R r;
    
    public synchronized R get(long timeout) throws SLCommandException, InterruptedException {
      
      long t0 = System.currentTimeMillis();
      long elapsed = 0;

      while (!available) {
        wait(timeout - elapsed);
        elapsed = System.currentTimeMillis() - t0;
        if (elapsed > timeout) {
          notifyAll();
          throw new SLCommandException(6000);
        }
      }

      R r = this.r;
      this.r = null;
      available = false;
      notifyAll();
      return r;
    }
    
    public synchronized void put(R r, long timeout) throws SLCommandException, InterruptedException {
      
      long t0 = System.currentTimeMillis();
      long elapsed = 0;

      while (available) {
        wait(timeout - elapsed);
        elapsed = System.currentTimeMillis() - t0;
        if (elapsed > timeout) {
          notifyAll();
          throw new SLCommandException(6000);
        }
      }
      
      this.r = r;
      available = true;
      notifyAll();
    }
    
  }
  
}
