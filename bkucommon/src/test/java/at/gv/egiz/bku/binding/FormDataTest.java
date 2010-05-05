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
