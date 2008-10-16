/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.gv.egiz.bku.online.applet;

import java.applet.AppletContext;
import java.net.URL;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.bku.smccstal.SignRequestHandler;
import at.gv.egiz.stal.signedinfo.ReferenceType;

/**
 *
 * @author clemens
 */
public class ExternalDisplaySignRequestHandler extends SignRequestHandler {

  private static final Log log = LogFactory.getLog(ExternalDisplaySignRequestHandler.class);
  
  AppletContext ctx;
  URL hashDataURL;

  public ExternalDisplaySignRequestHandler(AppletContext ctx, URL hashDataURL) {
    this.ctx = ctx;
    this.hashDataURL = hashDataURL;
  }

  @Override
  public void displayHashDataInputs(List<ReferenceType> signedReferences) throws Exception {
    //TODO pass reference Id's to servlet (TODO servlet)
    log.debug("displaying hashdata inputs at " + hashDataURL);
    ctx.showDocument(hashDataURL, "_blank");
  }

}
