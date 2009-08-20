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

import at.gv.egiz.bku.local.stal.LocalSTALFactory;
import at.gv.egiz.marshal.MarshallerFactory;
import at.gv.egiz.stal.QuitRequest;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.ext.PINManagementRequest;
import at.gv.egiz.stal.ext.PINManagementResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.apache.regexp.REUtil;

/**
 * PINManagementBKUWorker for non-applet version
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class PINManagementServlet extends HttpServlet {

//  static JAXBContext stalCtx;
  
  /**
   * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {

    LocalSTALFactory sf = new LocalSTALFactory();

    ArrayList<STALRequest> stalReqs = new ArrayList<STALRequest>();
    stalReqs.add(new PINManagementRequest());
    stalReqs.add(new QuitRequest());

    List<STALResponse> stalResps = sf.createSTAL().handleRequest(stalReqs); 

    String redirect = request.getParameter("redirect");
    if (redirect != null) {
      String referer = request.getHeader("Referer");
      if (referer != null) {
        redirect = new URL(new URL(referer), redirect).toExternalForm();
      }
      response.sendRedirect(redirect);
    } else {
      response.setStatus(HttpServletResponse.SC_OK);
//      if (stalResps.get(0) != null) {
//        PrintWriter out = response.getWriter();
//        try {
//          response.setContentType("text/xml;charset=UTF-8");
//          // cannot directly marshal STALResponse, no ObjectFactory in at.gv.egiz.stal
//          if (stalCtx == null) {
//            stalCtx = JAXBContext.newInstance("at.gv.egiz.stal:at.gv.egiz.stal.ext");
//          }
//          Marshaller m = MarshallerFactory.createMarshaller(stalCtx);
//          m.marshal(stalResps.get(0), out);
//          out.close();
//        } catch (JAXBException ex) {
//          throw new ServletException("Failed to marshal STAL response", ex);
//        } finally {
//          out.close();
//        }
//      } else {
//        throw new ServletException("internal error");
//      }
    }

    
//    try {
//      out.println("<html>");
//      out.println("<head>");
//      out.println("<title>Servlet PINManagementServlet</title>");
//      out.println("</head>");
//      out.println("<body>");
//      out.println("<h1>Servlet PINManagementServlet at " + request.getContextPath() + "</h1>");
//      out.println("<p>" + stalResps.size() + " responses:<ul>");
//      for (STALResponse resp : stalResps) {
//        out.println(" <li>" + resp.getClass());
//      }
//      Enumeration<String> headers = request.getHeaderNames();
//      out.println("</ul></p><p> headers: <ul>");
//      while (headers.hasMoreElements()) {
//        String header = headers.nextElement();
//        out.println("<li> " + header + ": " + request.getHeader(header));
//      }
//      Enumeration<String> params = request.getParameterNames();
//      out.println("</ul></p><p> params: <ul>");
//      while (params.hasMoreElements()) {
//        String param = params.nextElement();
//        out.println("<li> " + param + ": " + request.getParameter(param));
//      }
//      out.println("</ul></p></body>");
//      out.println("</html>");
//    } finally {
//      out.close();
//    }
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
