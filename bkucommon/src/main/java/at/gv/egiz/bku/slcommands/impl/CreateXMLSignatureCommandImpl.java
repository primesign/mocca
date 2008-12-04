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

import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.URIReferenceException;
import javax.xml.crypto.dsig.XMLSignatureException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import at.buergerkarte.namespaces.securitylayer._1.CreateXMLSignatureRequestType;
import at.buergerkarte.namespaces.securitylayer._1.DataObjectInfoType;
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
  protected static Log log = LogFactory
      .getLog(CreateXMLSignatureCommandImpl.class);

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

  @Override
  public void init(SLCommandContext ctx, Object unmarshalledRequest)
      throws SLCommandException {
    super.init(ctx, unmarshalledRequest);
  }

  @Override
  public void prepareXMLSignature() throws SLCommandException,
      SLRequestException {

    CreateXMLSignatureRequestType request = getRequestValue();

    // TODO: make configurable?
    IdValueFactory idValueFactory = new IdValueFactoryImpl();

    // TODO: make configurable?
    AlgorithmMethodFactory algorithmMethodFactory;
    try {
      algorithmMethodFactory = new AlgorithmMethodFactoryImpl(
          signingCertificate);
    } catch (NoSuchAlgorithmException e) {
      log.error("Failed to get DigestMethod.", e);
      throw new SLCommandException(4006);
    }

    signature = new Signature(getCmdCtx().getURLDereferencerContext(),
        idValueFactory, algorithmMethodFactory);

    // SigningTime
    signature.setSigningTime(new Date());

    // SigningCertificate
    signature.setSignerCeritifcate(signingCertificate);

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
   * 
   * @throws SLCommandException
   *           if getting the singing certificate fails
   */
  private void getSigningCertificate() throws SLCommandException {

    CreateXMLSignatureRequestType request = getRequestValue();
    keyboxIdentifier = request.getKeyboxIdentifier();

    InfoboxReadRequest stalRequest = new InfoboxReadRequest();
    stalRequest.setInfoboxIdentifier(keyboxIdentifier);

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
   * 
   * @throws SLCommandException
   *           if signing the signature fails
   * @throws SLViewerException
   */
  private void signXMLSignature() throws SLCommandException, SLViewerException {

    try {
      signature.sign(getCmdCtx().getSTAL(), keyboxIdentifier);
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
  public SLResult execute() {
    try {

      // get certificate in order to select appropriate algorithms for hashing
      // and signing
      getSigningCertificate();

      // prepare the XMLSignature for signing
      prepareXMLSignature();

      // sign the XMLSignature
      signXMLSignature();

      if (log.isTraceEnabled()) {

        DOMImplementationLS domImplLS = DOMUtils.getDOMImplementationLS();
        LSSerializer serializer = domImplLS.createLSSerializer();
        String debugString = serializer.writeToString(signature.getDocument());

        log.trace(debugString);

      }

      return new CreateXMLSignatureResultImpl(signature.getDocument());

    } catch (SLException e) {
      return new ErrorResultImpl(e, cmdCtx.getLocale());
    }
  }

  @Override
  public String getName() {
    return "CreateXMLSignatureRequest";
  }

}
