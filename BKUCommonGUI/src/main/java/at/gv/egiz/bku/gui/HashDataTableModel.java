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
import java.util.List;
import javax.swing.table.DefaultTableModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
class HashDataTableModel extends DefaultTableModel {

  protected static final Log log = LogFactory.getLog(HashDataTableModel.class);
  
  /** HashDataInput in first column, register hyperlinkrenderer only here */
  protected Class[] types = new Class[]{ HashDataInput.class, String.class };
  protected List<HashDataInput> hashDataInputs;
  
  public HashDataTableModel(List<HashDataInput> hashDataInputs) {
    super(0, 2);
    this.hashDataInputs = hashDataInputs;
    for (HashDataInput hdi : hashDataInputs) {
      addRow(new Object[]{ hdi, hdi.getMimeType()});
    }
  }

  @Override
  public Class getColumnClass(int columnIndex) {
    return types[columnIndex];
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return false;
  }
}