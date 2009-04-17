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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.bku.binding.HTTPBindingProcessor;
import at.gv.egiz.bku.binding.HttpUtil;
import at.gv.egiz.bku.conf.Configurator;
import at.gv.egiz.org.apache.tomcat.util.http.AcceptLanguage;

public class BKURequestHandler extends SpringBKUServlet {

	public final static String ENCODING = "UTF-8";

	protected Log log = LogFactory.getLog(BKURequestHandler.class);

	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, java.io.IOException {

        log.debug("Received SecurityLayer request");

        String acceptLanguage = req.getHeader("Accept-Language");
        Locale locale = AcceptLanguage.getLocale(acceptLanguage);
        log.debug("Accept-Language locale: " + locale);

        HTTPBindingProcessor bindingProcessor;
        bindingProcessor = (HTTPBindingProcessor) getBindingProcessorManager()
            .createBindingProcessor(req.getRequestURL().toString(), null, locale);
        Map<String, String> headerMap = new HashMap<String, String>();
        for (Enumeration<String> headerName = req.getHeaderNames(); headerName
            .hasMoreElements();) {
          String header = headerName.nextElement();
          if (header != null) {
            headerMap.put(header, req.getHeader(header));
          }
        }
        String charset = req.getCharacterEncoding();
        String contentType = req.getContentType();
        if (charset != null) {
          contentType += ";" + charset;
        }
        headerMap.put(HttpUtil.HTTP_HEADER_CONTENT_TYPE, contentType);
        bindingProcessor.setHTTPHeaders(headerMap);
        bindingProcessor.consumeRequestStream(req.getInputStream());

		// fixxme just for testing
		bindingProcessor.run();
		if (bindingProcessor.getRedirectURL() != null) {
			resp.sendRedirect(bindingProcessor.getRedirectURL());
			return;
		}
		resp.setStatus(bindingProcessor.getResponseCode());
		for (Iterator<String> it = bindingProcessor.getResponseHeaders().keySet()
				.iterator(); it.hasNext();) {
			String header = it.next();
			resp.setHeader(header, bindingProcessor.getResponseHeaders().get(header));
		}
		String sigLayout="";
		String version = configurator.getProperty(Configurator.SIGNATURE_LAYOUT);
		if ((version != null) && (!"".equals(version.trim()))) {
		  resp.setHeader(Configurator.SIGNATURE_LAYOUT, version);
		} else {
		  log.debug("Do not set siglayout header");
		}
			
        if (configurator.getProperty(Configurator.USERAGENT_CONFIG_P) != null) {
          resp.setHeader(HttpUtil.HTTP_HEADER_SERVER, configurator
              .getProperty(Configurator.USERAGENT_CONFIG_P));
        } else {
          resp.setHeader(HttpUtil.HTTP_HEADER_SERVER,
                  Configurator.USERAGENT_DEFAULT);
        }
		
		resp.setContentType(bindingProcessor.getResultContentType());
		resp.setCharacterEncoding(ENCODING);
		bindingProcessor.writeResultTo(resp.getOutputStream(), ENCODING);
		req.getInputStream().close();
		resp.getOutputStream().flush();
		resp.getOutputStream().close();
		log.debug("Finished Request");
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, java.io.IOException {
		doPost(req, resp);
	}
}
