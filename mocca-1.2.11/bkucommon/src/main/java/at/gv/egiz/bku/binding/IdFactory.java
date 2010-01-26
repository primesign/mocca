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
package at.gv.egiz.bku.binding;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Creates or converts Ids for BindingProcessors.
 * @author wbauer
 *
 */
public class IdFactory {

  public static int DEFAULT_NUMBER_OF_BITS = 168;

  private static Log log = LogFactory.getLog(IdFactory.class);

  private static IdFactory instance = new IdFactory();

  private SecureRandom random;
  private int numberOfBits = DEFAULT_NUMBER_OF_BITS;

  private IdFactory() {
    try {
      random = SecureRandom.getInstance("SHA1PRNG");
    } catch (NoSuchAlgorithmException e) {
      log.error("Cannot instantiate secure random" + e);
    }
  }

  public static IdFactory getInstance() {
    return instance;
  }


  /**
   * set the secure random number generator to create secure ids.
   * 
   * @param random
   *          must not be null
   */
  public void setSecureRandom(SecureRandom random) {
    if (random == null) {
      throw new NullPointerException("Cannot set secure random to null");
    }
    this.random = random;
  }

  /**
   * Don't use this method unless you know exactly what you do !
   * Be sure to use a sufficient large entropy 
   * @param numberOfBits >=1 (although this small entropy does not make sense)
   */
  public void setNumberOfBits(int numberOfBits) {
    if (numberOfBits <1) {
      throw new IllegalArgumentException("Cannot set number of bits < 1");
    }
    this.numberOfBits = numberOfBits;
  }

  public int getNumberOfBits() {
    return numberOfBits;
  }
  
  /**
   * Creates a new Id object with the factory's secure RNG and the set number of
   * bits.
   * 
   * @return
   */
  public Id createId() {
    return new IdImpl(numberOfBits, random);
  }

  /**
   * Creates an Id object for the provided String
   * 
   * @param idString
   *          may be null in this case the method call creates a new Id.
   * @return
   */
  public Id createId(String idString) {
    if (idString == null) {
      return createId();
    }
    return new IdImpl(idString);
  }
}