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



package at.gv.egiz.bku.gui.html;

import javax.swing.text.Element;
import javax.swing.text.html.ImageView;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
/**
   * ImageViewer.refreshImage() (and loadImage()) is private :-(
   */
  public class RestrictedImageView extends ImageView {

    public RestrictedImageView(Element elem) {
      super(elem);
    }

//    @Override
//    public Image getImage() {
//      int s = state;
//        if ((s & RELOAD_IMAGE_FLAG) != 0) {
//            refreshImage();
//        }
//        s = state;
//        if ((s & RELOAD_FLAG) != 0) {
//            synchronized(this) {
//                state = (state | RELOAD_FLAG) ^ RELOAD_FLAG;
//            }
//            setPropertiesFromAttributes();
//        }
//      return super.getImage();
//    }
    
//    /**
//     * check whether this URL corresponds to the data URI scheme 
//     * (and the referenced content is directly included in the document).
//     * @return
//     */
//    private boolean isDataURI() {
//      String src = (String)getElement().getAttributes().
//                             getAttribute(HTML.Attribute.SRC);
//      if (src == null) {
//        return false;
//      }
//
//      return src.toLowerCase().startsWith("data");
//    }
  }