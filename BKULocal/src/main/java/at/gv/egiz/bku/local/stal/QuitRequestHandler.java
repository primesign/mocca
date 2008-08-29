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
package at.gv.egiz.bku.local.stal;

import at.gv.egiz.bku.smccstal.AbstractRequestHandler;
import at.gv.egiz.bku.smccstal.SMCCSTALRequestHandler;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;

public class QuitRequestHandler extends AbstractRequestHandler {

  @Override
  public STALResponse handleRequest(STALRequest request) {
    return null;
  }

  @Override
  public boolean requireCard() {
    return false;
  }

  @Override
  public SMCCSTALRequestHandler newInstance() {
    return this;
  }

}
