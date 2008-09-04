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

import java.net.MalformedURLException;
import java.util.Locale;
import java.util.Set;

import at.gv.egiz.bku.slcommands.SLCommandInvoker;
import at.gv.egiz.stal.STALFactory;

/**
 * Central player that handles the protocol binding.
 * 
 * @author wbauer
 * 
 */
public interface BindingProcessorManager {

  /**
   * FactoryMethod creating a new BindingProcessor object.
   * The created binding processor must be passed to the process method to execute.
   * 
   * @param urlString
   *          the source url
   * @param aSessionId
   *          optional an external sessionId (e.g. http session) could be
   *          provided. This parameter may be null.
   * @param locale the locale used for user interaction, may be null
   */
  public BindingProcessor createBindingProcessor(String urlString,
      String aSessionId, Locale locale) throws MalformedURLException;

  /**
   * FactoryMethod creating a new BindingProcessor object.
   * The created binding processor must be passed to the process method to execute.
   * 
   * @param protcol
   *          the source url
   * @param aSessionId
   *          optional an external sessionId (e.g. http session) could be
   *          provided. This parameter may be null.
   */
  public BindingProcessor createBindingProcessor(String urlString,
      String aSessionId) throws MalformedURLException;

  
  /**
   * Gets the binding processor with a certain id. The binding processor must be passed to the 
   * process method before it is managed and thus returned by this method.
   * @param aId must not be null
   * @return null if the binding processor was not "processed" before.
   */
  public BindingProcessor getBindingProcessor(Id aId);

  /**
   * Sets the STAL factory that is used for creating STAL objects that are used by BindingProcessor objects.
   * For each new BindingProcessor a new STAL object is created.
   * @param aStalFactory the factory to be used. Must not be null.
   */
  public void setSTALFactory(STALFactory aStalFactory);
  
  /**
   * Sets the invoker to be used.
   * @param invoker
   */
  public void setSLCommandInvoker(SLCommandInvoker invoker);

  /**
   * Schedules the provided binding processor for processing and immediately returns.
   * 
   * @param aBindingProcessor
   */
  public void process(BindingProcessor aBindingProcessor);
  
  /**
   * Removes a formerly added (by calling the process method) binding processor.
   * @param bindingProcessor must not be null
   */
  public void removeBindingProcessor(Id sessionId); 
  
  /**
   * A set of all managed binding processors.
   * @return
   */
  public Set<Id> getManagedIds();
      
  public void shutdown();
}