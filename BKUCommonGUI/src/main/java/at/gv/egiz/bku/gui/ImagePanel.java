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

import java.awt.Graphics;
import java.awt.Image;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 *
 * @author clemens
 */
public class ImagePanel extends JPanel {

  protected Image backgroundImg;

  public ImagePanel(URL background) {
    this(new ImageIcon(background).getImage());
  }
  
  public ImagePanel(Image img) {
    this.backgroundImg = img;
    this.setOpaque(false);
  }
  
  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    g.drawImage(backgroundImg, 0, this.getHeight() - backgroundImg.getHeight(null), null);
  }
    
}
