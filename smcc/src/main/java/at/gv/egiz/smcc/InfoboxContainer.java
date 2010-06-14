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

package at.gv.egiz.smcc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.smartcardio.CardException;

import at.gv.egiz.smcc.util.ISO7816Utils;
import at.gv.egiz.smcc.util.TransparentFileInputStream;

public class InfoboxContainer {
  
  public static byte[] HEADER_AIK = { 'A', 'I', 'K' };
  
  private ArrayList<Infobox> infoboxes = new ArrayList<Infobox>();

  public InfoboxContainer(TransparentFileInputStream is, byte expectedType)
      throws IOException, CardException, SignatureCardException {
    
    is.mark(1);
    
    int b = is.read();
    if (b == -1 || b == 0x00 || b == 0xFF) {
      // empty
      return;
    }
    is.reset();
    
    is.mark(3);
    byte[] header = new byte[3];
    is.read(header);
    if (Arrays.equals(header, HEADER_AIK)) {
      int version = is.read();
      if (version != 1) {
        throw new InfoboxException("Infobox version " + version + " not supported.");
      }
      
      for (int tag; (tag = is.read()) != 0x00;) {
        int modifier = is.read();
        int length = is.read() + (is.read() << 8);
        byte[] data = new byte[length];
        is.read(data);
        Infobox infobox = new Infobox(tag, modifier, data);
        infoboxes.add(infobox);
      }
      
    } else {
      is.reset();
      byte[] data = ISO7816Utils.readTransparentFileTLV(is, expectedType);
      if (data != null) {
        infoboxes.add(new Infobox(0x01, 0x00, data));
      }
    }
    
  }

  public List<Infobox> getInfoboxes() {
    return infoboxes;
  }

}
