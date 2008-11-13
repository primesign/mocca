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
import at.gv.egiz.stal.HashDataInput;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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
        HashDataInput signedRef1 = new HashDataInput() {

          @Override
          public InputStream getHashDataInput() {
            return new ByteArrayInputStream("Ich bin ein einfacher Text mit Umlauten: öäüßéç@€\n123\n456\n\tHello, world!\n\nlkjsd\nnksdjf".getBytes());
          }

          @Override
          public String getMimeType() {
            return "text/plain";
          }

          @Override
          public String getReferenceId() {
            return "Reference-ref1-00000000000000000000001";
          }
          @Override
          public String getEncoding() {
            return "UTF-8";
          }
        };
        HashDataInput signedRef2 = new HashDataInput() {

          @Override
          public InputStream getHashDataInput() {
            return new ByteArrayInputStream("<xml>HashDataInput_002</xml>".getBytes());
          }

          @Override
          public String getMimeType() {
            return "text/xml";
          }

          @Override
          public String getReferenceId() {
            return "Reference-ref2-00000000000000000000002";
          }
          @Override
          public String getEncoding() {
            return "UTF-8";
          }
        };
        HashDataInput signedRef3 = new HashDataInput() {

          @Override
          public InputStream getHashDataInput() {
            return new ByteArrayInputStream("<xml>HashDataInput_003</xml>".getBytes());
          }

          @Override
          public String getMimeType() {
            return "text/xml";
          }

          @Override
          public String getReferenceId() {
            return "Reference-ref3-00000000000000000000003";
          }
          
          @Override
          public String getEncoding() {
            return "UTF-8";
          }
        };
        HashDataInput signedRef4 = new HashDataInput() {

          @Override
          public InputStream getHashDataInput() {
            return new ByteArrayInputStream("<xml>HashDataInput_004</xml>".getBytes());
          }

          @Override
          public String getMimeType() {
            return "text/xml";
          }

          @Override
          public String getReferenceId() {
            return "ref4";
          }

          @Override
          public String getEncoding() {
            return "UTF-8";
          }
        };

        //
        List<HashDataInput> signedRefs = new ArrayList();
        signedRefs.add(signedRef1);
//                    signedRefs.add(signedRef2);
//                    signedRefs.add(signedRef3);
//                    signedRefs.add(signedRef4);
//                    signedRefs.add(signedRef4);
//                    signedRefs.add(signedRef4);
//                    signedRefs.add(signedRef4);
//                    signedRefs.add(signedRef4);
//                    signedRefs = Collections.singletonList(signedRef1);
        gui.showHashDataInputDialog(signedRefs, true, returnListener, "return");
      }
    };



        gui.showWelcomeDialog();

        Thread.sleep(2000);
        
        gui.showWaitDialog(null);
        
        Thread.sleep(1000);
        
        gui.showWaitDialog("test");
        
        Thread.sleep(1000);
          

            gui.showInsertCardDialog(cancelListener, "cancel");

            Thread.sleep(2000);
            
            gui.showCardNotSupportedDialog(cancelListener, "cancel");
            
            Thread.sleep(2000);

            PINSpec cardPinSpec = new PINSpec(4, 4, "[0-9]", "Karten-PIN");
            
            gui.showCardPINDialog(cardPinSpec, okListener, "ok", cancelListener, "cancel");
            
            Thread.sleep(2000);

            gui.showSignaturePINDialog(signPinSpec, signListener, "sign", cancelListener, "cancel", hashdataListener, "hashdata");

            Thread.sleep(4000);

            gui.showSignaturePINRetryDialog(signPinSpec, 2, signListener, "sign", cancelListener, "cancel", hashdataListener, "hashdata");

            Thread.sleep(2000);
            
            gui.showErrorDialog(BKUGUIFacade.ERR_UNKNOWN, new Object[] {"Testfehler"}, null, null);
            
            Thread.sleep(2000);
              
            gui.showErrorDialog("error.test", new Object[] {"Testfehler", "noch ein TestFehler"}); 

            Thread.sleep(2000);
            
            gui.showErrorDialog("error.no.hashdata", null); 
            
            Thread.sleep(2000);
          
            gui.showErrorDialog(BKUGUIFacade.ERR_UNKNOWN, new Object[] {"Testfehler"}); 

            Thread.sleep(2000);
          
            gui.showErrorDialog("error.unknown", null); 


//            gui.showTextPlainHashDataInput("hallo,\n welt!", "12345", null, "cancel", null, "save");
//            gui.showTextPlainHashDataInput("hallo,\n welt!", "12345", null, "cancel", null, "save");
//            Thread.sleep(2000);

        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
  }
}
