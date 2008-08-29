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

import java.net.URL;

/**
 * Prototype of a DataurlconnectionSPI
 * @author wbauer
 *
 */
public interface DataUrlConnectionSPI extends DataUrlConnection {
  
  /**
   * Returns a new instance of this class to handle a dataurl.
   * Called by the factory each time the openConnection method is called. 
   * @return
   */
  public DataUrlConnectionSPI newInstance();
  
  /**
   * Initializes the DataUrlConnection
   * @param url
   */
  public void init(URL url);


}
