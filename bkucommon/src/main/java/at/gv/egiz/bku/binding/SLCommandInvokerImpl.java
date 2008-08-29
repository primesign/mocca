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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.bku.slcommands.SLCommand;
import at.gv.egiz.bku.slcommands.SLCommandInvoker;
import at.gv.egiz.bku.slcommands.SLResult;
import at.gv.egiz.bku.slcommands.SLSourceContext;
import at.gv.egiz.bku.slcommands.SLTargetContext;

/**
 * This class implements the entry point for the CCEs security management.
 * 
 * TODO the secuirty management is currently not implemented.
 */
public class SLCommandInvokerImpl implements SLCommandInvoker {
  
  private static Log log = LogFactory.getLog(SLCommandInvokerImpl.class);

  protected SLCommand command;
  protected SLResult result;

  /**
   * Invokes a sl command. 
   */
  public void invoke(SLSourceContext aContext) {
    // FIXXME add security policy here.
    log.warn("Security policy not implemented yet, invoking command: "+command);
    result = command.execute();
  }

  public SLResult getResult(SLTargetContext aContext) {
    // FIXXME
    log.warn("Security policy not implemented yet, getting result of command: "+command);
    return result;
  }

  public void setCommand(SLCommand aCmd) {
    command = aCmd;
  }

  @Override
  public SLCommandInvoker newInstance() {
    SLCommandInvokerImpl cmdInv = new SLCommandInvokerImpl();
    return cmdInv;
  }
  
  
}