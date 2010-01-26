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

import java.math.BigInteger;

import iaik.asn1.*;

/**
 * This class implements the ASN.1 representation of the 
 * <code>CitizenPublicKey</code> of a compressed identity link.
 * 
 * <pre>CitizenPublicKey ::= CHOICE { 
    onToken [0] INTEGER, 
    referenceURL [1] UTF8String, 
    x509Data [2] SubjectPublicKeyInfo 
}  
</pre>
 * 
 * @author mivkovic@egiz.gv.at, mcentner@egiz.gv.at
 */
public class CitizenPublicKey implements ASN1Type {

  /**
   * <code>onToken</code>
   */
  private int onToken; // INTEGER

  /**
   * Creates a new <code>CitizenPublicKey</code> with the given 
   * <code>onToken</code> value.
   * 
   * @param onToken
   */
  public CitizenPublicKey(int onToken) {
    this.onToken = onToken;
  }

  /**
   * Creates a new <code>CitizenPublicKey</code> from the given ASN.1 representation.
   * 
   * @param obj
   * @throws CodingException
   */
  public CitizenPublicKey(ASN1Object obj) throws CodingException {
    decode(obj);
  }

  @Override
  public void decode(ASN1Object obj) throws CodingException {
    try {
       BigInteger Value = (BigInteger)(obj.getValue());
       onToken = Value.intValue();
    } catch (Exception ex) {
      throw new CodingException(ex.toString());
    }
  }

  @Override
  public ASN1Object toASN1Object() {
    INTEGER ot = new INTEGER(onToken);
    return ot;
  }

  /**
   * Returns the DER encoding of this <code>CitizenPublicKey</code>.
   * 
   * @return the DER encoding of this <code>CitizenPublicKey</code>
   */
  public byte[] getEncoded() {
    return DerCoder.encode(toASN1Object());
  }

  public int getOnToken() {
    return onToken;
  }

}