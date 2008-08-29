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
package at.gv.egiz.smcc.utils;

import at.gv.egiz.smcc.PINProvider;
import at.gv.egiz.smcc.PINSpec;

public class SingletonPINProvider implements PINProvider {

  private String pin;
  private boolean pin_already_provided = false;

  public SingletonPINProvider(String pin) {
    this.pin = pin;
  }

  public String providePIN(PINSpec spec, int retries) {
    if (pin_already_provided)
      return null;
    pin_already_provided = true;
    return pin;
  }

}
