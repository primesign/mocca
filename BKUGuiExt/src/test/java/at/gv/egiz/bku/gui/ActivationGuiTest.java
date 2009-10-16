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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.gv.egiz.bku.gui;

import at.gv.egiz.bku.gui.*;
import java.awt.Container;
import java.awt.Dimension;
import javax.swing.JFrame;
import org.junit.Ignore;
import org.junit.Test;


/**
 *
 * @author clemens
 */
@Ignore
public class ActivationGuiTest {

    @Test
    public void testBKUGUI() {
        JFrame testFrame = new JFrame("BKUGUITest");
        Container contentPane = testFrame.getContentPane();
        contentPane.setPreferredSize(new Dimension(152, 145));
//        contentPane.setPreferredSize(new Dimension(300, 190));
        ActivationGUIFacade gui = new ActivationGUI(contentPane, null, BKUGUIFacade.Style.tiny, null, null, null);
        BKUGUIWorker worker = new BKUGUIWorker();
        worker.init(gui);
        testFrame.pack();
        testFrame.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        testFrame.setVisible(true);
        new Thread(worker).start();
        
        while(true) ;
    }
    
    @Test
    public void dummyTest() {
    }
    
//    public static void main(String[] args) {
//        new BKUGUITest().testBKUGUI();
//    }
}
