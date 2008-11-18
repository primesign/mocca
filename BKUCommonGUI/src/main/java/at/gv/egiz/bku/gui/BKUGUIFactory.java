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

import java.awt.Container;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Locale;

public class BKUGUIFactory {
  
  public static BKUGUIFacade createGUI(Container contentPane, 
          Locale locale, 
          BKUGUIFacade.Style style, 
          URL background, 
          ActionListener helpListener) {
    return new BKUGUIImpl(contentPane, locale, style, background, helpListener);
  }
}
