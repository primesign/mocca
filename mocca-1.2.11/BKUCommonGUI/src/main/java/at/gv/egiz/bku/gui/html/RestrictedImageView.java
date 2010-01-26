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

package at.gv.egiz.bku.gui.html;

import javax.swing.text.Element;
import javax.swing.text.html.HTML;
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
    
    /**
     * check whether this URL corresponds to the data URI scheme 
     * (and the referenced content is directly included in the document).
     * @return
     */
    private boolean isDataURI() {
      String src = (String)getElement().getAttributes().
                             getAttribute(HTML.Attribute.SRC);
      if (src == null) {
        return false;
      }

      return src.toLowerCase().startsWith("data");
    }
  }