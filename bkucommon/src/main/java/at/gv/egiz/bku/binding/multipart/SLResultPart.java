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
package at.gv.egiz.bku.binding.multipart;

import at.gv.egiz.bku.slcommands.SLResult;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.xml.transform.stream.StreamResult;

import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;

/**
 * 
 * @author clemens
 */
public class SLResultPart extends FilePart {

  protected SLResult slResult;
  protected String encoding;

  public SLResultPart(SLResult slResult, String encoding) {
    super("XMLResponse",
        new ByteArrayPartSource(null, "dummySource".getBytes()));
    this.slResult = slResult;
    this.encoding = encoding;
  }

  @Override
  protected void sendData(OutputStream out) throws IOException {
    slResult.writeTo(new StreamResult(new OutputStreamWriter(out, encoding)));
    // slResult.writeTo(new StreamResult(new OutputStreamWriter(System.out,
    // encoding)));
    // super.sendData(out);
  }
}
