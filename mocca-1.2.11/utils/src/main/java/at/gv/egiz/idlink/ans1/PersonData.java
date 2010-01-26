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
 * This class represents the ASN.1 version of the <code>PersonData</code>
 * of a compressed identity link.
 * 
 * <pre>
PersonData ::= CHOICE { 
    physcialPerson [0] PhysicalPersonData, 
    corporateBody [1] CorporateBodyData 
} </pre>
 * 
 * @author mivkovic@egiz.gv.at, mcentner@egiz.gv.at
 *
 */
public class PersonData implements ASN1Type {

  /**
   * <code>physicalPerson</code>
   */
  private PhysicalPersonData physicalPerson; // PhysicalPersonData

  /**
   * Creates a new <code>PersonData</code> with the given 
   * <code>physicalPersonData</code>.
   * 
   * @param physicalPersonData
   */
  public PersonData(PhysicalPersonData physicalPersonData) {
    physicalPerson = physicalPersonData;
  }

  /**
   * Creates a new <code>PersonData</code> from its ASN.1 representation.
   * 
   * @param obj
   * @throws CodingException
   */
  public PersonData(ASN1Object obj) throws CodingException {
    decode(obj);
  }

  @Override
  public void decode(ASN1Object obj) throws CodingException {
    try {
      physicalPerson = new PhysicalPersonData(obj);
    } catch (Exception ex) {
      throw new CodingException(ex.toString());
    }
  }

  @Override
  public ASN1Object toASN1Object() {
    return physicalPerson.toASN1Object();
  }

  /**
   * Returns the DER encoded representation of this <code>PersonData</code>.
   * 
   * @return the DER encoded representation of this <code>PersonData</code>
   */
  public byte[] getEncoded() {
    return DerCoder.encode(toASN1Object());
  }

  /**
   * @return the physicalPerson
   */
  public PhysicalPersonData getPhysicalPerson() {
    return physicalPerson;
  }
  
}