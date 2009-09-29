package at.gv.egiz.bku.binding;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;
import java.nio.charset.Charset;

import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

public class XWWWFormUrlInputIteratorTest {

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
  
  
}
