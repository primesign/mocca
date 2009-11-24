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

import at.gv.egiz.stal.HashDataInput;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class HyperlinkRenderer extends DefaultTableCellRenderer {

  protected boolean renderReferenceId;

  public HyperlinkRenderer(boolean renderReferenceId) {
    this.renderReferenceId = renderReferenceId;
  }

  /**
   * cannot change mouse cursor here, do in jTable
   * @param value
   */
  @Override
  protected void setValue(Object value) {
    String hrefText;
    if (((HashDataInput) value).getFilename() != null) {
      hrefText = ((HashDataInput) value).getFilename();
    } else {
      if (renderReferenceId) {
        hrefText = ((HashDataInput) value).getReferenceId();
      } else {
        hrefText = ((HashDataInput) value).getMimeType();
      }
    }
    super.setText("<html><u>" + hrefText + "</u></html>");
    setForeground(BKUGUIFacade.HYPERLINK_COLOR);
  }
}
