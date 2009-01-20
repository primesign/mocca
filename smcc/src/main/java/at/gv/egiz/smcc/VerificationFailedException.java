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
package at.gv.egiz.smcc;

public class VerificationFailedException extends SignatureCardException {

  private static final long serialVersionUID = 1L;

  public static final int UNKNOWN = -1;
  
  private int retries = UNKNOWN;
  
  public VerificationFailedException() {
  }

  public VerificationFailedException(String message, Throwable cause) {
    super(message, cause);
  }

  public VerificationFailedException(String message) {
    super(message);
  }

  public VerificationFailedException(Throwable cause) {
    super(cause);
  }

  public VerificationFailedException(int retries) {
    this.retries = retries;
  }

  public VerificationFailedException(int retries, String message, Throwable cause) {
    super(message, cause);
    this.retries = retries;
  }

  public VerificationFailedException(int retries, String message) {
    super(message);
    this.retries = retries;
  }

  public VerificationFailedException(int retries, Throwable cause) {
    super(cause);
    this.retries = retries;
  }

  public int getRetries() {
    return retries;
  }
  
}
