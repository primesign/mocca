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

package at.gv.egiz.smcc.ccid;

import at.gv.egiz.smcc.conf.SMCCConfiguration;
import javax.smartcardio.Card;
import javax.smartcardio.CardTerminal;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class ReaderFactory {

  protected final static Log log = LogFactory.getLog(ReaderFactory.class);
  
  protected SMCCConfiguration configuration;
  private static ReaderFactory instance;

  private ReaderFactory() {
  }
  
  public static ReaderFactory getInstance() {
    if (instance == null) {
      instance = new ReaderFactory();
    }
    return instance;
  }
  
  /**
   * @param configuration the configuration to set
   */
  public void setConfiguration(SMCCConfiguration configuration) {
    this.configuration = configuration;
  }

  public CCID getReader(Card icc, CardTerminal ct) {
    CCID reader;
    String name = ct.getName();
    if (name != null) {
      log.info("creating reader " + name);
      name = name.toLowerCase();
      //ReinerSCT: http://support.reiner-sct.de/downloads/LINUX
      //           http://www.linux-club.de/viewtopic.php?f=61&t=101287&start=0
      //old: REINER SCT CyberJack 00 00
      //new (CCID): 0C4B/0300 Reiner-SCT cyberJack pinpad(a) 00 00
      //Mac "Snow Leopard": Reiner-SCT cyberJack pinpad(a) 00 00
      //display: REINER SCT CyberJack 00 00
      if(name.startsWith("gemplus gempc pinpad") || name.startsWith("gemalto gempc pinpad")) {
        reader = new GemplusGemPCPinpad(icc, ct);
      } else if (name.startsWith("omnikey cardman 3621")) {
        reader = new OMNIKEYCardMan3621(icc, ct);
      } else if (name.startsWith("scm microsystems inc. sprx32 usb smart card reader")) {
        reader = new SCMMicrosystemsSPRx32(icc, ct);
      } else if (name.startsWith("cherry smartboard xx44")) {
        reader = new CherrySmartBoardXX44(icc, ct);
      } else {
        log.info("no suitable implementation found, using default");
        reader = new DefaultReader(icc, ct);
      }
    } else {
      reader = new DefaultReader(icc, ct);
    }

    if (configuration != null) {
      String disablePinpad = configuration.getProperty(SMCCConfiguration.DISABLE_PINPAD_P);
      log.debug("setting disablePinpad to " + Boolean.parseBoolean(disablePinpad));
      reader.setDisablePinpad(Boolean.parseBoolean(disablePinpad));
    }
    return reader;
  }
}
