/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.gv.egiz.bku.online.applet;

import at.gv.egiz.bku.smccstal.SignRequestHandler;

/**
 *
 * @author clemens
 */
public class SignRequestHandlerFactory {

  static SignRequestHandler getInstance() {
    //TODO return ExternalDisplaySignRequestHandler by default, WebServiceSignRequestHandler if requested
    //TODO get configuration as param
    return null;
  }
}
