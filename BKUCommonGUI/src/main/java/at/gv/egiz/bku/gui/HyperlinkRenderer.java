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

import at.gv.egiz.stal.HashDataInput;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class HyperlinkRenderer extends DefaultTableCellRenderer {

  private static final long serialVersionUID = 1L;
  
  protected boolean renderReferenceId;
  protected int fontSize;

  public HyperlinkRenderer(boolean renderReferenceId) {
    this.renderReferenceId = renderReferenceId;
    this.fontSize = super.getFont().getSize();
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
    super.setFont(super.getFont().deriveFont((float) (fontSize)));
    setForeground(BKUGUIFacade.HYPERLINK_COLOR);
  }
  
	public void setFontSize(int fontSize) {
		
		this.fontSize = fontSize;
	}
  
}
