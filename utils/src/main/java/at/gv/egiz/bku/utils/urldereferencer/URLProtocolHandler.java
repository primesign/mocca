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
package at.gv.egiz.bku.utils.urldereferencer;

import java.io.IOException;
import java.net.MalformedURLException;


public interface URLProtocolHandler {
  /**
   * 
   * @param aUrl
   * @param aContext
   * @return the streamdata of this url or null if the url cannot be resolved.
   * @throws IOException
   */
  public StreamData dereference(String aUrl, URLDereferencerContext aContext) throws IOException;
}