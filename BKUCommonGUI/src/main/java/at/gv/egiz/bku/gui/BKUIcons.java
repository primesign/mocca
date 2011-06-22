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
