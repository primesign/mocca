/*
 * Copyright 2011 by Graz University of Technology, Austria
 * MOCCA has been developed by the E-Government Innovation Center EGIZ, a joint
 * initiative of the Federal Chancellery Austria and Graz University of Technology.
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
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
