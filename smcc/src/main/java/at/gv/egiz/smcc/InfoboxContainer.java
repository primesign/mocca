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
