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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.bku.binding.BindingProcessor;
import at.gv.egiz.bku.binding.BindingProcessorManager;
import at.gv.egiz.bku.binding.BindingProcessorManagerImpl;
import at.gv.egiz.bku.binding.HTTPBindingProcessor;
import at.gv.egiz.bku.binding.Id;
//import java.net.MalformedURLException;
//import java.net.URL;

public class UIServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  private final Logger log = LoggerFactory.getLogger(UIServlet.class);

  private String expiredPageUrl = "expired.jsp";

  @Override
  public void init() throws ServletException {
    String url = MoccaParameterBean.getInitParameter("expiredPageUrl", getServletConfig(), getServletContext());
    if (url != null) {
//      try {
//        expiredPageUrl = new URL(url).toString();
//        log.info("Init expiredPageUrl to: {}.", expiredPageUrl);
//      } catch (MalformedURLException e) {
//        log.error("Failed to set expiredUrlPage '{}': {}.", url, e);
//      }
      expiredPageUrl = url;
      log.info("Init expiredPageUrl to: {}.", expiredPageUrl);
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    BindingProcessorManager bindingProcessorManager = (BindingProcessorManager) getServletContext()
        .getAttribute("bindingProcessorManager");
    if (bindingProcessorManager == null) {
      String msg = "Configuration error: BindingProcessorManager missing!";
      log.error(msg);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg);
      return;
    }

    Configuration conf = ((BindingProcessorManagerImpl) bindingProcessorManager).getConfiguration();
    if (conf == null)
      log.error("No configuration");
    else
      MoccaParameterBean.setP3PHeader(conf, resp);

    Id id = (Id) req.getAttribute("id");
    BindingProcessor bindingProcessor = null;
    if (id == null
        || !((bindingProcessor = bindingProcessorManager
            .getBindingProcessor(id)) instanceof HTTPBindingProcessor)) {
      resp.sendRedirect(expiredPageUrl);
      return;
    }
    
    MoccaParameterBean parameterBean = new MoccaParameterBean((HTTPBindingProcessor) bindingProcessor);
    req.setAttribute("moccaParam", parameterBean);
    
    String uiPage = MoccaParameterBean.getInitParameter("uiPage", getServletConfig(), getServletContext());
    uiPage = parameterBean.getUIPage(uiPage);
    if (uiPage == null) {
      uiPage = "applet.jsp";
    }

    RequestDispatcher dispatcher = req.getRequestDispatcher(uiPage);
    if (dispatcher == null) {
      log.warn("Failed to get RequestDispatcher for page {}.", uiPage);
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    } else {
      dispatcher.forward(req, resp);
    }

  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    BindingProcessorManager bindingProcessorManager = (BindingProcessorManager) getServletContext()
            .getAttribute("bindingProcessorManager");
    if (bindingProcessorManager == null) {
      String msg = "Configuration error: BindingProcessorManager missing!";
      log.error(msg);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg);
      return;
    }

    Configuration conf = ((BindingProcessorManagerImpl) bindingProcessorManager).getConfiguration();
    if (conf == null)
      log.error("No configuration");
    else
      MoccaParameterBean.setP3PHeader(conf, resp);

    super.doPost(req, resp);
  }

}
