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

package at.gv.egiz.bku.local.spring;

import at.gv.egiz.bku.conf.MoccaConfigurationFacade;
import at.gv.egiz.smcc.conf.SMCCConfiguration;
import org.apache.commons.configuration.Configuration;
import org.springframework.beans.factory.FactoryBean;

/**
 *
 * @author clemens
 */
public class SMCCConfigurationFactoryBean implements FactoryBean {

    SMCCConfiguration config;

    protected final ConfigurationFacade configurationFacade = new ConfigurationFacade();

    public class ConfigurationFacade implements MoccaConfigurationFacade {

        private Configuration configuration;
        public static final String DISABLE_PINPAD = "CCID.disablePinpad";

        public boolean isDisablePinapad() {
            return configuration.getBoolean(DISABLE_PINPAD, false);
        }
    }

    public void setConfiguration(Configuration configuration) {
        configurationFacade.configuration = configuration;
    }

    @Override
    public Object getObject() throws Exception {
        if (config == null) {
            config = new SMCCConfiguration();
            config.setDisablePinpad(configurationFacade.isDisablePinapad());
        }
        return config;
    }

    @Override
    public Class getObjectType() {
        return SMCCConfiguration.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
