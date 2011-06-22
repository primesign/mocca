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


package at.gv.egiz.bku.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;

public class HexDump {
  
  public static String hexDump(InputStream is) throws IOException {
    StringWriter writer = new StringWriter();
    hexDump(is, writer);
    return writer.toString();
  }
  
  public static void hexDump(InputStream is, Writer writer) throws IOException {
    hexDump(is, writer, 16);
  }
  
  public static void hexDump(InputStream is, Writer writer, int chunkSize) throws IOException {
    hexDump(is, writer, chunkSize, false);
  }
  
  public static void hexDump(InputStream is, Writer writer, int chunkSize, boolean plain) throws IOException {
    
    byte[] chunk = new byte[chunkSize];
    long adr = 0;
    for (int l; (l = is.read(chunk)) != -1;) {
      
      if (!plain) {
        writer.append(String.format("[%06x]", adr));
      }
      
      for (int i = 0; i < l; i++) {
        if (i % 8 == 0) {
          writer.append(" ");
        } else {
          writer.append(":");
        }
        writer.append(Integer.toHexString((chunk[i] & 240) >> 4));
        writer.append(Integer.toHexString(chunk[i] & 15));
      }

      for (int i = 0; i < (chunkSize - l); i++) {
        writer.append("   ");
      }
      
      if (!plain) {
        for (int i = 0; i < l; i++) {
          if (i % 8 == 0) {
            writer.append(" ");
          }
          if (chunk[i] > 31 && chunk[i] < 127) {
            writer.append((char) chunk[i]);
          } else {
            writer.append(".");
          }
        }
      }

      writer.append("\n");
      adr += l;
      
    }
    
  }

}
