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
