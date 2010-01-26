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
 */package at.gv.egiz.bku.utils;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * An URLEncoding <a
 * href="http://tools.ietf.org/html/rfc3986#section-2.1">RFC3986, Section
 * 2.1</a> Writer, that uses an UTF-8 encoding according to <a href
 * ="http://www.w3.org/TR/html40/appendix/notes.html#non-ascii-chars"
 * >http://www.w3.org/TR/html40/appendix/notes.html#non-ascii-chars</a> for
 * writing non-ASCII characters.
 * 
 * @author mcentner
 */
public class URLEncodingWriter extends Writer {

  protected OutputStreamWriter osw;

  public URLEncodingWriter(Appendable out) {
    URLEncodingOutputStream urlEnc = new URLEncodingOutputStream(out);
    osw = new OutputStreamWriter(urlEnc, Charset.forName("UTF-8"));
  }

  @Override
  public void close() throws IOException {
    osw.close();
  }

  @Override
  public void flush() throws IOException {
    osw.flush();
  }

  @Override
  public void write(char[] cbuf, int off, int len) throws IOException {
    osw.write(cbuf, off, len);
  }

}
