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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import at.gv.egiz.bku.utils.StreamUtil;

public class InputDecoderFactoryTest {

  protected String resourceName = "at/gv/egiz/bku/binding/Multipart.txt.bin";
  protected byte[] data;

  @Before
  public void setUp() throws IOException {
    InputStream is = getClass().getClassLoader().getResourceAsStream(
        resourceName);
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int i;

    while ((i = is.read(buffer)) != -1) {
      bos.write(buffer, 0, i);
    }
    is.close();
    data = bos.toByteArray();
  }

  @Test
  public void testPrefix() {
    InputDecoder dec = InputDecoderFactory.getDecoder(
        "multipart/form-data; boundary=AaB03x", null);
    assertTrue(dec instanceof MultiPartFormDataInputDecoder);
  }

  @Test
  public void testMultipart() throws IOException {
    InputDecoder dec = InputDecoderFactory
        .getDecoder(
            "multipart/form-data; boundary=---------------------------15671293698853",
            new ByteArrayInputStream(data));
    assertNotNull(dec);
    for (Iterator<FormParameter> fpi = dec.getFormParameterIterator(); fpi
        .hasNext();) {
      FormParameter fp = fpi.next();
      if (fp.getFormParameterName().equals("XMLRequest")) {
        assertEquals("text/xml", fp.getFormParameterContentType());
        return;
      }
    }
    assertTrue(false);
  }

  @Test
  public void testUrlEncoded() throws IOException {
    InputDecoder dec = InputDecoderFactory.getDecoder(
        "application/x-www-form-urlencoded", null);
    assertTrue(dec instanceof XWWWFormUrlInputDecoder);
    dec = InputDecoderFactory.getDecoder(
        "application/x-WWW-form-urlencoded;charset=UTF-8",
        new ByteArrayInputStream(
            "your_name=hansi+wurzel&userid=123&form_name=wasinet".getBytes()));
    assertTrue(dec instanceof XWWWFormUrlInputDecoder);
    Iterator<FormParameter> fpi = dec.getFormParameterIterator();
    FormParameter fp = fpi.next();
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    StreamUtil.copyStream(fp.getFormParameterValue(), os);
    String value = new String(os.toByteArray(), "UTF-8");
    assertEquals("hansi wurzel", value);
  }
}
