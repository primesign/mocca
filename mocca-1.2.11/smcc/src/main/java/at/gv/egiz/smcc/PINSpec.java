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

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 *
 * @author mcentner
 */
public class PINSpec {

  /**
   * The minimum PIN length.
   */
  protected int minLength = 0;
    
  /**
   * The maximum PIN length or -1 if not specified.
   */
  protected int maxLength = -1;
    
  /**
   * The recommended PIN length or -1 if not specified.
   */
  protected int recLength = -1;
  
  /**
   * The regular expression pattern of a single PIN digit or character.
   */
  protected String rexepPattern;
  
  /**
   * The name of the corresponding resource bundle.
   */
  protected String resourceBundleName;
   
  /**
   * The key of the PIN name in the resource bundle.
   */
  protected String nameKey;

  /**
   * The name of the PIN.
   */
  protected String name;
  
  /**
   * The key id to be used in VERIFY or CHANGE REFERENCE DATA APDUs.
   */
  protected byte kid;

  /**
   * The context AID of the key id.
   */
  protected byte[] context_aid;

  /**
   * Creates a new instance of this PINSpec with the given lengths, regular
   * expression pattern, the ResourceBundle name and key to lookup the PIN name
   * and the KID and AID.
   * 
   * @param minLenght the minimum length of the PIN
   * @param maxLength the maximum length of the PIN, or -1 if there is no maximum length
   * @param rexepPattern the regular expression pattern of a single PIN digit or character
   * @param resourceBundleName the name of a ResourceBundle for this PIN
   * @param resourceKey the key to look up the (localized) name of this PIN
   * @param kid the key id of the PIN
   * @param contextAID the AID the KID is valid in
   */
  public PINSpec(int minLenght, int maxLength, String rexepPattern,
      String resourceBundleName, String resourceKey, byte kid, byte[] contextAID) {

    this.minLength = minLenght;
    this.maxLength = maxLength;
    this.rexepPattern = rexepPattern;
    this.resourceBundleName = resourceBundleName;
    this.nameKey = resourceKey + ".name";
    this.kid = kid;
    this.context_aid = contextAID;
  }

  /**
   * Creates a new instance of this PINSpec with the given lengths, regular 
   * expression pattern, the name of the PIN and the KID and AID.
   * 
   * @param minLenght the minimum length of the PIN
   * @param maxLength the maximum length of the PIN, or -1 if there is no maximum length
   * @param rexepPattern the regular expression pattern of a single PIN digit or character
   * @param name the name of the PIN
   * @param kid the key id of the PIN
   * @param contextAID the AID the KID is valid in
   */
  public PINSpec(int minLenght, int maxLength, String rexepPattern,
      String name, byte kid, byte[] contextAID) {

    this.minLength = minLenght;
    this.maxLength = maxLength;
    this.rexepPattern = rexepPattern;
    this.name = name;
    this.kid = kid;
    this.context_aid = contextAID;
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
   * @return the localized (using the default locale) name of the PIN, or the
   *         name set by
   *         {@link #PINSpec(int, int, String, String, byte, byte[])}.
   */
  public String getLocalizedName() {
    if (name != null) {
      return name;
    } else if (resourceBundleName != null){
      try {
        return ResourceBundle.getBundle(resourceBundleName).getString(nameKey);
      } catch (MissingResourceException e) {
      }
    }
    return nameKey;
  }

  /**
   * @param locale the locale for which the name should be returned
   * @return the localized name of the PIN, or the name set by
   *         {@link #PINSpec(int, int, String, String, byte, byte[])}
   */
  public String getLocalizedName(Locale locale) {
    if (name != null) {
      return name;
    } else if (resourceBundleName != null) {
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
  public String getRexepPattern() {
    return rexepPattern;
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
    
}
