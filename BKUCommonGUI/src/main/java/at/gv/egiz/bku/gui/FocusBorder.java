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


package at.gv.egiz.bku.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.geom.Rectangle2D;

import javax.swing.border.Border;


public class FocusBorder implements Border {

	private static final Color DEFAULT_COLOR = Color.BLACK;
	
	private Color color;
	private float borderWidthFactor;

	public FocusBorder() {

		this.color = DEFAULT_COLOR;
		this.borderWidthFactor = 1.0f;
	}
	
	public FocusBorder(Color borderColor) {

		this.color = borderColor;
		this.borderWidthFactor = 1.0f;
	}

	@Override
	public Insets getBorderInsets(Component c) {

		return new Insets(3, 3, 6, 6);
	}

	@Override
	public boolean isBorderOpaque() {

		return true;
	}

	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width,
			int height) {
		Graphics2D g2 = (Graphics2D) g;
	
		g2.setPaint(color);
		float[] dash1 = { 2.0f };
	
		g2.setStroke(new BasicStroke(1.0f * borderWidthFactor, BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f));

		g2.draw(new Rectangle2D.Double(x + 1, y + 1, width - 6, height - 6));

	}

	public void setColor(Color color) {
		this.color = color;
	}

	public void setBorderWidthFactor(float borderWidthFactor) {
				
		this.borderWidthFactor = borderWidthFactor;
	}

}
