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


package at.gv.egiz.bku.online.webapp;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.MDC;

import at.gv.egiz.bku.binding.Id;
import at.gv.egiz.bku.binding.IdFactory;

/**
 * Servlet Filter implementation class BindingProcessorFilter
 */
public class TransactionIdFilter implements Filter {
  
  /**
   * @see Filter#destroy()
   */
  public void destroy() {
  }

  /**
   * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
   */
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException {

    if (request instanceof HttpServletRequest) {
      HttpSession session = ((HttpServletRequest) request).getSession();
      
      String tidx = null;
      // We expect the transaction index parameter to appear in GET requests only
      if ("GET".equals(((HttpServletRequest) request).getMethod())) {
        tidx = request.getParameter("tidx");
      }

      if (tidx == null) {
        TransactionId transactionIndex = (TransactionId) session
            .getAttribute(TransactionId.TRANSACTION_INDEX);
        if (transactionIndex != null) {
          tidx = Integer.toString(transactionIndex.next());
        } else {
          tidx = "0";
        }
      }
      
      Id id = IdFactory.getInstance().createId(session.getId() + "-" + tidx);
      MDC.put("id", id.toString());
      request.setAttribute("id", id);
      
      response = new TransactionIdResponseWrapper((HttpServletResponse) response, session.getId(), tidx);
    }

    // pass the request along the filter chain
    try {
      chain.doFilter(request, response);
    } finally {
      MDC.remove("id");
    }
    
  }

  /**
   * @see Filter#init(FilterConfig)
   */
  public void init(FilterConfig fConfig) throws ServletException {
  }
  
} 
