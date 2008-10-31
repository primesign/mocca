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

public class BKUGUIFactory {
  
  public static final String SIMPLE_GUI = "simple";
  public static final String ADVANCED_GUI = "advanced";
  
  private static BKUGUIFactory instance = new BKUGUIFactory();

  private BKUGUIFactory() {
  }

  protected BKUGUIFacade createNewGUI(String style) {
    if (ADVANCED_GUI.equals(style)) {
      return new BKUGUI();
    }
    return new SimpleGUI();
  }

  public static BKUGUIFacade createGUI(String style) {
    return instance.createNewGUI(style);
  }
}
