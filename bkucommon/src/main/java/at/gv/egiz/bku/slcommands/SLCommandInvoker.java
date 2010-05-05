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
package at.gv.egiz.bku.slcommands;

import at.gv.egiz.bku.slexceptions.SLCanceledException;
import at.gv.egiz.bku.slexceptions.SLException;

public interface SLCommandInvoker {

  /**
   * 
   * @param aContext
   * @throws SLCanceledException if the security management prevents execution of this command
   */
  public void invoke(SLSourceContext aContext) throws SLException;

  /**
   * 
   * @param aContext
   * @return
   * @throws SLCanceledException if the security management prevents execution of this command
   */
  public SLResult getResult(SLTargetContext aContext) throws SLException;

  public void setCommand(SLCommandContext commandContext, at.gv.egiz.bku.slcommands.SLCommand aCmd);
  
  /**
   * Prototype creation 
   * @return
   */
  public SLCommandInvoker newInstance();
}