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

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;

public class DebugReader extends FilterReader {
  
  private StringWriter buffer = new StringWriter();
  
  public DebugReader(Reader in) {
    super(in);
  }
  
  public DebugReader(Reader in, String start) {
    super(in);
    buffer.write(start);
  }

  @Override
  public int read() throws IOException {
    int c = super.read();
    if (c != -1)
      buffer.write(c);
    return c;
  }

  @Override
  public int read(char[] cbuf, int off, int len) throws IOException {
    int l = super.read(cbuf, off, len);
    if (l != -1 ) {
      buffer.write(cbuf, off, l);
    }
    return l;
  }

  public String getCachedString() {
    return buffer.toString();
  }
  
}
