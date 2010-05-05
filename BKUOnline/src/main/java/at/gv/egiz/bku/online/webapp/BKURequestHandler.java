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

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.bku.binding.HTTPBindingProcessor;
import at.gv.egiz.bku.binding.HTTPBindingProcessorImpl;

/**
 * Handles SL requests and instantiates BindingProcessors
 * 
 */
public class BKURequestHandler extends AbstractWebRequestHandler {
  
  private static final long serialVersionUID = 1L;
  
  private final Logger log = LoggerFactory.getLogger(BKURequestHandler.class);
  
  @Override
  protected void beforeAppletPage(HttpServletRequest req, HTTPBindingProcessor bindingProcessor) {
    // handle server side redirect url after processing
    String redirectUrl = ((HTTPBindingProcessorImpl) bindingProcessor).getRedirectURL(); 
    if ( redirectUrl != null) {
      log.info("Got redirect URL '{}'. Deferring browser redirect.", redirectUrl);
      req.getSession().setAttribute(REDIRECT_URL_SESSION_ATTRIBUTE, redirectUrl);
    }
  }

  @Override
  protected String getRequestProtocol(HttpServletRequest req) {
    return "HTTP";
  }

}
