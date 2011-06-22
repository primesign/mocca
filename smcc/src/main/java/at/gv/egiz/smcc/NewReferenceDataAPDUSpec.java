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

public class NewReferenceDataAPDUSpec extends VerifyAPDUSpec {
  
  /**
   * The offset for the insertion of the new PIN. (Default:
   * {@link VerifyAPDUSpec#pinLength} + 1})
   */
  protected int pinInsertionOffsetNew = 0;

  public NewReferenceDataAPDUSpec(byte[] apdu, int pinPosition, int pinFormat, int pinLength) {
    super(apdu, pinPosition, pinFormat, pinLength);
  }

  /**
   * @param apdu
   * @param pinPosition
   * @param pinFormat
   * @param pinLength
   * @param pinLengthSize
   * @param pinLengthPos
   */
  public NewReferenceDataAPDUSpec(byte[] apdu, int pinPosition,
      int pinFormat, int pinLength, int pinLengthSize, int pinLengthPos) {
    super(apdu, pinPosition, pinFormat, pinLength, pinLengthSize, pinLengthPos);
  }

  /**
   * @return the pinInsertionOffsetNew
   */
  public int getPinInsertionOffsetNew() {
    return pinInsertionOffsetNew;
  }

  /**
   * @param pinInsertionOffsetNew the pinInsertionOffsetNew to set
   */
  public void setPinInsertionOffsetNew(int pinInsertionOffsetNew) {
    this.pinInsertionOffsetNew = pinInsertionOffsetNew;
  }

  
  
}
