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

import at.buergerkarte.namespaces.securitylayer._1.InfoboxReadRequestType;
import at.gv.egiz.bku.slcommands.InfoboxReadResult;
import at.gv.egiz.bku.slcommands.SLCommandContext;
import at.gv.egiz.bku.slexceptions.SLCommandException;

/**
 * An implementation of this interface represents a infobox as defined in
 * Security-Layer 1.2.
 * 
 * @author mcentner
 */
public interface Infobox {

  /**
   * @return the identifier of this infobox
   */
  public String getIdentifier();

  /**
   * Read data from this infobox.
   * 
   * @param request
   *          the InfoboxReadRequest
   * @param cmdCtx
   *          the command context
   * 
   * @return the data read from this infobox as InfoboxReadResult
   * 
   * @throws SLCommandException
   *           if reading from this infobox fails
   */
  public InfoboxReadResult read(InfoboxReadRequestType request,
      SLCommandContext cmdCtx) throws SLCommandException;

}
