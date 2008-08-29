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
package at.gv.egiz.bku.slcommands.impl;

import java.io.ByteArrayOutputStream;

import javax.xml.transform.stream.StreamResult;

import org.junit.Test;

import at.gv.egiz.bku.slcommands.ErrorResult;
import at.gv.egiz.bku.slexceptions.SLException;

public class ErrorResultImplTest {

  @Test
  public void writeTo() {
    
    SLException slException = new SLException(0,"test.noerror", null);
    ErrorResult errorResult = new ErrorResultImpl(slException);
    
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    StreamResult result = new StreamResult(stream);
    errorResult.writeTo(result);
    
    System.out.println(stream.toString());
    
  }

  
}
