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
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.stal.ErrorResponse;
import at.gv.egiz.stal.InfoboxReadRequest;
import at.gv.egiz.stal.InfoboxReadResponse;
import at.gv.egiz.stal.STAL;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.SignRequest;
import at.gv.egiz.stal.SignResponse;

public class DummySTAL implements STAL { 

  static Log log = LogFactory.getLog(DummySTAL.class);

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
      log.error(e);
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
            log.error(e);
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
          s.update(signReq.getSignedInfo());
          byte[] sigVal = s.sign();
          SignResponse resp = new SignResponse();
          resp.setSignatureValue(sigVal);
          responses.add(resp);
        } catch (Exception e) {
          log.error(e);
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
