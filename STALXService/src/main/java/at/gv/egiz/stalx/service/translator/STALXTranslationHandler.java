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


package at.gv.egiz.stalx.service.translator;

import at.buergerkarte.namespaces.cardchannel.service.ATRType;
import at.buergerkarte.namespaces.cardchannel.service.CommandAPDUType;
import at.buergerkarte.namespaces.cardchannel.service.ObjectFactory;
import at.buergerkarte.namespaces.cardchannel.service.ResetType;
import at.buergerkarte.namespaces.cardchannel.service.ResponseAPDUType;
import at.buergerkarte.namespaces.cardchannel.service.ScriptType;
import at.buergerkarte.namespaces.cardchannel.service.VerifyAPDUType;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.ext.APDUScriptRequest;
import at.gv.egiz.stal.ext.APDUScriptRequest.RequestScriptElement;
import at.gv.egiz.stal.ext.APDUScriptResponse;
import at.gv.egiz.stal.service.translator.STALTranslator;
import at.gv.egiz.stal.service.translator.TranslationException;
import at.gv.egiz.stal.service.types.RequestType;
import at.gv.egiz.stal.service.types.ResponseType;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.JAXBElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class STALXTranslationHandler implements STALTranslator.TranslationHandler {

  private final Logger log = LoggerFactory.getLogger(STALXTranslationHandler.class);
  ObjectFactory of;

  public STALXTranslationHandler() {
    of = new ObjectFactory();
  }

  @Override
  public List<Class<?>> getSupportedTypes() {
    return Arrays.asList(new Class<?>[]{ScriptType.class,
              at.buergerkarte.namespaces.cardchannel.service.ResponseType.class,
              APDUScriptRequest.class,
              APDUScriptResponse.class});
  }

  @Override
  public JAXBElement<? extends RequestType> translate(STALRequest request) throws TranslationException {
    if (request instanceof APDUScriptRequest) {
      log.trace("Translate at.gv.egiz.stal.ext.APDUScriptRequest -> at.buergerkarte.namespaces.cardchannel.service.ScriptType.");

      ScriptType scriptT = of.createScriptType();

      List<RequestScriptElement> script = ((APDUScriptRequest) request).getScript();
      for (RequestScriptElement requestScriptElement : script) {
        if (requestScriptElement instanceof APDUScriptRequest.Reset) {
          scriptT.getResetOrCommandAPDUOrVerifyAPDU().add(of.createResetType());
        } else if (requestScriptElement instanceof APDUScriptRequest.Command) {
          APDUScriptRequest.Command cmd = (APDUScriptRequest.Command) requestScriptElement;
          CommandAPDUType commandAPDUType = of.createCommandAPDUType();
          commandAPDUType.setSequence(BigInteger.valueOf(cmd.getSequence()));
          commandAPDUType.setValue(cmd.getCommandAPDU());
          commandAPDUType.setExpectedSW(cmd.getExpectedSW());
          scriptT.getResetOrCommandAPDUOrVerifyAPDU().add(commandAPDUType);
        } else {
          log.error("Invalid requestScriptElement {}.", requestScriptElement.getClass());
          throw new TranslationException(requestScriptElement.getClass());
        }
      }

      return of.createScript(scriptT);
    } else {
      log.error("Cannot translate {}.", request.getClass());
      throw new TranslationException(request.getClass());
    }
  }

  @Override
  public STALRequest translate(RequestType request) throws TranslationException {
    if (request instanceof ScriptType) {

      log.trace("Translate at.buergerkarte.namespaces.cardchannel.service.ScriptType -> at.gv.egiz.stal.ext.APDUScriptRequest.");

      List<Object> resetOrCommandAPDUOrVerifyAPDU = ((ScriptType) request).getResetOrCommandAPDUOrVerifyAPDU();
      List<APDUScriptRequest.RequestScriptElement> requestScript = new ArrayList<APDUScriptRequest.RequestScriptElement>();

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
          log.error("CardChannel script command 'VerifyAPDU' not implemented.");
          throw new TranslationException(VerifyAPDUType.class);
        } else {
          log.error("Invalid requestScriptElement element {}.", element.getClass());
          throw new TranslationException(element.getClass());
        }
      }

      return new APDUScriptRequest(requestScript);

    } else {
      log.error("Cannot translate {}.", request.getClass());
      throw new TranslationException(request.getClass());
    }
  }

  @Override
  public JAXBElement<? extends ResponseType> translate(STALResponse response) throws TranslationException {
    if (response instanceof APDUScriptResponse) {
      log.trace("Translate at.gv.egiz.stal.ext.APDUScriptResponse -> at.buergerkarte.namespaces.cardchannel.service.ResponseType.");
      at.buergerkarte.namespaces.cardchannel.service.ResponseType responseT = of.createResponseType();
      List<APDUScriptResponse.ResponseScriptElement> responseScript = ((APDUScriptResponse) response).getScript();

      for (APDUScriptResponse.ResponseScriptElement element : responseScript) {

        if (element instanceof APDUScriptResponse.ATR) {

          byte[] atr = ((APDUScriptResponse.ATR) element).getAtr();

          ATRType atrType = of.createATRType();
          atrType.setValue(atr);
          atrType.setRc(BigInteger.ZERO);
          responseT.getATROrResponseAPDU().add(atrType);

        } else if (element instanceof APDUScriptResponse.Response) {

          APDUScriptResponse.Response resp = (APDUScriptResponse.Response) element;

          ResponseAPDUType responseAPDUType = of.createResponseAPDUType();
          responseAPDUType.setSequence(BigInteger.valueOf(resp.getSequence()));
          responseAPDUType.setRc(BigInteger.valueOf(resp.getRc()));
          responseAPDUType.setSW(resp.getSw());
          responseAPDUType.setValue(resp.getApdu());

          responseT.getATROrResponseAPDU().add(responseAPDUType);
        } else {
          log.error("Invalid responseScriptElement {}.", element.getClass());
          throw new TranslationException(element.getClass());
        }
      }
      return of.createResponse(responseT);
    } else {
      log.error("Cannot translate {}." + response.getClass());
      throw new TranslationException(response.getClass());
    }
  }

  @Override
  public STALResponse translate(ResponseType response) throws TranslationException {
    if (response instanceof at.buergerkarte.namespaces.cardchannel.service.ResponseType) {
      log.trace("Translate at.buergerkarte.namespaces.cardchannel.service.ResponseType -> at.gv.egiz.stal.ext.APDUScriptResponse.");

      List<Object> atrOrResponseAPDU = ((at.buergerkarte.namespaces.cardchannel.service.ResponseType) response).getATROrResponseAPDU();
      List<APDUScriptResponse.ResponseScriptElement> responseScript = new ArrayList<APDUScriptResponse.ResponseScriptElement>();

      for (Object object : atrOrResponseAPDU) {
        if (object instanceof ATRType) {
          byte[] atr = ((ATRType) object).getValue();
          responseScript.add(new APDUScriptResponse.ATR(atr));
        } else if (object instanceof ResponseAPDUType) {
          ResponseAPDUType respAPDU = (ResponseAPDUType) object;
          int sequence = (respAPDU.getSequence() != null)
                  ? respAPDU.getSequence().intValue()
                  : 0;
          int rc = (respAPDU.getRc() != null)
                  ? respAPDU.getRc().intValue()
                  : 0;
          responseScript.add(new APDUScriptResponse.Response(sequence,
                  respAPDU.getValue(),
                  respAPDU.getSW(),
                  rc));
        } else {
          log.error("Invalid responseScriptElement {}.", object.getClass());
          throw new TranslationException(object.getClass());
        }
      }
      return new APDUScriptResponse(responseScript);

    } else {
      log.error("Cannot translate {}.", response.getClass());
      throw new TranslationException(response.getClass());
    }
  }
}
