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
package at.gv.egiz.smcc.util;

import java.util.Arrays;

import javax.smartcardio.CommandAPDU;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import at.gv.egiz.smcc.VerifyAPDUSpec;
import at.gv.egiz.smcc.util.ISO7816Utils;
import static org.junit.Assert.*;

public class ISO7816UtilsTest {
  
  @Test
  public void testFormatPIN() {
    
    formatPIN(VerifyAPDUSpec.PIN_FORMAT_BINARY,
        VerifyAPDUSpec.PIN_JUSTIFICATION_LEFT, 7, "1234",
        new byte[] {
        (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00
        },
        new byte[] {
        (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0x00, (byte) 0x00, (byte) 0x00
        }
    );

    formatPIN(VerifyAPDUSpec.PIN_FORMAT_BINARY,
        VerifyAPDUSpec.PIN_JUSTIFICATION_RIGHT, 7, "12345",
        new byte[] {
        (byte) 0x00, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) 0x03, (byte) 0x02, (byte) 0x01
        },
        new byte[] {
        (byte) 0x00, (byte) 0x00, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff
        }
    );

    formatPIN(VerifyAPDUSpec.PIN_FORMAT_BCD,
        VerifyAPDUSpec.PIN_JUSTIFICATION_LEFT, 7, "12345",
        new byte[] {
        (byte) 0x12, (byte) 0x34, (byte) 0x50, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
        },
        new byte[] {
        (byte) 0xff, (byte) 0xff, (byte) 0xf0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
        }
    );

    formatPIN(VerifyAPDUSpec.PIN_FORMAT_BCD,
        VerifyAPDUSpec.PIN_JUSTIFICATION_RIGHT, 7, "1234567",
        new byte[] {
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x07, (byte) 0x65, (byte) 0x43, (byte) 0x21
        },
        new byte[] {
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0f, (byte) 0xff, (byte) 0xff, (byte) 0xff
        }
    );

    formatPIN(VerifyAPDUSpec.PIN_FORMAT_ASCII,
        VerifyAPDUSpec.PIN_JUSTIFICATION_LEFT, 7, "1234",
        new byte[] {
        (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34, (byte) 0x00, (byte) 0x00, (byte) 0x00
        },
        new byte[] {
        (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0x00, (byte) 0x00, (byte) 0x00
        }
    );

    formatPIN(VerifyAPDUSpec.PIN_FORMAT_ASCII,
        VerifyAPDUSpec.PIN_JUSTIFICATION_RIGHT, 7, "12345",
        new byte[] {
        (byte) 0x00, (byte) 0x00, (byte) 0x35, (byte) 0x34, (byte) 0x33, (byte) 0x32, (byte) 0x31
        },
        new byte[] {
        (byte) 0x00, (byte) 0x00, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff
        }
    );

    
  }
  
  private void formatPIN(int pinFormat, int pinJusitification, int pinLength, String pin, byte[] rfpin, byte[] rmask) {

    byte[] fpin = new byte[pinLength];
    byte[] mask = new byte[pinLength];
    
    ISO7816Utils.formatPIN(pinFormat, pinJusitification, fpin, mask, pin.toCharArray());
    
//    System.out.println(toString(fpin));
//    System.out.println(toString(mask));
    
    assertTrue(Arrays.equals(fpin, rfpin));
    assertTrue(Arrays.equals(mask, rmask));
    
  }
  
  @Test
  public void testCreateVerifyAPDU() {
    
    VerifyAPDUSpec verifyAPDUSpec;
    CommandAPDU apdu;
    byte[] ref;
    
    verifyAPDUSpec = new VerifyAPDUSpec(
        new byte[] {
            (byte) 0x00, (byte) 0x20, (byte) 0x00, (byte) 0x80, (byte) 0x08,
            (byte) 0x20, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff }, 
        1, VerifyAPDUSpec.PIN_FORMAT_BCD, 7, 4, 4);
    
    apdu = ISO7816Utils.createVerifyAPDU(verifyAPDUSpec, "1234".toCharArray());
    
//    System.out.println(toString(apdu.getBytes()));
    
    ref = new byte[] { (byte) 0x00, (byte) 0x20, (byte) 0x00, (byte) 0x80,
        (byte) 0x08, (byte) 0x24, (byte) 0x12, (byte) 0x34, (byte) 0xff,
        (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff };
    
    assertTrue(Arrays.equals(apdu.getBytes(), ref));
    
    ref = new byte[] { (byte) 0x00, (byte) 0x20, (byte) 0x00, (byte) 0x80,
        (byte) 0x08, (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34,
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };
    
    verifyAPDUSpec = new VerifyAPDUSpec(
        new byte[] {
            (byte) 0x00, (byte) 0x20, (byte) 0x00, (byte) 0x80, (byte) 0x08,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 }, 
        0, VerifyAPDUSpec.PIN_FORMAT_ASCII, 8);

    apdu = ISO7816Utils.createVerifyAPDU(verifyAPDUSpec, "1234".toCharArray());

//    System.out.println(toString(apdu.getBytes()));
    
    assertTrue(Arrays.equals(apdu.getBytes(), ref));

  }
  
  private String toString(byte[] b) {
    StringBuffer sb = new StringBuffer();
    if (b != null && b.length > 0) {
      sb.append(Integer.toHexString((b[0] & 240) >> 4));
      sb.append(Integer.toHexString(b[0] & 15));
    }
    for (int i = 1; i < b.length; i++) {
      sb.append(':');
      sb.append(Integer.toHexString((b[i] & 240) >> 4));
      sb.append(Integer.toHexString(b[i] & 15));
    }
    return sb.toString();
  }


}
