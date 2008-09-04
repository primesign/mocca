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
package at.gv.egiz.bku.online.webapp;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.bku.binding.HTTPBindingProcessor;
import at.gv.egiz.bku.binding.HttpUtil;
import at.gv.egiz.org.apache.tomcat.util.http.AcceptLanguage;

/**
 * Handles SL requests and instantiates BindingProcessors
 * 
 */
public class BKURequestHandler extends SpringBKUServlet {

	public final static String REDIRECT_URL = "appletPage.jsp";

	protected Log log = LogFactory.getLog(BKURequestHandler.class);

	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, java.io.IOException {
		log.debug("Got new request");
		String lang = req.getHeader("Accept-Language");
		Locale locale = AcceptLanguage.getLocale(lang);
		log.debug("Using locale: " + locale);
		HttpSession session = req.getSession();
		if (session != null) {
			session.invalidate();
		}
		String id = req.getSession(true).getId();
		log.debug("Using session id: " + id);
		HTTPBindingProcessor bindingProcessor;

		bindingProcessor = (HTTPBindingProcessor) getBindingProcessorManager()
				.createBindingProcessor(req.getRequestURL().toString(), id, locale);

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
		req.getInputStream().close();
		getBindingProcessorManager().process(bindingProcessor);
		resp.sendRedirect(REDIRECT_URL);
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, java.io.IOException {
		doPost(req, resp);
	}
}
