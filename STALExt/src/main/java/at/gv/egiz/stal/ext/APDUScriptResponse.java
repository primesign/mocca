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

import at.gv.egiz.stal.STALResponse;

public class APDUScriptResponse extends STALResponse {
  
  public static abstract class ResponseScriptElement {
    
  }

  public static class ATR extends ResponseScriptElement {
    
    private byte[] atr;

    public ATR(byte[] atr) {
      this.atr = atr;
    }

    public byte[] getAtr() {
      return atr;
    }
    
  }
  
  public static class Response extends ResponseScriptElement {
    
    public static final int RC_UNSPECIFIED = -1;
    
    private int sequence;
    
    private byte[] apdu;

    private byte[] sw;
    
    private int rc;

    public Response(int sequence, byte[] apdu, byte[] sw, int rc) {
      this.sequence = sequence;
      this.apdu = apdu;
      this.sw = sw;
      this.rc = rc;
    }
    
    public int getSequence() {
      return sequence;
    }

    public byte[] getApdu() {
      return apdu;
    }

    public byte[] getSw() {
      return sw;
    }

    public int getRc() {
      return rc;
    }
    
  }
  
  private List<ResponseScriptElement> script;

  public APDUScriptResponse(List<ResponseScriptElement> script) {
    super();
    this.script = script;
  }

  public List<ResponseScriptElement> getScript() {
    return script;
  }
  
}
