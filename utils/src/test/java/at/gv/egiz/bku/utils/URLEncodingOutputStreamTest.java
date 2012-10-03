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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URLEncoder;
import java.nio.charset.Charset;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class URLEncodingOutputStreamTest {

  private static String buf;
  
  private static Charset UTF_8 = Charset.forName("UTF-8");
  
  @BeforeClass
  public static void setUpClass() throws IOException {
    
    ClassLoader cl = URLEncodingOutputStreamTest.class.getClassLoader();
    InputStream is = cl.getResourceAsStream("BigRequest.xml");

    assertNotNull(is);
    
    InputStreamReader reader = new InputStreamReader(is, UTF_8);

    StringBuilder sb = new StringBuilder();
    
    char[] b = new char[512];
    for (int l; (l = reader.read(b)) != -1;) {
      sb.append(b, 0, l);
    }

    buf = sb.toString();
    
  }
  
  @Test
  public void testCompareResults() throws IOException {
    
    String out1;
    String out2;
    
    // new
    StringWriter writer = new StringWriter();
    URLEncodingOutputStream urlEnc = new URLEncodingOutputStream(writer);
    OutputStreamWriter streamWriter = new OutputStreamWriter(urlEnc, UTF_8);
    streamWriter.append(buf);
    streamWriter.close();
    out1 = writer.toString();
    
    // URLEncoder
    out2 = URLEncoder.encode(buf, UTF_8.name());

    for (int i = 0; i < out1.length(); i++) {
      if (out1.charAt(i) != out2.charAt(i)) {
        System.out.println(i + ": " + out1.substring(i));
        System.out.println(i + ": " + out2.substring(i));
      }
    }
    
    assertEquals(out1, out2);
    
  }
  
  @Ignore
  @Test
  public void testURLEncodingOutputStream() throws IOException {

    NullWriter writer = new NullWriter();
    
    URLEncodingOutputStream urlEnc = new URLEncodingOutputStream(writer);
    OutputStreamWriter streamWriter = new OutputStreamWriter(urlEnc, UTF_8);
    
    long t0, t1, dt = 0;
    for (int run = 0; run < 1000; run++) {
      t0 = System.currentTimeMillis();
      streamWriter.append(buf);
      t1 = System.currentTimeMillis();
      if (run > 1) {
        dt += t1 - t0;
      }
    }
    System.out.println("Time " + dt + "ms");
    streamWriter.close();
  }
  
  @Ignore
  @Test
  public void testURLEncodingNaive() throws IOException {

    String in = new String(buf);

    long t0, t1, dt = 0;
    for (int run = 0; run < 1000; run++) {
      t0 = System.currentTimeMillis();
      URLEncoder.encode(in, "UTF-8");
      t1 = System.currentTimeMillis();
      if (run > 1) {
        dt += t1 - t0;
      }
    }
    System.out.println("Time (naive) " + dt + "ms");
    
  }
 
  public class NullWriter extends Writer {

    @Override
    public void close() throws IOException {
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
    }
    
  }

}
