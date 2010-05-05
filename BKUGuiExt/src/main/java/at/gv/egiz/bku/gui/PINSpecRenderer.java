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
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class PINSpecRenderer extends DefaultTableCellRenderer {

  private static final long serialVersionUID = 1L;
  
  protected int fontSize;
  
	public PINSpecRenderer() {

		this.fontSize = super.getFont().getSize();
	}
  
  @Override
  protected void setValue(Object value) {
    PinInfo pinSpec = (PinInfo) value;
    super.setText(pinSpec.getLocalizedName());
    super.setFont(super.getFont().deriveFont((float) (fontSize)));
  }

	public void setFontSize(int fontSize) {

		this.fontSize = fontSize;
	}
  
}
