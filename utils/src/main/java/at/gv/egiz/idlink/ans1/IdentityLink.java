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

import iaik.asn1.ASN1Object;
import iaik.asn1.ASN1Type;
import iaik.asn1.BIT_STRING;
import iaik.asn1.CON_SPEC;
import iaik.asn1.CodingException;
import iaik.asn1.DerCoder;
import iaik.asn1.INTEGER;
import iaik.asn1.SEQUENCE;
import iaik.asn1.UTF8String;

/**
 * This class impelments an ASN.1 representation of the compressed <code>IdentiyLink</code>.
 * <pre>
PersonenBindung ::= SEQUENCE { 
    version INTEGER, 
    issuerTemplate UTF8String, 
    assertionID UTF8String, 
    issueInstant UTF8String, 
    personData PersonData, 
    citizenPublicKey SEQUENCE SIZE (1..MAX) OF CitizenPublicKey, 
    signatureValue BIT STRING, 
    referenceDigest [0] BIT STRING OPTIONAL, 
    referenceManifestDigest [1] BIT STRING OPTIONAL, 
    manifestReferenceDigest [2] BIT STRING OPTIONAL 
} 
</pre>
 * 
 * @author mivkovic@egiz.gv.at, mcentner@egiz.gv.at
 */
public class IdentityLink implements ASN1Type {

  private int version = 1; // INTEGER
  private String issuerTemplate; // UTF8String
  private String assertionID; // UTF8String
  private String issueInstant; // UTF8String
  private PersonData personData; // PersonData
  private CitizenPublicKey[] citizenPublicKeys; // SEQUENCE SIZE (1..MAX) OF
  private byte[] signatureValue; // BIT STRING
  private byte[] referenceDigest; // BIT STRING OPTIONAL
  private byte[] referenceManifestDigest; // BIT STRING OPTIONAL
  private byte[] manifestReferenceDigest; // BIT STRING OPTIONAL

  /**
   * Creates a new <code>IdentityLink</code> with the given
   * <code>issuerTemplate</code>, <code>assertionID</code>,
   * <code>issueInstant</code>, <code>personData</code>,
   * <code>citizenPublicKeys</code> and <code>signatureValue</code>.
   * 
   * @param issuerTemplate
   * @param assertionID
   * @param issueInstant
   * @param personData
   * @param citizenPublicKeys
   * @param signatureValue
   */
  public IdentityLink(String issuerTemplate, String assertionID,
      String issueInstant, PersonData personData,
      CitizenPublicKey[] citizenPublicKeys, byte[] signatureValue) {
    this.issuerTemplate = issuerTemplate;
    this.assertionID = assertionID;
    this.issueInstant = issueInstant;
    this.personData = personData;
    this.citizenPublicKeys = citizenPublicKeys;
    this.signatureValue = signatureValue;
  }
  
  /**
   * Create a new IdentityLink from an ASN1Object.
   * 
   * @param identiyLink
   * @throws CodingException
   */
  public IdentityLink(ASN1Object identiyLink) throws CodingException {
    decode(identiyLink);
  }
  
  @Override
  public void decode(ASN1Object obj) throws CodingException {
    issuerTemplate = (String) ((ASN1Object) obj.getComponentAt(1)).getValue();
    assertionID = (String) ((ASN1Object) obj.getComponentAt(2)).getValue();
    issueInstant = (String) ((ASN1Object) obj.getComponentAt(3)).getValue();

    if (((CON_SPEC) obj.getComponentAt(4)).getAsnType().getTag() == 0)
      personData = new PersonData((ASN1Object) obj.getComponentAt(4).getValue());
    else {
      throw new CodingException("CorporateBodyData currently not supported.");
    }

    SEQUENCE publicKeySequence = (SEQUENCE) obj.getComponentAt(5);
    int anz = publicKeySequence.countComponents();
    citizenPublicKeys = new CitizenPublicKey[anz];
    for (int i = 0; i < citizenPublicKeys.length; i++) {
      CON_SPEC tmp = (CON_SPEC) publicKeySequence.getComponentAt(i);
      if (tmp.getAsnType().getTag() == 0) {
        citizenPublicKeys[i] = new CitizenPublicKey((ASN1Object) tmp.getValue());
      } else {
        throw new CodingException(
            "Currently only PublicKeys on token are supported.");
      }
    }

    signatureValue = (byte[]) ((ASN1Object) obj.getComponentAt(6)).getValue();

    for (int i = 7; i < obj.countComponents(); i++) {
      CON_SPEC tmp = (CON_SPEC) obj.getComponentAt(i);
      switch (tmp.getAsnType().getTag()) {
      case 0:
        referenceDigest = (byte[]) ((BIT_STRING) tmp.getValue()).getValue();
        break;
      case 1:
        referenceManifestDigest = (byte[]) ((BIT_STRING) tmp.getValue())
            .getValue();
        break;
      case 2:
        manifestReferenceDigest = (byte[]) ((BIT_STRING) tmp.getValue())
            .getValue();
        break;
      }

    }

  }

  @Override
  public ASN1Object toASN1Object() {
    SEQUENCE pb = new SEQUENCE();
    pb.addComponent(new INTEGER(version));
    pb.addComponent(new UTF8String(issuerTemplate));
    pb.addComponent(new UTF8String(assertionID));
    pb.addComponent(new UTF8String(issueInstant));

    pb.addComponent(new CON_SPEC(0, personData.toASN1Object()));
    SEQUENCE seq = new SEQUENCE();
    for (int i = 0; i < citizenPublicKeys.length; i++) {
      seq.addComponent(new CON_SPEC(0, citizenPublicKeys[i].toASN1Object()));
    }
    pb.addComponent(seq);
    pb.addComponent(new BIT_STRING(signatureValue));
    if (referenceDigest != null)
      pb.addComponent(new CON_SPEC(0, new BIT_STRING(referenceDigest)));
    if (referenceManifestDigest != null)
      pb.addComponent(new CON_SPEC(1, new BIT_STRING(referenceManifestDigest)));
    if (manifestReferenceDigest != null)
      pb.addComponent(new CON_SPEC(2, new BIT_STRING(manifestReferenceDigest)));
    return pb;
  }

  /**
   * Returns the DER encoding of this <code>IdentityLink</code>.
   * 
   * @return the DER encoding of this <code>IdentityLink</code>
   */
  public byte[] toByteArray() {
    return DerCoder.encode(toASN1Object());
  }

  /**
   * @return the version
   */
  public int getVersion() {
    return version;
  }

  /**
   * @param version the version to set
   */
  public void setVersion(int version) {
    this.version = version;
  }

  /**
   * @return the issuerTemplate
   */
  public String getIssuerTemplate() {
    return issuerTemplate;
  }

  /**
   * @param issuerTemplate the issuerTemplate to set
   */
  public void setIssuerTemplate(String issuerTemplate) {
    this.issuerTemplate = issuerTemplate;
  }

  /**
   * @return the assertionID
   */
  public String getAssertionID() {
    return assertionID;
  }

  /**
   * @param assertionID the assertionID to set
   */
  public void setAssertionID(String assertionID) {
    this.assertionID = assertionID;
  }

  /**
   * @return the issueInstant
   */
  public String getIssueInstant() {
    return issueInstant;
  }

  /**
   * @param issueInstant the issueInstant to set
   */
  public void setIssueInstant(String issueInstant) {
    this.issueInstant = issueInstant;
  }

  /**
   * @return the personData
   */
  public PersonData getPersonData() {
    return personData;
  }

  /**
   * @param personData the personData to set
   */
  public void setPersonData(PersonData personData) {
    this.personData = personData;
  }

  /**
   * @return the citizenPublicKeys
   */
  public CitizenPublicKey[] getCitizenPublicKeys() {
    return citizenPublicKeys;
  }

  /**
   * @param citizenPublicKeys the citizenPublicKeys to set
   */
  public void setCitizenPublicKeys(CitizenPublicKey[] citizenPublicKeys) {
    this.citizenPublicKeys = citizenPublicKeys;
  }

  /**
   * @return the signatureValue
   */
  public byte[] getSignatureValue() {
    return signatureValue;
  }

  /**
   * @param signatureValue the signatureValue to set
   */
  public void setSignatureValue(byte[] signatureValue) {
    this.signatureValue = signatureValue;
  }

  /**
   * @return the referenceDigest
   */
  public byte[] getReferenceDigest() {
    return referenceDigest;
  }

  /**
   * @param referenceDigest the referenceDigest to set
   */
  public void setReferenceDigest(byte[] referenceDigest) {
    this.referenceDigest = referenceDigest;
  }

  /**
   * @return the referenceManifestDigest
   */
  public byte[] getReferenceManifestDigest() {
    return referenceManifestDigest;
  }

  /**
   * @param referenceManifestDigest the referenceManifestDigest to set
   */
  public void setReferenceManifestDigest(byte[] referenceManifestDigest) {
    this.referenceManifestDigest = referenceManifestDigest;
  }

  /**
   * @return the manifestReferenceDigest
   */
  public byte[] getManifestReferenceDigest() {
    return manifestReferenceDigest;
  }

  /**
   * @param manifestReferenceDigest the manifestReferenceDigest to set
   */
  public void setManifestReferenceDigest(byte[] manifestReferenceDigest) {
    this.manifestReferenceDigest = manifestReferenceDigest;
  }
  
}
