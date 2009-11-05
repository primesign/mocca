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
package at.gv.egiz.bku.online.applet.viewer;

import at.gv.egiz.bku.gui.viewer.FontProviderException;
import at.gv.egiz.bku.gui.viewer.FontProvider;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class URLFontLoader extends SwingWorker<Font, Object> implements FontProvider {

  protected static final Log log = LogFactory.getLog(URLFontLoader.class);
  protected URL fontURL;
  protected Font font;

  public URLFontLoader(URL codebase) throws MalformedURLException {
    this.fontURL = new URL(codebase, SANSMONO_FONT_RESOURCE);
    if (log.isDebugEnabled()) {
      log.debug(Thread.currentThread() + " setting font load URL: " + fontURL);
    }
  }

  public void loadInBackground() {
    if (log.isDebugEnabled()) {
      log.debug(Thread.currentThread() + " scheduling font loading in background: " + fontURL);
    }
    this.execute();
  }

  @Override
  protected Font doInBackground() throws MalformedURLException, FontFormatException, IOException {
    if (log.isDebugEnabled()) {
      log.debug(Thread.currentThread() + " loading font in background...");
    }
    return Font.createFont(Font.TRUETYPE_FONT, fontURL.openStream());
  }

  /**
   * waits for loadInBackground to finish 
   * @return the font loaded in loadInbackground
   * @throws Exception
   */
  @Override
  public Font getFont() throws FontProviderException {
    log.debug(Thread.currentThread() + " get font");
    try {
      return get();
    } catch (InterruptedException ex) {
      log.error("font loader interrupted");
//      Thread.currentThread().interrupt();
      throw new FontProviderException("font loader interrupted", ex);
    } catch (ExecutionException ex) {
      log.error("failed to load font", ex.getCause());
      throw new FontProviderException("failed to load font", ex.getCause());
    }
  }
}
