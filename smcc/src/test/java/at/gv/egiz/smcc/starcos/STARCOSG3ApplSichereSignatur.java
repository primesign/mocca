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


package at.gv.egiz.smcc.starcos;

import java.util.Arrays;
import java.util.Random;

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import at.gv.egiz.smcc.CardChannelEmul;
import at.gv.egiz.smcc.PIN;

public class STARCOSG3ApplSichereSignatur extends STARCOSAppl {

  protected static final int KID_PIN_SS = 0x81;

  protected byte[] dst = { (byte) 0x84, (byte) 0x03, (byte) 0x80, (byte) 0x02,
      (byte) 0x00, (byte) 0x80, (byte) 0x01, (byte) 0x04 };
  
  protected byte[] ht = { (byte) 0x80, (byte) 0x01, (byte) 0x10 };
  
  public void setHt1(byte[] ht) {
    this.ht = ht;
  }

  public void setDst1(byte[] dst) {
    this.dst = dst;
  }

  @Override
  public ResponseAPDU cmdPERFORM_SECURITY_OPERATION(CommandAPDU command, CardChannelEmul channel) throws CardException {
    
    checkINS(command, 0x2A);
  
    if (command.getP1() == 0x90 && command.getP2() == 0xA0) {
      
      // HASH
      byte[] data = command.getData();
      if (data[0] == (byte) 0x90 && data[1] == (byte) 0x14) {
        hash = Arrays.copyOfRange(data, 2, data.length);
        return new ResponseAPDU(new byte[] {(byte) 0x90, (byte) 0x00});
      } else {
        throw new CardException("HASH command only supports complete hash.");
      }
      
    } else if (command.getP1() == 0x9E && command.getP2() == 0x9A) {
      
      // COMPUTE DIGITAL SIGNATURE
      if (securityEnv == null) {
        // No security environment
        return new ResponseAPDU(new byte[] {(byte) 0x6F, (byte) 0x05});
      }
      
      if (command.getData() != null) {
        hash = command.getData();
      }
      
      if (hash == null) {
        // Command sequence not correct
        return new ResponseAPDU(new byte[] {(byte) 0x6F, (byte) 0x03});
      }
      if (hash.length != 20) {
        // Invalid hash length
        return new ResponseAPDU(new byte[] {(byte) 0x6A, (byte) 0x80});
      }
      if (pins.get(KID_PIN_SS).state != PIN.STATE_PIN_VERIFIED) {
        // Security Status not satisfied
        return new ResponseAPDU(new byte[] {(byte) 0x69, (byte) 0x82});
      }
      
      byte[] signature = new byte[48]; 
      
      // TODO replace by signature creation
      Random random = new Random();
      random.nextBytes(signature);
      
      byte[] response = new byte[signature.length + 2];
      System.arraycopy(signature, 0, response, 0, signature.length);
      response[signature.length] = (byte) 0x90;
      response[signature.length + 1] = (byte) 0x00;
      
      hash = null;
      pins.get(KID_PIN_SS).state = PIN.STATE_RESET;
      
      return new ResponseAPDU(response);
      
    } else {
      return new ResponseAPDU(new byte[] {(byte) 0x6A, (byte) 0x00});
    }
    
  }
  
  @Override
  public ResponseAPDU cmdMANAGE_SECURITY_ENVIRONMENT(CommandAPDU command, CardChannelEmul channel) throws CardException {

    checkINS(command, 0x22);

    switch (command.getP2()) {
    case 0xA4:
      switch (command.getP1()) {
      case 0x41:
        // INTERNAL AUTHENTICATE
      case 0x81:
        // EXTERNAL AUTHENTICATE
      }
    case 0xAA:
      switch (command.getP1()) {
      case 0x41:
        // SET HT
        if (Arrays.equals(ht, command.getData())) {
          return new ResponseAPDU(new byte[] {(byte) 0x90, (byte) 0x00});
        } else {
          return new ResponseAPDU(new byte[] {(byte) 0x6A, (byte) 0x80});
        }
      }
    case 0xB6:
      switch (command.getP1()) {
      case 0x41: {
        // PSO - COMPUTE DIGITAL SIGNATURE
        if (Arrays.equals(dst, command.getData())) {
          securityEnv = command.getData();
          return new ResponseAPDU(new byte[] {(byte) 0x90, (byte) 0x00});
        } else {
          return new ResponseAPDU(new byte[] {(byte) 0x6A, (byte) 0x80});
        }
      }
      case 0x81:
        // PSO - VERIFY DGITAL SIGNATURE
      }
    case 0xB8:
      switch (command.getP1()) {
      case 0x41:
        // PSO - DECIPHER
      case 0x81:
        // PSO - ENCIPHER
      }
    default:
      return new ResponseAPDU(new byte[] {(byte) 0x6A, (byte) 0x81});
    }
  
  }

  /**
   * set and activate pin
   * @param value if null, pin will be set to NOTACTIVE
   */
  @Override
  public void setPin(int kid, char[] value) {
    PIN pin = pins.get(kid);
    if (pin != null) {
      if (value == null) {
//        pin.pin = null;
        //TransportPIN
//        pin.pin = new byte[] { (byte) 0x26, (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
        pin.state = PIN.STATE_PIN_NOTACTIVE;
      } else {
        byte[] b = new byte[8];
        b[0] = (byte) (0x20 | value.length);
        for(int i = 1, j = 0; i < b.length; i++) {
          int h = ((j < value.length)
                  ? Character.digit(value[j++], 10)
                  : 0x0F);
          int l = ((j < value.length)
                  ? Character.digit(value[j++], 10)
                  : 0x0F);
          b[i] = (byte) ((h << 4) | l);
        }
        pin.pin = b;
        pin.state = PIN.STATE_RESET;
      }
    }
  }
}