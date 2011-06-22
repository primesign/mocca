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



package at.gv.egiz.mocca.id;

import at.gv.egiz.bku.slcommands.SLCommand;
import at.gv.egiz.bku.slcommands.SLCommandContext;
import at.gv.egiz.bku.slcommands.SLResult;
import at.gv.egiz.bku.slcommands.impl.ErrorResultImpl;
import at.gv.egiz.bku.slexceptions.SLCommandException;

public class SLCommandBroker {
  
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
