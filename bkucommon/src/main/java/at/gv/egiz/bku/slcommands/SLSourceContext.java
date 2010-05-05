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

import java.net.URL;
import java.security.cert.X509Certificate;

public class SLSourceContext {
  
  private URL sourceUrl;
  private boolean sourceIsDataURL;
  private X509Certificate sourceCertificate;
  private String sourceHTTPReferer;

  public URL getSourceUrl() {
    return sourceUrl;
  }

  public void setSourceUrl(URL sourceProtocol) {
    this.sourceUrl = sourceProtocol;
  }

  public boolean isSourceIsDataURL() {
    return sourceIsDataURL;
  }

  public void setSourceIsDataURL(boolean sourceIsDataURL) {
    this.sourceIsDataURL = sourceIsDataURL;
  }

  public X509Certificate getSourceCertificate() {
    return sourceCertificate;
  }

  public void setSourceCertificate(X509Certificate sourceCertificate) {
    this.sourceCertificate = sourceCertificate;
  }

  public String getSourceHTTPReferer() {
    return sourceHTTPReferer;
  }

  public void setSourceHTTPReferer(String sourceHTTPReferer) {
    this.sourceHTTPReferer = sourceHTTPReferer;
  }

}