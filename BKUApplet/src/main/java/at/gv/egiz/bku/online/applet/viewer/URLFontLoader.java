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


package at.gv.egiz.bku.online.applet.viewer;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.bku.gui.viewer.FontProvider;
import at.gv.egiz.bku.gui.viewer.FontProviderException;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class URLFontLoader extends SwingWorker<Font, Object> implements FontProvider {

  private final Logger log = LoggerFactory.getLogger(URLFontLoader.class);
  
  protected URL fontURL;

  public URLFontLoader(URL codebase) throws MalformedURLException {
    this.fontURL = new URL(codebase, SANSMONO_FONT_RESOURCE);
    log.debug("[{}] setting font load URL: {}.", Thread.currentThread().getName(), fontURL);
  }

  public void loadInBackground() {
    log.debug("[{}] scheduling font loading in background: {}.", Thread.currentThread().getName(), fontURL);
    this.execute();
  }

  @Override
  protected Font doInBackground() throws MalformedURLException, FontFormatException, IOException {
    log.debug("[{}] loading font in background.", Thread.currentThread().getName());
    return Font.createFont(Font.TRUETYPE_FONT, fontURL.openStream());
  }

  /**
   * waits for loadInBackground to finish 
   * @return the font loaded in loadInbackground
   * @throws Exception
   */
  @Override
  public Font getFont() throws FontProviderException {
    log.debug("[{}] get font (EDT?)", Thread.currentThread().getName());
    try {
      return get();
    } catch (InterruptedException ex) {
      log.error("Font loader interrupted.");
      throw new FontProviderException("Font loader interrupted.", ex);
    } catch (ExecutionException ex) {
      log.error("Failed to load font. {}", ex.getCause());
      throw new FontProviderException("Failed to load font.", ex.getCause());
    }
  }
}
