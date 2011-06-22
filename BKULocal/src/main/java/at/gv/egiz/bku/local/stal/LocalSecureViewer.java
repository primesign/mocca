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


package at.gv.egiz.bku.local.stal;

import at.gv.egiz.bku.slcommands.impl.DataObjectHashDataInput;
import at.gv.egiz.bku.smccstal.SecureViewer;
import java.io.IOException;
import java.util.ArrayList;

import at.gv.egiz.bku.gui.BKUGUIFacade;
import at.gv.egiz.stal.HashDataInput;
import at.gv.egiz.stal.impl.ByteArrayHashDataInput;
import at.gv.egiz.stal.signedinfo.ReferenceType;
import at.gv.egiz.stal.signedinfo.SignedInfoType;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class LocalSecureViewer implements SecureViewer {

  private final Logger log = LoggerFactory.getLogger(LocalSignRequestHandler.class);
  private List<HashDataInput> hashDataInputs = Collections.emptyList();

  protected BKUGUIFacade gui;

  public LocalSecureViewer(BKUGUIFacade gui) {
    this.gui = gui;
  }

  public void setDataToBeSigned(List<HashDataInput> dataToBeSigned) {
    this.hashDataInputs = dataToBeSigned;
  }

  /**
   *
   * @param dsigReferences
   * @throws java.lang.Exception
   */
  @Override
  public void displayDataToBeSigned(SignedInfoType signedInfo,
          ActionListener okListener, String okCommand)
          throws Exception {
    if (signedInfo.getReference().size() == 0) {
      log.error("No hashdata input selected to be displayed: null.");
      throw new Exception("No HashData Input selected to be displayed.");
    }

    ArrayList<HashDataInput> selectedHashDataInputs = new ArrayList<HashDataInput>();
    for (ReferenceType dsigRef : signedInfo.getReference()) {
      // don't get Manifest, QualifyingProperties, ...
      if (dsigRef.getType() == null) {
        String dsigRefId = dsigRef.getId();
        if (dsigRefId != null) {
          boolean hdiAvailable = false;
          for (HashDataInput hashDataInput : hashDataInputs) {
            if (dsigRefId.equals(hashDataInput.getReferenceId())) {
              log.debug("Display hashdata input for dsig:SignedReference {}.",
                  dsigRefId);
              selectedHashDataInputs.add(
                      ensureCachedHashDataInput(hashDataInput));
              hdiAvailable = true;
              break;
            }
          }
          if (!hdiAvailable) {
            log.error("No hashdata input for dsig:SignedReference {}.", dsigRefId);
            throw new Exception(
              "No HashDataInput available for dsig:SignedReference " + dsigRefId);
          }
        } else {
          throw new Exception(
            "Cannot get HashDataInput for dsig:Reference without Id attribute");
        }
      }
    }

    if (selectedHashDataInputs.size() < 1) {
      log.error("dsig:SignedInfo does not contain a data reference.");
      throw new Exception("dsig:SignedInfo does not contain a data reference.");
    }
    gui.showSecureViewer(selectedHashDataInputs, okListener, okCommand);
  }


  private HashDataInput ensureCachedHashDataInput(HashDataInput hashDataInput)
          throws IOException {
    if (!(hashDataInput instanceof DataObjectHashDataInput)) {
      
      log.warn("Expected DataObjectHashDataInput for LocalSignRequestHandler, got {}.",
              hashDataInput.getClass().getName());

      InputStream hdIs = hashDataInput.getHashDataInput();
      ByteArrayOutputStream baos = new ByteArrayOutputStream(hdIs.available());
      int b;
      while ((b = hdIs.read()) != -1) {
        baos.write(b);
      }
      hashDataInput = new ByteArrayHashDataInput(baos.toByteArray(),
              hashDataInput.getReferenceId(),
              hashDataInput.getMimeType(),
              hashDataInput.getEncoding(),
              hashDataInput.getFilename());
    }
    return hashDataInput;
  }

}
