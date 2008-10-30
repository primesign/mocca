/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.gv.egiz.bku.online.applet;

import java.applet.AppletContext;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author clemens
 */
public class ExternalHelpListener implements ActionListener {

  protected final static Log log = LogFactory.getLog(ExternalHelpListener.class);
  protected AppletContext ctx;
  protected String helpURLBase;
  protected String locale;

  public ExternalHelpListener(AppletContext ctx, URL helpURL, String locale) {
    if (ctx == null) {
      throw new RuntimeException("no applet context provided");
    }
    if (helpURL == null || "".equals(helpURL)) {
      throw new RuntimeException("no help URL provided");
    }
    this.ctx = ctx;
    this.helpURLBase = helpURL.toString();
    this.locale = locale;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    log.debug("received help action: " + e.getActionCommand());
    URL helpURL;
    try {
      String urlString = helpURLBase;
      if (locale != null) {
        urlString = appendParameter(urlString, "locale", locale);
      } 
      if (e.getActionCommand() != null && !"".equals(e.getActionCommand())) {
        urlString = appendParameter(urlString, "topic", e.getActionCommand());
      }
      helpURL = new URL(urlString);
    } catch (MalformedURLException ex) {
      try {
        log.error("failed to create help URL: " + ex.getMessage());
        helpURL = new URL(helpURLBase);
      } catch (MalformedURLException ex1) {
        log.error("failed to create default help URL, requested help will not be displayed");
        return;
      }
    }
    ctx.showDocument(helpURL, "_blank");
  }
  
  private String appendParameter(String url, String paramName, String paramValue) {
    if (url.indexOf('?') < 0) {
      return url + "?" + paramName + "=" + paramValue;
    } else {
      return url + "&" + paramName + "=" + paramValue;
    }
  }
}
