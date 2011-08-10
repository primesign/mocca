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

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 *
 * @author mcentner
 */
public class PinInfo {

  public enum STATE {UNKNOWN, ACTIV, NOT_ACTIV, BLOCKED};
  
  /**
   * the number of remaining retries is unknown or irrelevant (blocked, not active states)
   */
  public static final int UNKNOWN_RETRIES = -1;
  
  /**
   * The minimum PIN length.
   */
  protected final int minLength;
    
  /**
   * The maximum PIN length or -1 if not specified.
   */
  protected final int maxLength;

  /**
   * The recommended PIN length or -1 if not specified.
   */
  protected int recLength = -1;
  
  /**
   * The regular expression pattern of a single PIN digit or character.
   */
  protected final String regexpPattern;
  
  /**
   * The name of the corresponding resource bundle.
   */
  protected final String resourceBundleName;
   
  /**
   * The key of the PIN name in the resource bundle.
   */
  protected final String nameKey;

  /**
   * The key id to be used in VERIFY or CHANGE REFERENCE DATA APDUs.
   */
  protected final byte kid;

  /**
   * The context AID of the key id.
   */
  protected final byte[] context_aid;

  protected final int maxRetries;

  /**
   * The current status of this PIN
   */
  protected STATE state = STATE.UNKNOWN;

  /**
   * number of further allowed retries (before the pin is blocked)
   */
  protected int retries = UNKNOWN_RETRIES;

  
  /**
   * Creates a new instance of this PINSpec with the given lengths, regular
   * expression pattern, the ResourceBundle name and key to lookup the PIN name
   * and the KID and AID.
   * 
   * @param minLenght the minimum length of the PIN
   * @param maxLength the maximum length of the PIN, or -1 if there is no maximum length
   * @param regexpPattern the regular expression pattern of a single PIN digit or character
   * @param resourceBundleName the name of a ResourceBundle for this PIN
   * @param resourceKey the key to look up the (localized) name of this PIN
   * @param kid the key id of the PIN
   * @param contextAID the AID the KID is valid in
   */
  public PinInfo(int minLenght, int maxLength, String regexpPattern,
      String resourceBundleName, String resourceKey, byte kid, byte[] contextAID, int maxRetries) {

    this.minLength = minLenght;
    this.maxLength = maxLength;
    this.regexpPattern = regexpPattern;
    this.resourceBundleName = resourceBundleName;
    this.nameKey = resourceKey + ".name";
    this.kid = kid;
    this.context_aid = contextAID;
    this.maxRetries = maxRetries;
  }

  /**
   * This method sets the recommended PIN length.
   *
   * @param recLength the recommended PIN length
   */
  public void setRecLength(int recLength) {
    this.recLength = recLength;
  }
  
  /**
   * @return the localized (using the default locale) name of the PIN
   */
  public String getLocalizedName() {
    if (resourceBundleName != null){
      try {
        return ResourceBundle.getBundle(resourceBundleName).getString(nameKey);
      } catch (MissingResourceException e) {
      }
    }
    return nameKey;
  }

  /**
   * @param locale the locale for which the name should be returned
   * @return the localized name of the PIN, 
   */
  public String getLocalizedName(Locale locale) {
    if (resourceBundleName != null) {
      try {
        return ResourceBundle.getBundle(resourceBundleName, locale).getString(nameKey);
      } catch (MissingResourceException e) {
      }
    }
    return nameKey;
  }

  /**
   * @return the recommended PIN length if specified and
   *         <code>recommended</code> is <code>true</code>, or
   *         <code>minLength</code>-<code>maxLength</code>
   */
  public String getLocalizedLength() {
    
    if (recLength > 0) {
      return "" + recLength;
    } else if (maxLength == minLength) {
      return "" + minLength;
    } else if (maxLength > minLength) {
      return minLength + "-" + maxLength;
    } else {
      return minLength + "+";
    }

  }

  /**
   * @return the minimum length of the PIN
   */
  public int getMinLength() {
    return minLength;
  }

  /**
   * @return the maximum length of the PIN, or -1 if not specified.
   */
  public int getMaxLength() {
    return maxLength;
  }

  /**
   * @return the minimum length of the PIN
   */
  public int getRecMinLength() {
    return (recLength >= minLength) ? recLength : minLength;
  }

  /**
   * @return the maximum length of the PIN
   */
  public int getRecMaxLength() {
    return (recLength >= minLength) ? recLength : maxLength;
  }
  
  /**
   * @return the recommended length of the PIN, or -1 if not specified
   */
  public int getRecLength() {
    return recLength;
  }

  /**
   * @return the regular expression pattern of one single digit or character
   */
  public String getRegexpPattern() {
    return regexpPattern;
  }

  /**
   * @return the key id of the PIN
   */
  public byte getKID() {
    return kid;
  }

  /**
   * @return the AID the KID is valid in, or <code>null</code> if KID is global
   */
  public byte[] getContextAID() {
    return context_aid;
  }

  public STATE getState() {
    return state;
  }

  public int getRetries() {
    return retries;
  }

  //////////////////////////////////////////////////////////////////////////////
  //* PinInfo declares protected methods to be used from within card implementations.
  //* DO NOT REFACTOR CARD INTERFACE AND IMPLEMENTATIONS TO SEPARATE PACKAGES
  
  protected void setNotActive() {
    this.state = STATE.NOT_ACTIV;
    this.retries = UNKNOWN_RETRIES;
  }

  protected void setActive(int retries) {
    this.state = STATE.ACTIV;
    this.retries = retries;
  }

  protected void setBlocked() {
    this.state = STATE.BLOCKED;
    this.retries = UNKNOWN_RETRIES;
  }

  protected void setUnknown() {
    this.state = STATE.UNKNOWN;
    this.retries = UNKNOWN_RETRIES;
  }

}
