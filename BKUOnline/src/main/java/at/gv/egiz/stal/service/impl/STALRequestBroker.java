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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.gv.egiz.stal.service.impl;

import at.gv.egiz.stal.STAL;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.HashDataInputCallback;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author clemens
 */
public interface STALRequestBroker extends STAL {

    public static final int ERR_6000 = 6000;
    public static final long TIMEOUT_MS = 1000*60*5; //300000;

    public List<STALRequest> nextRequest(List<STALResponse> response);
//    public void setResponse(List<STALResponse> response) throws TimeoutException;
//    public void interruptRequestHandling(ErrorResponseType error);
    public HashDataInputCallback getHashDataInput();
}
