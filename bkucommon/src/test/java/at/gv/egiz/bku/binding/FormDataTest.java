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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import org.junit.Test;
import static org.junit.Assert.*;

import at.gv.egiz.bku.binding.FormDataURLSupplier;
import at.gv.egiz.bku.utils.StreamUtil;
import at.gv.egiz.bku.utils.urldereferencer.StreamData;
import at.gv.egiz.bku.utils.urldereferencer.URLDereferencerImpl;

public class FormDataTest implements FormDataURLSupplier {

  protected InputStream testStream = null;
  protected String contentType = null;
  protected String paramName = "";

  @Override
  public InputStream getFormData(String parameterName) {
    if (paramName.equals(parameterName)) {
      return testStream;
    } else {
      return null;
    }
  }

  @Override
  public String getFormDataContentType(String parameterName) {
    if (paramName.equals(parameterName)) {
      return contentType;
    } else {
      return null;
    }
  }

  @Test(expected = MalformedURLException.class)
  public void testInvalidFormdataUrl() throws IOException {
    String url = "abs://whatknowi";
    FormDataURLDereferencer dereferencer = new FormDataURLDereferencer(URLDereferencerImpl.getInstance(), this);
    StreamData sd = dereferencer.dereference(url);
    assertNull(sd);
    url = ":://whatknowi";
    sd = URLDereferencerImpl.getInstance().dereference(url);
    assertNull(sd);
    url = "";
    sd = URLDereferencerImpl.getInstance().dereference(url);
  }

  @Test
  public void testFormData() throws IOException {
    paramName = "Müllcontainer";
    testStream = new ByteArrayInputStream("HelloWorld".getBytes("UTF-8"));
    String url = "formdata:"+paramName;
    FormDataURLDereferencer dereferencer = new FormDataURLDereferencer(URLDereferencerImpl.getInstance(), this);
    StreamData sd = dereferencer.dereference(url);
    assertNotNull(sd);
    String result = StreamUtil.asString(sd.getStream(), "UTF-8");
    assertEquals("HelloWorld", result);
  }
  
  @Test(expected=IOException.class)
  public void testFormDataNotFound() throws IOException {
    paramName = "Müllcontainer";
    testStream = new ByteArrayInputStream("HelloWorld".getBytes("UTF-8"));
    String url = "formdata:"+paramName+"2";
    FormDataURLDereferencer dereferencer = new FormDataURLDereferencer(URLDereferencerImpl.getInstance(), this);
    dereferencer.dereference(url);
  }
  
}
