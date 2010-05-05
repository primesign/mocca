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

import java.util.Locale;

import at.gv.egiz.bku.utils.urldereferencer.URLDereferencer;
import at.gv.egiz.stal.STAL;

public class SLCommandContext {
  
  private STAL stal;
  
  private URLDereferencer urlDereferencer;
  
  private Locale locale;

  public SLCommandContext(STAL stal, URLDereferencer urlDereferencer) {
    this.stal = stal;
    this.urlDereferencer = urlDereferencer;
  }

  public SLCommandContext(STAL stal, URLDereferencer urlDereferencer,
      Locale locale) {
    this.stal = stal;
    this.urlDereferencer = urlDereferencer;
    this.locale = locale;
  }

  public void setSTAL(STAL aStal) {
    this.stal = aStal;
  }

  public void setURLDereferencer(URLDereferencer urlDereferencer) {
    this.urlDereferencer = urlDereferencer;
  }

  public STAL getSTAL() {
    return stal;
  }

  public URLDereferencer getURLDereferencer() {
    return urlDereferencer;
  }

  public Locale getLocale() {
    return locale;
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
  }
  
}