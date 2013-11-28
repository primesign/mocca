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

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.buergerkarte.namespaces.securitylayer._1_2_3.InfoboxAssocArrayPairType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.InfoboxReadDataAssocArrayType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.InfoboxReadParamsAssocArrayType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.InfoboxReadRequestType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.ObjectFactory;
import at.buergerkarte.namespaces.securitylayer._1_2_3.XMLContentType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.InfoboxReadParamsAssocArrayType.ReadKeys;
import at.buergerkarte.namespaces.securitylayer._1_2_3.InfoboxReadParamsAssocArrayType.ReadPairs;
import at.buergerkarte.namespaces.securitylayer._1_2_3.InfoboxReadParamsAssocArrayType.ReadValue;
import at.gv.egiz.bku.slcommands.InfoboxReadResult;
import at.gv.egiz.bku.slcommands.SLCommandContext;
import at.gv.egiz.bku.slcommands.SLMarshallerFactory;
import at.gv.egiz.bku.slexceptions.SLCommandException;

/**
 * An abstract base class for {@link Infobox} implementations of type associative array.
 * 
 * @author mcentner
 */
public abstract class AbstractAssocArrayInfobox extends AbstractInfoboxImpl
    implements AssocArrayInfobox {
  
  /**
   * Logging facility.
   */
  private final Logger log = LoggerFactory.getLogger(AbstractAssocArrayInfobox.class);

  /**
   * The search string pattern.
   */
  public static final String SEARCH_STRING_PATTERN = "(.&&[^/])+(/.&&[^/])*";
  
  /**
   * @return the keys available in this infobox.
   */
  public abstract String[] getKeys();
  
  /**
   * @return <code>true</code> if the values are XML entities, or <code>false</code> otherwise.
   */
  public abstract boolean isValuesAreXMLEntities();
  
  /**
   * Returns a key to value mapping for the given <code>keys</code>.
   * 
   * @param keys a list of keys
   * @param cmdCtx the command context
   * 
   * @return a key to value mapping for the given <code>keys</code>.
   * 
   * @throws SLCommandException if obtaining the values fails
   */
  public abstract Map<String, Object> getValues(List<String> keys, SLCommandContext cmdCtx) throws SLCommandException;

  /**
   * Returns all keys that match the given <code>searchString</code>.
   * 
   * @param searchString the search string 
   * 
   * @return all keys that match the given <code>searchString</code>
   * 
   * @throws SLCommandException if the given search string is invalid
   */
  protected List<String> selectKeys(String searchString) throws SLCommandException {
    
    if ("*".equals(searchString) || "**".equals(searchString)) {
      return Arrays.asList(getKeys());
    }
    
    if (!searchString.contains("*")) {
      Arrays.asList(getKeys()).contains(searchString);
      return Collections.singletonList(searchString);
    }
    
    if (Pattern.matches(SEARCH_STRING_PATTERN, searchString)) {
      
//      for (int i = 0; i < searchString.length(); i++) {
//        int codePoint = searchString.codePointAt(i);
//        
//      }
      
      // TODO : build pattern
      return Collections.emptyList();
    } else {
      log.info("Got invalid search string '{}'.", searchString);
      throw new SLCommandException(4010);
    }
    
  }

  /**
   * Read all keys specified by <code>readKeys</code>.
   * 
   * @param readKeys
   *          the ReadKeys element
   * @param cmdCtx
   *          the command context
   * @return a corresponding InfoboxReadResult
   * 
   * @throws SLCommandException
   *           if the ReadKeys element is invalid or obtaining the corresponding
   *           values fails
   */
  protected InfoboxReadResult readKeys(ReadKeys readKeys, SLCommandContext cmdCtx) throws SLCommandException {
    
    List<String> selectedKeys = selectKeys(readKeys.getSearchString());
    
    if (readKeys.isUserMakesUnique() && selectedKeys.size() > 1) {
      log.info("UserMakesUnique not supported");
      // TODO: give more specific error message
      throw new SLCommandException(4010);
    }
    
    ObjectFactory objectFactory = new ObjectFactory();
    
    InfoboxReadDataAssocArrayType infoboxReadDataAssocArrayType = objectFactory
        .createInfoboxReadDataAssocArrayType();

    List<String> keys = infoboxReadDataAssocArrayType.getKey();
    keys.addAll(selectedKeys);

    return new InfoboxReadResultImpl(infoboxReadDataAssocArrayType);
    
  }
  
  /**
   * Read all pairs specified by <code>readPairs</code>.
   * 
   * @param readPairs
   *          the readPairs element
   * @param cmdCtx
   *          the command context
   * @return a corresponding InfoboxReadResult
   * 
   * @throws SLCommandException
   *           if the ReadPairs element is invalid or obtaining the corresponding
   *           values fails
   */
  protected InfoboxReadResult readPairs(ReadPairs readPairs, SLCommandContext cmdCtx) throws SLCommandException {
    
    if (readPairs.isValuesAreXMLEntities() && !isValuesAreXMLEntities()) {
      log.info("Got valuesAreXMLEntities={} but infobox type is binary.", readPairs.isValuesAreXMLEntities());
      throw new SLCommandException(4010);
    }
    
    List<String> selectedKeys = selectKeys(readPairs.getSearchString());
    
    if (readPairs.isUserMakesUnique() && selectedKeys.size() > 1) {
      log.info("UserMakesUnique not supported.");
      // TODO: give more specific error message
      throw new SLCommandException(4010);
    }
    
    return new InfoboxReadResultImpl(marshallPairs(selectedKeys, getValues(
        selectedKeys, cmdCtx), readPairs.isValuesAreXMLEntities()));
  }
  
  /**
   * Read the value specified by <code>readPairs</code>.
   * 
   * @param readValue
   *          the readValue element
   * @param cmdCtx
   *          the command context
   * @return a corresponding InfoboxReadResult
   * 
   * @throws SLCommandException
   *           if the ReadValue element is invalid or obtaining the corresponding
   *           values fails
   */
  protected InfoboxReadResult readValue(ReadValue readValue, SLCommandContext cmdCtx) throws SLCommandException {
    
    if (readValue.isValueIsXMLEntity() && !isValuesAreXMLEntities()) {
      log.info("Got valuesAreXMLEntities={} but infobox type is binary.", readValue.isValueIsXMLEntity());
      throw new SLCommandException(4010);
    }
    
    List<String> selectedKeys;

    if (Arrays.asList(getKeys()).contains(readValue.getKey())) {
      selectedKeys = Collections.singletonList(readValue.getKey());
    } else {
      selectedKeys = Collections.emptyList();
    }
    
    return new InfoboxReadResultImpl(marshallPairs(selectedKeys, getValues(
        selectedKeys, cmdCtx), readValue.isValueIsXMLEntity()));
    
  }
  
  protected InfoboxReadDataAssocArrayType marshallPairs(List<String> selectedKeys, Map<String, Object> values, boolean areXMLEntities) throws SLCommandException {
    
    ObjectFactory objectFactory = new ObjectFactory();
    
    InfoboxReadDataAssocArrayType infoboxReadDataAssocArrayType = objectFactory.createInfoboxReadDataAssocArrayType();
    
    for (String key : selectedKeys) {
      InfoboxAssocArrayPairType infoboxAssocArrayPairType = objectFactory.createInfoboxAssocArrayPairType();
      infoboxAssocArrayPairType.setKey(key);
     
      Object value = values.get(key);
      if (areXMLEntities) {
        if (value instanceof byte[]) {
          log.info("Got valuesAreXMLEntities={} but infobox type is binary.", areXMLEntities);
          throw new SLCommandException(4122);
        } else {
          XMLContentType contentType = objectFactory.createXMLContentType();
          contentType.getContent().add(value);
          infoboxAssocArrayPairType.setXMLContent(contentType);
        }
      } else {
        infoboxAssocArrayPairType.setBase64Content((value instanceof byte[]) ? (byte[]) value : marshallValue(value));
      }
      
      infoboxReadDataAssocArrayType.getPair().add(infoboxAssocArrayPairType);
    }

    return infoboxReadDataAssocArrayType;
    
  }

  protected byte[] marshallValue(Object jaxbElement) throws SLCommandException {
    
    Marshaller marshaller = SLMarshallerFactory.getInstance().createMarshaller(false);
    ByteArrayOutputStream result = new ByteArrayOutputStream();
    try {
      marshaller.marshal(jaxbElement, result);
    } catch (JAXBException e) {
      log.info("Failed to marshall infobox content.", e);
      throw new SLCommandException(4122);
    }
    
    return result.toByteArray();
  
  }

  @Override
  public InfoboxReadResult read(InfoboxReadRequestType req,
      SLCommandContext cmdCtx) throws SLCommandException {

    InfoboxReadParamsAssocArrayType assocArrayParameters = req
        .getAssocArrayParameters();

    if (assocArrayParameters == null) {
      log.info("Infobox type is AssocArray but got no AssocArrayParameters.");
      throw new SLCommandException(4010);
    }

    if (assocArrayParameters.getReadKeys() != null) {
      return readKeys(assocArrayParameters.getReadKeys(), cmdCtx);
    }

    if (assocArrayParameters.getReadPairs() != null) {
      return readPairs(assocArrayParameters.getReadPairs(), cmdCtx);
    }

    // ReadValue
    if (assocArrayParameters.getReadValue() != null) {
      return readValue(assocArrayParameters.getReadValue(), cmdCtx);
    }

    log
        .info("Infobox type is AssocArray but got invalid AssocArrayParameters.");
    throw new SLCommandException(4010);
    
  }

}
