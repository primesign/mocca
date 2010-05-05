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

package at.gv.egiz.mocca.id;

import javax.servlet.http.HttpServletRequest;

import at.gv.egiz.bku.online.webapp.AbstractWebRequestHandler;

public class SAMLRequestHandler extends AbstractWebRequestHandler {
  
  private static final long serialVersionUID = 1L;
  
  @Override
  protected String getRequestProtocol(HttpServletRequest req) {
    return "SAML";
  }
  
}
