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

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.URIReferenceException;
import javax.xml.crypto.dsig.XMLSignatureException;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import at.buergerkarte.namespaces.securitylayer._1_2_3.CreateXMLSignatureRequestType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.DataObjectInfoType;
import at.gv.egiz.bku.binding.HTTPBindingProcessorImpl;
import at.gv.egiz.bku.conf.MoccaConfigurationFacade;
import at.gv.egiz.bku.slcommands.CreateXMLSignatureCommand;
import at.gv.egiz.bku.slcommands.SLCommandContext;
import at.gv.egiz.bku.slcommands.SLResult;
import at.gv.egiz.bku.slcommands.impl.xsect.AlgorithmMethodFactory;
import at.gv.egiz.bku.slcommands.impl.xsect.AlgorithmMethodFactoryImpl;
import at.gv.egiz.bku.slcommands.impl.xsect.IdValueFactory;
import at.gv.egiz.bku.slcommands.impl.xsect.IdValueFactoryImpl;
import at.gv.egiz.bku.slcommands.impl.xsect.Signature;
import at.gv.egiz.bku.slexceptions.SLCommandException;
import at.gv.egiz.bku.slexceptions.SLException;
import at.gv.egiz.bku.slexceptions.SLRequestException;
import at.gv.egiz.bku.slexceptions.SLViewerException;
import at.gv.egiz.dom.DOMUtils;
import at.gv.egiz.stal.InfoboxReadRequest;
import at.gv.egiz.stal.STALRequest;

/**
 * This class implements the security layer command
 * <code>CreateXMLSignatureRequest</code>.
 * 
 * @author mcentner
 */
public class CreateXMLSignatureCommandImpl extends
    SLCommandImpl<CreateXMLSignatureRequestType> implements
    CreateXMLSignatureCommand {

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
    public static final String USE_XADES_1_4 =
      HTTPBindingProcessorImpl.ConfigurationFacade.USE_XADES_1_4;
    public static final String USE_XADES_1_4_BLACKLIST =
      HTTPBindingProcessorImpl.ConfigurationFacade.USE_XADES_1_4_BLACKLIST;
    public static final String XADES_1_4_BLACKLIST_URL =
      HTTPBindingProcessorImpl.ConfigurationFacade.XADES_1_4_BLACKLIST_URL;
    public static final int XADES_1_4_BLACKLIST_EXPIRY =
        HTTPBindingProcessorImpl.ConfigurationFacade.XADES_1_4_BLACKLIST_EXPIRY;

    public void setConfiguration(Configuration configuration) {
      this.configuration = configuration;
    }

    public boolean getUseStrongHash() {
      return configuration.getBoolean(USE_STRONG_HASH, true);
    }

    public boolean getUseXAdES14() {
      return configuration.getBoolean(USE_XADES_1_4, true);
    }

    public boolean getUseXAdES14Blacklist() {
      return configuration.getBoolean(USE_XADES_1_4_BLACKLIST, true);
    }
  }

  private static long XADES_1_4_BLACKLIST_TS;
  private static final List<String> XADES_1_4_BLACKLIST;
  static {
    XADES_1_4_BLACKLIST = new ArrayList<String>();
    loadXAdES14Blacklist();
  }

  private static void loadXAdES14Blacklist() {
    XADES_1_4_BLACKLIST_TS = System.currentTimeMillis();
    XADES_1_4_BLACKLIST.clear();
    try {
      URLConnection blc = new URL(ConfigurationFacade.XADES_1_4_BLACKLIST_URL).openConnection();
      blc.setUseCaches(false);
      InputStream in = blc.getInputStream();
      Scanner s = new Scanner(in);
      while (s.hasNext()){
        XADES_1_4_BLACKLIST.add(s.next());
      }
    s.close();
    } catch (Exception e) {
      log.error("Blacklist load error", e);
    }
  }

  private static boolean matchesXAdES14Blacklist(String url) {
    log.debug("Checking DataURL against XAdES14 blacklist: {}", url);
    if ((System.currentTimeMillis() - XADES_1_4_BLACKLIST_TS) >
      (ConfigurationFacade.XADES_1_4_BLACKLIST_EXPIRY * 1000)) {
      log.debug("Updating XAdES14 blacklist");
      loadXAdES14Blacklist();
    }
    if (url != null) {
      for (String bl_entry : XADES_1_4_BLACKLIST) {
        if (url.matches(bl_entry)) {
          log.debug("XAdES14 blacklist match");
          return true;
        }
      }
    }
    return false;
  }

  public void setConfiguration(Configuration configuration) {
    configurationFacade.setConfiguration(configuration);
  }

  @Override
  public void prepareXMLSignature(SLCommandContext commandContext) throws SLCommandException,
      SLRequestException {

    CreateXMLSignatureRequestType request = getRequestValue();

    // TODO: make configurable?
    IdValueFactory idValueFactory = new IdValueFactoryImpl();

    // TODO: make configurable?
    AlgorithmMethodFactory algorithmMethodFactory;
    try {
      algorithmMethodFactory = new AlgorithmMethodFactoryImpl(
          signingCertificate, configurationFacade.getUseStrongHash());
    } catch (NoSuchAlgorithmException e) {
      log.error("Failed to get DigestMethod.", e);
      throw new SLCommandException(4006);
    }

    boolean useXAdES14 = configurationFacade.getUseXAdES14();
    if (useXAdES14 && configurationFacade.getUseXAdES14Blacklist()) {
      String dataURL = commandContext.getDataURL();
      if (matchesXAdES14Blacklist(dataURL))
        useXAdES14 = false;
    }

    signature = new Signature(commandContext.getURLDereferencer(),
        idValueFactory, algorithmMethodFactory, useXAdES14);

    // SigningTime
    signature.setSigningTime(new Date());

    // SigningCertificate
    signature.setSignerCertificate(signingCertificate);

    // SignatureInfo
    if (request.getSignatureInfo() != null) {
      signature.setSignatureInfo(request.getSignatureInfo());
    }

    // DataObjects
    for (DataObjectInfoType dataObjectInfo : request.getDataObjectInfo()) {
      signature.addDataObject(dataObjectInfo);
    }

    signature.buildXMLSignature();

  }

  /**
   * Gets the signing certificate from STAL.
   * @param commandContext TODO
   * 
   * @throws SLCommandException
   *           if getting the singing certificate fails
   */
  private void getSigningCertificate(SLCommandContext commandContext) throws SLCommandException {

    CreateXMLSignatureRequestType request = getRequestValue();
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
   * 
   * @throws SLCommandException
   *           if signing the signature fails
   * @throws SLViewerException
   */
  private void signXMLSignature(SLCommandContext commandContext) throws SLCommandException, SLViewerException {

    try {
      signature.sign(commandContext.getSTAL(), keyboxIdentifier);
    } catch (MarshalException e) {
      log.error("Failed to marshall XMLSignature.", e);
      throw new SLCommandException(4000);
    } catch (XMLSignatureException e) {
      if (e.getCause() instanceof URIReferenceException) {
        URIReferenceException uriReferenceException = (URIReferenceException) e
            .getCause();
        if (uriReferenceException.getCause() instanceof SLCommandException) {
          throw (SLCommandException) uriReferenceException.getCause();
        }
      }
      log.error("Failed to sign XMLSignature.", e);
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

      // prepare the XMLSignature for signing
      log.info("Preparing XML signature.");
      prepareXMLSignature(commandContext);

      // sign the XMLSignature
      log.info("Signing XML signature.");
      signXMLSignature(commandContext);
      if (log.isDebugEnabled()) {

        DOMImplementationLS domImplLS = DOMUtils.getDOMImplementationLS();
        LSSerializer serializer = domImplLS.createLSSerializer();
        String debugString = serializer.writeToString(signature.getDocument());

        log.debug(debugString);

      } else {
        log.info("XML signature signed.");
      }

      return new CreateXMLSignatureResultImpl(signature.getDocument());

    } catch (SLException e) {
      return new ErrorResultImpl(e, commandContext.getLocale());
    }
  }

  @Override
  public String getName() {
    return "CreateXMLSignatureRequest";
  }

}
