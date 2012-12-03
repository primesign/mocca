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



package at.gv.egiz.bku.local.webapp;

import iaik.security.ecc.provider.ECCProvider;
import iaik.security.provider.IAIK;
import iaik.xml.crypto.XSecProvider;

import java.security.Provider;
import java.security.Security;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoccaContextListener implements ServletContextListener {
  
  private static Logger log = LoggerFactory.getLogger(MoccaContextListener.class);

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
  }

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    
    log.info("Registering security providers ...");
    
    registerProviders();
    
    if (log.isDebugEnabled()) {
      StringBuilder sb = new StringBuilder();
      sb.append("Registered providers: ");
      int i = 1;
      for (Provider prov : Security.getProviders()) {
        sb.append("\n" + (i++) + ". : " + prov);
      }
      log.debug(sb.toString());
    }
  }
  
  protected static void registerProvider(Provider provider, int position) {
    String name = provider.getName();
    if (Security.getProvider(name) == null) {
      // register IAIK provider at first position
      try {
        Security.insertProviderAt(provider, position);
      } catch (SecurityException e) {
        log.info("Failed to register required security Provider.", e);
      }
    } else {
      log.info("Required security Provider {} already registered.", name);
    }
    
  }
  
  protected static void registerProviders() {
    registerProvider(new IAIK(), 1);
    registerProvider(new ECCProvider(false), 2);
    
    final String name = XSecProvider.NAME;
    if (Security.getProvider(XSecProvider.NAME) == null) {
      // register XML Security provider
      try {
        XSecProvider.addAsProvider(false);
      } catch (SecurityException e) {
        log.info("Failed to register required security Provider.", e);
      }
    } else {
      log.info("Required security Provider {} already registered.", name);
    }
    
  }

}
