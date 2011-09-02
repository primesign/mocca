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

        public boolean isDisablePinpad() {
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
            config.setDisablePinpad(configurationFacade.isDisablePinpad());
        }
        return config;
    }

    @Override
    public Class<SMCCConfiguration> getObjectType() {
        return SMCCConfiguration.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
