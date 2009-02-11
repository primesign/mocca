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

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.bku.slexceptions.SLRuntimeException;

/**
 * Used to handle DataUrl connections as specified in the CCE's HTTP protocol binding. 
 *
 */
public class DataUrl {

  private static Log log = LogFactory.getLog(DataUrl.class);
  private static DataUrlConnectionSPI connection;
  private static Properties configuration;
  private static SSLSocketFactory sslSocketFactory;
  private static HostnameVerifier hostNameVerifier;
  private URL url;

  /**
   * Sets the default DataUrlConnection implementation
   * @param aClass must not be null
   */
  static void setDataUrlConnectionImpl(DataUrlConnectionSPI conn) {
    if (conn != null) {
      connection = conn;
    }
  }

  public DataUrl(String aUrlString) throws MalformedURLException {
    url = new URL(aUrlString);
    if (connection == null) {
      log.debug("Using default DataURLConnection class");
      connection = new DataUrlConnectionImpl();
    }
    connection.setConfiguration(configuration);
    connection.setSSLSocketFactory(sslSocketFactory);
    connection.setHostnameVerifier(hostNameVerifier);
  }

  public DataUrlConnection openConnection() {
    try {
      log.debug("Opening dataurl connection");
      DataUrlConnectionSPI retVal = connection.newInstance();
      retVal.init(url);
      return retVal;
    } catch (Exception e) {
      log.error(e);
      throw new SLRuntimeException("Cannot instantiate a dataurlconnection:", e);
    }
  }


  /**
   * set configuration for all subsequently instantiated DataURL objects
   * @param props
   */
  public static void setConfiguration(Properties props) {
    configuration = props;
    if (configuration != null) {
      String className = configuration.getProperty(DataUrlConnection.DATAURLCONNECTION_CONFIG_P);
      if (className != null) {
        try {
          log.info("set DataURLConnection class: " + className);
          Class c = Class.forName(className);
          connection = (DataUrlConnectionSPI) c.newInstance();
        } catch (Exception ex) {
          log.error("failed to instantiate DataURL connection " + className, ex);
        }
      }
    }
  }

  /**
   * set SSLSocketFactory for all subsequently instantiated DataURL objects
   * @param socketFactory
   */
  public static void setSSLSocketFactory(SSLSocketFactory socketFactory) {
    sslSocketFactory = socketFactory;
  }

  /**
   * set HostnameVerifier for all subsequently instantiated DataURL objects
   * @param hostNameVerifier
   */
  public static void setHostNameVerifier(HostnameVerifier hostNameVerifier) {
    DataUrl.hostNameVerifier = hostNameVerifier;
  }
}