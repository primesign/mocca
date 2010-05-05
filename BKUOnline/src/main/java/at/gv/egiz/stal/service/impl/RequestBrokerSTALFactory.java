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

    public int getAppletTimeout() {
      return configuration.getInteger(APPLET_TIMEOUT, -1);
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
