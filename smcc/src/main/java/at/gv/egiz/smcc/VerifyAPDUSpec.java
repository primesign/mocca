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

public class VerifyAPDUSpec {
  
  public static final int PIN_JUSTIFICATION_LEFT = 0;
  
  public static final int PIN_JUSTIFICATION_RIGHT = 1;
  
  public static final int PIN_FORMAT_BINARY = 0;
  
  public static final int PIN_FORMAT_BCD = 1;
  
  public static final int PIN_FORMAT_ASCII = 2; 
  
  /**
   * The APDU template.
   */
  protected byte[] apdu;
  
  /**
   * The PIN position in bytes.
   */
  protected int pinPosition;
  
  /**
   * The PIN justification (either {@link #PIN_JUSTIFICATION_LEFT} or
   * {@link #PIN_JUSTIFICATION_RIGHT}).
   */
  protected int pinJustification = PIN_JUSTIFICATION_LEFT;

  /**
   * The PIN encoding format (one of {@value #PIN_FORMAT_BCD},
   * {@link #PIN_FORMAT_ASCII}).
   */
  protected int pinFormat;

  /**
   * The size of the PIN length in bits or 0 for no PIN length. (Default: 0)
   */
  protected int pinLengthSize = 0;
  
  /**
   * The PIN length in the template in bytes.
   */
  protected int pinLength;
  
  /**
   * The PIN length position in the template in bits or 0 for no PIN length.
   * (Default: 0)
   */
  protected int pinLengthPos = 0;

  /**
   * @param apdu
   * @param pinPosition
   * @param pinFormat
   * @param pinLength TODO
   */
  public VerifyAPDUSpec(byte[] apdu, int pinPosition, int pinFormat, int pinLength) {
    super();
    this.apdu = apdu;
    this.pinPosition = pinPosition;
    this.pinFormat = pinFormat;
    this.pinLength = pinLength;
  }

  /**
   * @param apdu
   * @param pinPosition
   * @param pinFormat
   * @param pinLength
   * @param pinLengthSize
   * @param pinLengthPos
   */
  public VerifyAPDUSpec(byte[] apdu, int pinPosition, int pinFormat,
      int pinLength, int pinLengthSize, int pinLengthPos) {
    super();
    this.apdu = apdu;
    this.pinPosition = pinPosition;
    this.pinFormat = pinFormat;
    this.pinLength = pinLength;
    this.pinLengthSize = pinLengthSize;
    this.pinLengthPos = pinLengthPos;
  }

  /**
   * @return the apdu
   */
  public byte[] getApdu() {
    return apdu;
  }

  /**
   * @param apdu the apdu to set
   */
  public void setApdu(byte[] apdu) {
    this.apdu = apdu;
  }

  /**
   * @return the pinPosition
   */
  public int getPinPosition() {
    return pinPosition;
  }

  /**
   * @param pinPosition the pinPosition to set
   */
  public void setPinPosition(int pinPosition) {
    this.pinPosition = pinPosition;
  }

  /**
   * @return the pinJustification
   */
  public int getPinJustification() {
    return pinJustification;
  }

  /**
   * @param pinJustification the pinJustification to set
   */
  public void setPinJustification(int pinJustification) {
    this.pinJustification = pinJustification;
  }

  /**
   * @return the pinFormat
   */
  public int getPinFormat() {
    return pinFormat;
  }

  /**
   * @param pinFormat the pinFormat to set
   */
  public void setPinFormat(int pinFormat) {
    this.pinFormat = pinFormat;
  }

  /**
   * @return the pinLengthSize
   */
  public int getPinLengthSize() {
    return pinLengthSize;
  }

  /**
   * @param pinLengthSize the pinLengthSize to set
   */
  public void setPinLengthSize(int pinLengthSize) {
    this.pinLengthSize = pinLengthSize;
  }

  /**
   * @return the pinLength
   */
  public int getPinLength() {
    return pinLength;
  }

  /**
   * @param pinLength the pinLength to set
   */
  public void setPinLength(int pinLength) {
    this.pinLength = pinLength;
  }

  /**
   * @return the pinLengthPos
   */
  public int getPinLengthPos() {
    return pinLengthPos;
  }

  /**
   * @param pinLengthPos the pinLengthPos to set
   */
  public void setPinLengthPos(int pinLengthPos) {
    this.pinLengthPos = pinLengthPos;
  }
  
}
