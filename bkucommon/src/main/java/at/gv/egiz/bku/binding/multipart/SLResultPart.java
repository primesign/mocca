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
