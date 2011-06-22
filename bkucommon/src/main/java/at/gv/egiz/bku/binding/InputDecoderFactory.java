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
