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
package at.gv.egiz.bku.binding;

import java.io.IOException;
import java.util.Map;

public class MultiTestDataUrlConnection extends TestDataUrlConnection {
  
  public static interface DataSourceProvider {
    public Map<String, String> getResponseHeaders();
    public String getResponseContent();
    public int getResponseCode();
    public void nextEvent();
  }
  
  
  protected DataSourceProvider dataSource;
  
  public void setDataSource(DataSourceProvider dataSource) {
    this.dataSource = dataSource;
  }
  
  public DataUrlResponse getResponse() throws IOException {
    if (dataSource == null) {
      return super.getResponse();
    }
    dataSource.nextEvent();
    responseHeaders = dataSource.getResponseHeaders();
    responseCode = dataSource.getResponseCode();
    responseContent = dataSource.getResponseContent();
    return super.getResponse();
  }

}
