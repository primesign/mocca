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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.buergerkarte.namespaces.cardchannel.CommandAPDUType;
import at.buergerkarte.namespaces.cardchannel.ResetType;
import at.buergerkarte.namespaces.cardchannel.ScriptType;
import at.buergerkarte.namespaces.cardchannel.VerifyAPDUType;
import at.buergerkarte.namespaces.securitylayer._1.Base64XMLContentType;
import at.buergerkarte.namespaces.securitylayer._1.InfoboxUpdateRequestType;
import at.gv.egiz.bku.slcommands.InfoboxUpdateCommand;
import at.gv.egiz.bku.slcommands.SLCommandContext;
import at.gv.egiz.bku.slcommands.SLResult;
import at.gv.egiz.bku.slexceptions.SLCommandException;
import at.gv.egiz.bku.slexceptions.SLExceptionMessages;

public class InfoboxUpdateCommandImpl extends
    SLCommandImpl<InfoboxUpdateRequestType> implements InfoboxUpdateCommand {
  
  private static Log log = LogFactory.getLog(InfoboxUpdateCommandImpl.class);

  public static final String INFOBOX_IDENTIFIER_CARD_CHANNEL = "CardChannel";

  protected String infoboxIdentifier;
  
  protected List<Object> cardChannelScript;
  
  @Override
  public String getInfoboxIdentifier() {
    return infoboxIdentifier;
  }
  
  @Override
  public void init(SLCommandContext ctx, Object request)
      throws SLCommandException {
    super.init(ctx, request);
    
    InfoboxUpdateRequestType req = getRequestValue();
    
    infoboxIdentifier = req.getInfoboxIdentifier();
    
    if (INFOBOX_IDENTIFIER_CARD_CHANNEL.equals(infoboxIdentifier)) {

      if (req.getAssocArrayParameters() != null) {
        log.info("Got AssocArrayParameters but Infobox type is BinaryFile.");
        throw new SLCommandException(4010);
      }
      
      Base64XMLContentType binaryFileParameters = req.getBinaryFileParameters();
      if (binaryFileParameters == null) {
        log.info("Got no BinaryFileParameters but Infobox type is BinaryFile.");
        throw new SLCommandException(4010);
      }

      if (binaryFileParameters.getBase64Content() == null) {
        log.info("Got Base64Content but ContentIsXMLEntity is true.");
        throw new SLCommandException(4010);
      }
      
      List<Object> content = binaryFileParameters.getXMLContent().getContent();
      if (content.isEmpty()) {
        log.info("Got no XMLContent but ContentIsXMLEntity is true.");
        throw new SLCommandException(4010);
      }
      
      for (Object element : content) {
        if (!(element instanceof ScriptType)) {
          log.info("Infobox identifier is '" + infoboxIdentifier + "' but XMLContent does not contain 'Script'.");
          throw new SLCommandException(4010);
        }
        
        setCardChannelScript(((ScriptType) element).getResetOrCommandAPDUOrVerifyAPDU());
      }
      
      if (getCardChannelScript() == null) {
        log.info("Infobox identifier is '" + infoboxIdentifier + "' but XMLContent does not contain 'Script'.");
        throw new SLCommandException(4010);
      }
      
    } else {
      throw new SLCommandException(4002,
          SLExceptionMessages.EC4002_INFOBOX_UNKNOWN,
          new Object[] { infoboxIdentifier });
    }
    
  }

  public List<Object> getCardChannelScript() {
    return cardChannelScript;
  }

  public void setCardChannelScript(List<Object> cardChannelScript) {
    this.cardChannelScript = cardChannelScript;
  }

  @Override
  public SLResult execute() {
    
    try {
      if (INFOBOX_IDENTIFIER_CARD_CHANNEL.equals(getInfoboxIdentifier())) {
        
        executeCardChannelScript();
        return new InfoboxUpdateResultImpl();
        
      } else {
        throw new SLCommandException(4002,
            SLExceptionMessages.EC4002_INFOBOX_UNKNOWN,
            new Object[] { infoboxIdentifier });
      }
    } catch (SLCommandException e) {
      return new ErrorResultImpl(e, cmdCtx.getLocale());
    }
    
  }
  
  protected void executeCardChannelScript() throws SLCommandException {
    
    if (cardChannelScript != null) {
      
      for (Object element : cardChannelScript) {
        if (element instanceof ResetType) {
          
        } else if (element instanceof CommandAPDUType) {
          
        } else if (element instanceof VerifyAPDUType) {
          
        }
      }
      
    }
    
  }

  @Override
  public String getName() {
    return "InfoboxUpdateRequest";
  }

}
