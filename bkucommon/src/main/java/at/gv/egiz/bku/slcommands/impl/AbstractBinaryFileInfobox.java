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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.buergerkarte.namespaces.securitylayer._1.InfoboxReadParamsBinaryFileType;
import at.buergerkarte.namespaces.securitylayer._1.InfoboxReadRequestType;

/**
 * An abstract base class for {@link Infobox} implementations of type binary file.
 * 
 * @author mcentner
 */
public abstract class AbstractBinaryFileInfobox extends AbstractInfoboxImpl implements BinaryFileInfobox {

  /**
   * Logging facility.
   */
  private static Log log = LogFactory.getLog(AbstractBinaryFileInfobox.class);
  
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
      log.debug("Got ContentIsXMLEntity=" + isXMLEntity + ".");
    }
    
  }
  

}
