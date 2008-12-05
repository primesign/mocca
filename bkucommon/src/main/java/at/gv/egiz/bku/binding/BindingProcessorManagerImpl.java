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
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.bku.slcommands.SLCommandInvoker;
import at.gv.egiz.bku.slexceptions.SLRuntimeException;
import at.gv.egiz.bku.utils.binding.Protocol;
import at.gv.egiz.stal.STAL;
import at.gv.egiz.stal.STALFactory;

/**
 * This class maintains all active BindingProcessor Objects. Currently, only
 * HTTPBinding is supported.
 */
public class BindingProcessorManagerImpl implements BindingProcessorManager {

  public final static Protocol[] SUPPORTED_PROTOCOLS = { Protocol.HTTP,
      Protocol.HTTPS };

  private static Log log = LogFactory.getLog(BindingProcessorManagerImpl.class);

  protected STALFactory stalFactory;
  protected SLCommandInvoker commandInvokerClass;
  
  private RemovalStrategy removalStrategy;
  private ExecutorService executorService;
  private Map<Id, ProcessingContext> contextMap = Collections.synchronizedMap(new HashMap<Id, ProcessingContext>());
//  private Map<Id, MapEntityWrapper> bindingProcessorMap = Collections
//      .synchronizedMap(new HashMap<Id, MapEntityWrapper>());

  /**
   * Container to hold a Future and Bindingprocessor object as map value.
   * 
   * @author wbauer
   * @see BindingProcessorManagerImpl#bindingProcessorMap
   */
//  static class MapEntityWrapper {
//    private Future<?> future;
//    private BindingProcessor bindingProcessor;
//
//    public MapEntityWrapper(Future<?> future, BindingProcessor bindingProcessor) {
//      if ((bindingProcessor == null) || (future == null)) {
//        throw new NullPointerException("Argument must not be null");
//      }
//      this.bindingProcessor = bindingProcessor;
//      this.future = future;
//    }
//
//    public Future<?> getFuture() {
//      return future;
//    }
//
//    public BindingProcessor getBindingProcessor() {
//      return bindingProcessor;
//    }
//
//    public int hashCode() {
//      return bindingProcessor.getId().hashCode();
//    }
//
//    public boolean equals(Object other) {
//      if (other instanceof MapEntityWrapper) {
//        MapEntityWrapper o = (MapEntityWrapper) other;
//        return (o.bindingProcessor.getId().equals(bindingProcessor.getId()));
//      } else {
//        return false;
//      }
//    }
//  }

  /**
   * 
   * @param fab
   *          must not be null
   * @param ci
   *          must not be null (prototype to generate new instances)
   */
  public BindingProcessorManagerImpl(STALFactory fab, SLCommandInvoker ci) {
    if (fab == null) {
      throw new NullPointerException("STALFactory must not be null");
    }
    stalFactory = fab;
    if (ci == null) {
      throw new NullPointerException("SLCommandInvoker must not be null");
    }
    commandInvokerClass = ci;
    executorService = Executors.newCachedThreadPool();
  }

  /**
   * 
   * @return the STALFactory currently used.
   */
  public STALFactory getStalFactory() {
    return stalFactory;
  }

  /**
   * Sets the STALFactory to be used.
   * 
   * @param stalFactory
   */
  public void setStalFactory(STALFactory stalFactory) {
    this.stalFactory = stalFactory;
  }

  /**
   * Could be used to setup a new executor service during application stratup.
   * 
   * @param executorService
   */
  public void setExecutorService(ExecutorService executorService) {
    this.executorService = executorService;
  }

  public void setRemovalStrategy(RemovalStrategy aStrategy) {
    removalStrategy = aStrategy;
  }

  public RemovalStrategy getRemovlaStrategy() {
    return removalStrategy;
  }

  public void shutdown() {
    log.info("Shutting down the BindingProcessorManager");
    executorService.shutdown();
  }

  public void shutdownNow() {
    log.info("Shutting down the BindingProcessorManager NOW!");
    executorService.shutdownNow();
    log.debug("Number of binding contexts currently managed: "
      + contextMap.size());
//        + bindingProcessorMap.size());
    if (log.isDebugEnabled()) {
      for (ProcessingContext ctx : contextMap.values()) {
        Id bpId = ctx.getBindingProcessor().getId();
        Future future = ctx.getFuture();
        log.debug(bpId + " cancelled: " + future.isCancelled());
        log.debug(bpId + " done: " + future.isDone());
      }
//      for (Iterator<MapEntityWrapper> it = bindingProcessorMap.values()
//          .iterator(); it.hasNext();) {
//        MapEntityWrapper entry = it.next();
//        log.debug(entry.getBindingProcessor().getId() + ": isDone: "
//            + entry.getFuture().isDone());
//        log.debug(entry.getBindingProcessor().getId() + ": isCanceled: "
//            + entry.getFuture().isCancelled());
//      }
    }
  }

  /**
   * Uses the default locale
   */
  public BindingProcessor createBindingProcessor(String srcUrl,
      String aSessionId) throws MalformedURLException {
    return createBindingProcessor(srcUrl, aSessionId, null);
  }

  /**
   * FactoryMethod creating a new BindingProcessor object.
   * 
   * @param protocol
   *          must not be null
   * @throws MalformedURLException
   */
  public BindingProcessor createBindingProcessor(String srcUrl,
      String aSessionId, Locale locale) throws MalformedURLException {
    URL url = new URL(srcUrl);
    String low = url.getProtocol().toLowerCase();
    Protocol proto = null;
    for (int i = 0; i < SUPPORTED_PROTOCOLS.length; i++) {
      if (SUPPORTED_PROTOCOLS[i].toString().equals(low)) {
        proto = SUPPORTED_PROTOCOLS[i];
        break;
      }
    }
    if (proto == null) {
      throw new UnsupportedOperationException();
    }
    BindingProcessor bindingProcessor = new HTTPBindingProcessor(aSessionId,
        commandInvokerClass.newInstance(), url);
    stalFactory.setLocale(locale);
    STAL stal = stalFactory.createSTAL();
    bindingProcessor.init(stal, commandInvokerClass.newInstance());
    if (locale != null) {
      bindingProcessor.setLocale(locale);
      stal.setLocale(locale);
    }
    return bindingProcessor;
  }

  /**
   * @return the bindingprocessor object for this id or null if no
   *         bindingprocessor was found.
   */
    @Override
  public BindingProcessor getBindingProcessor(Id aId) {
//    if (bindingProcessorMap.get(aId) != null) {
//      return bindingProcessorMap.get(aId).getBindingProcessor();
    ProcessingContext ctx = contextMap.get(aId);
    if (ctx != null) {
      return ctx.getBindingProcessor();
    } else {
      return null;
    }
  }

  /**
   * 
   */
    @Override
  public void setSTALFactory(STALFactory aStalFactory) {
    if (aStalFactory == null) {
      throw new NullPointerException("Cannot set STALFactory to null");
    }
    stalFactory = aStalFactory;
  }

  /**
   * Causes the BindingProcessorManager to manage the provided BindingProcessor
   * Creates a processing context,
   * schedules the provided binding processor for processing and 
   * immediately returns the context.
   * 
   * @param aBindingProcessor
   *          must not be null
   */
  @Override
  public ProcessingContext process(BindingProcessor aBindingProcessor) {
    if (contextMap.containsKey(aBindingProcessor.getId())) {
//    if (bindingProcessorMap.containsKey(aBindingProcessor.getId())) {
      log.fatal("Clashing ids, cannot process bindingprocessor with id:"
          + aBindingProcessor.getId());
      throw new SLRuntimeException(
          "Clashing ids, cannot process bindingprocessor with id:"
              + aBindingProcessor.getId());
    }
    log.debug("processing bindingprocessor: " + aBindingProcessor.getId());
    Future<?> f = executorService.submit(aBindingProcessor);
    ProcessingContext ctx = new ProcessingContext(aBindingProcessor, f);
    contextMap.put(aBindingProcessor.getId(), ctx);
//    bindingProcessorMap.put(aBindingProcessor.getId(), new MapEntityWrapper(f,
//        aBindingProcessor));
    return ctx;
  }

  @Override
  public void setSLCommandInvoker(SLCommandInvoker invoker) {
    commandInvokerClass = invoker;
  }

  @Override
  public void removeBindingProcessor(Id sessionId) {
    log.debug("Removing binding processor: " + sessionId);
    ProcessingContext ctx = contextMap.get(sessionId);
    if (ctx == null) {
      log.warn("no processing context to remove for session " + sessionId);
      return;
    }
    Future f = ctx.getFuture();
    
//    MapEntityWrapper wrapper = bindingProcessorMap.get(sessionId);
//    if (wrapper == null) {
//      return;
//    }
//    Future<?> f = wrapper.getFuture();
    
    if (!f.isDone()) {
      log.trace("canceling " + sessionId);
      f.cancel(true);
    }
    contextMap.remove(sessionId);
//    bindingProcessorMap.remove(sessionId);
  }

  @Override
  public Set<Id> getManagedIds() {
    Set<Id> result = new HashSet<Id>();
    synchronized (contextMap) {
      for (Id id : contextMap.keySet()) {
        result.add(id);
      }
    }
//    synchronized (bindingProcessorMap) {
//      for (Iterator<Id> it = bindingProcessorMap.keySet().iterator(); it
//          .hasNext();) {
//        result.add(it.next());
//      }
//    }
    return result;
  }
}