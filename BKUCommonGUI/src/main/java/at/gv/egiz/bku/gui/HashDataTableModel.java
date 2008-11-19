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
  
  protected Class[] types = new Class[]{ String.class, String.class };
  protected List<HashDataInput> hashDataInputs;
  
  public HashDataTableModel(List<HashDataInput> hashDataInputs) {
    super(0, 2);
    this.hashDataInputs = hashDataInputs;
    for (HashDataInput hdi : hashDataInputs) {
      addRow(new Object[]{hdi.getReferenceId(), hdi.getMimeType()});
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

//  public HashDataInput getValue(int rowIndex) {
//    return hashDataInputs.get(rowIndex);
//  }
//  

//  public class HashDataLinkRenderer extends JLabel
//          implements TableCellRenderer {
////        extends DefaultTableCellRenderer {
//    
////    protected ActionListener saveHashDataListener;
////
////    public HashDataLinkRenderer(ActionListener saveHashDataListener) {
////      this.saveHashDataListener = saveHashDataListener;
////    }
//    
//    @Override
//    public Component getTableCellRendererComponent(JTable table,
//            Object value,
//            boolean isSelected,
//            boolean hasFocus,
//            final int row,
//            int column) {
//      final HashDataInput hdi = (HashDataInput) value;
//      log.debug("render hashdatainput " + hdi.getReferenceId() + " - (" + row + "," + column + ") " + isSelected + hasFocus);
//      setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//      setFont(getFont().deriveFont(getFont().getStyle() & ~java.awt.Font.BOLD));
//      setText(hdi.getReferenceId() + " (" + hdi.getMimeType() + ")");
//      addMouseListener(new MouseAdapter() {
//
//        @Override
//        public void mouseClicked(MouseEvent e) {
//          log.debug("received mouseclick on " + hdi.getReferenceId());
////          saveHashDataListener.actionPerformed();
//          JOptionPane.showInputDialog(hashDataInputs.get(row).getReferenceId());
//        }
//        
//      });
//
//      return this;
//    }
//  }
}