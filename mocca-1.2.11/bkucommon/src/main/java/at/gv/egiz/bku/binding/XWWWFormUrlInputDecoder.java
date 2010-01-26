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

import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.fileupload.ParameterParser;

public class XWWWFormUrlInputDecoder implements InputDecoder {

  /**
   * The MIME type 'application/x-www-form-urlencoded'.
   */
  public static final String CONTENT_TYPE = "application/x-www-form-urlencoded";

  /**
   * The form parameter iterator.
   */
  protected XWWWFormUrlInputIterator iterator;

  @SuppressWarnings("unchecked")
  @Override
  public void setContentType(String contentType) {
    ParameterParser pp = new ParameterParser();
    pp.setLowerCaseNames(true);
    Map<String, String> params = pp.parse(contentType, new char[] { ':', ';' });
    if (!params.containsKey(CONTENT_TYPE)) {
      throw new IllegalArgumentException(
          "not a url encoded content type specification: " + contentType);
    }
  }

  @Override
  public Iterator<FormParameter> getFormParameterIterator() {
    return iterator;
  }

  @Override
  public void setInputStream(InputStream is) {
    iterator = new XWWWFormUrlInputIterator(is);
  }
}
