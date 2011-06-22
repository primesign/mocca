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


package at.gv.egiz.bku.binding;

import java.util.Locale;
import java.util.Set;

/**
 * A <code>BindingProcessorManager</code> provides factory methods for creating
 * <code>BindingProcessor</code>s and allows for scheduling them for processing.
 * 
 * @author wbauer, mcentner
 */
public interface BindingProcessorManager {

  /**
   * Creates a new BindingProcessor for the given <code>protocol</code>.
   * 
   * @param protocol
   *          the name of the protocol binding the created BindingProcessor is
   *          required to implement
   * @param locale
   *          the locale to be used by the binding processor, may be
   *          <code>null</code>
   */
  public BindingProcessor createBindingProcessor(String protocol, Locale locale);

  /**
   * Creates a new BindingProcessor for the given <code>protocol</code>.
   * 
   * @param protocol
   *          the name of the protocol binding the created BindingProcessor is
   *          required to implement
   */
  public BindingProcessor createBindingProcessor(String protocol);

  /**
   * Returns the BindingProcessor which has been scheduled for processing with
   * the given <code>id</code>.
   * 
   * @param id
   *          the processing id of the requested BindingProcessor
   * 
   * @return the BindingProcessor which has been scheduled for processing with
   *         the given <code>id</code>, or <code>null</code> if no
   *         BindingProcessor has been scheduled with the given <code>id</code>.
   */
  public BindingProcessor getBindingProcessor(Id id);

  /**
   * Schedules the given BindingProcessor for processing.
   * <p>
   * <ol>
   * <li>Creates a processing context with the given <code>id</code>.</li>
   * <li>Schedules the given BindingProcessor for processing, and</li>
   * <li>Immediately returns the processing context.</li>
   * </ol>
   * </p>
   * 
   * @param id
   * @param bindingProcessor
   */
  public BindingProcessorFuture process(Id id, BindingProcessor bindingProcessor);

  /**
   * Removes the BindingProcessor with the given processing id.
   * 
   * @param id
   *          the processing id of the BindingProcessor to be removed
   */
  public void removeBindingProcessor(Id id);

  /**
   * Returns the set of <code>Id</code>s of currently managed BindingProcessor.
   * 
   * @return the set of <code>Id</code>s of currently managed BindingProcessor.
   */
  public Set<Id> getManagedIds();

  /**
   * Schedule shutdown of this BindingProcessorManager.
   */
  public void shutdown();

  /**
   * Immediately shutdown this BindingProcessorManager.
   */
  public void shutdownNow();
}