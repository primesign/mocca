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

import at.gv.egiz.smcc.PinInfo;
import java.awt.Color;
import java.awt.Font;
import java.util.ResourceBundle;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class PINStatusRenderer extends DefaultTableCellRenderer {
  
  private static final long serialVersionUID = 1L;
  
  public static final Color RED = new Color(0.9f, 0.0f, 0.0f);
  public static final Color GREEN = new Color(0.0f, 0.8f, 0.0f);
  protected ResourceBundle messages;
  protected int fontSize;

  public PINStatusRenderer(ResourceBundle messages) {
    this.messages = messages;
    this.fontSize = super.getFont().getSize();
  }

  @Override
  protected void setValue(Object value) {
    PinInfo.STATE pinStatus = ((PinInfo) value).getState();
    
    super.setFont(super.getFont().deriveFont(super.getFont().getStyle() | Font.BOLD));
    super.setFont(super.getFont().deriveFont((float) (fontSize)));
      
    if (pinStatus == PinInfo.STATE.NOT_ACTIV) {
      super.setForeground(RED);
      super.setText("<html>" + messages.getString(PINManagementGUIFacade.STATUS_NOT_ACTIVE));
    } else if (pinStatus == PinInfo.STATE.ACTIV) {
      super.setForeground(GREEN);
      super.setText("<html>" + messages.getString(PINManagementGUIFacade.STATUS_ACTIVE) + " (" + ((PinInfo) value).getRetries() + ")");
    } else if (pinStatus == PinInfo.STATE.BLOCKED) {
      super.setForeground(RED);
      super.setText("<html>" + messages.getString(PINManagementGUIFacade.STATUS_BLOCKED));
    } else if (pinStatus == PinInfo.STATE.UNKNOWN) {
      super.setForeground(Color.BLACK);
      super.setText("<html>" + messages.getString(PINManagementGUIFacade.STATUS_UNKNOWN));
    } else {
      super.setForeground(Color.RED);
      super.setText("<html>" + messages.getString(PINManagementGUIFacade.STATUS_UNKNOWN));
    }
  }
  
	public void setFontSize(int fontSize) {
		
		this.fontSize = fontSize;
	}
}
