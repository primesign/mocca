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
