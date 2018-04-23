/*
 * Copyright 2015 Datentechnik Innovation GmbH and Prime Sign GmbH, Austria
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

import iaik.asn1.DerCoder;
import iaik.asn1.INTEGER;
import iaik.asn1.SEQUENCE;
import iaik.asn1.structures.AlgorithmID;
import iaik.cms.CMSException;
import iaik.cms.CMSSignatureException;
import iaik.utils.Util;

import java.math.BigInteger;
import java.security.InvalidParameterException;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.buergerkarte.namespaces.securitylayer._1_2_3.BulkRequestType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.BulkRequestType.CreateSignatureRequest;
import at.buergerkarte.namespaces.securitylayer._1_2_3.CreateCMSSignatureRequestType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.ExcludedByteRangeType;
import at.gv.egiz.bku.conf.MoccaConfigurationFacade;
import at.gv.egiz.bku.slcommands.BulkCommand;
import at.gv.egiz.bku.slcommands.SLCommandContext;
import at.gv.egiz.bku.slcommands.SLResult;
import at.gv.egiz.bku.slcommands.impl.cms.BulkCollectionSecurityProvider;
import at.gv.egiz.bku.slcommands.impl.cms.BulkSignature;
import at.gv.egiz.bku.slcommands.impl.cms.BulkSignatureInfo;
import at.gv.egiz.bku.slcommands.impl.cms.CMSHashDataInput;
import at.gv.egiz.bku.slcommands.impl.xsect.STALSignatureException;
import at.gv.egiz.bku.slexceptions.SLCommandException;
import at.gv.egiz.bku.slexceptions.SLException;
import at.gv.egiz.bku.slexceptions.SLRequestException;
import at.gv.egiz.bku.slexceptions.SLViewerException;
import at.gv.egiz.stal.BulkSignRequest;
import at.gv.egiz.stal.BulkSignResponse;
import at.gv.egiz.stal.ErrorResponse;
import at.gv.egiz.stal.HashDataInput;
import at.gv.egiz.stal.InfoboxReadRequest;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.SignRequest;
import at.gv.egiz.stal.SignRequest.SignedInfo;

/**
 * This class implements the security layer command <code>BulkRequest</code>.
 * 
 * @author szoescher
 */
public class BulkCommandImpl extends SLCommandImpl<BulkRequestType> implements BulkCommand {

  private final static String ID_ECSIGTYPE = "1.2.840.10045.4";

  /**
   * Logging facility.
   */
  private final static Logger log = LoggerFactory.getLogger(BulkCommandImpl.class);

  /**
   * The signing certificate.
   */
  protected X509Certificate signingCertificate;

  /**
   * The keybox identifier of the key used for signing.
   */
  protected String keyboxIdentifier;

  /**
   * The configuration facade used to access the MOCCA configuration.
   */
  private ConfigurationFacade configurationFacade = new ConfigurationFacade();

  private class ConfigurationFacade implements MoccaConfigurationFacade {
    private Configuration configuration;

    public static final String USE_STRONG_HASH = "UseStrongHash";

    public void setConfiguration(Configuration configuration) {
      this.configuration = configuration;
    }

    public boolean getUseStrongHash() {
      return configuration.getBoolean(USE_STRONG_HASH, true);
    }
  }

  @Override
  public String getName() {
    return "BulkRequest";
  }

  public void setConfiguration(Configuration configuration) {
    configurationFacade.setConfiguration(configuration);
  }

  @Override
  public SLResult execute(SLCommandContext commandContext) {

    List<BulkSignature> signatures = new LinkedList<BulkSignature>();

    try {

      List<CreateSignatureRequest> signatureRequests = getRequestValue().getCreateSignatureRequest();
      
      
      List<String> requestIds = new LinkedList<String>();

      if (signatureRequests != null && signatureRequests.size() != 0) {

        BulkCollectionSecurityProvider securityProvider = new BulkCollectionSecurityProvider();

        log.debug("get keyboxIdentifier from BulkSingatureRequest");
        keyboxIdentifier = setKeyboxIdentifier(signatureRequests);

        log.info("Requesting signing certificate.");
        signingCertificate = requestSigningCertificate(keyboxIdentifier, commandContext);
        log.debug("Got signing certificate. {}", signingCertificate);


        for (int i=0; i<signatureRequests.size(); i++) {
          
          CreateSignatureRequest request = signatureRequests.get(i);
          if (request.getCreateCMSSignatureRequest() != null) {
            log.info("execute CMSSignature request.");
            
            requestIds.add(request.getId());
            
            BulkSignature signature = prepareCMSSignatureRequests(securityProvider, request.getCreateCMSSignatureRequest(),
                commandContext);
            
            signatures.add(signature);
      
            for(HashDataInput hashDataInput : securityProvider.getBulkSignatureInfo().get(i).getHashDataInput()){
              
              if(hashDataInput instanceof CMSHashDataInput) {
              	CMSHashDataInput cmsHashDataInput = (CMSHashDataInput) hashDataInput;
                 log.debug("setting fileName {}", getFileName(request, i+1));
                 cmsHashDataInput.setFilename(getFileName(request, i+1));
                 cmsHashDataInput.setDigest(signature.getSignerInfo().getDigest());
              } 
              

            }
          }  else {
            if (request.getCreateXMLSignatureRequest() != null) {
              log.error("XML signature requests are currently not supported in bulk signature requests.");
              throw new SLCommandException(4124);
            }
          }
        }


        return new BulkSignatureResultImpl(signBulkRequest(securityProvider.getBulkSignatureInfo(), commandContext,
            signatures), requestIds);
        


      }

    } catch (SLException e) {
      return new ErrorResultImpl(e, commandContext.getLocale());
     
    } catch (CMSException e) {
       log.error("Error reading message digest.",e);
     }
    return null;
  }

  private String getFileName(CreateSignatureRequest request, int requestCounter) {

    String referenceURL = null;
    		  
		if (request.getCreateCMSSignatureRequest().getDataObject() != null
				&& request.getCreateCMSSignatureRequest().getDataObject().getContent() != null) {
			referenceURL = request.getCreateCMSSignatureRequest().getDataObject().getContent().getReference();
		}

    if (StringUtils.isNotEmpty(referenceURL)) {
      return FilenameUtils.getBaseName(referenceURL);
    } else {

      StringBuilder fileNameBuilder = new StringBuilder();

      if (StringUtils.isNotEmpty(request.getDisplayName())) {
        fileNameBuilder.append(request.getDisplayName());
      } else {
        fileNameBuilder.append(HashDataInput.DEFAULT_FILENAME);
        fileNameBuilder.append("_");
        fileNameBuilder.append(requestCounter);
      }
      
      return fileNameBuilder.toString();
    }
  }

  private List<byte[]> signBulkRequest(List<BulkSignatureInfo> bulkSignatureInfo, SLCommandContext commandContext,
      List<BulkSignature> signatures) throws SLCommandException, SLRequestException {

    try {

      List<byte[]> signatureValues;

      BulkSignRequest signRequest = getSTALSignRequest(bulkSignatureInfo);

      // send BulkStalRequest
      List<STALResponse> responses = commandContext.getSTAL().handleRequest(
          Collections.singletonList((STALRequest) signRequest));

      if (responses == null || responses.size() != 1) {
        throw new SignatureException("Failed to access STAL.");
      }

      STALResponse response = responses.get(0);
      
      // setSignatureValues from STALResponse  
      if (response instanceof BulkSignResponse) {
        BulkSignResponse bulkSignatureResponse = ((BulkSignResponse) response);

        signatureValues = new LinkedList<byte[]>();
        for (int i = 0; i < bulkSignatureResponse.getSignResponse().size(); i++) {
          byte[] sig = ((BulkSignResponse) response).getSignResponse().get(i).getSignatureValue();
          log.debug("Got signature response: " + Util.toBase64String(sig));
          signatures.get(i).getSignerInfo()
              .setSignatureValue(wrapSignatureValue(sig, bulkSignatureInfo.get(i).getSignatureAlgorithm()));
          signatureValues.add(signatures.get(i).getEncoded());
        }

        return signatureValues;

      } else if (response instanceof ErrorResponse) {

        ErrorResponse err = (ErrorResponse) response;
        log.debug("Error signing bulk request. Error response code: " + err.getErrorCode() + " (" + err.getErrorMessage() + ").");
        throw new SLCommandException(err.getErrorCode());
      }

    } catch (SignatureException e) {
      log.error("Error creating CMSSignature", e);
      throw new SLCommandException(4000);
    } catch (CMSException e) {
      log.error("Error creating CMSSignature", e);
    }
    return null;
  }

  private String setKeyboxIdentifier(List<CreateSignatureRequest> signatureRequests) throws SLCommandException {
    
    String keyboxIdentifier = null;

    for (CreateSignatureRequest request : signatureRequests) {
      if (request.getCreateCMSSignatureRequest() != null) {

        if (keyboxIdentifier == null) {
          keyboxIdentifier = request.getCreateCMSSignatureRequest().getKeyboxIdentifier();
        } else {
          if (request.getCreateCMSSignatureRequest().getKeyboxIdentifier() == null) {
            log.error("No keyboxIdentifier has been specified for this signature request.");
            throw new SLCommandException(3003);

          } else if (!request.getCreateCMSSignatureRequest().getKeyboxIdentifier().equals(keyboxIdentifier)) {

            log.error("Error creating bulk signature. The bulkSignature value has to be the same fo all signature requests.");
            throw new SLCommandException(3003);
          }
        }
      }
    }


    return keyboxIdentifier;
  }

  private BulkSignature prepareCMSSignatureRequests(BulkCollectionSecurityProvider securityProvieder,
      CreateCMSSignatureRequestType request, SLCommandContext commandContext) throws SLCommandException,
      SLRequestException, SLViewerException {

    BulkSignature signature;

    // prepare the CMSSignature for signing
    log.debug("Preparing CMS signature.");
    signature = prepareCMSSignature(request, commandContext);
    
    try {
     
      // update securityProvieder with parameters of the given signature
      securityProvieder.updateBulkCollectionSecurityProvider(keyboxIdentifier, signature.getHashDataInput(),
          signature.getExcludedByteRange());

      // prepare the CMSSignatures of the Bulk Request
      log.debug("Signing CMS signature.");
     
    
    
    return prepareStalRequest(securityProvieder, signature, commandContext);
   
    } catch (Exception e) {
      log.error("Error creating CMS Signature.", e);
      throw new SLCommandException(4000);
    }

  }

  private BulkSignature prepareCMSSignature(CreateCMSSignatureRequestType request, SLCommandContext commandContext)
      throws SLCommandException, SLRequestException {

    // DataObject, SigningCertificate, SigningTime
    Date signingTime = new Date();
    try {
      return new BulkSignature(
          request.getDataObject() != null ? request.getDataObject() : request.getReferenceObject(),
          request.getStructure(), signingCertificate, signingTime, commandContext.getURLDereferencer(),
          configurationFacade.getUseStrongHash());
    } catch (SLCommandException e) {
      log.error("Error creating CMS Signature.", e);
      throw e;
    } catch (InvalidParameterException e) {
      log.error("Error creating CMS Signature.", e);
      throw new SLCommandException(3004);
    } catch (Exception e) {
      log.error("Error creating CMS Signature.", e);
      throw new SLCommandException(4000);
    }
  }

  private BulkSignature prepareStalRequest(BulkCollectionSecurityProvider securityProvieder, BulkSignature signature,
      SLCommandContext commandContext) throws SLCommandException, SLViewerException {

    try {

      signature.sign(securityProvieder, commandContext.getSTAL(), keyboxIdentifier);
      return signature;
    } catch (CMSException e) {
      log.error("Error creating CMSSignature", e);
      throw new SLCommandException(4000);
    } catch (CMSSignatureException e) {
      log.error("Error creating CMSSignature", e);
      throw new SLCommandException(4000);
    }
  }

  private X509Certificate requestSigningCertificate(String keyboxIdentifier, SLCommandContext commandContext)
      throws SLCommandException {

    InfoboxReadRequest stalRequest = new InfoboxReadRequest();
    stalRequest.setInfoboxIdentifier(keyboxIdentifier);

    STALHelper stalHelper = new STALHelper(commandContext.getSTAL());

    stalHelper.transmitSTALRequest(Collections.singletonList((STALRequest) stalRequest));
    List<X509Certificate> certificates = stalHelper.getCertificatesFromResponses();
    if (certificates == null || certificates.size() != 1) {
      log.info("Got an unexpected number of certificates from STAL.");
      throw new SLCommandException(4000);
    }
    return signingCertificate = certificates.get(0);

  }

  private static BulkSignRequest getSTALSignRequest(List<BulkSignatureInfo> bulkSignatureInfo) {
    BulkSignRequest bulkSignRequest = new BulkSignRequest();

    for (int i = 0; i< bulkSignatureInfo.size(); i++) {
      
      BulkSignatureInfo signatureInfo = bulkSignatureInfo.get(i);
      SignRequest signRequest = new SignRequest();
      signRequest.setKeyIdentifier(signatureInfo.getKeyboxIdentifier());
      log.debug("SignedAttributes: " + Util.toBase64String(signatureInfo.getSignedAttributes()));
      SignedInfo signedInfo = new SignedInfo();
      signedInfo.setValue(signatureInfo.getSignedAttributes());
      signedInfo.setIsCMSSignedAttributes(true);
      signRequest.setSignedInfo(signedInfo);   
      log.info("set displayName for Request {}", signatureInfo.getHashDataInput().get(0).getFilename());
      signRequest.setDisplayName(signatureInfo.getHashDataInput().get(0).getFilename());
      signRequest.setMimeType(signatureInfo.getHashDataInput().get(0).getMimeType());

      signRequest.setSignatureMethod(signatureInfo.getSignatureMethod());
      signRequest.setDigestMethod(signatureInfo.getDigestMethod());
      signRequest.setHashDataInput(signatureInfo.getHashDataInput());

      ExcludedByteRangeType excludedByteRange = signatureInfo.getExcludedByteRange();
      if (excludedByteRange != null) {
        SignRequest.ExcludedByteRange ebr = new SignRequest.ExcludedByteRange();
        ebr.setFrom(excludedByteRange.getFrom());
        ebr.setTo(excludedByteRange.getTo());
        signRequest.setExcludedByteRange(ebr);
      }

      bulkSignRequest.getSignRequests().add(signRequest);
    }
    return bulkSignRequest;
  }

  private static byte[] wrapSignatureValue(byte[] sig, AlgorithmID sigAlgorithmID) {
    String id = sigAlgorithmID.getAlgorithm().getID();
    if (id.startsWith(ID_ECSIGTYPE)) // X9.62 Format ECDSA signatures
    {
      // Wrap r and s in ASN.1 SEQUENCE
      byte[] r = Arrays.copyOfRange(sig, 0, sig.length / 2);
      byte[] s = Arrays.copyOfRange(sig, sig.length / 2, sig.length);
      SEQUENCE sigS = new SEQUENCE();
      sigS.addComponent(new INTEGER(new BigInteger(1, r)));
      sigS.addComponent(new INTEGER(new BigInteger(1, s)));
      return DerCoder.encode(sigS);
    } else
      return sig;
  }

}