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
