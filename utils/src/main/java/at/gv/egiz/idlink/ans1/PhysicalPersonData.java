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
package at.gv.egiz.idlink.ans1;

import iaik.asn1.*;

/**
 * This class represents the ASN.1 version of the <code>PhysicalPersonData</code>
 * of an compressed identity link.
 * 
 * <pre>PhysicalPersonData ::= SEQUENCE { 
    baseId UTF8String, 
    givenName UTF8String, 
    familyName UTF8String, 
    dateOfBirth UTF8String
}</pre>
 * 
 * @author mivkovic@egiz.gv.at, mcentner@eigz.gv.at
 */
public class PhysicalPersonData implements ASN1Type {

  /**
   * <code>baseId</code>
   */
  private String baseId; // UTF8String
  
  /**
   * <code>givenName</code>
   */
  private String givenName; // UTF8String
  
  /**
   * <code>familyName</code>
   */
  private String familyName; // UTF8String
  
  /**
   * <code>dataOfBirth</code>
   */
  private String dateOfBirth; // UTF8String

  /**
   * Creates a new <code>PhysicalPersonData</code> with the
   * given <code>baseId</code>, <code>givenName</code>, <code>familyName</code>
   * and <code>dataOfBirth</code>.
   * 
   * @param baseId
   * @param givenName
   * @param familyName
   * @param dateOfBirth
   */
  public PhysicalPersonData(String baseId, String givenName, String familyName, String dateOfBirth) {
    this.baseId = baseId;
    this.givenName = givenName;
    this.familyName = familyName;
    this.dateOfBirth = dateOfBirth;
  }

  /**
   * Creates a new <code>PhysicalPersonData</code> from its ASN.1 representation.
   * 
   * @param obj
   * 
   * @throws CodingException
   */
  public PhysicalPersonData(ASN1Object obj) throws CodingException {
    decode(obj);
  }

  @Override
  public void decode(ASN1Object obj) throws CodingException {
    try {
      baseId = (String) ((ASN1Object) obj.getComponentAt(0)).getValue();
      givenName = (String) ((ASN1Object) obj.getComponentAt(1)).getValue();
      familyName = (String) ((ASN1Object) obj.getComponentAt(2)).getValue();
      dateOfBirth = (String) ((ASN1Object) obj.getComponentAt(3)).getValue();
    } catch (Exception ex) {
      throw new CodingException(ex.toString());
    }
  }

  @Override
  public ASN1Object toASN1Object() {
    SEQUENCE ppd = new SEQUENCE();
    ppd.addComponent(new UTF8String(baseId));
    ppd.addComponent(new UTF8String(givenName));
    ppd.addComponent(new UTF8String(familyName));
    ppd.addComponent(new UTF8String(dateOfBirth));
    return ppd;
  }

  /**
   * Returns the DER encoding of this <code>PhysicalPersonData</code>.
   * 
   * @return the DER encoding of this <code>PhysicalPersonData</code>
   */
  public byte[] toByteArray() {
    return DerCoder.encode(toASN1Object());
  }

  /**
   * @return the baseId
   */
  public String getBaseId() {
    return baseId;
  }

  /**
   * @return the givenName
   */
  public String getGivenName() {
    return givenName;
  }

  /**
   * @return the familyName
   */
  public String getFamilyName() {
    return familyName;
  }

  /**
   * @return the dateOfBirth
   */
  public String getDateOfBirth() {
    return dateOfBirth;
  }
  
}