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

import iaik.asn1.CodingException;
import iaik.asn1.DerCoder;
import iaik.utils.Base64OutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.bku.slexceptions.SLCommandException;
import at.gv.egiz.bku.slexceptions.SLExceptionMessages;
import at.gv.egiz.bku.slexceptions.SLRuntimeException;
import at.gv.egiz.idlink.ans1.IdentityLink;
import at.gv.egiz.stal.ErrorResponse;
import at.gv.egiz.stal.InfoboxReadResponse;
import at.gv.egiz.stal.STAL;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;

/**
 * A helper class for transmitting {@link STALRequest}s and obtaining their
 * respective {@link STALResponse}s.
 * 
 * @author mcentner
 */
public class STALHelper {

  /**
   * Logging facility.
   */
  private final Logger log = LoggerFactory.getLogger(STALHelper.class);
  
  /**
   * The STAL implementation.
   */
  private STAL stal;
  
  /**
   * An iterator over the <code>STALResponse</code>s received in
   * {@link SLCommandImpl#transmitSTALRequest(List)}.
   */
  protected Iterator<STALResponse> stalResponses;
  
  /**
   * Creates a new instance of this STALHelper with the given
   * <code>stal</code>. 
   * 
   * @param stal the STAL to be used
   */
  public STALHelper(STAL stal) {
    if (stal == null) {
      throw new NullPointerException("Argument 'stal' must not be null.");
    }
    this.stal = stal;
  }

  /**
   * Calls {@link STAL#handleRequest(List)} with the given
   * <code>stalRequests</code>.
   * 
   * @param stalRequests
   * @throws SLCommandException
   */
   public void transmitSTALRequest(List<? extends STALRequest> stalRequests) throws SLCommandException {
    List<STALResponse> responses = stal.handleRequest(stalRequests);
    if (responses == null) {
      Logger log = LoggerFactory.getLogger(this.getClass());
      log.info("Received no responses from STAL.");
      throw new SLCommandException(4000);
    } else if (responses.size() != stalRequests.size()) {
      Logger log = LoggerFactory.getLogger(this.getClass());
      log.info("Received invalid count of responses from STAL. Expected "
          + stalRequests.size() + ", but got " + responses.size() + ".");
      // throw new SLCommandException(4000);
    }
    stalResponses = responses.iterator();
  }

  /**
   * @return <code>true</code> if there are more {@link STALResponse}s to be
   *         fetched with {@link #nextResponse(Class)}, or <code>false</code>
   *         otherwise.
   */
  public boolean hasNextResponse() {
    return (stalResponses != null) ? stalResponses.hasNext() : false;
  }

  /**
   * Returns the next response of type <code>responseClass</code> that has been
   * received by {@link #transmitSTALRequest(List)}.
   * 
   * @param responseClass
   *          the response must be an instance of
   * @return the next response of type <code>responseClass</code>
   * 
   * @throws NoSuchElementException
   *           if there is no more response
   * @throws SLCommandException
   *           if the next response is of type {@link ErrorResponse} or not of
   *           type <code>responseClass</code>
   */
  public STALResponse nextResponse(
      Class<? extends STALResponse> responseClass) throws SLCommandException {

    if (stalResponses == null) {
      throw new NoSuchElementException();
    }

    STALResponse response = stalResponses.next();

    if (response instanceof ErrorResponse) {
      throw new SLCommandException(((ErrorResponse) response).getErrorCode());
    }

    if (!(responseClass.isAssignableFrom(response.getClass()))) {
      Logger log = LoggerFactory.getLogger(this.getClass());
      log.info("Received " + response.getClass() + " from STAL but expected "
          + responseClass);
      throw new SLCommandException(4000);
    }

    return response;

  }

  /**
   * Gets the list of certificates from the next STAL responses.
   * 
   * @return the list of certificates
   * 
   * @throws SLCommandException if getting the list of certificates fails
   */
  public List<X509Certificate> getCertificatesFromResponses() throws SLCommandException {
    
    List<X509Certificate> certificates = new ArrayList<X509Certificate>();

    CertificateFactory certFactory;
    try {
      certFactory = CertificateFactory.getInstance("X509");
    } catch (CertificateException e) {
      // we should always be able to get an X509 certificate factory
      log.error("CertificateFactory.getInstance(\"X509\") failed.", e);
      throw new SLRuntimeException(e);
    }
    
    InfoboxReadResponse response;
    while(hasNextResponse()) {
      response = (InfoboxReadResponse) nextResponse(InfoboxReadResponse.class);
      byte[] cert = response.getInfoboxValue();
      try {
        certificates.add((X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(cert)));
      } catch (CertificateException e) {
        if (log.isDebugEnabled()) {
          ByteArrayOutputStream certDump = new ByteArrayOutputStream();
          OutputStreamWriter writer = new OutputStreamWriter(certDump);
          try {
            writer.write("-----BEGIN CERTIFICATE-----\n");
            writer.flush();
            Base64OutputStream b64os = new Base64OutputStream(certDump);
            b64os.write(cert);
            b64os.close();
            writer.write("\n-----END CERTIFICATE-----");
            writer.flush();
          } catch (IOException e1) {
            log.info("Failed to decode certificate.", e);
          }
          log.debug("Failed to decode certificate.\n{}", certDump.toString(), e);
        } else {
          log.info("Failed to decode certificate.", e);
        }
        throw new SLCommandException(4000,
            SLExceptionMessages.EC4000_UNCLASSIFIED_INFOBOX_INVALID,
            new Object[] { "Certificates" });
      }
    }
    
    return certificates;

  }
  
  /**
   * Gets the IdentitiyLink form the next STAL response.
   * 
   * @return the IdentityLink
   * 
   * @throws SLCommandException if getting the IdentitiyLink fails
   */
  public IdentityLink getIdentityLinkFromResponses() throws SLCommandException {

    // IdentityLink
    InfoboxReadResponse response;
    if (hasNextResponse()) {
      response = (InfoboxReadResponse) nextResponse(InfoboxReadResponse.class);
      byte[] idLink = response.getInfoboxValue();
      try {
        return new IdentityLink(DerCoder.decode(idLink));
      } catch (CodingException e) {
        log.info("Failed to decode infobox 'IdentityLink'.", e);
        throw new SLCommandException(4000,
            SLExceptionMessages.EC4000_UNCLASSIFIED_INFOBOX_INVALID,
            new Object[] { "IdentityLink" });
      }
    } else {
      log.info("No infobox 'IdentityLink' returned from STAL.");
      throw new SLCommandException(4000);
    }
    
  }
  

}
