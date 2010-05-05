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
package at.gv.egiz.bku.viewer;

import at.gv.egiz.bku.gui.viewer.FontProviderException;
import at.gv.egiz.bku.gui.viewer.FontProvider;
import java.awt.Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads font(s) as classpath resource.
 * Loaded fonts are shared within all instances in this VM (classloader)
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class ResourceFontLoader implements FontProvider {
  
  public static final String FONT_RESOURCE = "DejaVuLGCSansMono.ttf";

  private final Logger log = LoggerFactory.getLogger(ResourceFontLoader.class);

  /** TextValidator and (local) SecureViewerDialog (see LocalStalFactory) use ResourceFontLoader, load resource only once  */
  protected static Font font;

  /**
   *
   * @return
   * @throws FontProviderException encapsulating FontFormatException (if resource doesn't contain the plain format) 
   * or IOException (if resource cannot be retrieved)
   */
  @Override
  public Font getFont() throws FontProviderException {
    if (font == null) {
      try {
        if (log.isDebugEnabled()) {
          log.debug("loading " + getClass().getClassLoader().getResource(FONT_RESOURCE));
        }
        font = Font.createFont(Font.PLAIN, getClass().getClassLoader().getResourceAsStream(FONT_RESOURCE));
      } catch (Exception ex) {
        log.error("failed to load font", ex);
        throw new FontProviderException("failed to load font", ex);
      }
    }
    log.trace("font resource loaded");
    return font;
  }
}
