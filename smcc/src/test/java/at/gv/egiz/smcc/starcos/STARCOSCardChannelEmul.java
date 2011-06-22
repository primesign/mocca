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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import at.gv.egiz.smcc.AbstractAppl;
import at.gv.egiz.smcc.CardChannelEmul;
import at.gv.egiz.smcc.File;
import at.gv.egiz.smcc.PIN;

public class STARCOSCardChannelEmul extends CardChannelEmul {

  public static final int KID_PIN_Glob = 0x01;
  
  protected List<File> globalFiles = new ArrayList<File>();
  
  protected HashMap<Integer, PIN> globalPins = new HashMap<Integer, PIN>();

  public void setGlobalFiles(List<File> globalFiles) {
    this.globalFiles = globalFiles;
  }
  
  public void setGlobalPins(HashMap<Integer, PIN> globalPins) {
    this.globalPins = globalPins;
  }

  protected ResponseAPDU cmdSELECT(CommandAPDU command) throws CardException {
  
    byte[] fid = command.getData();
    
    switch (command.getP1()) {
    case 0x00: // MF
      if (fid.length !=0) {
        return new ResponseAPDU(new byte[] {(byte) 0x6A, (byte) 0x80}); 
      } else {
        currentFile = null;
        currentAppl = null;
        return new ResponseAPDU(new byte[] {(byte) 0x90, (byte) 0x00});
      }

    case 0x01: // Lower-level DF
      throw new CardException("Not supported.");
      
    case 0x02: // EF in current DF
      if (currentAppl != null) {
        if (command.getP2() != 0x04) {
          throw new CardException("Not supported.");
        }
        for (File file : currentAppl.getFiles()) {
          if (Arrays.equals(fid, file.fid)) {
            currentFile = file;
            byte[] response = new byte[file.fcx.length + 2];
            System.arraycopy(file.fcx, 0, response, 0, file.fcx.length);
            response[file.fcx.length] = (byte) 0x90;
            response[file.fcx.length + 1] = (byte) 0x00;
            return new ResponseAPDU(response);
          }
        }
        return new ResponseAPDU(new byte[] {(byte) 0x6A, (byte) 0x82});
      } else if (globalFiles != null) {
        if (command.getP2() != 0x04) {
          throw new CardException("Not supported.");
        }
        for (File file : globalFiles) {
          if (Arrays.equals(fid, file.fid)) {
            currentFile = file;
            byte[] response = new byte[file.fcx.length + 2];
            System.arraycopy(file.fcx, 0, response, 0, file.fcx.length);
            response[file.fcx.length] = (byte) 0x90;
            response[file.fcx.length + 1] = (byte) 0x00;
            return new ResponseAPDU(response);
          }
        }
        return new ResponseAPDU(new byte[] {(byte) 0x6A, (byte) 0x82});
      } else {
        throw new CardException("Not supported.");
      }
      
    case 0x03: // Higher-level DF
      throw new CardException("Not supported.");
      
    case 0x04: // Selection by DF name
      AbstractAppl appl = cardEmul.getApplication(fid);
      if (appl != null) {
        if (command.getP2() != 0x00) {
          throw new CardException("Not supported.");
        }
        if (currentAppl != null && currentAppl != appl) {
          currentAppl.leaveApplContext();
          currentFile = null;
        }
        currentAppl = appl;

        byte[] fci = currentAppl.getFCX();
        byte[] response = new byte[fci.length + 2];
        System.arraycopy(fci, 0, response, 0, fci.length);
        response[fci.length] = (byte) 0x90;
        response[fci.length + 1] = (byte) 0x00;
        return new ResponseAPDU(response);
      }
      
    default:
      return new ResponseAPDU(new byte[] {(byte) 0x6A, (byte) 0x86});
    }
    
  }
  
  protected ResponseAPDU cmdREAD_RECORD(CommandAPDU command) throws CardException {
    if (command.getINS() != 0xB2) {
      throw new IllegalArgumentException("INS has to be 0xB2");
    }
    if (currentFile == null) {
      return new ResponseAPDU(new byte[]{ (byte) 0x69, (byte) 0x86 });
    }
    if (command.getP1() != 0x01 || command.getP2() != 0x04) {
      throw new CardException("Not implemented.");
    }
    byte[] response = new byte[currentFile.file.length + 2];
    System.arraycopy(currentFile.file, 0, response, 0, currentFile.file.length);
    response[currentFile.file.length] = (byte) 0x90;
    response[currentFile.file.length + 1] = (byte) 0x00;
    return new ResponseAPDU(response);
  }

  protected ResponseAPDU cmdREAD_BINARY(CommandAPDU command) throws CardException {

    if (command.getINS() != 0xB0) {
      throw new IllegalArgumentException("INS has to be 0xB0.");
    }

    if (currentFile == null) {
      return new ResponseAPDU(new byte[] {(byte) 0x69, (byte) 0x86}); 
    }
    
    if ((command.getP1() & 0x80) > 0) {
      throw new CardException("Not implemented.");
    }
    
    int offset = command.getP2() + (command.getP1() << 8);
    if (offset > currentFile.file.length) {
      // Wrong length
      return new ResponseAPDU(new byte[] {(byte) 0x67, (byte) 0x00});
    }
    
    if (command.getNe() == 0) {
      throw new CardException("Not implemented.");
    }
    
    if (currentFile.kid != -1) {
      PIN pin;
      if ((currentFile.kid & 0x80) > 0) {
        if (currentAppl == null
            || (pin = currentAppl.pins.get(currentFile.kid)) == null
            || pin.state != PIN.STATE_PIN_VERIFIED) {
          return new ResponseAPDU(new byte[] {(byte) 0x69, (byte) 0x82});
        }
      } else {
        if ((pin = globalPins.get(currentFile.kid)) == null
          || pin.state != PIN.STATE_PIN_VERIFIED) {
          return new ResponseAPDU(new byte[] {(byte) 0x69, (byte) 0x82});
        }
      }
    }
    
    if (command.getNe() == 256 || command.getNe() <= currentFile.file.length - offset) {
      int len = Math.min(command.getNe(), currentFile.file.length - offset);
      byte[] response = new byte[len + 2];
      System.arraycopy(currentFile.file, offset, response, 0, len);
      response[len] = (byte) 0x90;
      response[len + 1] = (byte) 0x00;
      return new ResponseAPDU(response);
    } else if (command.getNe() >= currentFile.file.length - offset) {
      return new ResponseAPDU(new byte[] {(byte) 0x62, (byte) 0x82});
    } else {
      return new ResponseAPDU(new byte[] {(byte) 0x67, (byte) 0x00});
    }
    
  }

  
  @Override
  public ResponseAPDU transmit(CommandAPDU command) throws CardException {
  
    if (command.getCLA() == 0x00) {
  
      switch (command.getINS()) {
  
      // SELECT
      case 0xA4:
        return cmdSELECT(command);
      
      // READ BINARY
      case 0xB0:
        return cmdREAD_BINARY(command);
      
      // READ RECORD
      case 0xB2:
        return cmdREAD_RECORD(command);

      // VERIFY
      case 0x20:
        return cmdVERIFY(command);
      
      // MANAGE SECURITY ENVIRONMENT
      case 0x22: {
        if (currentAppl != null) {
          return currentAppl.cmdMANAGE_SECURITY_ENVIRONMENT(command, this);
        } else {
          return new ResponseAPDU(new byte[] {(byte) 0x6F, (byte) 0x05}); 
        }
      }
      
      // CHANGE REFERENCE DATA
      case 0x24: {
        return cmdCHANGE_REFERENCE_DATA(command);
      }
      
      // PERFORM SECURITY OPERATION
      case 0x2A: {
        if (currentAppl != null) {
          return currentAppl.cmdPERFORM_SECURITY_OPERATION(command, this);
        } else {
          return new ResponseAPDU(new byte[] {(byte) 0x6F, (byte) 0x05});
        }
      }
  
      // INTERNAL AUTHENTICATE
      case 0x88: {
        if (currentAppl != null) {
          return currentAppl.cmdINTERNAL_AUTHENTICATE(command, this);
        } else {
          return new ResponseAPDU(new byte[] {(byte) 0x6F, (byte) 0x05});
        }
      }
      
      default:
        return new ResponseAPDU(new byte[] { (byte) 0x6D, (byte) 0x00});
      }
  
    } else {
      return new ResponseAPDU(new byte[] { (byte) 0x6E, (byte) 0x00}); 
    }
  
  }

  protected ResponseAPDU verifyPin(int kid, byte[] reference) {
    
    PIN pin;
    if ((kid & 0x80) > 0 && currentAppl != null) {
      pin = currentAppl.pins.get(kid);
    } else {
      pin = globalPins.get(kid);
    }
    
    if (pin != null) {

      if (reference == null || reference.length == 0) {
        if (pin.state == PIN.STATE_PIN_NOTACTIVE) {
          return new ResponseAPDU(new byte[] { (byte) 0x69, (byte) 0x84 });
        } else if (pin.state == PIN.STATE_PIN_BLOCKED) {
          return new ResponseAPDU(new byte[] { (byte) 0x63, (byte) 0xc0 });
        } else {
          return new ResponseAPDU(new byte[] { (byte) 0x63, (byte) (pin.kfpc | 0xC0)});
        }
      }
      
      if (reference.length != 8) {
        return new ResponseAPDU(new byte[] {(byte) 0x67, (byte) 0x00});
      }
      
      if (Arrays.equals(reference, pin.pin)) {
        switch (pin.state) {
        case PIN.STATE_PIN_BLOCKED:
          return new ResponseAPDU(new byte[] { (byte) 0x69, (byte) 0x83 });
  
        case PIN.STATE_RESET:
          pin.state = PIN.STATE_PIN_VERIFIED;

        default:
          pin.kfpc = 10;
          return new ResponseAPDU(new byte[] { (byte) 0x90, (byte) 0x00 });
        }
      } else {
        switch (pin.state) {
        case PIN.STATE_PIN_BLOCKED:
          return new ResponseAPDU(new byte[] { (byte) 0x69, (byte) 0x83 });
        
        default:
          if (--pin.kfpc > 0) {
            return new ResponseAPDU(new byte[] { (byte) 0x63, (byte) (pin.kfpc | 0xC0)});
          } else {
            pin.state = PIN.STATE_PIN_BLOCKED;
            return new ResponseAPDU(new byte[] { (byte) 0x69, (byte) 0x83 });
          }
        }
        
      }
      
    } else {
      return new ResponseAPDU(new byte[] {(byte) 0x6A, (byte) 0x00});
    }
    
  }

  protected ResponseAPDU cmdVERIFY(CommandAPDU command) throws CardException {
    
    if (command.getINS() != 0x20) {
      throw new IllegalArgumentException("INS has to be 0x20.");
    }
    
    if (command.getP1() != 00) {
      return new ResponseAPDU(new byte[] {(byte) 0x6B, (byte) 0x00});
    }
    
    return verifyPin(command.getP2(), command.getData());
  
  }

  protected ResponseAPDU cmdCHANGE_REFERENCE_DATA(CommandAPDU command) {
    
    if (command.getINS() != 0x24) {
      throw new IllegalArgumentException("INS has to be 0x24.");
    }

    byte[] data = command.getData();

    ResponseAPDU response;
    
    if (command.getP1() == 0x01) {
    
      if (data.length != 8) {
        return new ResponseAPDU(new byte[] {(byte) 0x67, (byte) 0x00});
      }
      
      PIN pin;
      if ((command.getP2() & 0x80) > 0 && currentAppl != null) {
        pin = currentAppl.pins.get(command.getP2());
      } else {
        pin = globalPins.get(command.getP2());
      }
      if (pin.state == PIN.STATE_PIN_NOTACTIVE) {
        pin.pin = data;
        pin.state = PIN.STATE_RESET;
        response = new ResponseAPDU(new byte[] { (byte) 0x90, (byte) 0x00 });
      } else {
        // P1 == 0x01 not allowed on active pin (?)
        response = new ResponseAPDU(new byte[] { (byte) 0x6A, (byte) 0x86});
      }

    } else if (command.getP1() == 0x00) {
      
      if (data.length != 16) {
        return new ResponseAPDU(new byte[] {(byte) 0x67, (byte) 0x00});
      }
  
      response = verifyPin(0xFF & command.getP2(), Arrays.copyOf(data, 8));

      if (response.getSW() == 0x9000) {
        PIN pin;
        if ((command.getP2() & 0x80) > 0 && currentAppl != null) {
          pin = currentAppl.pins.get(command.getP2());
        } else {
          pin = globalPins.get(command.getP2());
        }
        pin.pin = Arrays.copyOfRange(data, 8, 16);
        pin.state = PIN.STATE_PIN_VERIFIED;
      }

    } else {
      return new ResponseAPDU(new byte[] { (byte) 0x6A, (byte) 0x81 });
    }
     
    return response;
    
  }
  
  public void setPin(int kid, char[] value) {
    PIN pin = globalPins.get(kid);
    if (pin != null) {
      if (value == null) {
//        pin.pin = null;
        //TransportPIN
//        pin.pin = new byte[] { (byte) 0x24, (byte) 0x12, (byte) 0x34, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
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
      }
    }
  }


}