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

import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class can be used to check the BindingProcessorManager for expired entries and remove them.
 * Should be run periodically. 
 *
 */
public class ExpiryRemover implements RemovalStrategy {

  private static Log log = LogFactory.getLog(ExpiryRemover.class);

  protected BindingProcessorManager bindingProcessorManager;
  // keep max 5 min.
  protected long maxAcceptedAge = 1000 * 60 * 5;

  @Override
  public void execute() {
    log.debug("Triggered Expiry Remover");
    if (bindingProcessorManager == null) {
      log.warn("Bindingprocessor not set, skipping removal");
      return;
    }
    Set<Id> managedIds = bindingProcessorManager.getManagedIds();
    for (Iterator<Id> it = managedIds.iterator(); it.hasNext();) {
      Id bindId = it.next();
      BindingProcessor bp = bindingProcessorManager.getBindingProcessor(bindId);
      if (bp != null) {
        if (bp.getLastAccessTime().getTime() < (System.currentTimeMillis() - maxAcceptedAge)) {
          log.debug("Removing binding processor: " + bp.getId());
          bindingProcessorManager.removeBindingProcessor(bp.getId());
        }
      }
    }
  }

  public void setMaxAcceptedAge(long maxAcceptedAge) {
    this.maxAcceptedAge = maxAcceptedAge;
  }

  @Override
  public void setBindingProcessorManager(BindingProcessorManager bp) {
    bindingProcessorManager = bp;
  }

}