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
import at.gv.egiz.bku.gui.ViewerDialog;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import javax.swing.SwingUtilities;

/**
 * This class depends on BKU utils, and therefore is not part of BKUCommonGUI
 * 
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class DefaultHelpListener extends AbstractHelpListener {

  public DefaultHelpListener(URL helpURL, Locale locale) {
    super(helpURL, locale);
  }

  @Override
  public void showDocument(URL helpURL, final String helpTopic) throws Exception {
    log.debug("open connection " + helpURL);
    URLConnection conn = helpURL.openConnection();
    
    log.debug("show help document " + conn.getContentType()); // + ";" + conn.getContentEncoding());
    
//    Charset cs;
//    if (conn.getContentEncoding() == null) {
//      cs = Charset.forName("UTF-8");
//    } else {
//      try {
//        cs = Charset.forName(conn.getContentEncoding());
//      } catch (Exception ex) {
//        log.debug("charset " + conn.getContentEncoding() + " not supported, assuming UTF-8: " + ex.getMessage());
//        cs = Charset.forName("UTF-8");
//      }  
//    }
    
//    InputStreamReader isr = new InputStreamReader(conn.getInputStream(), cs);
//    final Reader content = new BufferedReader(isr);
    final InputStream content = conn.getInputStream();
    final String mimeType = conn.getContentType();
      
    log.debug("schedule help dialog");
    
    SwingUtilities.invokeLater(new Runnable() {

        @Override
        public void run() {
          
          log.debug("show help dialog");
          
          ViewerDialog.showHelp(null, helpTopic, content, mimeType, messages);
      
        }
      });
//    gui.showHelpDialog(helpDocument.getStream(), mimetype, encoding);
  }
}
