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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import org.junit.Test;

public class HexDumpTest {

  @Test
  public void testHexDump() throws IOException {
    
    byte[] bytes = new byte[734];
    int i = 0;
    for (; i < 256; i++) {
      bytes[i] = (byte) i;
    }
    
    Random random = new Random();
    for (; i < bytes.length; i++) {
      bytes[i] = (byte) random.nextInt();
    }

    PrintWriter writer = new PrintWriter(System.out);
    HexDump.hexDump(new ByteArrayInputStream(bytes), writer, 32);
    writer.flush();

  }
  

}
