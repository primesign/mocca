/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
