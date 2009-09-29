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

import at.gv.egiz.bku.binding.DataUrlConnection;
import at.gv.egiz.bku.slcommands.SLResult;
import at.gv.egiz.bku.slcommands.SLResult.SLResultType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.xml.transform.stream.StreamResult;

import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.PartSource;

public class SLResultPart extends FilePart {

  protected SLResult slResult;
  protected String encoding;

  public SLResultPart(SLResult slResult, String encoding) {
    super((slResult.getResultType() == SLResultType.XML) 
            ? DataUrlConnection.FORMPARAM_XMLRESPONSE
            : DataUrlConnection.FORMPARAM_BINARYRESPONSE, 
            new PartSource() {
              
              @Override
              public long getLength() {
                // may return null, as sendData() is overridden
                return 0;
              }
              
              @Override
              public String getFileName() {
                // return null, to prevent content-disposition header 
                return null;
              }
              
              @Override
              public InputStream createInputStream() throws IOException {
                // may return null, as sendData() is overridden below 
                return null;
              }
            }
         );
    this.slResult = slResult;
    this.encoding = encoding;
  }

  @Override
  protected void sendData(OutputStream out) throws IOException {
    slResult.writeTo(new StreamResult(new OutputStreamWriter(out, encoding)), false);
  }
}
