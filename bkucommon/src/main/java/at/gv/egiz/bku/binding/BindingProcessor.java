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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Locale;

import at.gv.egiz.bku.slcommands.SLCommandInvoker;
import at.gv.egiz.stal.STAL;

/**
 * Represents an single instance of a SL HTTP binding.
 * 
 * @author wbauer
 *
 */
public interface BindingProcessor extends Runnable {

  /**
   * The stream must be read completely within this method.
   * 
   * The caller is advised to check the result in case an error occurred.
   * 
   * @see #getResult()
   */
  public void consumeRequestStream(InputStream aIs);

  /**
   * The unique Id of this http binding instance.
   * @return
   */
  public Id getId();

  /**
   * The used underlying STAL instance
   * @return
   */
  public STAL getSTAL();

  public SLCommandInvoker getCommandInvoker();

  public Date getLastAccessTime();
  
  public void updateLastAccessTime();
  
  public String getResultContentType();
  
  public void writeResultTo(OutputStream os, String encoding) throws IOException;

  public void init(STAL aStal, SLCommandInvoker aCommandInvoker);
  
  /**
  * Sets the preferred locale for userinteraction.
  * If the locale is not set the default locale will be used.
  * @param locale must not be null.
  */
 public void setLocale(Locale locale);
 
 public boolean isFinished();
}