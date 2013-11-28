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

import at.buergerkarte.namespaces.securitylayer._1_2_3.InfoboxReadRequestType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.InfoboxUpdateRequestType;
import at.gv.egiz.bku.slcommands.InfoboxReadResult;
import at.gv.egiz.bku.slcommands.InfoboxUpdateResult;
import at.gv.egiz.bku.slcommands.SLCommandContext;
import at.gv.egiz.bku.slexceptions.SLCommandException;

/**
 * An abstract base class for {@link Infobox} implementations.
 * 
 * @author mcentner
 */
public abstract class AbstractInfoboxImpl implements Infobox {

  @Override
  public InfoboxReadResult read(InfoboxReadRequestType request,
      SLCommandContext cmdCtx) throws SLCommandException {
    throw new SLCommandException(4011);
  }

  @Override
  public InfoboxUpdateResult update(InfoboxUpdateRequestType request,
      SLCommandContext cmdCtx) throws SLCommandException {
    throw new SLCommandException(4011);
  }
  
}
