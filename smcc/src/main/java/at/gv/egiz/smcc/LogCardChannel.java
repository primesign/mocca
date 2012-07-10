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


package at.gv.egiz.smcc;

import java.nio.ByteBuffer;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogCardChannel extends CardChannel {
  
  private final Logger log = LoggerFactory.getLogger(LogCardChannel.class);
  
  protected CardChannel channel;
  
  public LogCardChannel(CardChannel channel) {
    if (channel == null) {
      throw new NullPointerException();
    }
    this.channel = channel;
  }

  @Override
  public void close() throws CardException {
    channel.close();
  }

  @Override
  public Card getCard() {
    return channel.getCard();
  }

  @Override
  public int getChannelNumber() {
    return channel.getChannelNumber();
  }

  @Override
  public ResponseAPDU transmit(CommandAPDU command) throws CardException {
    if (log.isTraceEnabled()) {
      switch (command.getINS()) {
      case 0x20:    // VERIFY
      case 0x21:    // VERIFY
      case 0x2C:	// RESET RETRY COUNTER 
      case 0x24: {  // CHANGE REFERENCE DATA
        // Don't log possibly sensitive command data
        StringBuilder sb = new StringBuilder();
        sb.append(command);
        sb.append("\n-> ");
        byte[] c = new byte[4];
        c[0] = (byte) command.getCLA();
        c[1] = (byte) command.getINS();
        c[2] = (byte) command.getP1();
        c[3] = (byte) command.getP2();
        sb.append(toString(c));
        if (command.getNc() > 0) {
          sb.append(':');
          sb.append(toString(new byte[] {(byte) command.getNc()}));
          for (int i = 0; i < command.getNc(); i++) {
            sb.append(":XX");
          }
        }
        if (command.getNe() > 0) {
          sb.append(':');
          sb.append(toString(new byte[] {(byte) command.getNe()}));
        }
        log.trace(sb.toString());
      }; break;

      default:
        log.trace(command + "\n-> " + toString(command.getBytes()));
      }
      long t0 = System.currentTimeMillis();
      ResponseAPDU response = channel.transmit(command);
      long t1 = System.currentTimeMillis();
      log.trace(response + " [" + (t1 - t0) + "ms]\n<- " + toString(response.getBytes()));
      return response;
    } else {
      return channel.transmit(command);
    }
  }

  @Override
  public int transmit(ByteBuffer command, ByteBuffer response) throws CardException {
    if (log.isTraceEnabled()) {
      long t0 = System.currentTimeMillis();
      int l = channel.transmit(command, response);
      long t1 = System.currentTimeMillis();
      log.trace("[" + (t1 - t0) + "ms]");
      return l;
    } else {
      return channel.transmit(command, response);
    }
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
