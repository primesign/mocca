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



package at.gv.egiz.stal.service.impl;

import java.util.Locale;

import org.apache.commons.configuration.Configuration;

import at.gv.egiz.bku.conf.MoccaConfigurationFacade;
import at.gv.egiz.bku.jmx.ComponentMXBean;
import at.gv.egiz.bku.jmx.ComponentState;
import at.gv.egiz.stal.STAL;
import at.gv.egiz.stal.STALFactory;

/**
 *
 * @author clemens, mcentner
 */
public class RequestBrokerSTALFactory implements STALFactory, ComponentMXBean {
  
  public final ConfigurationFacade configurationFacade = new ConfigurationFacade();

  public class ConfigurationFacade implements MoccaConfigurationFacade {

    private Configuration configuration;

    public static final String APPLET_TIMEOUT = "AppletTimeout";

    public long getAppletTimeout() {
      return configuration.getLong(APPLET_TIMEOUT, -1);
    }

  }

  public void setConfiguration(Configuration configuration) {
    configurationFacade.configuration = configuration;
  }
  
  @Override
  public STAL createSTAL() {
    return new STALRequestBrokerImpl(configurationFacade.getAppletTimeout());
  }

  @Override
  public void setLocale(Locale locale) {
  }

  @Override
  public ComponentState checkComponentState() {
    return new ComponentState(true);
  }
    
}
