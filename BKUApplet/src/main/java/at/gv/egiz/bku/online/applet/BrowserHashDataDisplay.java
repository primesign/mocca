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

package at.gv.egiz.bku.online.applet;

import java.net.URL;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.bku.smccstal.SignRequestHandler;
import at.gv.egiz.stal.signedinfo.ReferenceType;
import java.applet.AppletContext;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class BrowserHashDataDisplay extends SignRequestHandler {

  private static final Log log = LogFactory.getLog(BrowserHashDataDisplay.class);
  
  protected AppletContext ctx;
  protected URL hashDataURL;

  public BrowserHashDataDisplay(AppletContext ctx, URL hashDataURL) {
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
