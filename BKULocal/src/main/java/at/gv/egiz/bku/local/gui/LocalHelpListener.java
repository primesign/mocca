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

import at.gv.egiz.bku.gui.AbstractHelpListener;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class LocalHelpListener extends AbstractHelpListener {

  public LocalHelpListener(URL baseURL, String locale) {
    super(baseURL, locale);
  }

  @Override
  public void showDocument(URL helpDocument) throws IOException, URISyntaxException {
    Desktop.getDesktop().browse(helpDocument.toURI());
  }

}
