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


package at.gv.egiz.bku.binding;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;

import org.junit.Ignore;
import org.junit.Test;

import at.gv.egiz.bku.utils.URLEncodingWriter;
import static org.junit.Assert.*;

public class XWWWFormUrlInputIteratorTest {

  @Test
  public void testEmpty() throws IOException {
    
    ByteArrayInputStream emptyStream = new ByteArrayInputStream(new byte[] {});
    
    XWWWFormUrlInputIterator decoder = new XWWWFormUrlInputIterator(emptyStream);
    
    assertFalse(decoder.hasNext());
    
  }
  
  @Test
  public void testOneParam() throws IOException {
    
    final String name = "name";
    final String value = "value";
    
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    OutputStreamWriter w = new OutputStreamWriter(os, Charset.forName("UTF-8"));
    w.write(name);
    w.write("=");
    w.write(value);
    w.flush();
    w.close();
    
    ByteArrayInputStream in = new ByteArrayInputStream(os.toByteArray());
    XWWWFormUrlInputIterator decoder = new XWWWFormUrlInputIterator(in);
    
    assertTrue(decoder.hasNext());
    FormParameter param = decoder.next();
    assertNotNull(param);
    assertEquals(name, param.getFormParameterName());
    InputStream vis = param.getFormParameterValue();
    assertNotNull(vis);
    InputStreamReader r = new InputStreamReader(vis);
    char[] buf = new char[value.length() + 1];
    int len = r.read(buf);
    assertEquals(value.length(), len);
    assertEquals(value, new String(buf, 0, len));
    assertFalse(decoder.hasNext());
    Exception ex = null;
    try {
      decoder.next();
    } catch (Exception e) {
      ex = e;
    }
    assertNotNull(ex);
    
  }

  @Test
  public void testTwoParam() throws IOException {
    
    final String name1 = "name";
    final String value1 = "value";
    final String name2 = "Name_2";
    final String value2 = "Value 2";
    
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    OutputStreamWriter w = new OutputStreamWriter(os, Charset.forName("UTF-8"));
    w.write(name1);
    w.write("=");
    w.write(value1);
    w.write("&");
    w.write(URLEncoder.encode(name2, "UTF-8"));
    w.write("=");
    w.write(URLEncoder.encode(value2, "UTF-8"));
    w.flush();
    w.close();
    
    ByteArrayInputStream in = new ByteArrayInputStream(os.toByteArray());
    XWWWFormUrlInputIterator decoder = new XWWWFormUrlInputIterator(in);
    
    assertTrue(decoder.hasNext());
    FormParameter param = decoder.next();
    assertNotNull(param);
    assertEquals(name1, param.getFormParameterName());
    InputStream vis = param.getFormParameterValue();
    assertNotNull(vis);
    InputStreamReader r = new InputStreamReader(vis);
    char[] buf = new char[value1.length() + 1];
    int len = r.read(buf);
    assertEquals(value1.length(), len);
    assertEquals(value1, new String(buf, 0, len));

    assertTrue(decoder.hasNext());
    param = decoder.next();
    assertNotNull(param);
    assertEquals(name2, param.getFormParameterName());
    vis = param.getFormParameterValue();
    assertNotNull(vis);
    r = new InputStreamReader(vis);
    buf = new char[value2.length() + 1];
    len = r.read(buf);
    assertEquals(value2.length(), len);
    assertEquals(value2, new String(buf, 0, len));
    
    assertFalse(decoder.hasNext());
  }

  @Test
  public void testURLEnc() throws IOException {
    
    String name = "name";
    byte[] value = new byte[128];
    for (int i = 0; i < value.length; i++) {
      value[i] = (byte) i;
    }
    
    String encValue = URLEncoder.encode(new String(value, "UTF-8"), "ASCII");
    System.out.println(encValue);
    
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    OutputStreamWriter w = new OutputStreamWriter(os, Charset.forName("UTF-8"));
    w.write(name);
    w.write("=");
    w.write(encValue);
    w.flush();
    w.close();
    
    ByteArrayInputStream in = new ByteArrayInputStream(os.toByteArray());
    XWWWFormUrlInputIterator decoder = new XWWWFormUrlInputIterator(in);

    assertTrue(decoder.hasNext());
    FormParameter param = decoder.next();
    assertNotNull(param);
    assertEquals(name, param.getFormParameterName());
    InputStream vis = param.getFormParameterValue();
    assertNotNull(vis);
    byte[] buf = new byte[value.length];
    int len = vis.read(buf);
    assertArrayEquals(value, buf);
    assertEquals(value.length, len);
    assertFalse(decoder.hasNext());
    Exception ex = null;
    try {
      decoder.next();
    } catch (Exception e) {
      ex = e;
    }
    assertNotNull(ex);
    
  }
  
  @Test
  public void testURLEnc1() throws IOException {
    
    InputStream urlEncStream = new BufferedInputStream(getClass()
        .getResourceAsStream("XWWWFormUrlEncoded1.txt"));
    
    XWWWFormUrlInputIterator decoder = new XWWWFormUrlInputIterator(urlEncStream);
    
    assertTrue(decoder.hasNext());
    FormParameter param = decoder.next();
    assertNotNull(param);
    assertEquals("XMLRequest", param.getFormParameterName());
    InputStream vis = param.getFormParameterValue();
    assertNotNull(vis);
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    byte[] buf = new byte[1024];
    for (int l; (l = vis.read(buf)) != -1;) {
      os.write(buf, 0, l);
    }
    assertEquals(-1, vis.read());
    assertFalse(decoder.hasNext());
    assertEquals(-1, urlEncStream.read());
    
  }

  @Test
  public void testURLEnc2() throws IOException {
    
    InputStream urlEncStream = new BufferedInputStream(getClass()
        .getResourceAsStream("XWWWFormUrlEncoded2.txt"));
    
    XWWWFormUrlInputIterator decoder = new XWWWFormUrlInputIterator(urlEncStream);
    
    assertTrue(decoder.hasNext());
    FormParameter param = decoder.next();
    assertNotNull(param);
    assertEquals("XMLRequest", param.getFormParameterName());
    InputStream vis = param.getFormParameterValue();
    assertNotNull(vis);
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    byte[] buf = new byte[1024];
    for (int l; (l = vis.read(buf)) != -1;) {
      os.write(buf, 0, l);
    }
    assertEquals(-1, vis.read());
    vis.close();
    
    assertTrue(decoder.hasNext());
    param = decoder.next();
    assertNotNull(param);
    assertEquals("EmptyParam", param.getFormParameterName());
    vis = param.getFormParameterValue();
    assertNotNull(vis);
    assertEquals(-1, vis.read());
    vis.close();

    assertTrue(decoder.hasNext());
    param = decoder.next();
    assertNotNull(param);
    assertEquals("TransferParam__", param.getFormParameterName());
    vis = param.getFormParameterValue();
    assertNotNull(vis);
    for (int l = 0; (l = vis.read(buf)) != -1;) {
      os.write(buf, 0, l);
    }
    assertEquals(-1, vis.read());
    vis.close();
    
  }
  
  @Ignore
  @Test
  public void testURLEncLoremIpsum() throws IOException {
    
    InputStream urlEncStream = new BufferedInputStream(getClass()
        .getResourceAsStream("UrlEncodedLoremIpsum.txt"));
    
    XWWWFormUrlInputIterator decoder = new XWWWFormUrlInputIterator(urlEncStream);
    
    assertTrue(decoder.hasNext());
    FormParameter param = decoder.next();
    assertNotNull(param);
    assertEquals("LoremIpsum", param.getFormParameterName());
    InputStream vis = param.getFormParameterValue();
    assertNotNull(vis);
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    byte[] buf = new byte[1024];
    for (int l; (l = vis.read(buf)) != -1;) {
      os.write(buf, 0, l);
    }
    assertEquals(-1, vis.read());
    vis.close();
    
    assertFalse(decoder.hasNext());
    
  }
  
  
  public static void main(String[] args) throws IOException {
    
    URL resource = XWWWFormUrlInputIteratorTest.class
        .getResource("LoremIpsum.txt");
    
    BufferedInputStream is = new BufferedInputStream(resource.openStream());
    
    InputStreamReader reader = new InputStreamReader(is, "UTF-8");
    
    StringBuilder sb = new StringBuilder();
    char[] b = new char[1024];
    for (int l; (l = reader.read(b)) != -1;) {
      sb.append(b, 0, l);
    }
    String li = sb.toString();

    FileOutputStream os = new FileOutputStream("UrlEncodedLoremIpsum.txt");
    OutputStreamWriter writer = new OutputStreamWriter(new BufferedOutputStream(os), "ISO-8859-1");
    URLEncodingWriter encoder = new URLEncodingWriter(writer);
    
    for (int i = 0; i < 100; i++) {
      encoder.write(li);
    }
    
    encoder.flush();
    encoder.close();
    
  }
  
}
