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
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import at.gv.egiz.bku.utils.StreamUtil;

public class RequestFactory implements FixedFormParameters {

  protected String requestResourceName = "at/gv/egiz/bku/binding/Nulloperation.xml";

  protected Map<String, String> formString = new HashMap<String, String>();
  protected Map<String, String> formResources = new HashMap<String, String>();

  public RequestFactory() {
    formResources.put(XMLREQUEST, requestResourceName);
  }

  public void addForm(String formName, String content) {
    formString.put(formName, content);
  }

  public void addFormAsResource(String formName, String resourceName) {
    formResources.put(formName, resourceName);
  }

  public InputStream getURLencoded() throws IOException {
    StringBuffer sb = new StringBuffer();
    for (Iterator<String> si = formString.keySet().iterator(); si.hasNext();) {
      String formName = si.next();
      String formVal = formString.get(formName);
      sb.append(URLEncoder.encode(formName, "UTF-8"));
      sb.append("=");
      sb.append(URLEncoder.encode(formVal, "UTF-8"));
      if (si.hasNext()) {
        sb.append("&");
      } else {
        if (formResources.keySet().iterator().hasNext()) {
          sb.append("&");
        }
      }
    }

    for (Iterator<String> si = formResources.keySet().iterator(); si.hasNext();) {
      String formName = si.next();
      String formVal = URLEncoder.encode(StreamUtil.asString(getClass()
          .getClassLoader().getResourceAsStream(formResources.get(formName)),
          "UTF-8"), "UTF-8");
      sb.append(URLEncoder.encode(formName, "UTF-8"));
      sb.append("=");
      sb.append(formVal);
      if (si.hasNext()) {
        sb.append("&");
      }
    }
    return new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));
  }
  
  public String getURLencodedAsString() throws IOException {
    StringBuffer sb = new StringBuffer();
    for (Iterator<String> si = formString.keySet().iterator(); si.hasNext();) {
      String formName = si.next();
      String formVal = formString.get(formName);
      sb.append(URLEncoder.encode(formName, "UTF-8"));
      sb.append("=");
      sb.append(URLEncoder.encode(formVal, "UTF-8"));
      if (si.hasNext()) {
        sb.append("&");
      } else {
        if (formResources.keySet().iterator().hasNext()) {
          sb.append("&");
        }
      }
    }

    for (Iterator<String> si = formResources.keySet().iterator(); si.hasNext();) {
      String formName = si.next();
      String formVal = URLEncoder.encode(StreamUtil.asString(getClass()
          .getClassLoader().getResourceAsStream(formResources.get(formName)),
          "UTF-8"), "UTF-8");
      sb.append(URLEncoder.encode(formName, "UTF-8"));
      sb.append("=");
      sb.append(formVal);
      if (si.hasNext()) {
        sb.append("&");
      }
    }
    return sb.toString();
  }

  public String getNullOperationXML() throws IOException {
    return StreamUtil.asString(getClass().getClassLoader().getResourceAsStream(
        requestResourceName), "UTF-8");
  }
}
