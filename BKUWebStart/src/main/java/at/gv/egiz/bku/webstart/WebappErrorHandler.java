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

package at.gv.egiz.bku.webstart;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.HttpMethods;
import org.mortbay.jetty.servlet.ErrorPageErrorHandler;
import org.mortbay.jetty.servlet.ServletHandler;

/**
 * @author tkellner_local
 *
 */
public class WebappErrorHandler extends ErrorPageErrorHandler {
	private String link404, link404help;

	public WebappErrorHandler(Locale locale) {
		super();

		link404 = "/help/404.html";
		link404help = "/help/404h.html";
		if (locale != null)
		{
			String language = locale.getLanguage();
			if (!language.isEmpty())
			{
				link404 = "/help/" + language + "/404.html";
				link404help = "/help/" + language + "/404h.html";
			}
		}
	}

	@Override
	public void handle(String target, HttpServletRequest request,
			HttpServletResponse response, int dispatch) throws IOException {
		String method = request.getMethod();
		if(!method.equals(HttpMethods.GET) && !method.equals(HttpMethods.POST))
		{
			HttpConnection.getCurrentConnection().getRequest().setHandled(true);
			return;
		}

		Integer code=(Integer)request.getAttribute(ServletHandler.__J_S_ERROR_STATUS_CODE);
		if (code.equals(404))
		{
			HttpConnection.getCurrentConnection().getRequest().setHandled(true);
			response.sendRedirect(request.getRequestURI().startsWith("/help") ? link404help : link404);
			return;
		}

		super.handle(target, request, response, dispatch);
	}
}
