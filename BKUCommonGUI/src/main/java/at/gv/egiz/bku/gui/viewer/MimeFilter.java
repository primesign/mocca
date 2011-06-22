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


package at.gv.egiz.bku.gui.viewer;

import java.io.File;
import java.util.ResourceBundle;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author clemens
 */
class MimeFilter extends FileFilter {

  protected String mimeType;
  protected ResourceBundle messages;

  public MimeFilter(String mimeType, ResourceBundle messages) {
    this.mimeType = mimeType;
    this.messages = messages;
  }

  @Override
  public boolean accept(File f) {

    if (f.isDirectory()) {
      return true;
    }
    return MimeTypes.getExtension(mimeType).equalsIgnoreCase(getExtension(f));
  }

  private String getExtension(File f) {
    String ext = null;
    String s = f.getName();
    int i = s.lastIndexOf('.');

    if (i > 0 && i < s.length() - 1) {
      ext = s.substring(i + 1).toLowerCase();
    }
    return ext;
  }

  @Override
  public String getDescription() {
    return messages.getString(MimeTypes.getDescriptionKey(mimeType));
  }

  public static String getExtension(String mimeType) {
    return MimeTypes.getExtension(mimeType);
  }
}