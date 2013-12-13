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


package at.gv.egiz.stal.dummy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.stal.ErrorResponse;
import at.gv.egiz.stal.InfoboxReadRequest;
import at.gv.egiz.stal.InfoboxReadResponse;
import at.gv.egiz.stal.STAL;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.SignRequest;
import at.gv.egiz.stal.SignResponse;

public class DummySTAL implements STAL { 

  private final Logger log = LoggerFactory.getLogger(DummySTAL.class);

  protected X509Certificate cert = null;
  protected PrivateKey privateKey = null;
  
  public DummySTAL() {
    try {
      KeyStore ks = KeyStore.getInstance("pkcs12");
      InputStream ksStream = getClass().getClassLoader().getResourceAsStream(
      "at/gv/egiz/bku/slcommands/impl/Cert.p12");
      ks.load(ksStream, "1622".toCharArray());
      for (Enumeration<String> aliases = ks.aliases(); aliases
          .hasMoreElements();) {
        String alias = aliases.nextElement();
        log.debug("Found alias " + alias + " in keystore");
        if (ks.isKeyEntry(alias)) {
          log.debug("Found key entry for alias: " + alias);
          privateKey = (PrivateKey) ks.getKey(alias, "1622".toCharArray());
          cert = (X509Certificate) ks.getCertificate(alias);  
          System.out.println(cert);
        }
      }
    } catch (Exception e) {
      log.error("Failed to create DummySTAL.", e);
    }

  }

  @Override
  public List<STALResponse> handleRequest(List<? extends STALRequest> requestList) {

    List<STALResponse> responses = new ArrayList<STALResponse>();
    for (STALRequest request : requestList) {

      log.debug("Got STALRequest " + request + ".");

      if (request instanceof InfoboxReadRequest) {

        String infoboxIdentifier = ((InfoboxReadRequest) request)
            .getInfoboxIdentifier();
        InputStream stream = getClass().getClassLoader().getResourceAsStream(
            "at/gv/egiz/stal/dummy/infoboxes4/" + infoboxIdentifier + ".bin");

        STALResponse response;
        if (stream != null) {

          log.debug("Infobox " + infoboxIdentifier + " found.");

          byte[] infobox;
          try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int b;
            while ((b = stream.read()) != -1) {
              buffer.write(b);
            }
            infobox = buffer.toByteArray();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }

          InfoboxReadResponse infoboxReadResponse = new InfoboxReadResponse();
          infoboxReadResponse.setInfoboxValue(infobox);
          response = infoboxReadResponse;

        } else if ((infoboxIdentifier.equals("SecureSignatureKeypair")) ||(infoboxIdentifier.equals("CertifiedKeypair"))) {
          try {
            InfoboxReadResponse infoboxReadResponse = new InfoboxReadResponse();
            infoboxReadResponse.setInfoboxValue(cert.getEncoded());
            response = infoboxReadResponse;
          } catch (CertificateEncodingException e) {
            log.error("Failed to encode certificate.", e);
            response = new ErrorResponse();
          }
        } else {

          log.debug("Infobox " + infoboxIdentifier + " not found.");

          response = new ErrorResponse();
        }
        responses.add(response);

      } else if (request instanceof SignRequest) {
        try {

          SignRequest signReq = (SignRequest) request;
          Signature s = Signature.getInstance("SHA1withRSA");
          s.initSign(privateKey);
          s.update(signReq.getSignedInfo().getValue());
          byte[] sigVal = s.sign();
          SignResponse resp = new SignResponse();
          resp.setSignatureValue(sigVal);
          responses.add(resp);
        } catch (Exception e) {
          log.error("Failed to create signature.", e);
          responses.add(new ErrorResponse());
        }

      } else {

        log.debug("Request not implemented.");

        responses.add(new ErrorResponse());
      }

    }

    return responses;
  }
}
