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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * bTimeOut: spr532 (win driver) interprets 0x00 as zero sec
 * instead of default
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class SCMMicrosystemsSPRx32 extends DefaultReader {
  
  public static final byte bTimeOut = 0x3c;
  public static final byte bTimeOut2 = 0x0f;

  protected final static Log log = LogFactory.getLog(SCMMicrosystemsSPRx32.class);

  public SCMMicrosystemsSPRx32(Card icc, CardTerminal ct) {
    super(icc, ct);
  }

  @Override
  public byte getbTimeOut() {
    return bTimeOut;
  }

  @Override
  public byte getbTimeOut2() {
    return bTimeOut2;   
  }

}
