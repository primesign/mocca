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

import javax.smartcardio.Card;
import javax.smartcardio.CardTerminal;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class ReaderFactory {

  public static CCID getReader(Card icc, CardTerminal ct) {
    String name = ct.getName();
    if (name != null) {
      name = name.toLowerCase();
      //ReinerSCT: http://support.reiner-sct.de/downloads/LINUX
      //           http://www.linux-club.de/viewtopic.php?f=61&t=101287&start=0
      //old: REINER SCT CyberJack 00 00
      //new (CCID): 0C4B/0300 Reiner-SCT cyberJack pinpad(a) 00 00
      //display: REINER SCT CyberJack 00 00
      if(name.startsWith("gemplus gempc pinpad")) {
        return new GemplusGemPCPinpad(icc, ct);
      } else if (name.startsWith("omnikey cardman 3621")) {
        return new OMNIKEYCardMan3621(icc, ct);
      } else if (name.startsWith("scm microsystems inc. sprx32 usb smart card reader")) {
        return new SCMMicrosystemsSPRx32(icc, ct);
      } else if (name.startsWith("cherry smartboard xx44")) {
        return new CherrySmartBoardXX44(icc, ct);
      }
    }
    return new DefaultReader(icc, ct);
  }
}
