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
package at.gv.egiz.stal.ext;

import java.util.List;

import at.gv.egiz.stal.STALRequest;;

public class APDUScriptRequest extends STALRequest {

  public static abstract class RequestScriptElement {

  }

  public static class Reset extends RequestScriptElement {

  }
  
  public static class Command extends RequestScriptElement {
    
    private int sequence;
    
    private byte[] commandAPDU; 

    private byte[] expectedSW;

    public Command(int sequence, byte[] commandAPDU, byte[] expectedSW) {
      this.sequence = sequence;
      this.commandAPDU = commandAPDU;
      this.expectedSW = expectedSW;
    }
    
    public int getSequence() {
      return sequence;
    }

    public byte[] getCommandAPDU() {
      return commandAPDU;
    }

    public byte[] getExpectedSW() {
      return expectedSW;
    }
    
  }

  private List<RequestScriptElement> script;
  
  public APDUScriptRequest(List<RequestScriptElement> script) {
    super();
    this.script = script;
  }

  public List<RequestScriptElement> getScript() {
    return script;
  }
  
}
