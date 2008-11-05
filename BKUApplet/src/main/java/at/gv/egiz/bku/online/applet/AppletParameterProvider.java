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

package at.gv.egiz.bku.online.applet;

import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public interface AppletParameterProvider {

  /**
   * Applet configuration parameters
   * 
   * @param paramKey
   * @return null if no parameter is provided for the given key
   */
  String getAppletParameter(String paramKey);

  /**
   * Get applet configuration parameter as (absolute) URL
   * 
   * @param paramKey
   * @return a URL
   * @throws MalformedURLException if configured URL is invalid 
   * or no parameter is provided for the given key
   */
  URL getURLParameter(String paramKey) throws MalformedURLException;
  
  /**
   * Get applet configuration parameter as (absolute) URL
   * 
   * @param paramKey
   * @param sessionId adds the jsessionid to the URL
   * @return a URL
   * @throws MalformedURLException if configured URL is invalid 
   * or no parameter is provided for the given key
   */
  URL getURLParameter(String paramKey, String sessionId) throws MalformedURLException;
}
