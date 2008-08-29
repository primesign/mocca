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
package at.gv.egiz.bku.local.stal;

import java.util.Locale;

import at.gv.egiz.smcc.PINProvider;
import at.gv.egiz.smcc.PINSpec;

public class SwingPINProvider implements PINProvider {
  
  private Locale locale = Locale.getDefault();
  SwingPinDialog dialog; 
  
  public SwingPINProvider() {
    this.locale = Locale.getDefault();
   
  }

  public Locale getLocale() {
    return locale;
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  @Override
  public String providePIN(PINSpec pinSpec, int retries) {
    dialog = new SwingPinDialog(null, false);
    dialog.setResizable(false);
    dialog.setRetries(retries);
    dialog.setPinSpec(pinSpec);
    dialog.initComponents();
    dialog.setVisible(true);
    dialog.requestFocus();
    dialog.setAlwaysOnTop(true);
    dialog.waitFinished();
    dialog.dispose();
    return dialog.getPIN();
  }

}
