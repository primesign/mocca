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

import java.security.cert.X509Certificate;

public class SLTargetContext {
  private String targetProtocol;
  private boolean targetIsDataURL;
  private X509Certificate targetCertificate;

  public String getTargetProtocol() {
    return targetProtocol;
  }

  public void setTargetProtocol(String targetProtocol) {
    this.targetProtocol = targetProtocol;
  }

  public boolean isTargetIsDataURL() {
    return targetIsDataURL;
  }

  public void setTargetIsDataURL(boolean targetIsDataURL) {
    this.targetIsDataURL = targetIsDataURL;
  }

  public X509Certificate getTargetCertificate() {
    return targetCertificate;
  }

  public void setTargetCertificate(X509Certificate targetCertificate) {
    this.targetCertificate = targetCertificate;
  }

}