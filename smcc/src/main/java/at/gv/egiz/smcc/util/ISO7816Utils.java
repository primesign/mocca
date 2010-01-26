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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import at.gv.egiz.smcc.ChangeReferenceDataAPDUSpec;
import at.gv.egiz.smcc.NewReferenceDataAPDUSpec;
import at.gv.egiz.smcc.SecurityStatusNotSatisfiedException;
import at.gv.egiz.smcc.SignatureCardException;
import at.gv.egiz.smcc.VerifyAPDUSpec;

public class ISO7816Utils {
  
  public static TransparentFileInputStream openTransparentFileInputStream(
      final CardChannel channel, int maxSize) {
    
    TransparentFileInputStream file = new TransparentFileInputStream(maxSize) {

      @Override
      protected byte[] readBinary(int offset, int len) throws IOException {

        ResponseAPDU resp;
        try {
          resp = channel.transmit(new CommandAPDU(0x00, 0xB0,
              0x7F & (offset >> 8), offset & 0xFF, len));
        } catch (CardException e) {
          throw new IOException(e);
        }

        Throwable cause;
        if (resp.getSW() == 0x9000) {
          return resp.getData();
        } else if (resp.getSW() == 0x6982) {
          cause = new SecurityStatusNotSatisfiedException();
        } else {
          cause = new SignatureCardException("Failed to read bytes (offset=" + offset + ",len="
              + len + ") SW=" + Integer.toHexString(resp.getSW()) + ".");
        }
        throw new IOException(cause);

      }
      
    };

    return file;
    
  }

  public static byte[] readTransparentFile(CardChannel channel, int maxSize)
      throws CardException, SignatureCardException {
    
    TransparentFileInputStream is = openTransparentFileInputStream(channel, maxSize);
    
    try {

      ByteArrayOutputStream os = new ByteArrayOutputStream();
      
      int len;
      for (byte[] b = new byte[256]; (len = is.read(b)) != -1;) {
        os.write(b, 0, len);
      }
      
      return os.toByteArray();
      
    } catch (IOException e) {
      Throwable cause = e.getCause();
      if (cause instanceof CardException) {
        throw (CardException) cause;
      }
      if (cause instanceof SignatureCardException) {
        throw (SignatureCardException) cause;
      }
      throw new SignatureCardException(e);
    }
    
  }
  
  public static byte[] readTransparentFileTLV(CardChannel channel, int maxSize,
      byte expectedType) throws CardException, SignatureCardException {

    TransparentFileInputStream is = openTransparentFileInputStream(channel,
        maxSize);
    
    return readTransparentFileTLV(is, maxSize, expectedType);

  }
  
  public static byte[] readTransparentFileTLV(TransparentFileInputStream is, int maxSize,
      byte expectedType) throws CardException, SignatureCardException {


    try {

      is.mark(256);

      // check expected type
      int b = is.read();
      if (b == 0x00) {
        return null;
      }
      if (b == -1 || expectedType != (0xFF & b)) {
        throw new SignatureCardException("Unexpected TLV type. Expected "
            + Integer.toHexString(expectedType) + " but was "
            + Integer.toHexString(b) + ".");
      }

      // get actual length
      int actualSize = 2;
      b = is.read();
      if (b == -1) {
        return null;
      } else if ((0x80 & b) > 0) {
        int octets = (0x0F & b);
        actualSize += octets;
        for (int i = 1; i <= octets; i++) {
          b = is.read();
          if (b == -1) {
            return null;
          }
          actualSize += (0xFF & b) << ((octets - i) * 8);
        }
      } else {
        actualSize += 0xFF & b;
      }

      // set limit to actual size and read into buffer
      is.reset();
      is.setLimit(actualSize);
      byte[] buf = new byte[actualSize];
      if (is.read(buf) == actualSize) {
        return buf;
      } else {
        return null;
      }

    } catch (IOException e) {
      Throwable cause = e.getCause();
      if (cause instanceof CardException) {
        throw (CardException) cause;
      }
      if (cause instanceof SignatureCardException) {
        throw (SignatureCardException) cause;
      }
      throw new SignatureCardException(e);
    }

  }
  
  public static int getLengthFromFCx(byte[] fcx) {
    
    int len = -1;
    
    if (fcx.length != 0 && (fcx[0] == (byte) 0x62 || fcx[0] == (byte) 0x6F)) {
      int pos = 2;
      while (pos < (fcx[1] - 2)) {
        switch (fcx[pos]) {
        
        case (byte) 0x80: 
        case (byte) 0x81: {
          len = 0xFF & fcx[pos + 2];
          for (int i = 1; i < fcx[pos + 1]; i++) {
            len<<=8;
            len+=0xFF & fcx[pos + i + 2];
          }
        }

        default:
          pos += 0xFF & fcx[pos + 1] + 2;
        }
      }
    }
    
    return len;
    
  }
  
  public static byte[] readRecord(CardChannel channel, int record) throws CardException, SignatureCardException {
    
    ResponseAPDU resp = channel.transmit(
        new CommandAPDU(0x00, 0xB2, record, 0x04, 256));
    if (resp.getSW() == 0x9000) {
      return resp.getData();
    } else {
      throw new SignatureCardException("Failed to read records. SW="
          + Integer.toHexString(resp.getSW()));
    }
    
  }

  public static void formatPIN(int pinFormat, int pinJustification, byte[] fpin, byte[] mask, char[] pin) {
    
    boolean left = (pinJustification == VerifyAPDUSpec.PIN_JUSTIFICATION_LEFT);
    
    int j = (left) ? 0 : fpin.length - 1;
    int step = (left) ? 1 : - 1;
    switch (pinFormat) {
      case VerifyAPDUSpec.PIN_FORMAT_BINARY:
        if (fpin.length < pin.length) {
          throw new IllegalArgumentException();
        }
        for (int i = 0; i < pin.length; i++) {
          fpin[j] = (byte) Character.digit(pin[i], 10);
          mask[j] = (byte) 0xFF;
          j += step;
        }
        break;
      
      case VerifyAPDUSpec.PIN_FORMAT_BCD:
        if (fpin.length * 2 < pin.length) {
          throw new IllegalArgumentException();
        }
        for (int i = 0; i < pin.length; i++) {
          int digit = Character.digit(pin[i], 10);
          boolean h = (i % 2 == 0) ^ left;
          fpin[j] |= h ? digit : digit << 4 ;
          mask[j] |= h ? (byte) 0x0F : (byte) 0xF0;
          j += (i % 2) * step;
        }
        break;
  
      case VerifyAPDUSpec.PIN_FORMAT_ASCII:
        if (fpin.length < pin.length) {
          throw new IllegalArgumentException();
        }
        byte[] asciiPin = Charset.forName("ASCII").encode(CharBuffer.wrap(pin)).array();
        for (int i = 0; i < pin.length; i++) {
          fpin[j] = asciiPin[i];
          mask[j] = (byte) 0xFF;
          j += step;
        }
        break;
      }
  
  }
  
  public static void insertPIN(byte[] apdu, int pos, byte[] fpin, byte[] mask) {
    for (int i = 0; i < fpin.length; i++) {
      apdu[pos + i] &= ~mask[i];
      apdu[pos + i] |= fpin[i]; 
    }
  }
  
  public static void insertPINLength(byte[] apdu, int length, int lengthSize, int pos, int offset) {
    
    // use short (2 byte) to be able to shift the pin length
    // by the number of bits given by the pin length position
    short size = (short) (0x00FF & length);
    short sMask = (short) ((1 << lengthSize) - 1);
    // shift to the proper position 
    int shift = 16 - lengthSize - (pos % 8);
    offset += (pos / 8) + 5;
    size <<= shift;
    sMask <<= shift;
    // insert upper byte
    apdu[offset] &= (0xFF & (~sMask >> 8));
    apdu[offset] |= (0xFF & (size >> 8));
    // insert lower byte
    apdu[offset + 1] &= (0xFF & ~sMask);
    apdu[offset + 1] |= (0xFF & size);
    
  }

  public static CommandAPDU createVerifyAPDU(VerifyAPDUSpec apduSpec, char[] pin) {

    // format pin
    byte[] fpin = new byte[apduSpec.getPinLength()];
    byte[] mask = new byte[apduSpec.getPinLength()];
    formatPIN(apduSpec.getPinFormat(), apduSpec.getPinJustification(), fpin, mask, pin);

    byte[] apdu = apduSpec.getApdu();

    // insert formated pin
    insertPIN(apdu, apduSpec.getPinPosition() + 5, fpin, mask);

    // insert pin length
    if (apduSpec.getPinLengthSize() != 0) {
      insertPINLength(apdu, pin.length, apduSpec.getPinLengthSize(), apduSpec.getPinLengthPos(), 0);
    }

    return new CommandAPDU(apdu);
    
  }

  public static CommandAPDU createChangeReferenceDataAPDU(
      ChangeReferenceDataAPDUSpec apduSpec, char[] oldPin, char[] newPin) {
    
    // format old pin
    byte[] fpin = new byte[apduSpec.getPinLength()];
    byte[] mask = new byte[apduSpec.getPinLength()];
    formatPIN(apduSpec.getPinFormat(), apduSpec.getPinJustification(), fpin, mask, oldPin);

    byte[] apdu = apduSpec.getApdu();
    
    // insert formated old pin
    insertPIN(apdu, apduSpec.getPinPosition() + apduSpec.getPinInsertionOffsetOld() + 5, fpin, mask);

    // insert pin length
    if (apduSpec.getPinLengthSize() != 0) {
      insertPINLength(apdu, oldPin.length, apduSpec.getPinLengthSize(),
          apduSpec.getPinLengthPos(), apduSpec.getPinInsertionOffsetOld());
    }

    // format new pin
    fpin = new byte[apduSpec.getPinLength()];
    mask = new byte[apduSpec.getPinLength()];
    formatPIN(apduSpec.getPinFormat(), apduSpec.getPinJustification(), fpin, mask, newPin);
    
    // insert formated new pin
    insertPIN(apdu, apduSpec.getPinPosition() + apduSpec.getPinInsertionOffsetNew() + 5, fpin, mask);

    // insert pin length
    if (apduSpec.getPinLengthSize() != 0) {
      insertPINLength(apdu, newPin.length, apduSpec.getPinLengthSize(),
          apduSpec.getPinLengthPos(), apduSpec.getPinInsertionOffsetNew());
    }

    return new CommandAPDU(apdu);
    
  }

  public static CommandAPDU createNewReferenceDataAPDU(
      NewReferenceDataAPDUSpec apduSpec, char[] newPin) {
    
    // format old pin
    byte[] fpin = new byte[apduSpec.getPinLength()];
    byte[] mask = new byte[apduSpec.getPinLength()];
    formatPIN(apduSpec.getPinFormat(), apduSpec.getPinJustification(), fpin, mask, newPin);

    byte[] apdu = apduSpec.getApdu();
    
    // insert formated new pin
    insertPIN(apdu, apduSpec.getPinPosition() + apduSpec.getPinInsertionOffsetNew() + 5, fpin, mask);

    // insert pin length
    if (apduSpec.getPinLengthSize() != 0) {
      insertPINLength(apdu, newPin.length, apduSpec.getPinLengthSize(),
          apduSpec.getPinLengthPos(), apduSpec.getPinInsertionOffsetNew());
    }

    return new CommandAPDU(apdu);
    
  }

  
}
