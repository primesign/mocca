/*
 * Copyright 2013 by Graz University of Technology, Austria
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

import iaik.cms.CMSException;
import iaik.cms.CMSSignatureException;

import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.buergerkarte.namespaces.securitylayer._1_2_3.CreateCMSSignatureRequestType;
import at.gv.egiz.bku.conf.MoccaConfigurationFacade;
import at.gv.egiz.bku.slcommands.CreateCMSSignatureCommand;
import at.gv.egiz.bku.slcommands.SLCommandContext;
import at.gv.egiz.bku.slcommands.SLResult;
import at.gv.egiz.bku.slcommands.impl.cms.Signature;
import at.gv.egiz.bku.slexceptions.SLCommandException;
import at.gv.egiz.bku.slexceptions.SLException;
import at.gv.egiz.bku.slexceptions.SLRequestException;
import at.gv.egiz.bku.slexceptions.SLViewerException;
import at.gv.egiz.stal.InfoboxReadRequest;
import at.gv.egiz.stal.STALRequest;

/**
 * This class implements the security layer command
 * <code>CreateCMSSignatureRequest</code>.
 * 
 * @author tkellner
 */
public class CreateCMSSignatureCommandImpl extends
    SLCommandImpl<CreateCMSSignatureRequestType> implements
    CreateCMSSignatureCommand {

  /**
   * Logging facility.
   */
  private final Logger log = LoggerFactory.getLogger(CreateCMSSignatureCommandImpl.class);

  /**
   * The signing certificate.
   */
  protected X509Certificate signingCertificate;

  /**
   * The keybox identifier of the key used for signing.
   */
  protected String keyboxIdentifier;

  /**
   * The to-be signed signature.
   */
  protected Signature signature;

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

  public void setConfiguration(Configuration configuration) {
    configurationFacade.setConfiguration(configuration);
  }

  @Override
  public void prepareCMSSignature(SLCommandContext commandContext) throws SLCommandException,
      SLRequestException {

    CreateCMSSignatureRequestType request = getRequestValue();

    // DataObject, SigningCertificate, SigningTime
    Date signingTime = new Date();
    try {
      signature = new Signature(request.getDataObject(), request.getStructure(),
          signingCertificate, signingTime, configurationFacade.getUseStrongHash());
    } catch (Exception e) {
      log.error("Error creating CMS Signature.", e);
      throw new SLCommandException(4000);
    }
  }

  /**
   * Gets the signing certificate from STAL.
   * @param commandContext TODO
   * 
   * @throws SLCommandException
   *           if getting the singing certificate fails
   */
  private void getSigningCertificate(SLCommandContext commandContext) throws SLCommandException {

    CreateCMSSignatureRequestType request = getRequestValue();
    keyboxIdentifier = request.getKeyboxIdentifier();

    InfoboxReadRequest stalRequest = new InfoboxReadRequest();
    stalRequest.setInfoboxIdentifier(keyboxIdentifier);

    STALHelper stalHelper = new STALHelper(commandContext.getSTAL());

    stalHelper.transmitSTALRequest(Collections.singletonList((STALRequest) stalRequest));
    List<X509Certificate> certificates = stalHelper.getCertificatesFromResponses();
    if (certificates == null || certificates.size() != 1) {
      log.info("Got an unexpected number of certificates from STAL.");
      throw new SLCommandException(4000);
    }
    signingCertificate = certificates.get(0);

  }

  /**
   * Signs the signature.
   * @param commandContext TODO
   * @return the CMS signature
   * @throws SLCommandException
   *           if signing the signature fails
   * @throws SLViewerException
   */
  private byte[] signCMSSignature(SLCommandContext commandContext) throws SLCommandException, SLViewerException {

    try {
      return signature.sign(commandContext.getSTAL(), keyboxIdentifier);
    } catch (CMSException e) {
      log.error("Error creating CMSSignature", e);
      throw new SLCommandException(4000);
    } catch (CMSSignatureException e) {
      log.error("Error creating CMSSignature", e);
      throw new SLCommandException(4000);
    }
  }

  @Override
  public SLResult execute(SLCommandContext commandContext) {
    try {

      // get certificate in order to select appropriate algorithms for hashing
      // and signing
      log.info("Requesting signing certificate.");
      getSigningCertificate(commandContext);
      if (log.isDebugEnabled()) {
        log.debug("Got signing certificate. {}", signingCertificate);
      } else {
        log.info("Got signing certificate.");
      }

      // prepare the CMSSignature for signing
      log.info("Preparing CMS signature.");
      prepareCMSSignature(commandContext);

      // sign the CMSSignature
      log.info("Signing CMS signature.");
      byte[] sig = signCMSSignature(commandContext);
      log.info("CMS signature signed.");

      return new CreateCMSSignatureResultImpl(sig);

    } catch (SLException e) {
      return new ErrorResultImpl(e, commandContext.getLocale());
    }
  }

  @Override
  public String getName() {
    return "CreateCMSSignatureRequest";
  }

}
