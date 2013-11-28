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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.buergerkarte.namespaces.securitylayer._1_2_3.InfoboxReadParamsBinaryFileType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.InfoboxReadRequestType;

/**
 * An abstract base class for {@link Infobox} implementations of type binary file.
 * 
 * @author mcentner
 */
public abstract class AbstractBinaryFileInfobox extends AbstractInfoboxImpl implements BinaryFileInfobox {

  /**
   * Logging facility.
   */
  private final Logger log = LoggerFactory.getLogger(AbstractBinaryFileInfobox.class);
  
  /**
   * Is this infobox' content an XML entity?
   */
  protected boolean isXMLEntity = false;
  
  /**
   * @return <code>true</code> if this infobox' content is an XML entity or <code>false</code> otherwise.
   */
  public boolean isXMLEntity() {
    return isXMLEntity;
  }

  /**
   * Sets the value returned by {@link #isXMLEntity()} according to the given
   * <code>request</code>.
   * 
   * @param request the InfoboxReadRequest
   */
  public void setIsXMLEntity(InfoboxReadRequestType request) {
    
    InfoboxReadParamsBinaryFileType binaryFileParameters = request.getBinaryFileParameters();
    if (binaryFileParameters != null) {
      isXMLEntity = binaryFileParameters.isContentIsXMLEntity();
      log.debug("Got ContentIsXMLEntity={}.", isXMLEntity);
    }
    
  }
  

}
