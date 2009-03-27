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

import at.gv.egiz.bku.gui.PINManagementGUIFacade.STATUS;
import java.awt.Color;
import java.awt.Font;
import java.util.ResourceBundle;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class PINStatusRenderer extends DefaultTableCellRenderer {
  
//  private static final Log log = LogFactory.getLog(PINStatusRenderer.class);

  public static final Color RED = new Color(0.9f, 0.0f, 0.0f);
  public static final Color GREEN = new Color(0.0f, 0.8f, 0.0f);
  protected ResourceBundle messages;

  public PINStatusRenderer(ResourceBundle messages) {
    this.messages = messages;
  }

  @Override
  protected void setValue(Object value) {
    STATUS pinStatus = (STATUS) value;
    super.setFont(super.getFont().deriveFont(super.getFont().getStyle() | Font.BOLD));
      
    if (pinStatus == STATUS.NOT_ACTIV) {
      super.setForeground(RED);
      super.setText("<html>" + messages.getString(PINManagementGUIFacade.STATUS_NOT_ACTIVE) + "</html>");
    } else if (pinStatus == STATUS.ACTIV) {
      super.setForeground(GREEN);
      super.setText("<html>" + messages.getString(PINManagementGUIFacade.STATUS_ACTIVE) + "</html>");
    } else if (pinStatus == STATUS.BLOCKED) {
      super.setForeground(RED);
      super.setText("<html>" + messages.getString(PINManagementGUIFacade.STATUS_BLOCKED) + "</html>");
    } else {
      super.setForeground(Color.BLACK);
      super.setText("<html>" + messages.getString(PINManagementGUIFacade.STATUS_UNKNOWN) + "</html>");
    }
  }
}
