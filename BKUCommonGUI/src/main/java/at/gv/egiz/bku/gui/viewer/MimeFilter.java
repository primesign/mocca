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
package at.gv.egiz.bku.gui.viewer;

import java.io.File;
import java.util.ResourceBundle;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author clemens
 */
class MimeFilter extends FileFilter {

  private static final String MIMETYPE_DESC_XML = "mimetype.desc.xml";
  private static final String MIMETYPE_DESC_HTML = "mimetype.desc.html";
  private static final String MIMETYPE_DESC_XHTML = "mimetype.desc.xhtml";
  private static final String MIMETYPE_DESC_TXT = "mimetype.desc.txt";
  private static final String MIMETYPE_DESC_PDF = "mimetype.desc.pdf";
  private static final String MIMETYPE_DESC_BIN = "mimetype.desc.bin";
  private static final String MIMETYPE_DESC_UNKNOWN = "mimetype.desc.unknown";

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

    String ext = getExtension(f);
    if ("text/xml".equals(mimeType)) {
      return "xml".equalsIgnoreCase(ext);
    } else if ("text/html".equals(mimeType)) {
      return "html".equalsIgnoreCase(ext) || "htm".equalsIgnoreCase(ext);
    } else if ("application/xhtml+xml".equals(mimeType)) {
      return "xhtml".equalsIgnoreCase(ext);
    } else if ("text/plain".equals(mimeType)) {
      return "txt".equalsIgnoreCase(ext);
    } else if ("application/pdf".equals(mimeType)) {
      return "pdf".equalsIgnoreCase(ext);
    } else {
      return true;
    }
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
    if ("text/xml".equals(mimeType)) {
      return messages.getString(MIMETYPE_DESC_XML);
    } else if ("text/html".equals(mimeType)) {
      return messages.getString(MIMETYPE_DESC_HTML);
    } else if ("application/xhtml+xml".equals(mimeType)) {
      return messages.getString(MIMETYPE_DESC_XHTML);
    } else if ("text/plain".equals(mimeType)) {
      return messages.getString(MIMETYPE_DESC_TXT);
    } else if ("application/pdf".equals(mimeType)) {
      return messages.getString(MIMETYPE_DESC_PDF);
    } else if ("application/octet-stream".equals(mimeType)) {
      return messages.getString(MIMETYPE_DESC_BIN);
    } else {
      return messages.getString(MIMETYPE_DESC_UNKNOWN);
    }
  }

  public static String getExtension(String mimeType) {
    if ("text/xml".equals(mimeType)) {
      return ".xml";
    } else if ("text/html".equals(mimeType)) {
      return ".html";
    } else if ("application/xhtml+xml".equals(mimeType)) {
      return ".xhtml";
    } else if ("text/plain".equals(mimeType)) {
      return ".txt";
    } else if ("application/pdf".equals(mimeType)) {
      return ".pdf";
    } else if ("application/octet-stream".equals(mimeType)) {
      return ".bin";
    } else {
      return "";
    }
  }
}