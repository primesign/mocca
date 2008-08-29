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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.ParameterParser;

import at.gv.egiz.bku.slexceptions.SLRuntimeException;
import at.gv.egiz.bku.utils.StreamUtil;

/**
 * Implementation based on Java's URLDecoder class 
 *
 */
// FIXME replace this code by a streaming variant
public class XWWWFormUrlInputDecoder implements InputDecoder {

  public final static String CHAR_SET = "charset";
  public final static String NAME_VAL_SEP = "=";
  public final static String SEP = "\\&";

  private String contentType;
  private InputStream dataStream;
  private String charset = "UTF-8";

  protected List<FormParameter> decodeInput(InputStream is) throws IOException {
    List<FormParameter> result = new LinkedList<FormParameter>();
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    StreamUtil.copyStream(is, bos);
    String inputString = new String(bos.toByteArray());
    String[] nameValuePairs = inputString.split(SEP);
    //inputString = URLDecoder.decode(inputString, charset);
    for (int i = 0; i < nameValuePairs.length; i++) {
      String[] fields = nameValuePairs[i].split(NAME_VAL_SEP, 2);
      if (fields.length != 2) {
        throw new SLRuntimeException("Invalid form encoding, missing value");
      }
      String name = URLDecoder.decode(fields[0], charset); 
      String value =URLDecoder.decode(fields[1], charset);
      ByteArrayInputStream bais = new ByteArrayInputStream(value
          .getBytes(charset));
      FormParameterImpl fpi = new FormParameterImpl(contentType, name, bais, null);
      result.add(fpi);
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void setContentType(String contentType) {
    ParameterParser pp = new ParameterParser();
    pp.setLowerCaseNames(true);
    Map<String, String> params = pp.parse(contentType, new char[] { ':', ';' });
    if (!params.containsKey("application/x-www-form-urlencoded")) {
      throw new IllegalArgumentException(
          "not a url encoded content type specification: " + contentType);
    }
    String cs = params.get(CHAR_SET);
    if (cs != null) {
      charset = cs;
    }
    this.contentType = contentType;
  }

  @Override
  public Iterator<FormParameter> getFormParameterIterator() {
    try {
      return decodeInput(dataStream).iterator();
    } catch (IOException e) {
      throw new SLRuntimeException(e);
    }
  }

  @Override
  public void setInputStream(InputStream is) {
    dataStream = is;
  }
}
