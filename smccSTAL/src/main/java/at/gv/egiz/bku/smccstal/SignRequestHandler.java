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


package at.gv.egiz.bku.smccstal;

import iaik.me.asn1.ASN1;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SignatureException;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.bku.gui.BKUGUIFacade;
import at.gv.egiz.bku.pin.gui.SignPINGUI;
import at.gv.egiz.smcc.CancelledException;
import at.gv.egiz.smcc.LockedException;
import at.gv.egiz.smcc.NotActivatedException;
import at.gv.egiz.smcc.SignatureCard;
import at.gv.egiz.smcc.SignatureCard.KeyboxName;
import at.gv.egiz.smcc.SignatureCardException;
import at.gv.egiz.smcc.TimeoutException;
import at.gv.egiz.stal.ErrorResponse;
import at.gv.egiz.stal.HashDataInput;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.SignRequest;
import at.gv.egiz.stal.SignResponse;
import at.gv.egiz.stal.signedinfo.CanonicalizationMethodType;
import at.gv.egiz.stal.signedinfo.DigestMethodType;
import at.gv.egiz.stal.signedinfo.ObjectFactory;
import at.gv.egiz.stal.signedinfo.ReferenceType;
import at.gv.egiz.stal.signedinfo.SignatureMethodType;
import at.gv.egiz.stal.signedinfo.SignedInfoType;

public class SignRequestHandler extends AbstractRequestHandler {

    private final static Logger log = LoggerFactory.getLogger(SignRequestHandler.class);

    private final static String CMS_DEF_SIGNEDINFO_ID = "SignedInfo-1";
    private final static String OID_MESSAGEDIGEST = "1.2.840.113549.1.9.4";

    private static JAXBContext jaxbContext;

    static {
        try {
          jaxbContext = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
        } catch (JAXBException e) {
          Logger log = LoggerFactory.getLogger(SignRequestHandler.class);
          log.error("Cannot init jaxbContext", e);
        }
    }

    protected SecureViewer secureViewer;
    
    public SignRequestHandler(SecureViewer secureViewer) {
      this.secureViewer = secureViewer;
    }

    private static ErrorResponse errorResponse(int errorCode, String errorMessage, Exception e)
    {
      log.error(errorMessage, e);
      ErrorResponse err = new ErrorResponse(errorCode);
      err.setErrorMessage(errorMessage + (e == null ? "" : " " + e));
      return err;
    }

    @Override
    public STALResponse handleRequest(STALRequest request) throws InterruptedException {
        if (request instanceof SignRequest) {
            SignRequest signReq = (SignRequest) request;
            byte[] signedInfoData = signReq.getSignedInfo().getValue();
            try {
                SignedInfoType signedInfo;
                if (signReq.getSignedInfo().isIsCMSSignedAttributes()) {
                  signedInfo = createCMSSignedInfo(signReq);
                } else {
                  Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                  InputStream is = new ByteArrayInputStream(signedInfoData);
                  @SuppressWarnings("unchecked")
                  JAXBElement<SignedInfoType> si =
                      (JAXBElement<SignedInfoType>) unmarshaller.unmarshal(is);
                  signedInfo = si.getValue();
                }
                String signatureMethod = signedInfo.getSignatureMethod().getAlgorithm();
                log.debug("Found signature method: {}.", signatureMethod);
                KeyboxName kb = SignatureCard.KeyboxName.getKeyboxName(signReq.getKeyIdentifier());

                byte[] resp = card.createSignature(new ByteArrayInputStream(signedInfoData), kb,
                        new SignPINGUI(gui, secureViewer, signedInfo), signatureMethod);
                if (resp == null) {
                    return errorResponse(6001, "Response is null", null);
                }
                SignResponse stalResp = new SignResponse();
                stalResp.setSignatureValue(resp);
                return stalResp;
            } catch (NotActivatedException e) {
              gui.showErrorDialog(BKUGUIFacade.ERR_CARD_NOTACTIVATED, null, this, null);
              waitForAction();
              gui.showMessageDialog(BKUGUIFacade.TITLE_WAIT,
                      BKUGUIFacade.MESSAGE_WAIT);
              return errorResponse(6001, "Citizen card not activated.", e);
            } catch (LockedException e) {
              gui.showErrorDialog(BKUGUIFacade.ERR_CARD_LOCKED, null, this, null);
              waitForAction();
              gui.showMessageDialog(BKUGUIFacade.TITLE_WAIT,
                      BKUGUIFacade.MESSAGE_WAIT);
              return errorResponse(6001, "Citizen card locked.", e);
            } catch (CancelledException cx) {
                return errorResponse(6001, "User cancelled request.", null);
            } catch (TimeoutException ex) {
              gui.showMessageDialog(BKUGUIFacade.TITLE_ENTRY_TIMEOUT,
                      BKUGUIFacade.ERR_PIN_TIMEOUT, null,
                      BKUGUIFacade.BUTTON_CANCEL, this, null);
              waitForAction();
              gui.showMessageDialog(BKUGUIFacade.TITLE_WAIT,
                      BKUGUIFacade.MESSAGE_WAIT);
              return errorResponse(6001, "Timeout during pin entry.", null);
            } catch (SignatureCardException e) {
                return errorResponse(4000,"Error while creating signature.", e);
            } catch (JAXBException e) {
                return errorResponse(1000, "Cannot unmarshal signed info.", e);
            } catch (IOException e) {
              return errorResponse(4000, "Error while creating signature.", e);
            } catch (SignatureException e) {
              return errorResponse(4000, "Error while parsing CMS signature.", e);
            } 
        } else {
            return errorResponse(1000, "Got unexpected STAL request: " + request + ".", null);
        }
    }

    private static SignedInfoType createCMSSignedInfo(SignRequest signReq) throws SignatureException {
      SignedInfoType signedInfo = new SignedInfoType();
      byte[] signedInfoData = signReq.getSignedInfo().getValue();

      CanonicalizationMethodType canonicalizationMethod =
          new CanonicalizationMethodType();
      canonicalizationMethod.setAlgorithm("");
      signedInfo.setCanonicalizationMethod(canonicalizationMethod);

      SignatureMethodType signatureMethod = new SignatureMethodType();
      signatureMethod.setAlgorithm(signReq.getSignatureMethod());
      signedInfo.setSignatureMethod(signatureMethod);

      signedInfo.setId(CMS_DEF_SIGNEDINFO_ID);

      List<ReferenceType> references = signedInfo.getReference();
      ReferenceType reference = new ReferenceType();
      reference.setId(HashDataInput.CMS_DEF_REFERENCE_ID);
      DigestMethodType digestMethod = new DigestMethodType();
      digestMethod.setAlgorithm(signReq.getDigestMethod());
      reference.setDigestMethod(digestMethod);
      byte[] messageDigest = null;
      try {
        ASN1 signedAttributes = new ASN1(signedInfoData);
        if (!signedAttributes.isConstructed())
          throw new SignatureException("Error while parsing CMS signature");
        for (int i = 0; i < signedAttributes.getSize(); ++i) {
          ASN1 signedAttribute = signedAttributes.getElementAt(i);
          if (!signedAttribute.isConstructed())
            throw new SignatureException("Error while parsing CMS signature");
          ASN1 oid = signedAttribute.getElementAt(0);
          if (oid.gvObjectId().equals(OID_MESSAGEDIGEST)) {
            ASN1 value = signedAttribute.getElementAt(1);
            if (!value.isConstructed())
              throw new SignatureException("Error while parsing CMS signature");
            messageDigest = value.getElementAt(0).gvByteArray();
            break;
          }
        }
      } catch (IOException e) {
        throw new SignatureException(e);
      }
      reference.setDigestValue(messageDigest);
      if (signReq.getExcludedByteRange() != null) {
        // Abuse URI to store ExcludedByteRange
        String range = "CMSExcludedByteRange:" +
            signReq.getExcludedByteRange().getFrom() + "-" +
            signReq.getExcludedByteRange().getTo();
        reference.setURI(range);
      }
      references.add(reference);
      return signedInfo;
    }

    @Override
    public boolean requireCard() {
        return true;
    }

}
