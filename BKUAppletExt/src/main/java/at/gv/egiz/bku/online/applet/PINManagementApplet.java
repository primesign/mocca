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
import at.gv.egiz.bku.gui.BKUGUIFacade;
import at.gv.egiz.bku.gui.PINManagementGUI;
import at.gv.egiz.bku.gui.PINManagementGUIFacade;
import at.gv.egiz.bku.gui.SwitchFocusListener;
import java.awt.Container;
import java.net.URL;
import java.util.Locale;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class PINManagementApplet extends BKUApplet {

  private static final long serialVersionUID = 1L;
  private static Log log = LogFactory.getLog(PINManagementApplet.class);

  @Override
  protected BKUGUIFacade createGUI(Container contentPane,
          Locale locale,
          BKUGUIFacade.Style guiStyle,
          URL backgroundImgURL,
          AbstractHelpListener helpListener,
          SwitchFocusListener switchFocusListener) {
    return new PINManagementGUI(contentPane, locale, guiStyle, backgroundImgURL, helpListener, switchFocusListener);
  }

  @Override
  protected AppletBKUWorker createBKUWorker(BKUApplet applet, BKUGUIFacade gui) {
    return new PINManagementBKUWorker(applet, (PINManagementGUIFacade) gui);
  }
}
