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
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.bku.slexceptions.SLRuntimeException;

/**
 * Used to handle DataUrl connections as specified in the CCE's HTTP protocol binding. 
 *
 */
public class DataUrl {
  private static DataUrlConnectionSPI defaultDataUrlConnection = new DataUrlConnectionImpl();
  private static Log log = LogFactory.getLog(DataUrl.class);
  private static Properties configuration;
  
  private URL url;

  /**
   * Sets the default DataUrlConnection implementation
   * @param aClass must not be null
   */
  public static void setDataUrlConnectionClass(DataUrlConnectionSPI dataUrlConnection) {
    if (dataUrlConnection == null) {
      throw new NullPointerException("Default dataurlconnection must not be set to null");
    }
    defaultDataUrlConnection = dataUrlConnection;
  }

  public DataUrl(String aUrlString) throws MalformedURLException {
    url = new URL(aUrlString);
  }

  public DataUrlConnection openConnection() {
    try {
      log.debug("Opening dataurl connection");
      DataUrlConnectionSPI retVal = defaultDataUrlConnection.newInstance();
      retVal.init(url);
      return retVal;
    } catch (Exception e) {
      log.error(e);
      throw new SLRuntimeException("Cannot instantiate a dataurlconnection:",e);
    }
  }
  
  public static void setConfiguration(Properties props) {
    configuration = props;
    defaultDataUrlConnection.setConfiguration(configuration);
  }
}