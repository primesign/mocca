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

package at.gv.egiz.bku.gui;

import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class BKUIcons {

  /** 128x128, 48x48, 32x32, 24x24, 16x16 pixels */
  public static final ArrayList<Image> icons = new ArrayList<Image>();

  static {
    String[] iconResources = new String[] {
      "/at/gv/egiz/bku/gui/chip128.png",
      "/at/gv/egiz/bku/gui/chip48.png",
      "/at/gv/egiz/bku/gui/chip32.png",
      "/at/gv/egiz/bku/gui/chip24.png",
      "/at/gv/egiz/bku/gui/chip16.png"};
    for (String ir : iconResources) {
      URL resource = BKUIcons.class.getResource(ir);
      if (ir != null) {
        try {
          icons.add(ImageIO.read(resource));
        } catch (IOException ex) {
          Logger log = LoggerFactory.getLogger(BKUIcons.class);
          log.warn("failed to load mocca icon " + ir, ex);
        }
      }
    }
  }
}
