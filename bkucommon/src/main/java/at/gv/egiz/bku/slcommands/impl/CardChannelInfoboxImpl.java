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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.WeakHashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.buergerkarte.namespaces.cardchannel.ATRType;
import at.buergerkarte.namespaces.cardchannel.CommandAPDUType;
import at.buergerkarte.namespaces.cardchannel.ObjectFactory;
import at.buergerkarte.namespaces.cardchannel.ResetType;
import at.buergerkarte.namespaces.cardchannel.ResponseAPDUType;
import at.buergerkarte.namespaces.cardchannel.ResponseType;
import at.buergerkarte.namespaces.cardchannel.ScriptType;
import at.buergerkarte.namespaces.cardchannel.VerifyAPDUType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.Base64XMLContentType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.InfoboxReadRequestType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.InfoboxUpdateRequestType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.XMLContentType;
import at.gv.egiz.bku.slcommands.InfoboxReadResult;
import at.gv.egiz.bku.slcommands.InfoboxUpdateResult;
import at.gv.egiz.bku.slcommands.SLCommandContext;
import at.gv.egiz.bku.slexceptions.SLCommandException;
import at.gv.egiz.bku.slexceptions.SLRuntimeException;
import at.gv.egiz.stal.STAL;
import at.gv.egiz.stal.ext.APDUScriptRequest;
import at.gv.egiz.stal.ext.APDUScriptResponse;
import at.gv.egiz.stal.ext.APDUScriptRequest.RequestScriptElement;
import at.gv.egiz.stal.ext.APDUScriptResponse.ResponseScriptElement;

public class CardChannelInfoboxImpl extends AbstractBinaryFileInfobox {
  
  private final Logger log = LoggerFactory.getLogger(CardChannelInfoboxImpl.class);
  
  private static WeakHashMap<STAL, JAXBElement<ResponseType>> scriptResults = new WeakHashMap<STAL, JAXBElement<ResponseType>>();
  
  private static JAXBContext jaxbContext; 
  
  static {
    try {
      jaxbContext = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
    } catch (JAXBException e) {
      throw new SLRuntimeException("Failed to initalize CardChannel infobox.", e);
    }
  }

  public CardChannelInfoboxImpl() {
    isXMLEntity = true;
  }
  
  @Override
  public String getIdentifier() {
    return "CardChannel";
  }

  @Override
  public InfoboxReadResult read(InfoboxReadRequestType request,
      SLCommandContext cmdCtx) throws SLCommandException {
    
    at.buergerkarte.namespaces.securitylayer._1_2_3.ObjectFactory objectFactory
    = new at.buergerkarte.namespaces.securitylayer._1_2_3.ObjectFactory();

    Base64XMLContentType content = objectFactory.createBase64XMLContentType();
    XMLContentType xmlContent = objectFactory.createXMLContentType();
    content.setXMLContent(xmlContent);

    JAXBElement<ResponseType> response = scriptResults.get(cmdCtx.getSTAL());
    if (response != null) {
      xmlContent.getContent().add(response);
    }

    return new InfoboxReadResultImpl(content);
    
  }

  @SuppressWarnings("unchecked")
  @Override
  public InfoboxUpdateResult update(InfoboxUpdateRequestType request,
      SLCommandContext cmdCtx) throws SLCommandException {

    Base64XMLContentType binaryFileParameters = request.getBinaryFileParameters();
    
    if (binaryFileParameters.getBase64Content() != null) {
      log.info("Got Base64Content but ContentIsXMLEntity is true.");
      throw new SLCommandException(4010);
    }
    
    XMLContentType content = binaryFileParameters.getXMLContent();
    if (content instanceof at.gv.egiz.slbinding.impl.XMLContentType) {

      ByteArrayOutputStream redirectedStream = ((at.gv.egiz.slbinding.impl.XMLContentType) content).getRedirectedStream();
      if (redirectedStream != null) {

        if (log.isDebugEnabled()) {

          StringBuilder sb = new StringBuilder();
          sb.append("CardChannel script:\n");
          try {
            sb.append(new String(redirectedStream.toByteArray(), "UTF-8"));
          } catch (UnsupportedEncodingException e) {
            sb.append(e.getMessage());
          }
          log.debug(sb.toString());
        }
        
        Object object;
        try {
          Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
          object = unmarshaller.unmarshal(new ByteArrayInputStream(redirectedStream.toByteArray()));
        } catch (JAXBException e) {
          log.info("Failed to parse CardChannel script.", e);
          throw new SLCommandException(4011);
        }
        
        if (object instanceof JAXBElement) {
          executeCardChannelScript(((JAXBElement<ScriptType>) object).getValue(), cmdCtx);
          return new InfoboxUpdateResultImpl();
        }
        
      }

    
    }
    log.info("Infobox identifier is '{}' but XMLContent does not contain 'Script'.", getIdentifier());
    throw new SLCommandException(4010);
    
  }

  protected void executeCardChannelScript(ScriptType script,
      SLCommandContext cmdCtx) throws SLCommandException {
    
    List<Object> resetOrCommandAPDUOrVerifyAPDU = script.getResetOrCommandAPDUOrVerifyAPDU();
    List<RequestScriptElement> requestScript = new ArrayList<RequestScriptElement>();
    
    for (Object element : resetOrCommandAPDUOrVerifyAPDU) {
      
      if (element instanceof ResetType) {
      
        requestScript.add(new APDUScriptRequest.Reset());
      
      } else if (element instanceof CommandAPDUType) {
      
        CommandAPDUType commandAPDU = (CommandAPDUType) element;
        int sequence = (commandAPDU.getSequence() != null) 
              ? commandAPDU.getSequence().intValue() 
              : 0;

        requestScript.add(
            new APDUScriptRequest.Command(
                sequence, 
                commandAPDU.getValue(), 
                commandAPDU.getExpectedSW()));
        
      } else if (element instanceof VerifyAPDUType) {
        log.warn("CardChannel script command 'VerifyAPDU' not implemented.");
        throw new SLCommandException(4011);
      }
    }
    
    APDUScriptRequest scriptRequest = new APDUScriptRequest(requestScript);
    
    STAL stal = cmdCtx.getSTAL();
    STALHelper helper = new STALHelper(stal);
    
    helper.transmitSTALRequest(Collections.singletonList(scriptRequest));
    
    List<ResponseScriptElement> responseScript = ((APDUScriptResponse) helper
        .nextResponse(APDUScriptResponse.class)).getScript();
    
    ObjectFactory objectFactory = new ObjectFactory();
    
    ResponseType responseType = objectFactory.createResponseType();
    
    
    for (ResponseScriptElement element : responseScript) {
      
      if (element instanceof APDUScriptResponse.ATR) {
        
        byte[] atr = ((APDUScriptResponse.ATR) element).getAtr();
        
        ATRType atrType = objectFactory.createATRType();
        atrType.setValue(atr);
        atrType.setRc(BigInteger.ZERO);
        responseType.getATROrResponseAPDU().add(atrType);
        
      } else if (element instanceof APDUScriptResponse.Response) {
        
        APDUScriptResponse.Response response = (APDUScriptResponse.Response) element;
        
        ResponseAPDUType responseAPDUType = objectFactory.createResponseAPDUType();
        responseAPDUType.setSequence(BigInteger.valueOf(response.getSequence()));
        responseAPDUType.setRc(BigInteger.valueOf(response.getRc()));
        responseAPDUType.setSw(response.getSw());
        responseAPDUType.setValue(response.getApdu());
        
        responseType.getATROrResponseAPDU().add(responseAPDUType);
      }
      
    }
    
    scriptResults.put(stal, objectFactory.createResponse(responseType));
  }

  
}
