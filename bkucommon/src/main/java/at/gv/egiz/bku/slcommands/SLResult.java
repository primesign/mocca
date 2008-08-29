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
package at.gv.egiz.bku.slcommands;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;

public interface SLResult {
  
  public static enum SLResultType {BINARY, XML}; 
  
  public SLResultType getResultType();
  
  /**
   * The MIME Type of the Result.  
   * 
   * @return may result null if unknown.
   */
  public String getMimeType();

  public void writeTo(Result aResult);
  
  /**
   * 
   * @param result
   * @param transformer may be null.
   */
  public void writeTo(Result result, Transformer transformer) throws TransformerException;
}