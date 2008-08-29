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
package at.gv.egiz.bku.smccstal;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.smcc.CancelledException;
import at.gv.egiz.smcc.PINProvider;
import at.gv.egiz.smcc.PINSpec;
import at.gv.egiz.smcc.SignatureCard;
import at.gv.egiz.smcc.SignatureCardException;
import at.gv.egiz.smcc.SignatureCard.KeyboxName;
import at.gv.egiz.stal.ErrorResponse;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.SignRequest;
import at.gv.egiz.stal.SignResponse;
import at.gv.egiz.stal.signedinfo.ObjectFactory;
import at.gv.egiz.stal.signedinfo.SignedInfoType;
import at.gv.egiz.stal.util.JCEAlgorithmNames;

public class SignRequestHandler extends AbstractRequestHandler implements
    PINProvider {
  private static Log log = LogFactory.getLog(SignRequestHandler.class);

  private static JAXBContext jaxbContext;

  static {
    try {
      jaxbContext = JAXBContext.newInstance(ObjectFactory.class.getPackage()
          .getName());
    } catch (JAXBException e) {
      log.fatal("Cannot init jaxbContext", e);
    }
  }

  private int retryCounter = 0;

  public SignRequestHandler() {
  }

  @SuppressWarnings("unchecked")
  @Override
  public STALResponse handleRequest(STALRequest request) {
    if (request instanceof SignRequest) {
      SignRequest signReq = (SignRequest) request;
      newSTALMessage("Message.RequestCaption", "Message.SignRequest");
      try {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        InputStream is = new ByteArrayInputStream(signReq.getSignedInfo());
        JAXBElement<SignedInfoType> signedInfo = (JAXBElement<SignedInfoType>) unmarshaller
            .unmarshal(is);
        String signatureMethod = signedInfo.getValue().getSignatureMethod()
            .getAlgorithm();
        log.debug("Found signature method: " + signatureMethod);
        String jceName = JCEAlgorithmNames.getJCEHashName(signatureMethod);
        if (jceName == null) {
          log.error("Hash algorithm not supported:");
          return new ErrorResponse(1000);
        }
        MessageDigest md = MessageDigest.getInstance(jceName);
        md.update(signReq.getSignedInfo());
        KeyboxName kb = SignatureCard.KeyboxName.getKeyboxName(signReq
            .getKeyIdentifier());
        byte[] resp = card.createSignature(md.digest(), kb, this);
        if (resp == null) {
          return new ErrorResponse(6001);
        }
        SignResponse stalResp = new SignResponse();
        stalResp.setSignatureValue(resp);
        return stalResp;
      } catch (CancelledException cx) {
        log.debug("User cancelled request");
        return new ErrorResponse(6001);
      } catch (SignatureCardException e) {
        log.error("Error while creating signature: " + e);
        return new ErrorResponse(4000);
      } catch (JAXBException e) {
        log.error("Cannot unmarshall signed info", e);
        return new ErrorResponse(1000);
      } catch (NoSuchAlgorithmException e) {
        log.error(e);
        return new ErrorResponse(1000);
      }
    } else {
      log.fatal("Got unexpected STAL request: " + request);
      return new ErrorResponse(1000);
    }
  }

  @Override
  public boolean requireCard() {
    return true;
  }

  @Override
  public String providePIN(PINSpec spec, int retries) {
    if (retryCounter++ > 0) {
      log.info("PIN wrong retrying ...");
      gui.showSignaturePINRetryDialog(spec, retries, this, "sign", this,
          "cancel", this, "hashData");
    } else {
      gui.showSignaturePINDialog(spec, this, "sign", this, "cancel", this,
          "hashData");
    }
    do {
      waitForAction();
      if (actionCommand.equals("cancel")) {
        return null;
      } else if (actionCommand.equals("hashData")) {
        // FIXME provide hashdata input
        gui.showHashDataInputDialog(null, this, "ok");
      } else if (actionCommand.equals("sign")) {
        return new String(gui.getPin());
      } else if (actionCommand.equals("ok")) {
        gui.showSignaturePINDialog(spec, this, "sign", this, "cancel", this,
        "hashData"); 
      }
    } while (true);
  }

  @Override
  public SMCCSTALRequestHandler newInstance() {
    return new SignRequestHandler();
  }
}
