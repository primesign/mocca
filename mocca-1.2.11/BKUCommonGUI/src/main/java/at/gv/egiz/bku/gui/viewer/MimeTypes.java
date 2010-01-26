/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.gv.egiz.bku.gui.viewer;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author clemens
 */
public class MimeTypes {

  private static final Map<String , String> FILE_EXTENSIONS = new HashMap<String, String>() {{
    put("application/msword", ".doc");
    put("application/octet-stream", ".bin");
    put("application/pdf", ".pdf");
    put("application/xhtml+xml", ".xhtml");
    put("text/html", ".html");
    put("text/plain", ".txt");
    put("text/xml", ".xml");
  }};

  private static final Map<String , String> DESCRIPTIONS = new HashMap<String, String>() {{
    put("application/msword", "mimetype.desc.doc");
    put("application/octet-stream", "mimetype.desc.bin");
    put("application/pdf", "mimetype.desc.pdf");
    put("application/xhtml+xml", "mimetype.desc.xhtml");
    put("text/html", "mimetype.desc.html");
    put("text/plain", "mimetype.desc.txt");
    put("text/xml", "mimetype.desc.xml");
  }};

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
