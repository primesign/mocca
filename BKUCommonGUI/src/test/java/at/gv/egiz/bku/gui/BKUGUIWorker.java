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

import at.gv.egiz.smcc.PINSpec;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;

/**
 *
 * @author clemens
 */
public class BKUGUIWorker implements Runnable {

    BKUGUIFacade gui;

    public void init(BKUGUIFacade gui) {
        this.gui = gui;
    }

    @Override
    public void run() {
        try {

            final PINSpec signPinSpec = new PINSpec(6, 10, "[0-9]", "Signatur-PIN");

            
            final ActionListener cancelListener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    System.out.println("CANCEL EVENT OCCURED: " + e);
                }
            };
            ActionListener okListener = new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("OK EVENT OCCURED: " + e);
                }
            };
            final ActionListener signListener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    System.out.println("SIGN EVENT OCCURED: " + e);
                }
            };
            ActionListener hashdataListener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    System.out.println("HASHDATA EVENT OCCURED: " + e);
                    ActionListener returnListener = new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            gui.showSignaturePINDialog(signPinSpec, signListener, "sign", cancelListener, "cancel", null, "hashdata");
                        }
                    };
                    HashDataInput signedRef = new HashDataInput() {

                        @Override
                        public InputStream getHashDataInput() {
                            return new ByteArrayInputStream("<xml>HashDataInput_002</xml>".getBytes());
                        }

                        @Override
                        public String getMimeType() {
                            return "text/xml";
                        }
                    };
                    gui.showHashDataInputDialog(Collections.singletonList(signedRef), returnListener, "return");
                }
            };
           


//        gui.showWelcomeDialog();
//
//        Thread.sleep(2000);
        
            gui.showInsertCardDialog(cancelListener, "cancel");
            
            Thread.sleep(2000);
            
            gui.showCardNotSupportedDialog(cancelListener, "cancel");
            
            Thread.sleep(2000);

//            PINSpec cardPinSpec = new PINSpec(4, 4, "[0-9]", "Karten-PIN");
//            
//            gui.showCardPINDialog(cardPinSpec, okListener, "ok", cancelListener, "cancel");
//            
//            Thread.sleep(2000);
//
            
//            gui.showSignaturePINDialog(signPinSpec, signListener, "sign", cancelListener, "cancel", hashdataListener, "hashdata");
//
//            Thread.sleep(2000);
//            
//            gui.showSignaturePINRetryDialog(signPinSpec, 2, signListener, "sign", cancelListener, "cancel", hashdataListener, "hashdata");
//
//            Thread.sleep(2000);
////            
//            gui.showErrorDialog("Testfehler occured", null, null);
//            
//            Thread.sleep(2000);
//            
//            gui.showErrorDialog("Testfehler occured"); 

        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}
