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
import at.gv.egiz.smcc.PINSpec;
import java.util.Map;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class PINStatusTableModel extends DefaultTableModel {

//  protected static final Log log = LogFactory.getLog(PINStatusTableModel.class);
  protected Class[] types;

  public PINStatusTableModel(Map<PINSpec, STATUS> pinStatuses) {
    super(0, 2);
    if (pinStatuses == null) {
      throw new RuntimeException("pinStatuses must not be null");
    }
//    log.trace(pinStatuses.size() + " PINs");
    types = new Class[] { PINSpec.class, STATUS.class };
    for (PINSpec pinSpec : pinStatuses.keySet()) {
      addRow(new Object[] { pinSpec, pinStatuses.get(pinSpec) });
    }
//    PINSpec activePIN = new PINSpec(0, 1, null, "active-PIN", (byte) 0x01);
//    PINSpec blockedPIN = new PINSpec(0, 1, null, "blocked-PIN", (byte) 0x01);
//    addRow(new Object[] { activePIN, PINStatusProvider.STATUS.ACTIV });
//    addRow(new Object[] { blockedPIN, PINStatusProvider.STATUS.BLOCKED });
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
