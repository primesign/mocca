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


package at.gv.egiz.smcc.acos;

import java.util.Arrays;

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import at.gv.egiz.smcc.AbstractAppl;
import at.gv.egiz.smcc.CardChannelEmul;
import at.gv.egiz.smcc.File;
import at.gv.egiz.smcc.PIN;

public abstract class ACOSCardChannelEmul extends CardChannelEmul {

  protected ResponseAPDU cmdSELECT(CommandAPDU command) throws CardException {
  
    byte[] fid = command.getData();
    
    AbstractAppl appl = cardEmul.getApplication(fid);
    if (appl != null) {
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
    
    if (command.getP1() == 0x00) {
      // SELECT with FID
      if (currentAppl instanceof AbstractAppl) {
        
        for (File file : ((AbstractAppl) currentAppl).getFiles()) {
          
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
        return new ResponseAPDU(new byte[] {(byte) 0x6A, (byte) 0x82});
      }
    }
    
    // Not found
    return new ResponseAPDU(new byte[] {(byte) 0x6A, (byte) 0x82});
    
  }
  
  public abstract ResponseAPDU cmdREAD_BINARY(CommandAPDU command) throws CardException;


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
      
      // VERIFY
      case 0x20:
        if ((command.getP2() & 0x80) > 0) {
          return cmdVERIFY(command);
        } else {
          return new ResponseAPDU(new byte[] {(byte) 0x6A, (byte) 0x81}); 
        }
      
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
    if (currentAppl != null) {
      pin = currentAppl.pins.get(kid);
    } else {
      pin = null;
    }
    
    if (pin != null) {
  
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

  public ResponseAPDU cmdVERIFY(CommandAPDU command) throws CardException {
    
    if (command.getINS() != 0x20) {
      throw new IllegalArgumentException("INS has to be 0x20.");
    }
    
    if (command.getP1() != 00) {
      return new ResponseAPDU(new byte[] {(byte) 0x6B, (byte) 0x00});
    }
    
    return verifyPin(command.getP2(), command.getData());
  
  }

  public ResponseAPDU cmdCHANGE_REFERENCE_DATA(CommandAPDU command) {
    
    if (command.getINS() != 0x24) {
      throw new IllegalArgumentException("INS has to be 0x24.");
    }
    
    if (command.getP1() == 0x00) {
      
      byte[] data = command.getData();
      if (data.length != 16) {
        return new ResponseAPDU(new byte[] {(byte) 0x67, (byte) 0x00});
      }
  
      ResponseAPDU response = verifyPin(command.getP2(), Arrays.copyOf(data, 8));
      if (response.getSW() == 0x9000) {
        PIN pin;
        if (currentAppl != null) {
          pin = currentAppl.pins.get(command.getP2());
        } else {
          pin = null;
        }
        pin.pin = Arrays.copyOfRange(data, 8, 16);
      }
      
      return response;
      
    } else {
      return new ResponseAPDU(new byte[] {(byte) 0x6A, (byte) 0x81});
    }
    
  }

}