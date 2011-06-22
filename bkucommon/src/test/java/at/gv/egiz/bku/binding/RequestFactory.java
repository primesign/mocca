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
