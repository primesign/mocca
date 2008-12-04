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
package at.gv.egiz.bku.slcommands.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.buergerkarte.namespaces.securitylayer._1.InfoboxAssocArrayPairType;
import at.buergerkarte.namespaces.securitylayer._1.InfoboxReadDataAssocArrayType;
import at.buergerkarte.namespaces.securitylayer._1.InfoboxReadParamsAssocArrayType;
import at.buergerkarte.namespaces.securitylayer._1.InfoboxReadRequestType;
import at.buergerkarte.namespaces.securitylayer._1.ObjectFactory;
import at.buergerkarte.namespaces.securitylayer._1.XMLContentType;
import at.buergerkarte.namespaces.securitylayer._1.InfoboxReadParamsAssocArrayType.ReadKeys;
import at.buergerkarte.namespaces.securitylayer._1.InfoboxReadParamsAssocArrayType.ReadPairs;
import at.buergerkarte.namespaces.securitylayer._1.InfoboxReadParamsAssocArrayType.ReadValue;
import at.gv.egiz.bku.slcommands.InfoboxReadResult;
import at.gv.egiz.bku.slcommands.SLCommandContext;
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
  private static Log log = LogFactory.getLog(AbstractAssocArrayInfobox.class);

  /**
   * The search string pattern.
   */
  public static final String SEARCH_STRING_PATTERN = ".&&[^/](/.&&[^/])*";
  
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
    
    if (Pattern.matches(SEARCH_STRING_PATTERN, searchString)) {
      
//      for (int i = 0; i < searchString.length(); i++) {
//        int codePoint = searchString.codePointAt(i);
//        
//      }
      
      // TODO : build pattern
      return Collections.emptyList();
    } else {
      log.info("Got invalid search string '" + searchString + "'");
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
      log.info("Got valuesAreXMLEntities=" + readPairs + " but infobox type is binary.");
      throw new SLCommandException(4010);
    }
    
    if (!readPairs.isValuesAreXMLEntities() && isValuesAreXMLEntities()) {
      log.info("Got valuesAreXMLEntities=" + readPairs + " but infobox type is XML.");
      throw new SLCommandException(4010);
    }

    List<String> selectedKeys = selectKeys(readPairs.getSearchString());
    
    if (readPairs.isUserMakesUnique() && selectedKeys.size() > 1) {
      log.info("UserMakesUnique not supported");
      // TODO: give more specific error message
      throw new SLCommandException(4010);
    }
    
    ObjectFactory objectFactory = new ObjectFactory();

    InfoboxReadDataAssocArrayType infoboxReadDataAssocArrayType = objectFactory.createInfoboxReadDataAssocArrayType();

    Map<String, Object> values = getValues(selectedKeys, cmdCtx);
    for (String key : selectedKeys) {
      InfoboxAssocArrayPairType infoboxAssocArrayPairType = objectFactory.createInfoboxAssocArrayPairType();
      infoboxAssocArrayPairType.setKey(key);
      Object value = values.get(key);
      if (value instanceof byte[]) {
        infoboxAssocArrayPairType.setBase64Content((byte[]) value);
      } else {
        infoboxAssocArrayPairType.setXMLContent((XMLContentType) value);
      }
      infoboxReadDataAssocArrayType.getPair().add(infoboxAssocArrayPairType);
    }
    
    return new InfoboxReadResultImpl(infoboxReadDataAssocArrayType);
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
      log.info("Got valuesAreXMLEntities=" + readValue + " but infobox type is binary.");
      throw new SLCommandException(4010);
    }
    
    if (!readValue.isValueIsXMLEntity() && isValuesAreXMLEntities()) {
      log.info("Got valuesAreXMLEntities=" + readValue + " but infobox type is XML.");
      throw new SLCommandException(4010);
    }
    
    List<String> selectedKeys;

    if (Arrays.asList(getKeys()).contains(readValue.getKey())) {
      selectedKeys = Collections.singletonList(readValue.getKey());
    } else {
      selectedKeys = Collections.emptyList();
    }
    
    ObjectFactory objectFactory = new ObjectFactory();

    InfoboxReadDataAssocArrayType infoboxReadDataAssocArrayType = objectFactory.createInfoboxReadDataAssocArrayType();
    
    Map<String, Object> values = getValues(selectedKeys, cmdCtx);
    for (String key : selectedKeys) {
      InfoboxAssocArrayPairType infoboxAssocArrayPairType = objectFactory.createInfoboxAssocArrayPairType();
      infoboxAssocArrayPairType.setKey(key);
      Object value = values.get(key);
      if (value instanceof byte[]) {
        infoboxAssocArrayPairType.setBase64Content((byte[]) value);
      } else {
        infoboxAssocArrayPairType.setXMLContent((XMLContentType) value);
      }
      infoboxReadDataAssocArrayType.getPair().add(infoboxAssocArrayPairType);
    }
    
    return new InfoboxReadResultImpl(infoboxReadDataAssocArrayType);
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
