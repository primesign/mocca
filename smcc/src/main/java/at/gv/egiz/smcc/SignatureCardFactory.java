//Copyright (C) 2002 IAIK
//http://jce.iaik.at
//
//Copyright (C) 2003 Stiftung Secure Information and 
//                 Communication Technologies SIC
//http://www.sic.st
//
//All rights reserved.
//
//This source is provided for inspection purposes and recompilation only,
//unless specified differently in a contract with IAIK. This source has to
//be kept in strict confidence and must not be disclosed to any third party
//under any circumstances. Redistribution in source and binary forms, with
//or without modification, are <not> permitted in any case!
//
//THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
//ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
//IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
//ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
//FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
//DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
//OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
//HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
//LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
//OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
//SUCH DAMAGE.
//
//
package at.gv.egiz.smcc;

import javax.smartcardio.ATR;
import javax.smartcardio.Card;

public class SignatureCardFactory {

  public static SignatureCardFactory getInstance() {
    return new SignatureCardFactory();
  }

  private SignatureCardFactory() {

  }

  public SignatureCard createSignatureCard(Card card)
      throws CardNotSupportedException {
    
    if(card == null) {
      SignatureCard sCard = new SWCard();
      sCard.init(card);
      return sCard;
    }

    ATR atr = card.getATR();
    byte[] historicalBytes = atr.getHistoricalBytes();
    if(historicalBytes == null || historicalBytes.length < 3) {
      throw new CardNotSupportedException("Card not supported: ATR=" + toString(atr.getBytes()));
    }
    
    int t = ((0xFF & (int) historicalBytes[0]) << 16) + 
      ((0xFF & (int) historicalBytes[1]) << 8) + 
      (0xFF & (int) historicalBytes[2]);

    SignatureCard sCard;
    switch (t) {
      case 0x455041 :
      case 0x4D4341 :
        sCard = new ACOSCard();
        break;

      case 0x805102 :
        sCard = new STARCOSCard();
        break;

      default :
        throw new CardNotSupportedException("Card not supported: ATR=" + toString(atr.getBytes()));
    }
    sCard.init(card);
    return sCard;
    
  }
  
  public static String toString(byte[] b) {
    StringBuffer sb = new StringBuffer();
    if (b != null && b.length > 0) {
      sb.append(Integer.toHexString((b[0] & 240) >> 4));
      sb.append(Integer.toHexString(b[0] & 15));
    }
    for(int i = 1; i < b.length; i++) {
      sb.append(':');
      sb.append(Integer.toHexString((b[i] & 240) >> 4));
      sb.append(Integer.toHexString(b[i] & 15));
    }
    return sb.toString();
  }


}
