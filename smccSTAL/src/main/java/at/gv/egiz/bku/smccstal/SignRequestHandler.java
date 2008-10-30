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

import at.gv.egiz.bku.gui.BKUGUIFacade;
import java.awt.event.ActionEvent;
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
import at.gv.egiz.smcc.LockedException;
import at.gv.egiz.smcc.NotActivatedException;
import at.gv.egiz.smcc.PINProvider;
import at.gv.egiz.smcc.PINSpec;
import at.gv.egiz.smcc.SignatureCard;
import at.gv.egiz.smcc.SignatureCardException;
import at.gv.egiz.smcc.SignatureCard.KeyboxName;
import at.gv.egiz.stal.ErrorResponse;
import at.gv.egiz.stal.HashDataInput;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.SignRequest;
import at.gv.egiz.stal.SignResponse;
import at.gv.egiz.stal.signedinfo.ObjectFactory;
import at.gv.egiz.stal.signedinfo.SignedInfoType;
import at.gv.egiz.stal.util.JCEAlgorithmNames;
import java.awt.event.ActionListener;
import java.security.DigestException;
import java.util.List;

public abstract class SignRequestHandler extends AbstractRequestHandler implements HashDataInputDisplay {

    private static Log log = LogFactory.getLog(SignRequestHandler.class);
    private static JAXBContext jaxbContext;
    
    static {
        try {
            jaxbContext = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
        } catch (JAXBException e) {
            log.fatal("Cannot init jaxbContext", e);
        }
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
                JAXBElement<SignedInfoType> si = (JAXBElement<SignedInfoType>) unmarshaller.unmarshal(is);
                String signatureMethod = si.getValue().getSignatureMethod().getAlgorithm();
                log.debug("Found signature method: " + signatureMethod);
                String jceName = JCEAlgorithmNames.getJCEHashName(signatureMethod);
                if (jceName == null) {
                    log.error("Hash algorithm not supported:");
                    return new ErrorResponse(4006);
                }
                MessageDigest md = MessageDigest.getInstance(jceName);
                md.update(signReq.getSignedInfo());
                KeyboxName kb = SignatureCard.KeyboxName.getKeyboxName(signReq.getKeyIdentifier());
                byte[] resp = card.createSignature(md.digest(), kb, new STALPinProvider(si.getValue()));
                if (resp == null) {
                    return new ErrorResponse(6001);
                }
                SignResponse stalResp = new SignResponse();
                stalResp.setSignatureValue(resp);
                return stalResp;
            } catch (NotActivatedException e) {
              log.info("Citizen card not activated.", e);
              return new ErrorResponse(6001);
            } catch (LockedException e) {
              log.info("Citizen card locked.", e);
              return new ErrorResponse(6001);
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

//    @Override
//    public String providePIN(PINSpec spec, int retries) {
//        if (retryCounter++ > 0) {
//            log.info("PIN wrong retrying ...");
//            gui.showSignaturePINRetryDialog(spec, retries, this, "sign", this,
//              "cancel", this, "hashData");
//        } else {
//            gui.showSignaturePINDialog(spec, this, "sign", this, "cancel", this,
//              "hashData");
//        }
//        do {
//            waitForAction();
//            if (actionCommand.equals("cancel")) {
//                return null;
//            } else if (actionCommand.equals("hashData")) {
//                if (signedInfo != null) {
//                    try {
//                        gui.showWaitDialog(null);
//                        if (hashDataInputs == null || hashDataInputs.size() == 0) {
//                          HashMap<String, ReferenceType> signedReferences = new HashMap<String, ReferenceType>();
//                          for (ReferenceType reference : signedInfo.getReference()) {
//                            //don't get Manifest, QualifyingProperties, ...
//                            if (reference.getType() == null) {
//                              signedReferences.put(reference.getId(), reference);
//                            }
//                          }
//                          hashDataInputs = getHashDataInputs(signedReferences.keySet());
//                          for (HashDataInput hashDataInput : hashDataInputs) {
//                            ReferenceType reference = signedReferences.get(hashDataInput.getReferenceId());
//                            String algorithm = reference.getDigestMethod().getAlgorithm();
//                            MessageDigest md = MessageDigest.getInstance(algorithm);
//                            DigestInputStream dis = new DigestInputStream(hashDataInput.getHashDataInput(), md);
//                            while(dis.read() != -1) ;
//                            byte[] digestValue = md.digest();
//                            boolean valid = reference.getDigestValue().equals(digestValue);
//                          }
//                        }
//                        gui.showHashDataInputDialog(hashDataInputs, this, "ok");
//                    } catch (Exception ex) {
//                        //FIXME localize messages
//                        log.error("Failed to obtain HashDataInputs: " + ex.getMessage());
//                        gui.showErrorDialog("Failed to obtain HashDataInputs: " + ex.getMessage(), this, "ok");
//                    }
//                } else {
//                    //FIXME get all hashdatainputs
//                    gui.showErrorDialog("Failed to obtain HashDataInputs: No dsig:SignedInfo provided.", this, "ok");
//                }
//            } else if (actionCommand.equals("sign")) {
//                return new String(gui.getPin());
//            } else if (actionCommand.equals("ok")) {
//                gui.showSignaturePINDialog(spec, this, "sign", this, "cancel", this,
//                  "hashData");
//            }
//        } while (true);
//    }

//    @Override
//    public SMCCSTALRequestHandler newInstance() {
//        return new SignRequestHandler();
//    }

    

    
  class STALPinProvider implements PINProvider, ActionListener {
    
    protected SignedInfoType signedInfo;
    protected List<HashDataInput> hashDataInputs;
    private int retryCounter = 0;

    public STALPinProvider(SignedInfoType signedInfo) {
      this.signedInfo = signedInfo;
    }
    
    private void showSignaturePINDialog(PINSpec spec, int retries) {
      if (retryCounter > 0) {
          gui.showSignaturePINRetryDialog(spec, retries, SignRequestHandler.this, "sign", SignRequestHandler.this,
            "cancel", SignRequestHandler.this, "hashData");
        } else {
          gui.showSignaturePINDialog(spec, SignRequestHandler.this, "sign", SignRequestHandler.this, "cancel", SignRequestHandler.this,
            "hashData");
        }
    }
  
    @Override
    public String providePIN(PINSpec spec, int retries) {
    
      showSignaturePINDialog(spec, retries);
      
    do {
      waitForAction();
      gui.showWaitDialog(null);
      if (actionCommand.equals("cancel")) {
        return null;
      } else if (actionCommand.equals("hashData")) {
        
        showSignaturePINDialog(spec, retries);
          
          try {
            displayHashDataInputs(signedInfo.getReference());
          } catch (DigestException ex) { 
            log.error("Bad digest value: " + ex.getMessage());
            gui.showErrorDialog(BKUGUIFacade.ERR_INVALID_HASH, new Object[] {ex.getMessage()});
          } catch (Exception ex) {
            log.error("Could not display hashdata inputs: " + ex.getMessage());
            gui.showErrorDialog(BKUGUIFacade.ERR_DISPLAY_HASHDATA, new Object[] {ex.getMessage()}, SignRequestHandler.this, "ok");
          }
        
        // OLD HASHDATA DISPLAY (in applet), 
        // register SignRequestHandler.this as hashdataListener to use
//        if (signedInfo != null) {
//          try {
//            if (hashDataInputs == null || hashDataInputs.size() == 0) {
//              hashDataInputs = getCashedHashDataInputs(signedInfo.getReference());
//            }
//            gui.showHashDataInputDialog(hashDataInputs, SignRequestHandler.this, "ok");
//          } catch (DigestException ex) { 
//            log.error("Bad digest value: " + ex.getMessage());
//            gui.showErrorDialog(BKUGUIFacade.ERR_INVALID_HASH, new Object[] {ex.getMessage()});
//          } catch (Exception ex) {
//            //FIXME localize messages
//            log.error("Failed to obtain HashDataInputs: " + ex.getMessage());
//            gui.showErrorDialog(BKUGUIFacade.ERR_NO_HASHDATA, new Object[] {ex.getMessage()}, SignRequestHandler.this, "ok");
//          }
//        } else {
//          //FIXME get all hashdatainputs
//          gui.showErrorDialog(BKUGUIFacade.ERR_NO_HASHDATA, new Object[] {"No dsig:SignedInfo provided"}, SignRequestHandler.this, "ok");
//        }
      } else if (actionCommand.equals("sign")) {
        retryCounter++;
        return new String(gui.getPin());
      } else if (actionCommand.equals("ok")) {
        showSignaturePINDialog(spec, retries);
      }
    } while (true);
  }

    @Override
    public void actionPerformed(ActionEvent e) {
      throw new UnsupportedOperationException("Not supported yet.");
    }
  }
}
