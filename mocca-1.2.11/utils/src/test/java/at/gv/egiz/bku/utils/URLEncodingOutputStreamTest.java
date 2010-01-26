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
    streamWriter.flush();
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
