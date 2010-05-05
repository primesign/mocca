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
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class PINStatusTableModel extends DefaultTableModel {

  private static final long serialVersionUID = 1L;

  protected Class<?>[] types;

  public PINStatusTableModel(PinInfo[] pinSpecs) {
    super(0, 2);
    types = new Class<?>[] { String.class, PinInfo.class };
    for (PinInfo pinSpec : pinSpecs) {
      addRow(new Object[] { pinSpec.getLocalizedName(), pinSpec });
    }
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return types[columnIndex];
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return false;
  }
}
