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
package at.gv.egiz.smcc;

public class ChangeReferenceDataAPDUSpec extends VerifyAPDUSpec {
  
  /**
   * The offset for the insertion of the old PIN. (Default: 0)
   */
  protected int pinInsertionOffsetOld = 0;
  
  /**
   * The offset for the insertion of the new PIN. (Default:
   * {@link VerifyAPDUSpec#pinLength} + 1})
   */
  protected int pinInsertionOffsetNew = pinLength;

  public ChangeReferenceDataAPDUSpec(byte[] apdu, int pinPosition, int pinFormat, int pinLength) {
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
  public ChangeReferenceDataAPDUSpec(byte[] apdu, int pinPosition,
      int pinFormat, int pinLength, int pinLengthSize, int pinLengthPos) {
    super(apdu, pinPosition, pinFormat, pinLength, pinLengthSize, pinLengthPos);
  }
  
  /**
   * @param apdu
   * @param pinPosition
   * @param pinFormat
   * @param pinLength
   * @param pinLengthSize
   * @param pinLengthPos
   * @param pinInsertionOffsetNew
   */
  public ChangeReferenceDataAPDUSpec(byte[] apdu, int pinPosition,
      int pinFormat, int pinLength, int pinLengthSize, int pinLengthPos,
      int pinInsertionOffsetNew) {
    super(apdu, pinPosition, pinFormat, pinLength, pinLengthSize, pinLengthPos);
    this.pinInsertionOffsetNew = pinInsertionOffsetNew;
  }

  /**
   * @return the pinInsertionOffsetOld
   */
  public int getPinInsertionOffsetOld() {
    return pinInsertionOffsetOld;
  }

  /**
   * @param pinInsertionOffsetOld the pinInsertionOffsetOld to set
   */
  public void setPinInsertionOffsetOld(int pinInsertionOffsetOld) {
    this.pinInsertionOffsetOld = pinInsertionOffsetOld;
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
