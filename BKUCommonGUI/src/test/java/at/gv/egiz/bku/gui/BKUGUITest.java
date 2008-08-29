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

import javax.swing.JFrame;
import org.junit.Ignore;
import org.junit.Test;


/**
 *
 * @author clemens
 */
public class BKUGUITest {

    @Ignore
    public void testBKUGUI() {
        JFrame testFrame = new JFrame("BKUGUITest");
        BKUGUI gui = new BKUGUI();
        gui.init(testFrame.getContentPane(), null);
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
}
