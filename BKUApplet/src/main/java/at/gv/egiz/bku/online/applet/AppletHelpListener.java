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

import at.gv.egiz.bku.gui.AbstractHelpListener;
import java.applet.AppletContext;
import java.net.URL;

/**
 *
 * @author clemens
 */
public class AppletHelpListener extends AbstractHelpListener {

  protected AppletContext ctx;

  public AppletHelpListener(AppletContext ctx, URL helpURL, String locale) {
    super(helpURL, locale);
    if (ctx == null) {
      throw new RuntimeException("no applet context provided");
    }
    this.ctx = ctx;
  }

  @Override
  public void showDocument(URL helpDocument) throws Exception {
    ctx.showDocument(helpDocument, "_blank");
  }

  
}
