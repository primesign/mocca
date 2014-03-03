package at.gv.egiz.bku.utils;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationUtil {
  public static List<String> getStringListFromObjectList(List<Object> olist) {
    List<String> slist = new ArrayList<String>();
    for (Object entry : olist)
      slist.add(entry == null ? null : entry.toString());
    return slist;
  }
}
