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

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.Future;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class ProcessingContext {

  public static final String BINDING_PROCESSOR = "binding.processor";
  public static final String FUTURE = "future";

  protected static final Log log = LogFactory.getLog(ProcessingContext.class);

  protected Map<String, Object> properties = new Hashtable<String, Object>();

  public ProcessingContext(BindingProcessor bp, Future future) {
    properties.put(BINDING_PROCESSOR, bp);
    properties.put(FUTURE, future);
  }

  public BindingProcessor getBindingProcessor() {
    return (BindingProcessor) properties.get(BINDING_PROCESSOR);
  }

  public Future getFuture() {
    return (Future) properties.get(FUTURE);
  }

  public Object get(String key) {
    return properties.get(key);
  }

  public void put(String key, Object value) {
    properties.put(key, value);
  }
}
