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
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author clemens
 */
class SimpleHashDataTableModel extends DefaultTableModel {

  protected List<HashDataInput> signedReferences;
  
  protected Class[] types = new Class[]{
    java.lang.String.class
  };

  public SimpleHashDataTableModel(List<HashDataInput> signedReferences) {
    super(0, 1);
    this.signedReferences = signedReferences;
    for (HashDataInput hashDataInput : signedReferences) {
      
//      String desc = hashDataInput.getReferenceId() + " (" + hashDataInput.getMimeType() + ")";
      addRow(new Object[]{hashDataInput});
    }
  }

  @Override
  public Class getColumnClass(int columnIndex) {
    return types[columnIndex];
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    if (columnIndex == 1)
      return true;
    return false;
  }
}