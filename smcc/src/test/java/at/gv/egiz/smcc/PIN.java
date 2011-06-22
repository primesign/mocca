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

public class PIN {
  
  public static final int STATE_RESET = 0;
  
  public static final int STATE_PIN_VERIFIED = 1;
  
  public static final int STATE_PIN_BLOCKED = -1;

  public static final int STATE_PIN_NOTACTIVE = 2;
  
  public byte[] pin;
  
  public int kid;
  
  public int state; // = STATE_RESET;
  
  public int kfpc; // = 10;

  public PIN() {
  };
  
  //TODO also provde default constructor without state param
  public PIN(byte[] pin, int kid, int kfpc, int state) {
    this.pin = pin;
    this.kid = kid;
    this.kfpc = kfpc;
    this.state = state;
  }

  /**
   * @return the pin
   */
  public byte[] getPin() {
    return pin;
  }

  /**
   * @param pin the pin to set
   */
  public void setPin(byte[] pin) {
    this.pin = pin;
  }

  /**
   * @return the kid
   */
  public int getKid() {
    return kid;
  }

  /**
   * @param kid the kid to set
   */
  public void setKid(int kid) {
    this.kid = kid;
  }

  /**
   * @return the state
   */
  public int getState() {
    return state;
  }

  /**
   * @param state the state to set
   */
  public void setState(int state) {
    this.state = state;
  }

  /**
   * @return the kfpc
   */
  public int getKfpc() {
    return kfpc;
  }

  /**
   * @param kfpc the kfpc to set
   */
  public void setKfpc(int kfpc) {
    this.kfpc = kfpc;
  }

  
  
}
