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

import javax.xml.bind.JAXBElement;

import at.gv.egiz.bku.slcommands.SLCommand;
import at.gv.egiz.bku.slcommands.SLCommandContext;
import at.gv.egiz.bku.slexceptions.SLCommandException;

/**
 * This class serves as abstract base class for the implementation of a security
 * layer command.
 * 
 * @author mcentner
 * 
 * @param <T>
 *          the type of the corresponding request value
 */
public abstract class SLCommandImpl<T> implements SLCommand {

  /**
   * The <code>SLCommandContext</code> for this <code>SLCommand</code>.
   */
  protected SLCommandContext cmdCtx;
  
  /**
   * The STAL helper.
   */
  protected STALHelper stalHelper;

  /**
   * The request element of this command.
   */
  protected JAXBElement<T> request;

  @SuppressWarnings("unchecked")
  @Override
  public void init(SLCommandContext ctx, Object request)
      throws SLCommandException {

    this.request = (JAXBElement<T>) request;

    this.cmdCtx = ctx;
    stalHelper = new STALHelper(cmdCtx.getSTAL());

  }

  /**
   * Returns the request value.
   * 
   * It is a convenience method for <code>request.getValue()</code>.
   * 
   * @see JAXBElement#getValue()
   * @return the request value
   */
  protected T getRequestValue() {
    return request.getValue();
  }

  /**
   * @return the corresponding <code>SLCommandContext</code>
   */
  protected SLCommandContext getCmdCtx() {
    return cmdCtx;
  }
}
