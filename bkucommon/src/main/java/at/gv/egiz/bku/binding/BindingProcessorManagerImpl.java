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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.bku.conf.MoccaConfigurationFacade;
import at.gv.egiz.bku.jmx.ComponentMXBean;
import at.gv.egiz.bku.jmx.ComponentState;
import at.gv.egiz.bku.slcommands.SLCommandInvoker;
import at.gv.egiz.bku.slexceptions.SLRuntimeException;
import at.gv.egiz.bku.utils.binding.Protocol;
import at.gv.egiz.stal.STALFactory;

/**
 * This class maintains all active BindingProcessor Objects. Currently, only
 * HTTPBinding is supported.
 */
public class BindingProcessorManagerImpl implements BindingProcessorManager, ComponentMXBean {
  
  public static long DEFAULT_MAX_ACCEPTED_AGE = 5 * 60 * 1000; // 5 Minutes
  
  public static int DEFAULT_CLEAN_UP_INTERVAL = 60;

  private final static Logger log = LoggerFactory.getLogger(BindingProcessorManagerImpl.class);

  private List<BindingProcessorFactory> factories = Collections.emptyList();

  private STALFactory stalFactory;
  
  private SLCommandInvoker commandInvoker;
  
  private ExecutorService executorService = Executors.newCachedThreadPool();

  private Map<Id, BindingProcessorFuture> submittedFutures = Collections
      .synchronizedMap(new HashMap<Id, BindingProcessorFuture>());

  private int cleanUpInterval = DEFAULT_CLEAN_UP_INTERVAL;

  private long maxAcceptedAge = -1;

  public final ConfigurationFacade configurationFacade = new ConfigurationFacade();

  public class ConfigurationFacade implements MoccaConfigurationFacade {

    private Configuration configuration;

    public static final String BINDING_PROCESSOR_TIMEOUT = "BindingProcessorTimeout";

    public long getBindingProcessorTimeout() {
      return configuration.getLong(BINDING_PROCESSOR_TIMEOUT, DEFAULT_MAX_ACCEPTED_AGE);
    }

  }

  private ScheduledExecutorService cleanUpService = Executors
      .newSingleThreadScheduledExecutor();

  public BindingProcessorManagerImpl() {
    cleanUpService.scheduleAtFixedRate(new CleanUpTask(), getCleanUpInterval(),
        getCleanUpInterval(), TimeUnit.SECONDS);
  }

  /**
   * @return the configuration
   */
  public Configuration getConfiguration() {
    return configurationFacade.configuration;
  }

  /**
   * @param configuration the configuration to set
   */
  public void setConfiguration(Configuration configuration) {
    configurationFacade.configuration = configuration;
  }

  /**
   * @return the factoryMap
   */
  public List<BindingProcessorFactory> getFactories() {
    return factories;
  }

  /**
   * @param factoryMap the factoryMap to set
   */
  public void setFactories(List<BindingProcessorFactory> factories) {
    this.factories = factories;
  }

  /**
   * Sets a SLCommandInvoker prototype used to create a SLCommandInvoker for
   * initialization of a BindingProcessor.
   * 
   * @param invoker
   */
  public void setSlCommandInvoker(SLCommandInvoker invoker) {
    commandInvoker = invoker;
  }

  /**
   * @return the SLCommandInvoker prototype used to create a SLCommandInvoker
   *         for initialization of a BindingProcessor.
   */
  public SLCommandInvoker getCommandInvoker() {
    return commandInvoker;
  }

  /**
   * @return the STALFactory currently used.
   */
  public STALFactory getStalFactory() {
    return stalFactory;
  }

  /**
   * @return the current BindingProcessor cleanup interval
   */
  public int getCleanUpInterval() {
    return cleanUpInterval;
  }

  /**
   * @param cleanUpInterval the BindingProcessor cleanup interval
   */
  public void setCleanUpInterval(int cleanUpInterval) {
    this.cleanUpInterval = cleanUpInterval;
  }

  /**
   * @return the current maximum inactive time of a BindingProcessor
   */
  public long getMaxAcceptedAge() {
    return maxAcceptedAge < 0 ?
        configurationFacade.getBindingProcessorTimeout() : maxAcceptedAge;
  }

  /**
   * @param maxAcceptedAge the maximum inactive time of a BindingProcessor
   */
  public void setMaxAcceptedAge(long maxAcceptedAge) {
    this.maxAcceptedAge = maxAcceptedAge;
  }

  /**
   * Sets the STALFactory used to create a STAL implementation for initialization of
   * a BindingProcessor.
   * 
   * @param stalFactory
   */
  public void setStalFactory(STALFactory stalFactory) {
    this.stalFactory = stalFactory;
  }

  /* (non-Javadoc)
   * @see at.gv.egiz.bku.binding.BindingProcessorManager#shutdown()
   */
  @Override
  public void shutdown() {
    log.info("Shutting down the BindingProcessorManager.");
    executorService.shutdown();
    cleanUpService.shutdown();
  }

  /* (non-Javadoc)
   * @see at.gv.egiz.bku.binding.BindingProcessorManager#shutdownNow()
   */
  @Override
  public void shutdownNow() {
    log.info("Shutting down the BindingProcessorManager NOW!");
    cleanUpService.shutdownNow();
    executorService.shutdownNow();
    log.debug("Number of binding contexts currently managed: {}.", submittedFutures.size());
    if (log.isDebugEnabled()) {
      for (BindingProcessorFuture future : submittedFutures.values()) {
        if (future.isCancelled()) {
          log.debug("BindingProcessor {} is cancelled.", future.getBindingProcessor().getId());
        } else {
          log.debug("BindingProcessor {} is done: {}.", future.getBindingProcessor().getId(), future.isDone());
        }
      }
    }
  }

  /* (non-Javadoc)
   * @see at.gv.egiz.bku.binding.BindingProcessorManager#createBindingProcessor(java.lang.String, java.lang.String)
   */
  @Override
  public BindingProcessor createBindingProcessor(String protocol) {
    Protocol p = Protocol.fromString(protocol);
    for (BindingProcessorFactory factory : factories) {
      if (factory.getSupportedProtocols().contains(p)) {
        return factory.createBindingProcessor();
      }
    }
    throw new IllegalArgumentException();
  }

  /* (non-Javadoc)
   * @see at.gv.egiz.bku.binding.BindingProcessorManager#createBindingProcessor(java.lang.String, java.lang.String, java.util.Locale)
   */
  @Override
  public BindingProcessor createBindingProcessor(String protocol, Locale locale) {
    BindingProcessor bindingProcessor = createBindingProcessor(protocol);
    bindingProcessor.setLocale(locale);
    return bindingProcessor;
  }

  /* (non-Javadoc)
   * @see at.gv.egiz.bku.binding.BindingProcessorManager#process(java.lang.String, at.gv.egiz.bku.binding.BindingProcessor)
   */
  @Override
  public BindingProcessorFuture process(Id id, BindingProcessor bindingProcessor) {
    
    log.trace("Initialize BindingProcessor for processing.");
    bindingProcessor.init(id.toString(), stalFactory.createSTAL(), commandInvoker.newInstance());
    
    BindingProcessorFuture future = new BindingProcessorFuture(bindingProcessor);
    if (submittedFutures.containsKey(bindingProcessor.getId())) {
      log.error("BindingProcessor with with id {} already submitted.", id);
      throw new SLRuntimeException("BindingProcessor with with id " + id
          + " already submitted.");
    }
    
    try {
      log.debug("Submitting BindingProcessor {} for processing.", id);
      executorService.execute(future);
      submittedFutures.put(bindingProcessor.getId(), future);
    } catch (RejectedExecutionException e) {
      log.error("BindingProcessor {} processing rejected.", id, e);
      throw new SLRuntimeException("BindingProcessor {} " + id + " processing rejected.", e);
    }
    
    return future;
    
  }

  /* (non-Javadoc)
   * @see at.gv.egiz.bku.binding.BindingProcessorManager#getBindingProcessor(at.gv.egiz.bku.binding.Id)
   */
  @Override
  public BindingProcessor getBindingProcessor(Id id) {
    BindingProcessorFuture future = submittedFutures.get(id);
    if (future != null) {
      return future.getBindingProcessor();
    } else {
      return null;
    }
  }

  /* (non-Javadoc)
   * @see at.gv.egiz.bku.binding.BindingProcessorManager#removeBindingProcessor(at.gv.egiz.bku.binding.Id)
   */
  @Override
  public void removeBindingProcessor(Id id) {
    BindingProcessorFuture future = submittedFutures.remove(id);
    if (future != null) {
      if (!future.isDone()) {
        log.debug("Interrupting BindingProcessor {}.", id );
        future.cancel(true);
      }
      if (log.isInfoEnabled()) {
        Object[] args = {id, future.getExecutionTime() / 1000.0, future.getAge() / 1000.0};
        log.info("Removing BindingProcessor {} (active:{}s/age:{}s).", args);
      }
    }
  }

  /* (non-Javadoc)
   * @see at.gv.egiz.bku.binding.BindingProcessorManager#getManagedIds()
   */
  @Override
  public Set<Id> getManagedIds() {
    return Collections.unmodifiableSet(new HashSet<Id>(submittedFutures.keySet()));
  }

  /* (non-Javadoc)
   * @see at.gv.egiz.bku.jmx.ComponentMXBean#checkComponentState()
   */
  @Override
  public ComponentState checkComponentState() {
    return new ComponentState(true);
  }
  
  public class CleanUpTask implements Runnable {
    
    @Override
    public void run() {
      Collection<BindingProcessorFuture> futures = submittedFutures.values();
      List<Id> toBeRemoved = new ArrayList<Id>();
//      int active = 0;
      for(BindingProcessorFuture future : futures) {
        BindingProcessor bindingProcessor = future.getBindingProcessor();
        if (!future.isDone()) {
//          active++;
        }
        if ((System.currentTimeMillis() - bindingProcessor
            .getLastAccessTime().getTime()) > getMaxAcceptedAge()) {
          Id id = bindingProcessor.getId();
          toBeRemoved.add(id);
          log.debug("Removing BindingProcessor {} for exceeding {}ms", id, getMaxAcceptedAge());
        }
      }
      for (Id id : toBeRemoved) {
        removeBindingProcessor(id);
      }
    }
    
  }
}
