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
