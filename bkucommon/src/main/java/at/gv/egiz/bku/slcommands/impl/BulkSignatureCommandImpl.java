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


import iaik.cms.CMSException;
import iaik.cms.CMSSignatureException;

import java.security.InvalidParameterException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.buergerkarte.namespaces.securitylayer._1_2_3.BulkRequestType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.BulkRequestType.CreateSignatureRequest;
import at.buergerkarte.namespaces.securitylayer._1_2_3.CreateCMSSignatureRequestType;
import at.gv.egiz.bku.slcommands.BulkSignatureCommand;
import at.gv.egiz.bku.slcommands.SLCommandContext;
import at.gv.egiz.bku.slcommands.SLResult;
import at.gv.egiz.bku.slcommands.impl.cms.BulkCollectionSecurityProvider;
import at.gv.egiz.bku.slcommands.impl.cms.BulkSTALSecurityProvider;
import at.gv.egiz.bku.slcommands.impl.cms.BulkSignature;
import at.gv.egiz.bku.slcommands.impl.cms.BulkSignatureInfo;
import at.gv.egiz.bku.slexceptions.SLCommandException;
import at.gv.egiz.bku.slexceptions.SLException;
import at.gv.egiz.bku.slexceptions.SLRequestException;
import at.gv.egiz.bku.slexceptions.SLViewerException;
import at.gv.egiz.bku.conf.MoccaConfigurationFacade;
import at.gv.egiz.stal.InfoboxReadRequest;
import at.gv.egiz.stal.STALRequest;

/**
 * This class implements the security layer command
 * <code>BulkRequest</code>.
 * 
 * @author szoescher
 */
public class BulkSignatureCommandImpl extends
		SLCommandImpl<BulkRequestType> implements
		BulkSignatureCommand {
	
	  /**
	   * Logging facility.
	   */
	  private final static Logger log = LoggerFactory.getLogger(CreateXMLSignatureCommandImpl.class);	    
		  
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
	    return "BulkRequestCommandImpl";
	  }
	  
	  public void setConfiguration(Configuration configuration) {
		    configurationFacade.setConfiguration(configuration);
		  }
	  

	  
	  
	@Override
	public SLResult execute(SLCommandContext commandContext) {

		List<byte[]> signatures = null;
		
		try {
			
			
			List<CreateSignatureRequest> signatureRequests = getRequestValue().getCreateSignatureRequest();
			

			if (signatureRequests != null && signatureRequests.size() != 0) {

				BulkCollectionSecurityProvider securityProvieder = new BulkCollectionSecurityProvider();
				
				log.debug("get keyboxIdentifier from BulkSingatureRequest");
				keyboxIdentifier = setKeyboxIdentifier(signatureRequests);

				log.info("Requesting signing certificate.");
				signingCertificate = requestSigningCertificate(keyboxIdentifier, commandContext);
				log.debug("Got signing certificate. {}", signingCertificate);

				for (CreateSignatureRequest request : signatureRequests) {

					if (request.getCreateCMSSignatureRequest() != null) {
						log.info("execute CMSSignature request.");
						prepareCMSSignatureRequests(securityProvieder, request.getCreateCMSSignatureRequest(),
								commandContext);
					}
				}

						signatures  = executeBulkRequest(securityProvieder.getBulkSignatureInfo(), signatureRequests.get(0)
						.getCreateCMSSignatureRequest(), commandContext);		

			}

		} catch (SLException e) {
		      return new ErrorResultImpl(e, commandContext.getLocale());
	    }
		return new BulkSignatureResultImpl(signatures);
	}


	private List<byte[]> executeBulkRequest(List<BulkSignatureInfo> bulkSignatureInfo, CreateCMSSignatureRequestType request,
			SLCommandContext commandContext) throws SLCommandException, SLRequestException {

		BulkSignature signature;

		// prepare the CMSSignature for signing
		log.debug("Preparing CMS signature.");
		signature = prepareCMSSignature(request, commandContext);

		// prepare BulkSTALSecurityProvider
		BulkSTALSecurityProvider securityProvider = new BulkSTALSecurityProvider(bulkSignatureInfo,
				commandContext.getSTAL());

		try {

			 signature.sign(securityProvider, commandContext.getSTAL(), keyboxIdentifier);
			 return securityProvider.getSignatureValues();
			 
		} catch (CMSException e) {
			log.error("Error creating CMSSignature", e);
			throw new SLCommandException(4000);
		} catch (CMSSignatureException e) {
			log.error("Error creating CMSSignature", e);
			throw new SLCommandException(4000);
		}
	}


	private String setKeyboxIdentifier( List<CreateSignatureRequest> signatureRequests) {
		for(CreateSignatureRequest request : signatureRequests){
			if(request.getCreateCMSSignatureRequest() != null){
				return request.getCreateCMSSignatureRequest().getKeyboxIdentifier();
			}
		}

		return null;
	}

	private byte[] prepareCMSSignatureRequests(BulkCollectionSecurityProvider securityProvieder,
			CreateCMSSignatureRequestType request, SLCommandContext commandContext) throws SLCommandException,
			SLRequestException, SLViewerException {

		BulkSignature signature;

		// prepare the CMSSignature for signing
		log.debug("Preparing CMS signature.");
		signature = prepareCMSSignature(request, commandContext);

		//update securityProvieder with parameters of the given signature
		securityProvieder.updateBulkCollectionSecurityProvider(keyboxIdentifier, signature.getHashDataInput(), signature.getExcludedByteRange());
		
		// prepare the CMSSignatures of the Bulk Request
		log.debug("Signing CMS signature.");
		return (prepareStalRequest(securityProvieder, signature, commandContext));
	}

	private BulkSignature prepareCMSSignature(CreateCMSSignatureRequestType request, SLCommandContext commandContext)
			throws SLCommandException, SLRequestException {

		    // DataObject, SigningCertificate, SigningTime
		    Date signingTime = new Date();
		    try {
		      return new BulkSignature(request.getDataObject(), request.getStructure(),
		          signingCertificate, signingTime, commandContext.getURLDereferencer(),
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


	  private byte[] prepareStalRequest(BulkCollectionSecurityProvider securityProvieder, BulkSignature signature, SLCommandContext commandContext) throws SLCommandException, SLViewerException {

	    try {    	
	 
	      return signature.sign(securityProvieder, commandContext.getSTAL(), keyboxIdentifier);
	    } catch (CMSException e) {
	      log.error("Error creating CMSSignature", e);
	      throw new SLCommandException(4000);
	    } catch (CMSSignatureException e) {
	      log.error("Error creating CMSSignature", e);
	      throw new SLCommandException(4000);
	    }
	  }
	
	  private X509Certificate requestSigningCertificate(String keyboxIdentifier, SLCommandContext commandContext) throws SLCommandException {

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
	


	




}