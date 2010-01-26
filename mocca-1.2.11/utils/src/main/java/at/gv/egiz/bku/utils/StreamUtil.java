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
