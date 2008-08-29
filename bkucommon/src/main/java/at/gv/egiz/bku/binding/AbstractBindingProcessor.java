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
package at.gv.egiz.bku.binding;

import java.io.InputStream;
import java.util.Date;

import at.gv.egiz.bku.slcommands.SLCommandInvoker;
import at.gv.egiz.stal.STAL;

public abstract class AbstractBindingProcessor implements BindingProcessor {
  protected Id id;
  protected STAL stal;
  protected SLCommandInvoker commandInvoker;
  protected long lastAccessedTime = System.currentTimeMillis();

  public AbstractBindingProcessor(String idString) {
    this.id = IdFactory.getInstance().createId(idString);
  }

  /**
   * @see java.lang.Thread#run()
   */
  public abstract void run();

  /**
   * The caller is advised to check the result in case an error occurred.
   * 
   * @see #getResult()
   */
  public abstract void consumeRequestStream(InputStream aIs);

  public Id getId() {
    return id;
  }

  public STAL getSTAL() {
    return stal;
  }

  public SLCommandInvoker getCommandInvoker() {
    return commandInvoker;
  }
  
  public void updateLastAccessTime() {
    lastAccessedTime = System.currentTimeMillis();
  }

  public Date getLastAccessTime() {
    return new Date(lastAccessedTime);
  }

  /**
   * To be called after object creation.
   * 
   * @param aStal
   *          must not be null
   * @param aCommandInvoker
   *          must not be null
   */
  public void init(STAL aStal, SLCommandInvoker aCommandInvoker) {
    if (aStal == null) {
      throw new NullPointerException("STAL must not be set to null");
    }
    if (aCommandInvoker == null) {
      throw new NullPointerException("Commandinvoker must not be set to null");
    }
    stal = aStal;
    commandInvoker = aCommandInvoker;
    Thread.currentThread().setName("BPID#"+getId().toString());
  }
}