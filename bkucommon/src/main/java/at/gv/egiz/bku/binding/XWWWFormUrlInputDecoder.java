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
