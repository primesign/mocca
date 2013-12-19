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

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author clemens
 */
public class MimeTypes {

  private static final Map<String , String> FILE_EXTENSIONS = new HashMap<String, String>() {
    private static final long serialVersionUID = 1L;
    {
      put("application/gzip", ".gz");
      put("application/msword", ".doc");
      put("application/octet-stream", ".bin");
      put("application/pdf", ".pdf");
      put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", ".docx");
      put("application/xhtml+xml", ".xhtml");
      put("application/xml", ".xml");
      put("application/zip", ".zip");
      put("image/gif", ".gif");
      put("image/jpeg", ".jpg");
      put("image/png", ".png");
      put("text/html", ".html");
      put("text/plain", ".txt");
      put("text/xml", ".xml");
    }
  };

  private static final Map<String , String> DESCRIPTIONS = new HashMap<String, String>() {
    private static final long serialVersionUID = 1L;
    {
      put("application/gzip", "mimetype.desc.gz");
      put("application/msword", "mimetype.desc.doc");
      put("application/octet-stream", "mimetype.desc.bin");
      put("application/pdf", "mimetype.desc.pdf");
      put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "mimetype.desc.docx");
      put("application/xhtml+xml", "mimetype.desc.xhtml");
      put("application/xml", "mimetype.desc.xml");
      put("application/zip", "mimetype.desc.zip");
      put("image/gif", "mimetype.desc.gif");
      put("image/jpeg", "mimetype.desc.jpg");
      put("image/png", "mimetype.desc.png");
      put("text/html", "mimetype.desc.html");
      put("text/plain", "mimetype.desc.txt");
      put("text/xml", "mimetype.desc.xml");
    }
  };

  public static String getExtension(String mimetype) {
    if (FILE_EXTENSIONS.containsKey(mimetype)) {
      return FILE_EXTENSIONS.get(mimetype);
    }
    return "";
  }

  /**
   * @return bundle key to be resolved in message resource bundle
   */
  public static String getDescriptionKey(String mimetype) {
    if (DESCRIPTIONS.containsKey(mimetype)) {
      return DESCRIPTIONS.get(mimetype);
    }
    return "mimetype.desc.unknown";
  }
}
