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
