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