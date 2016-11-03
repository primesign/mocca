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

import iaik.security.ec.common.ECStandardizedParameterFactory;
import iaik.security.ec.errorhandling.InvalidCurveParameterSpecException;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.ECField;
import java.security.spec.ECFieldF2m;
import java.security.spec.ECFieldFp;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.EllipticCurve;
import java.util.Enumeration;

import javax.xml.bind.JAXBElement;

import org.w3._2000._09.xmldsig_.DSAKeyValueType;
import org.w3._2000._09.xmldsig_.RSAKeyValueType;
import org.w3._2001._04.xmldsig_more_.BasePointParamsType;
import org.w3._2001._04.xmldsig_more_.CharTwoFieldElemType;
import org.w3._2001._04.xmldsig_more_.CurveParamsType;
import org.w3._2001._04.xmldsig_more_.DomainParamsType;
import org.w3._2001._04.xmldsig_more_.DomainParamsType.NamedCurve;
import org.w3._2001._04.xmldsig_more_.ECDSAKeyValueType;
import org.w3._2001._04.xmldsig_more_.ECPointType;
import org.w3._2001._04.xmldsig_more_.ExplicitParamsType;
import org.w3._2001._04.xmldsig_more_.FieldElemType;
import org.w3._2001._04.xmldsig_more_.FieldParamsType;
import org.w3._2001._04.xmldsig_more_.PnBFieldParamsType;
import org.w3._2001._04.xmldsig_more_.PrimeFieldElemType;
import org.w3._2001._04.xmldsig_more_.PrimeFieldParamsType;
import org.w3._2001._04.xmldsig_more_.TnBFieldParamsType;

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
    } else if (publicKey instanceof ECPublicKey) {
      ECDSAKeyValueType keyValueType = createKeyValueType((ECPublicKey) publicKey);
      return ecFactory.createECDSAKeyValue(keyValueType);
    } else if ("EC".equals(publicKey.getAlgorithm())) {
      byte[] encoded = publicKey.getEncoded();
      try {
        ECPublicKey key = new iaik.security.ec.common.ECPublicKey(encoded);
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
  
  public ECDSAKeyValueType createKeyValueType(ECPublicKey publicKey) throws KeyTypeNotSupportedException {
    
    ECDSAKeyValueType keyValueType = ecFactory.createECDSAKeyValueType();

    ECParameterSpec params = publicKey.getParams();
    if (params != null) {
      keyValueType.setDomainParameters(createDomainParamsType(params));
    }

    if (!publicKey.getW().equals(java.security.spec.ECPoint.POINT_INFINITY)) {
      keyValueType.setPublicKey(createPointType(publicKey.getW(), params.getCurve().getField()));
    }
    
    return keyValueType;
  }
  
  public ECPointType createPointType(ECPoint point, ECField field) throws KeyTypeNotSupportedException {
    ECPointType pointType = ecFactory.createECPointType();
    pointType.setX(createFieldElemType(point.getAffineX(), field));
    pointType.setY(createFieldElemType(point.getAffineY(), field));
    return pointType;
  }
  
  public FieldElemType createFieldElemType(BigInteger point, ECField field) throws KeyTypeNotSupportedException {
    if (field instanceof ECFieldFp) {
      PrimeFieldElemType fieldElemType = ecFactory.createPrimeFieldElemType();
      fieldElemType.setValue(point);
      return fieldElemType;
    } else if (field instanceof ECFieldF2m) {
      CharTwoFieldElemType fieldElemType = ecFactory.createCharTwoFieldElemType();
      fieldElemType.setValue(bigInteger2byteArray(point));
      return fieldElemType;
    } else {
      throw new KeyTypeNotSupportedException("Field element type not supported.");
    }
  }
  
  public FieldParamsType createFieldParamsType(ECField field) throws KeyTypeNotSupportedException {
    
    if (field instanceof ECFieldFp) {
      // PrimeFieldParamsType
      PrimeFieldParamsType primeFieldParamsType = ecFactory.createPrimeFieldParamsType();
      primeFieldParamsType.setP(((ECFieldFp) field).getP());
      return primeFieldParamsType;
    } else if (field instanceof ECFieldF2m) {
      // CharTwoFieldParamsType
      ECFieldF2m fieldf2m = (ECFieldF2m) field;
      int[] ks = fieldf2m.getMidTermsOfReductionPolynomial();

      // detect if trinomial or pentanomial base is present...
      switch (ks.length) {
      case 1:
        // trinomial base
        TnBFieldParamsType tnBFieldParamsType = ecFactory.createTnBFieldParamsType();
        tnBFieldParamsType.setM(BigInteger.valueOf(fieldf2m.getM()));
        tnBFieldParamsType.setK(BigInteger.valueOf(ks[0]));
        return tnBFieldParamsType;

      case 3:
        // pentanomial base
        PnBFieldParamsType pnBFieldParamsType = ecFactory.createPnBFieldParamsType();
        pnBFieldParamsType.setM(BigInteger.valueOf(fieldf2m.getM()));
        pnBFieldParamsType.setK1(BigInteger.valueOf(ks[0]));
        pnBFieldParamsType.setK2(BigInteger.valueOf(ks[1]));
        pnBFieldParamsType.setK3(BigInteger.valueOf(ks[2]));
        return pnBFieldParamsType;
      
      default:
        throw new KeyTypeNotSupportedException("Only trinomial and pentanomial base is supported.");
      }
      
    } else {
      throw new KeyTypeNotSupportedException("Field element type not supported.");
    }
    
  }

  private boolean fieldsEqual(ECField f1, ECField f2) {
    if (f1 instanceof ECFieldF2m) {
      if (!(f2 instanceof ECFieldF2m)) {
        return false;
      }
      ECFieldF2m f2m1 = (ECFieldF2m) f1;
      ECFieldF2m f2m2 = (ECFieldF2m) f2;
      return (f2m1.getM() == f2m2.getM() && f2m1.getReductionPolynomial().equals(f2m2.getReductionPolynomial()));
    } else if (f1 instanceof ECFieldFp) {
      if (!(f2 instanceof ECFieldFp)) {
        return false;
      }
      ECFieldFp fp1 = (ECFieldFp) f1;
      ECFieldFp fp2 = (ECFieldFp) f2;
      return (fp1.getP().equals(fp2.getP()));
    }
    return false;
  }

  private boolean curvesEqual(EllipticCurve c1, EllipticCurve c2) {
    if (c1.getA().equals(c2.getA()) && c1.getB().equals(c2.getB()))
      return fieldsEqual(c1.getField(), c2.getField());
    return false;
  }

  private String findOID(ECParameterSpec params) {
    EllipticCurve curve = params.getCurve();
    Enumeration<String> oids = ECStandardizedParameterFactory.getPrimeCurveOIDs();
    while (oids.hasMoreElements()) {
      String oid = oids.nextElement();
      iaik.security.ec.common.ECParameterSpec params2 = ECStandardizedParameterFactory.getParametersByOID(oid);
      if (curvesEqual(curve, params2.getCurve())) {
        return oid;
      }
    }
    oids = ECStandardizedParameterFactory.getBinaryCurveOIDs();
    while (oids.hasMoreElements()) {
      String oid = oids.nextElement();
      iaik.security.ec.common.ECParameterSpec params2 = ECStandardizedParameterFactory.getParametersByOID(oid);
      if (curvesEqual(curve, params2.getCurve())) {
        return oid;
      }
    }
    return null;
  }

  public DomainParamsType createDomainParamsType(ECParameterSpec params) throws KeyTypeNotSupportedException {
    iaik.security.ec.common.ECParameterSpec params2;
    try {
      params2 = iaik.security.ec.common.ECParameterSpec.getParameterSpec(params);
    } catch (InvalidCurveParameterSpecException e) {
      throw new KeyTypeNotSupportedException(e);
    }
    DomainParamsType domainParamsType = ecFactory.createDomainParamsType();
    String oid = params2.getOID();
    if (oid == null) {
      oid = findOID(params);
    }
    if (oid != null) {
      // NamedCurve
      NamedCurve namedCurve = ecFactory.createDomainParamsTypeNamedCurve();
      namedCurve.setURN("urn:oid:" + oid);
      domainParamsType.setNamedCurve(namedCurve);
    } else {
      // Explicit parameters
      EllipticCurve curve = params.getCurve();

      ExplicitParamsType explicitParamsType = ecFactory.createExplicitParamsType();
      explicitParamsType.setFieldParams(createFieldParamsType(curve.getField()));

      CurveParamsType curveParamsType = ecFactory.createCurveParamsType();
      ECField field = params.getCurve().getField();

      // curve coefficients
      curveParamsType.setA(createFieldElemType(curve.getA(), field));
      curveParamsType.setB(createFieldElemType(curve.getB(), field));
      
      // seed
      if (curve.getSeed() != null)
        curveParamsType.setSeed(curve.getSeed());
      explicitParamsType.setCurveParams(curveParamsType);

      // BasePoint parameters
      BasePointParamsType basePointParamsType = ecFactory.createBasePointParamsType();
      if (!params.getGenerator().equals(ECPoint.POINT_INFINITY)) {
        basePointParamsType.setBasePoint(createPointType(params.getGenerator(), field));
      }
      basePointParamsType.setOrder(params.getOrder());
      basePointParamsType.setCofactor(BigInteger.valueOf(params.getCofactor()));
      explicitParamsType.setBasePointParams(basePointParamsType);
      domainParamsType.setExplicitParams(explicitParamsType);
    }

    return domainParamsType;
    
  }
  
}
