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


package at.gv.egiz.bku.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

public class StreamUtil {

  /**
   * Copies data. None of the streams will be closed.
   * 
   * @param is
   * @param os
   * @throws IOException
   */
  public static void copyStream(InputStream is, OutputStream os)
      throws IOException {
    copyStream(is, os, 1024);
  }

  /**
   * Copies data. None of the streams will be closed.
   * 
   * @param is
   * @param os
   * @throws IOException
   */
  public static void copyStream(InputStream is, OutputStream os, int bufferSize)
      throws IOException {
    byte[] buffer = new byte[bufferSize];
    copyStream(is, os, buffer);
  }

  /**
   * Copies data. None of the streams will be closed.
   * 
   * @param is
   * @param os
   * @throws IOException
   */
  public static void copyStream(InputStream is, OutputStream os, byte[] buffer)
      throws IOException {
    for (int i = is.read(buffer); i > -1; i = is.read(buffer)) {
      os.write(buffer, 0, i);
    }
  }
  
  /**
   * Copies data. None of the streams will be closed.
   * 
   * @param is
   * @param os
   * @throws IOException
   */
  public static void copyStream(Reader is, Writer os)
      throws IOException {
    copyStream(is, os, 1024);
  }

  /**
   * Copies data. None of the streams will be closed.
   * 
   * @param is
   * @param os
   * @throws IOException
   */
  public static void copyStream(Reader is, Writer os, int bufferSize)
      throws IOException {
    char[] chars = new char[bufferSize];
    for (int i = is.read(chars); i > -1; i = is.read(chars)) {
      os.write(chars, 0, i);
    }
  }

  
  public static String asString(InputStream is, String charset)
      throws IOException {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    copyStream(is, os);
    return new String(os.toByteArray(), charset);
  }
}
