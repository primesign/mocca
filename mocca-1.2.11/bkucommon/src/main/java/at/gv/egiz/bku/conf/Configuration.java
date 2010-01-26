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

package at.gv.egiz.bku.conf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * BKU Common Configuration
 * 
 * Injected to BKU Common classes as defined in mocca-conf.xml
 * 
 * Replace at.gv.egiz.bku.conf.Configurator,
 * currently only few configuration options are supported.
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class Configuration {

  public static final int MAX_DATAURL_HOPS_DEFAULT = 50;
  public static final String IMPLEMENTATION_NAME_DEFAULT = "MOCCA";
  public static final String IMPLEMENTATION_VERSION_DEFAULT = "UNKNOWN";

  private static final Log log = LogFactory.getLog(Configuration.class);

  private int maxDataUrlHops = -1;
  private String implementationName;
  private String implementationVersion;

  public void setMaxDataUrlHops(int maxDataUrlHops) {
    this.maxDataUrlHops = maxDataUrlHops;
  }

  /**
	 * Defines the maximum number of dataurl connects that are allowed within a
	 * single SL Request processing.
	 */
  public int getMaxDataUrlHops() {
    if (maxDataUrlHops < 0) {
      log.warn("maxDataUrlHops not configured, using default: " + MAX_DATAURL_HOPS_DEFAULT);
      return MAX_DATAURL_HOPS_DEFAULT;
    }
    return maxDataUrlHops;
  }

  /**
   * @return the implementationName
   */
  public String getImplementationName() {
    if (implementationName == null) {
      log.info("implementationName not configured, using default: " + IMPLEMENTATION_NAME_DEFAULT);
      return "MOCCA";
    }
    return implementationName;
  }

  /**
   * @param implementationName the implementationName to set
   */
  public void setImplementationName(String implementationName) {
    this.implementationName = implementationName;
  }

  /**
   * @return the implementationVersion
   */
  public String getImplementationVersion() {
    if (implementationName == null) {
      log.info("implementationName not configured, using default: " + IMPLEMENTATION_VERSION_DEFAULT);
      return IMPLEMENTATION_VERSION_DEFAULT;
    }
    return implementationVersion;
  }

  /**
   * @param implementationVersion the implementationVersion to set
   */
  public void setImplementationVersion(String implementationVersion) {
    this.implementationVersion = implementationVersion;
  }




}
