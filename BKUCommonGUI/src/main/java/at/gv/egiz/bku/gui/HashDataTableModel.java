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
import java.util.List;
import javax.swing.table.DefaultTableModel;

/**
 * 
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
class HashDataTableModel extends DefaultTableModel {

  private static final long serialVersionUID = 1L;

  /** HashDataInput in first column, register hyperlinkrenderer only here */
  protected Class<?>[] types;
  protected List<HashDataInput> hashDataInputs;

  public HashDataTableModel(List<HashDataInput> hashDataInputs, boolean twoColLayout) {
    super(0, (twoColLayout) ? 2 : 1);
    this.hashDataInputs = hashDataInputs;

    if (twoColLayout) {
      types = new Class[] { HashDataInput.class, String.class };
      for (HashDataInput hdi : hashDataInputs) {
        addRow(new Object[] { hdi, hdi.getMimeType() });
      }
    } else {
      types = new Class[] { HashDataInput.class };
      for (HashDataInput hdi : hashDataInputs) {
        addRow(new Object[] { hdi });
      }
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
