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
package at.gv.egiz.stal;

import java.util.List;
import java.util.Locale;

/**
 * Interface for all implementations of the Security Tokean Abstraction Layer.
 * This interface is used by the BKU to handle all security token related parts.
 * 
 *
 */
public interface STAL {

  /**
   * Handles a list of security token commands.
   * @param aRequestList
   * @return
   */
  public List<STALResponse> handleRequest(List<? extends STALRequest> aRequestList);
  
  /**
   * Sets the preferred locale for userinteraction (e.g. PIN dialogs).
   * If the locale is not set the default locale will be used.
   * @param locale must not be null.
   */
  public void setLocale(Locale locale);
}