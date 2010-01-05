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
package at.gv.egiz.smcc.starcos;

import at.gv.egiz.smcc.CardEmul;
import at.gv.egiz.smcc.File;
import at.gv.egiz.smcc.PIN;

/**
 *
 * @author clemens
 */
public class STARCOSG3CardChannelEmul extends STARCOSCardChannelEmul {

  public STARCOSG3CardChannelEmul(CardEmul cardEmul, byte[] Glob_PIN, int PIN_STATE) {
    super(cardEmul, Glob_PIN, PIN_STATE);

    // G3 version file
    byte[] versionFileFID = new byte[]{(byte) 0x00, (byte) 0x32};
    byte[] versionFile = new byte[]{
      (byte) 0xa5, (byte) 0x0e, (byte) 0x53, (byte) 0x02, (byte) 0x01, (byte) 0x20, (byte) 0x54, (byte) 0x08,
      (byte) 0x01, (byte) 0x01, (byte) 0x03, (byte) 0x01, (byte) 0x04, (byte) 0x01, (byte) 0x70, (byte) 0x01};
    byte[] versionFileFCX = new byte[]{
      (byte) 0x62, (byte) 0x1a, (byte) 0x80, (byte) 0x02, (byte) 0x00, (byte) 0x14, (byte) 0x82, (byte) 0x05,
      (byte) 0x44, (byte) 0x41, (byte) 0x00, (byte) 0x14, (byte) 0x01, (byte) 0x83, (byte) 0x02, (byte) 0x00,
      (byte) 0x32, (byte) 0x88, (byte) 0x01, (byte) 0xd8, (byte) 0x8a, (byte) 0x01, (byte) 0x05, (byte) 0xa1,
      (byte) 0x03, (byte) 0x8b, (byte) 0x01, (byte) 0x03};

    globalFiles.add(new File(versionFileFID, versionFile, versionFileFCX));

  }
 }
