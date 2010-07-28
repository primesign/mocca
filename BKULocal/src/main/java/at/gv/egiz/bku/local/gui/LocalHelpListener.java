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
package at.gv.egiz.bku.local.gui;

import at.gv.egiz.bku.gui.ViewerHelpListener;
import java.awt.Desktop;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.net.URL;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Open help document in browser, fallback to help viewer (swing dialog) if Java Desktop API not supported.
 * 
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class LocalHelpListener extends ViewerHelpListener {

  private final Logger log = LoggerFactory.getLogger(LocalHelpListener.class);
  
  protected Desktop desktop;

  public LocalHelpListener(URL baseURL, Locale locale) {
    super(baseURL, locale);
    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
      desktop = Desktop.getDesktop();
    } else {
      log.warn("Java Desktop API not available on current platform (libgnome installed?), " +
              "falling back to help viewer");
    }
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    if (desktop != null) {
      try {
        desktop.browse(new URI(getHelpURL()));
      } catch (Exception ex) {
        log.error("Failed display help document {}.", getHelpURL(), ex);
        super.mouseClicked(e);
      }
    } else {
      super.mouseClicked(e);
    }
  }

  @Override
  public void keyPressed(KeyEvent e) {
    if (desktop != null) {
      try {
        desktop.browse(new URI(getHelpURL()));
      } catch (Exception ex) {
        log.error("Failed display help document {}.", getHelpURL(), ex);
        super.keyPressed(e);
      }
    } else {
      super.keyPressed(e);
    }
  }
}
