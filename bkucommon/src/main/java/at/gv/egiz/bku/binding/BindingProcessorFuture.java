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

package at.gv.egiz.bku.binding;

import java.util.concurrent.FutureTask;

public class BindingProcessorFuture extends FutureTask<Object> {

  private BindingProcessor bindingProcessor;
  
  private long startTime;
  
  private long executionTime;
  
  public BindingProcessorFuture(BindingProcessor bindingProcessor) {
    super(bindingProcessor, null);
    this.bindingProcessor = bindingProcessor;
  }

  /**
   * @return the bindingProcessor
   */
  public BindingProcessor getBindingProcessor() {
    return bindingProcessor;
  }

  /* (non-Javadoc)
   * @see java.util.concurrent.FutureTask#run()
   */
  @Override
  public void run() {
    startTime = System.currentTimeMillis();
    try {
      super.run();
    } finally {
      executionTime = System.currentTimeMillis() - startTime;
    }
  }

  /**
   * @return the startTime
   */
  public long getStartTime() {
    return startTime;
  }

  /**
   * @return the executionTime
   */
  public long getExecutionTime() {
    return executionTime;
  }
  
  public long getAge() {
    return System.currentTimeMillis() - startTime;
  }
  
}
