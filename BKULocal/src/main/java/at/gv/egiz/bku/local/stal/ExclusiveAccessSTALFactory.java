/*
* Copyright 2009 Federal Chancellery Austria and
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

import at.gv.egiz.stal.STAL;
import at.gv.egiz.stal.STALFactory;

public class ExclusiveAccessSTALFactory implements STALFactory {

  private STALFactory stalFactory;
  
  private ExclusiveAccessSTAL stal;
  
  private Locale locale;
  
  /**
   * @return the stalFactory
   */
  public STALFactory getStalFactory() {
    return stalFactory;
  }

  /**
   * @param stalFactory the stalFactory to set
   */
  public synchronized void setStalFactory(STALFactory stalFactory) {
    this.stalFactory = stalFactory;
    stalFactory.setLocale(locale);
  }

  @Override
  public synchronized STAL createSTAL() {
    if (stal == null && stalFactory != null) {
      STAL delegate = stalFactory.createSTAL();
      stal = new ExclusiveAccessSTAL(delegate);
    }
    return stal;
  }

  @Override
  public synchronized void setLocale(Locale locale) {
    this.locale = locale;
    if (stalFactory != null) {
      stalFactory.setLocale(locale);
    }
  }

}
