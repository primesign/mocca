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