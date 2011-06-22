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
