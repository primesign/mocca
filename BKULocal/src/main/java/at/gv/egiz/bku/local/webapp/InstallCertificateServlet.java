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
package at.gv.egiz.bku.local.webapp;

import iaik.pkcs.PKCS7CertList;
import iaik.utils.Util;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class InstallCertificateServlet extends HttpServlet {
  public static final String HTTPS_REDIRECT = "https://localhost:3496/";

  public static final String SERVER_CA_CERTIFICATE_ATTRIBUTE = "mocca.tls.server.ca.certificate";
  protected PKCS7CertList p7c;
  private static final Log log = LogFactory.getLog(InstallCertificateServlet.class);

  @Override
  public void init() throws ServletException {
    super.init();
    Certificate caCert = (Certificate) getServletContext().getAttribute(SERVER_CA_CERTIFICATE_ATTRIBUTE);
    if (caCert != null) {
      try {
        p7c = new PKCS7CertList();
        p7c.setCertificateList(new iaik.x509.X509Certificate[] { Util.convertCertificate(caCert) });
      } catch (CertificateException ex) {
        log.error("failed to import local ca certificate " + SERVER_CA_CERTIFICATE_ATTRIBUTE, ex);
      }
    } else {
      log.error("failed to import local ca certificate " + SERVER_CA_CERTIFICATE_ATTRIBUTE);
    }
  }

  /**
   * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {

//    try {
//      SSLContext sslCtx1 = SSLContext.getDefault();
//      log.debug("Default SSLContext (" + sslCtx1.getProtocol() + "): " + sslCtx1.getClass().getName());
//    } catch (NoSuchAlgorithmException ex) {
//      log.debug("no sslContext: " + ex.getMessage(), ex);
//    }
//
//    try {
//      SSLContext sslCtx2 = SSLContext.getInstance("TLS");
//      log.debug("TLS SSLContext: " + sslCtx2.getClass().getName());
//
//      SSLServerSocketFactory serverSocketFactory = sslCtx2.getServerSocketFactory();
//      SSLSessionContext serverSessionContext = sslCtx2.getServerSessionContext();
//
//      if (serverSocketFactory != null) {
//        log.debug("SSL ServerSocketFactory: " + serverSocketFactory.getClass().getName());
//      }
//      if (serverSessionContext != null) {
//        log.debug("SSL ServerSessionContext: " + serverSessionContext.getClass().getName());
//      }
//    } catch (NoSuchAlgorithmException ex) {
//      log.debug("no sslContext: " + ex.getMessage(), ex);
//    }
//
//    try {
//      SSLContext sslCtx3 = SSLContext.getInstance("SSLv3");
//      log.debug("TLS SSLContext: " + sslCtx3.getClass().getName());
//    } catch (NoSuchAlgorithmException ex) {
//      log.debug("no sslContext: " + ex.getMessage(), ex);
//    }


 


    if (p7c != null) {
      log.debug("returning local ca certificate");
      response.setContentType("application/x-x509-ca-cert"); 
      p7c.writeTo(response.getOutputStream());
      response.getOutputStream().flush();
    } else {
      log.debug("no local ca certificate, redirecting to " + HTTPS_REDIRECT);
      response.sendRedirect(HTTPS_REDIRECT);
    }

  }

  // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
  /**
   * Handles the HTTP <code>GET</code> method.
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
    processRequest(request, response);
  }

  /**
   * Handles the HTTP <code>POST</code> method.
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
    processRequest(request, response);
  }

  /**
   * Returns a short description of the servlet.
   * @return a String containing servlet description
   */
  @Override
  public String getServletInfo() {
    return "Short description";
  }// </editor-fold>
}
