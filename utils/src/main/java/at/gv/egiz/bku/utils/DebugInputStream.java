/*
* Copyright 2009 Federal Chancellery Austria and
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
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DebugInputStream extends FilterInputStream {

  private ByteArrayOutputStream buffer = new ByteArrayOutputStream();
  
  public DebugInputStream(InputStream in) {
    super(in);
  }

  /* (non-Javadoc)
   * @see java.io.FilterInputStream#read()
   */
  @Override
  public int read() throws IOException {
    int b = super.read();
    buffer.write(b);
    return b;
  }

  /* (non-Javadoc)
   * @see java.io.FilterInputStream#read(byte[], int, int)
   */
  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    int l = super.read(b, off, len);
    if (l > 0) {
      buffer.write(b, off, l);
    }
    return l;
  }

  public byte[] getBufferedBytes() {
    return buffer.toByteArray();
  }

}
