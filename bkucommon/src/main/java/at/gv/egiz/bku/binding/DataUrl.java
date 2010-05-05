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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Used to handle DataUrl connections as specified in the CCE's HTTP protocol binding. 
 *
 */
public class DataUrl {

  private static DataURLConnectionFactory connectionFactory;
  
  /**
   * @return the connectionFactory
   */
  public static DataURLConnectionFactory getConnectionFactory() {
    return connectionFactory;
  }

  /**
   * @param connectionFactory the connectionFactory to set
   */
  public static void setConnectionFactory(
      DataURLConnectionFactory connectionFactory) {
    DataUrl.connectionFactory = connectionFactory;
  }

  /**
   * The URL.
   */
  private URL url;

  public DataUrl(String spec) throws MalformedURLException {
    url = new URL(spec);
  }

  public DataUrlConnection openConnection() throws IOException {
    if (connectionFactory != null) {
      return connectionFactory.openConnection(url);
    } else {
      return new DataUrlConnectionImpl(url);
    }
  }
}