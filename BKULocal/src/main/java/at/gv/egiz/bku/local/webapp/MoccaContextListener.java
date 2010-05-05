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
  
  private Logger log = LoggerFactory.getLogger(MoccaContextListener.class);

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
  
  protected void registerProvider(Provider provider, int position) {
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
  
  protected void registerProviders() {

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
