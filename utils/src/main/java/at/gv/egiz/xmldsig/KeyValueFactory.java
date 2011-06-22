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


package at.gv.egiz.xmldsig;

import iaik.security.ecc.interfaces.ECDSAParams;
import iaik.security.ecc.interfaces.ECDSAPublicKey;
import iaik.security.ecc.math.ecgroup.Coordinate;
import iaik.security.ecc.math.ecgroup.ECPoint;
import iaik.security.ecc.math.ecgroup.EllipticCurve;
import iaik.security.ecc.math.field.BinaryField;
import iaik.security.ecc.math.field.Field;
import iaik.security.ecc.math.field.FieldElement;
import iaik.security.ecc.math.field.PrimeField;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPublicKey;

import javax.xml.bind.JAXBElement;

import org.w3._2000._09.xmldsig_.DSAKeyValueType;
import org.w3._2000._09.xmldsig_.RSAKeyValueType;
import org.w3._2001._04.xmldsig_more_.BasePointParamsType;
import org.w3._2001._04.xmldsig_more_.CharTwoFieldElemType;
import org.w3._2001._04.xmldsig_more_.CurveParamsType;
import org.w3._2001._04.xmldsig_more_.DomainParamsType;
import org.w3._2001._04.xmldsig_more_.ECDSAKeyValueType;
import org.w3._2001._04.xmldsig_more_.ECPointType;
import org.w3._2001._04.xmldsig_more_.ExplicitParamsType;
import org.w3._2001._04.xmldsig_more_.FieldElemType;
import org.w3._2001._04.xmldsig_more_.FieldParamsType;
import org.w3._2001._04.xmldsig_more_.PnBFieldParamsType;
import org.w3._2001._04.xmldsig_more_.PrimeFieldElemType;
import org.w3._2001._04.xmldsig_more_.PrimeFieldParamsType;
import org.w3._2001._04.xmldsig_more_.TnBFieldParamsType;
import org.w3._2001._04.xmldsig_more_.DomainParamsType.NamedCurve;

public class KeyValueFactory {
  
  private static byte[] bigInteger2byteArray(BigInteger bigPositiveInt) {
    if (bigPositiveInt == null)
      throw new NullPointerException("Argument 'bigPositiveInt' must not be null");
    if (bigPositiveInt.signum() != 1)
      throw new IllegalArgumentException("Argument 'bigPositiveInt' must not be negative");

    byte[] byteRepresentation = bigPositiveInt.toByteArray();
    if (byteRepresentation[0] == 0) {
      byte[] oldByteRepresentation = byteRepresentation;
      byteRepresentation = new byte[oldByteRepresentation.length - 1];
      System.arraycopy(oldByteRepresentation, 1, byteRepresentation, 0, oldByteRepresentation.length - 1);
    }
    return byteRepresentation;
  }
  
  org.w3._2001._04.xmldsig_more_.ObjectFactory ecFactory = new org.w3._2001._04.xmldsig_more_.ObjectFactory();

  org.w3._2000._09.xmldsig_.ObjectFactory dsFactory = new org.w3._2000._09.xmldsig_.ObjectFactory();

  public JAXBElement<?> createKeyValue(PublicKey publicKey) throws KeyTypeNotSupportedException {
    
    if (publicKey instanceof RSAPublicKey) {
      RSAKeyValueType keyValueType = createRSAKeyValueType((RSAPublicKey) publicKey);
      return dsFactory.createRSAKeyValue(keyValueType);
    } else if (publicKey instanceof DSAPublicKey) {
      DSAKeyValueType keyValueType = createKeyValueType((DSAPublicKey) publicKey);
      return dsFactory.createDSAKeyValue(keyValueType);
    } else if (publicKey instanceof ECDSAPublicKey) {
      ECDSAKeyValueType keyValueType = createKeyValueType((ECDSAPublicKey) publicKey);
      return ecFactory.createECDSAKeyValue(keyValueType);
    } else if ("EC".equals(publicKey.getAlgorithm())) {
      byte[] encoded = publicKey.getEncoded();
      try {
        iaik.security.ecc.ecdsa.ECPublicKey key = new iaik.security.ecc.ecdsa.ECPublicKey(encoded);
        ECDSAKeyValueType keyValueType = createKeyValueType(key);
        return ecFactory.createECDSAKeyValue(keyValueType);
      } catch (InvalidKeyException e) {
        throw new KeyTypeNotSupportedException("Public key of type "
            + publicKey.getAlgorithm() + " (" + publicKey.getClass()
            + ") not supported.");
      }
    } else {
      throw new KeyTypeNotSupportedException("Public key of type "
          + publicKey.getAlgorithm() + " (" + publicKey.getClass()
          + ") not supported.");
    }
    
  }
  
  public RSAKeyValueType createRSAKeyValueType(RSAPublicKey publicKey) {
    
    RSAKeyValueType keyValueType = dsFactory.createRSAKeyValueType();
    keyValueType.setExponent(bigInteger2byteArray(publicKey.getPublicExponent()));
    keyValueType.setModulus(bigInteger2byteArray(publicKey.getModulus()));
    
    return keyValueType;
  }
  
  public DSAKeyValueType createKeyValueType(DSAPublicKey publicKey) {
    
    DSAKeyValueType keyValueType = dsFactory.createDSAKeyValueType();
    
    if (publicKey.getParams() != null) {
      // P, Q, G
      DSAParams params = publicKey.getParams();
      if (params.getP() != null && params.getQ() != null) {
        keyValueType.setP(bigInteger2byteArray(params.getP()));
        keyValueType.setQ(bigInteger2byteArray(params.getQ()));
      }
      if (params.getG() != null) {
        keyValueType.setG(bigInteger2byteArray(params.getG()));
      }
    }
    //
    keyValueType.setY(bigInteger2byteArray(publicKey.getY()));
    
    return keyValueType;
  }
  
  public ECDSAKeyValueType createKeyValueType(ECDSAPublicKey publicKey) throws KeyTypeNotSupportedException {
    
    ECDSAKeyValueType keyValueType = ecFactory.createECDSAKeyValueType();
    
    ECDSAParams params = publicKey.getParameter();
    if (params != null) {
      keyValueType.setDomainParameters(createDomainParamsType(params));
    }

    if (!publicKey.getW().isInfinity()) {
      keyValueType.setPublicKey(createPointType(publicKey.getW()));
    }
    
    return keyValueType;
  }
  
  public ECPointType createPointType(ECPoint point) throws KeyTypeNotSupportedException {
    ECPointType pointType = ecFactory.createECPointType();
    Coordinate affine = point.getCoordinates().toAffine();
    pointType.setX(createFieldElemType(affine.getX()));
    pointType.setY(createFieldElemType(affine.getY()));
    return pointType;
  }
  
  public FieldElemType createFieldElemType(FieldElement fieldElement) throws KeyTypeNotSupportedException {
    int fieldId = fieldElement.getField().getFieldId();
    if (fieldId == PrimeField.PRIME_FIELD_ID) {
      PrimeFieldElemType fieldElemType = ecFactory.createPrimeFieldElemType();
      fieldElemType.setValue(fieldElement.toBigInt());
      return fieldElemType;
    } else if (fieldId == BinaryField.BINARY_FIELD_ID) {
      CharTwoFieldElemType fieldElemType = ecFactory.createCharTwoFieldElemType();
      fieldElemType.setValue(fieldElement.toByteArray());
      return fieldElemType;
    } else {
      throw new KeyTypeNotSupportedException("Field element of type " + fieldId + " not supported.");
    }
  }
  
  public FieldParamsType createFieldParamsType(Field field) throws KeyTypeNotSupportedException {
    
    if (field.getFieldId() == PrimeField.PRIME_FIELD_ID) {
      // PrimeFieldParamsType
      PrimeFieldParamsType primeFieldParamsType = ecFactory.createPrimeFieldParamsType();
      primeFieldParamsType.setP(field.getSize());
      return primeFieldParamsType;
    } else if (field.getFieldId() == BinaryField.BINARY_FIELD_ID && field instanceof BinaryField) {
      // CharTwoFieldParamsType
      
      BinaryField binaryField = (BinaryField) field;
      int[] irreduciblePolynomial = binaryField.getIrreduciblePolynomial();

      // The irreducible polynomial as a BinaryFieldValue
      FieldElement irreducible = binaryField.newElement(irreduciblePolynomial);
      
      int order = binaryField.getOrder();
      int[] coeffPositions = new int[3];
      
      // Get coefficients of irreducible polynomial
      int coeffCount = 2;
      for (int i = 1; i < order -1; i++) {
        if (irreducible.testBit(i)) {
          coeffPositions[coeffCount - 2] = i;
          coeffCount++;
          if (coeffCount == 5)
            break;
        }
      }
      // detect if trinomial or pentanomial base is present...
      switch (coeffCount) {
      case 3:
        // trinomial base
        TnBFieldParamsType tnBFieldParamsType = ecFactory.createTnBFieldParamsType();
        tnBFieldParamsType.setM(BigInteger.valueOf(binaryField.getOrder()));
        tnBFieldParamsType.setK(BigInteger.valueOf(coeffPositions[0]));
        return tnBFieldParamsType;

      case 5:
        // pentanomial base
        PnBFieldParamsType pnBFieldParamsType = ecFactory.createPnBFieldParamsType();
        pnBFieldParamsType.setM(BigInteger.valueOf(binaryField.getOrder()));
        pnBFieldParamsType.setK1(BigInteger.valueOf(coeffPositions[0]));
        pnBFieldParamsType.setK2(BigInteger.valueOf(coeffPositions[1]));
        pnBFieldParamsType.setK3(BigInteger.valueOf(coeffPositions[2]));
        return pnBFieldParamsType;
      
      default:
        throw new KeyTypeNotSupportedException("Only trinomial and pentanomial base is supported.");
      }
      
    } else {
      throw new KeyTypeNotSupportedException("Field element of type " + field.getFieldId() + " not supported.");
    }
    
  }
  
  public DomainParamsType createDomainParamsType(ECDSAParams params) throws KeyTypeNotSupportedException {
    
    DomainParamsType domainParamsType = ecFactory.createDomainParamsType();
    EllipticCurve curve = params.getG().getCurve();
    
    String oid = params.getOID();
    if (oid !=  null) {
      // NamedCurve
      NamedCurve namedCurve = ecFactory.createDomainParamsTypeNamedCurve();
      namedCurve.setURN("urn:oid:" + oid);
      domainParamsType.setNamedCurve(namedCurve);
    } else {
      // Explicit parameters
      ExplicitParamsType explicitParamsType = ecFactory.createExplicitParamsType();
      explicitParamsType.setFieldParams(createFieldParamsType(curve.getField()));
      
      CurveParamsType curveParamsType = ecFactory.createCurveParamsType();
      
      // curve coefficients
      curveParamsType.setA(createFieldElemType(curve.getA()));
      curveParamsType.setB(createFieldElemType(curve.getB()));
      
      // seed
      if (params.getS() != null) {
        curveParamsType.setSeed(bigInteger2byteArray(params.getS()));
      }
      explicitParamsType.setCurveParams(curveParamsType);
      

      // BasePoint parameters
      BasePointParamsType basePointParamsType = ecFactory.createBasePointParamsType();
      if (!params.getG().isInfinity()) {
        basePointParamsType.setBasePoint(createPointType(params.getG()));
      }
      basePointParamsType.setOrder(params.getR());
      if(params.getK() != null) {
        basePointParamsType.setCofactor(params.getK());
      }
      explicitParamsType.setBasePointParams(basePointParamsType);
      
      domainParamsType.setExplicitParams(explicitParamsType);
    }

    return domainParamsType;
    
  }
  
}
