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
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory to get a matching instance for a encoded input stream when reading a http request.
 *
 */
public class InputDecoderFactory {

  public final static String MULTIPART_FORMDATA = "multipart/form-data";
  public final static String URL_ENCODED = "application/x-www-form-urlencoded";

  private static InputDecoderFactory instance = new InputDecoderFactory();

  private String defaultEncoding = URL_ENCODED;
  private Map<String, Class<? extends InputDecoder>> decoderMap = new HashMap<String, Class<? extends InputDecoder>>();

  private InputDecoderFactory() {
    decoderMap.put(MULTIPART_FORMDATA, MultiPartFormDataInputDecoder.class);
    decoderMap.put(URL_ENCODED, XWWWFormUrlInputDecoder.class);
  }

  public static InputDecoder getDefaultDecoder(InputStream is) {
    return getDecoder(instance.defaultEncoding, is);
  }

  /**
   * 
   * @param contentType
   * @param is
   * @return null if the content type is not supported
   */
  public static InputDecoder getDecoder(String contentType, InputStream is) {
    
    Logger log = LoggerFactory.getLogger(InputDecoderFactory.class);
    
    String prefix = contentType.split(";")[0].trim().toLowerCase();
    Class<? extends InputDecoder> dec = instance.decoderMap.get(prefix);
    if (dec == null) {
      log.info("Unknown encoding prefix " + contentType);
      return null;
    }
    InputDecoder id;
    try {
      id = dec.newInstance();
      id.setContentType(contentType);
      id.setInputStream(is);
      return id;
    } catch (InstantiationException e) {
      log.error("Failed to instantiate InputDecoder.", e);
      throw new IllegalArgumentException(
          "Cannot get an input decoder for content type: " + contentType);
    } catch (IllegalAccessException e) {
      log.error("Failed to instantiate InputDecoder.", e);
      throw new IllegalArgumentException(
          "Cannot get an input decoder for content type: " + contentType);
    }
  }

  /**
   * Allows to register decoders for special mime types.
   * @param mimeType
   * @param decoder
   */
  public static void registerDecoder(String mimeType,
      Class<? extends InputDecoder> decoder) {
    instance.decoderMap.put(mimeType.toLowerCase(), decoder);
  }
}
