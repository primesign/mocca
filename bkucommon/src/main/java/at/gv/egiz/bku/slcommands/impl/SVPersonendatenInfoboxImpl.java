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


package at.gv.egiz.bku.slcommands.impl;

import iaik.asn1.ASN;
import iaik.asn1.ASN1Object;
import iaik.asn1.CodingException;
import iaik.asn1.DerCoder;
import iaik.asn1.NumericString;
import iaik.asn1.OCTET_STRING;
import iaik.asn1.ObjectID;
import iaik.asn1.SEQUENCE;
import iaik.asn1.SET;
import iaik.asn1.UNKNOWN;
import iaik.asn1.structures.ChoiceOfTime;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.buergerkarte.namespaces.cardchannel.AttributeList;
import at.buergerkarte.namespaces.cardchannel.AttributeType;
import at.buergerkarte.namespaces.cardchannel.ObjectFactory;
import at.gv.egiz.bku.slcommands.SLCommandContext;
import at.gv.egiz.bku.slexceptions.SLCommandException;
import at.gv.egiz.bku.slexceptions.SLExceptionMessages;
import at.gv.egiz.stal.InfoboxReadRequest;
import at.gv.egiz.stal.InfoboxReadResponse;
import at.gv.egiz.stal.STALRequest;

/**
 * An implementation of the {@link Infobox} <em>Certificates</em> as 
 * specified in Security Layer 1.2. 
 * 
 * @author mcentner
 */
public class SVPersonendatenInfoboxImpl extends AbstractAssocArrayInfobox {
  
  /**
   * Logging facility.
   */
  private final Logger log = LoggerFactory.getLogger(SVPersonendatenInfoboxImpl.class);

  public static final String EHIC = "EHIC";
  
  public static final String GRUNDDATEN = "Grunddaten";
  
  public static final String STATUS = "Status";
  
  public static final String SV_PERSONENBINDUNG = "SV-Personenbindung";
  
  /**
   * The valid keys.
   */
  public static final String[] KEYS = new String[] {
    GRUNDDATEN, EHIC, STATUS, SV_PERSONENBINDUNG
  };

  @Override
  public String getIdentifier() {
    return "SV-Personendaten";
  }

  @Override
  public String[] getKeys() {
    return KEYS;
  }

  @Override
  public boolean isValuesAreXMLEntities() {
    return true;
  }

  @Override
  public Map<String, Object> getValues(List<String> keys, SLCommandContext cmdCtx) throws SLCommandException {
    
    STALHelper stalHelper = new STALHelper(cmdCtx.getSTAL());
    
    if (keys != null && !keys.isEmpty()) {
      
      List<STALRequest> stalRequests = new ArrayList<STALRequest>();

      // get values
      InfoboxReadRequest infoboxReadRequest;
      for (int i = 0; i < keys.size(); i++) {
        infoboxReadRequest = new InfoboxReadRequest();
        infoboxReadRequest.setInfoboxIdentifier(keys.get(i));
        stalRequests.add(infoboxReadRequest);
      }

      stalHelper.transmitSTALRequest(stalRequests);

      Map<String, Object> values = new HashMap<String, Object>();

      try {
        for (int i = 0; i < keys.size(); i++) {
          
          String key = keys.get(i);
          InfoboxReadResponse nextResponse = (InfoboxReadResponse) stalHelper.nextResponse(InfoboxReadResponse.class);

          
          ObjectFactory objectFactory = new ObjectFactory();
          
          if (EHIC.equals(key)) {
            AttributeList attributeList = createAttributeList(nextResponse.getInfoboxValue());
            values.put(key, objectFactory.createEHIC(attributeList));
          } else if (GRUNDDATEN.equals(key)) {
            AttributeList attributeList = createAttributeList(nextResponse.getInfoboxValue());
            values.put(key, objectFactory.createGrunddaten(attributeList));
          } else if (SV_PERSONENBINDUNG.equals(key)) {
            values.put(key, objectFactory.createSVPersonenbindung(nextResponse.getInfoboxValue()));
          } else if (STATUS.equals(key)) {
            AttributeList attributeList = createAttributeListFromRecords(nextResponse.getInfoboxValue());
            values.put(key, objectFactory.createStatus(attributeList));
          }
          
        }
      } catch (CodingException e) {
        log.info("Failed to decode '{}' infobox.", getIdentifier(), e);
        throw new SLCommandException(4000,
            SLExceptionMessages.EC4000_UNCLASSIFIED_INFOBOX_INVALID,
            new Object[] { "IdentityLink" });

      }
      
      return values;
      
    } else {
      
      return new HashMap<String, Object>();
      
    }
    
    
  }
  
  public static AttributeList createAttributeList(byte[] infoboxValue) throws CodingException {
    
    ObjectFactory objectFactory = new ObjectFactory();
    
    ASN1Object asn1 = DerCoder.decode(infoboxValue);
    
    AttributeList attributeList = objectFactory.createAttributeList();
    List<AttributeType> attributes = attributeList.getAttribute();
    
    if (asn1.isA(ASN.SEQUENCE)) {
      for (int i = 0; i < ((SEQUENCE) asn1).countComponents(); i++) {

        AttributeType attributeType = objectFactory.createAttributeType();

        if (asn1.getComponentAt(i).isA(ASN.SEQUENCE)) {
          SEQUENCE attribute = (SEQUENCE) asn1.getComponentAt(i);
          if (attribute.getComponentAt(0).isA(ASN.ObjectID)) {
            ObjectID objectId = (ObjectID) attribute.getComponentAt(0);
            attributeType.setOid("urn:oid:" + objectId.getID());
          }
          if (attribute.getComponentAt(1).isA(ASN.SET)) {
            SET values = (SET) attribute.getComponentAt(1);
            for (int j = 0; j < values.countComponents(); j++) {
              setAttributeValue(attributeType, values.getComponentAt(j));
            }
          }
        }
        
        attributes.add(attributeType);
        
      }
      
    }
    
    return attributeList;
    
  }
  
  public static AttributeList createAttributeListFromRecords(byte[] infoboxValue) throws CodingException {
    
    ObjectFactory objectFactory = new ObjectFactory();

    AttributeList attributeList = objectFactory.createAttributeList();
    List<AttributeType> attributes = attributeList.getAttribute();

    byte[] records = infoboxValue;
    
    while (records != null && records.length > 0) {

      int length;
      
      if (records[0] != 0x00) {
        
        ASN1Object asn1 = DerCoder.decode(records);

        AttributeType attributeType = objectFactory.createAttributeType();

        if (asn1.isA(ASN.SEQUENCE)) {
          SEQUENCE attribute = (SEQUENCE) asn1;
          if (attribute.getComponentAt(0).isA(ASN.ObjectID)) {
            ObjectID objectId = (ObjectID) attribute.getComponentAt(0);
            attributeType.setOid("urn:oid:" + objectId.getID());
          }
          if (attribute.getComponentAt(1).isA(ASN.SET)) {
            SET values = (SET) attribute.getComponentAt(1);
            for (int j = 0; j < values.countComponents(); j++) {
              setAttributeValue(attributeType, values.getComponentAt(j));
            }
          }
        }
        
        attributes.add(attributeType);

        length = DerCoder.encode(asn1).length;

      } else {
        length = 1;
      }
      
      if (length < records.length) {
        records = Arrays.copyOfRange(records, length + 1, records.length);
      } else {
        records = null;
      }
      
    }
      
    return attributeList;
    
  }
  
  private static void setAttributeValue(AttributeType attributeType, ASN1Object value) {
    
    Logger log = LoggerFactory.getLogger(SVPersonendatenInfoboxImpl.class);
    
    if (value.isA(ASN.OCTET_STRING)) {
      
      try {
        byte[] octets = ((OCTET_STRING) value).getWholeValue();
        attributeType.setLatin1String(new String(octets, Charset.forName("ISO-8859-1")));
      } catch (IOException e) {
        log.info("Failed to set Latin1String.", e);
      }
      
    } else if (value.isA(ASN.NumericString)) {
      
      attributeType.setNumericString((String) ((NumericString) value).getValue());
      
    } else if (value.isA(ASN.GeneralizedTime)) {
      
      try {
        ChoiceOfTime choiceOfTime = new ChoiceOfTime(value);
        
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        gregorianCalendar.setTime(choiceOfTime.getDate());

        DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
        XMLGregorianCalendar xmlGregorianCalendar = datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
        xmlGregorianCalendar.setTimezone(0);
        
        attributeType.setGeneralizedTime(xmlGregorianCalendar);
      } catch (Exception e) {
        log.info("Failed to set GeneralizedTime.", e);
      }
      
    } else if (value.isA(ASN.INTEGER)) {
      
      attributeType.setInteger((BigInteger) value.getValue());
      
    } else if (value.isA(ASN.UTF8String)) {
      
      attributeType.setUTF8String((String) value.getValue());
      
    } else if (value.isA(ASN.PrintableString)) {
      
      attributeType.setPrintableString((String) value.getValue());
      
    } else if (value.isA(ASN.UNKNOWN)) {
      
      byte[] bytes = (byte[]) ((UNKNOWN) value).getValue();
      
      try {
        BigInteger bigInteger = new BigInteger(bytes);
        String string = bigInteger.toString(16);
        
        Date date = new SimpleDateFormat("yyyyMMdd").parse(string);
        attributeType.setDate(new SimpleDateFormat("yyyy-MM-dd").format(date));
      } catch (Exception e) {
        log.info("Failed to set Date.", e);
      }
    }
    
  }

    
    
  

}
