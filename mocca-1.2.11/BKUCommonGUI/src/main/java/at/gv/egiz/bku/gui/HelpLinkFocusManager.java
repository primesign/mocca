package at.gv.egiz.bku.gui;

import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URL;

import javax.accessibility.AccessibleHyperlink;
import javax.accessibility.AccessibleHypertext;
import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * 
 * @author Thomas Zefferer <thomas.zefferer@iaik.tugraz.at>
 */
public final class HelpLinkFocusManager extends KeyAdapter { 

	private static final int FOCUS_UNDEFINED = -1;
	
	private int focusedHyperlinkIndex = FOCUS_UNDEFINED;
	private JEditorPane displayPane;
	
	public HelpLinkFocusManager(JEditorPane displayPane) {

		super();
		this.displayPane = displayPane;
	}

	public void keyPressed(KeyEvent e) {

		AccessibleHypertext accessibleHypertext = (AccessibleHypertext) this.displayPane
				.getAccessibleContext().getAccessibleText();

		if (accessibleHypertext.getLinkCount() > 0) {
			switch (e.getKeyCode()) {

			case KeyEvent.VK_RIGHT:
				if (this.focusedHyperlinkIndex != FOCUS_UNDEFINED) {
					removeHyperlinkFocus();
				}

				this.focusedHyperlinkIndex++;

				if (this.focusedHyperlinkIndex >= accessibleHypertext
						.getLinkCount()) {

					this.focusedHyperlinkIndex = 0;
				}

				setHyperlinkFocus();
				break;

			case KeyEvent.VK_LEFT:
				if (this.focusedHyperlinkIndex != FOCUS_UNDEFINED) {
					removeHyperlinkFocus();
				}

				this.focusedHyperlinkIndex--;

				if (this.focusedHyperlinkIndex < 0) {
					this.focusedHyperlinkIndex = accessibleHypertext
							.getLinkCount() - 1;
				}

				setHyperlinkFocus();
				break;

			case KeyEvent.VK_SPACE:
			case KeyEvent.VK_ENTER:
				

				AccessibleHyperlink link = accessibleHypertext
						.getLink(this.focusedHyperlinkIndex);
				if (link != null) {
					URL url = (URL) link.getAccessibleActionObject(0);
					Element element = ((DefaultStyledDocument) this.displayPane
							.getDocument()).getCharacterElement(link
							.getStartIndex());
					HyperlinkEvent linkEvent = new HyperlinkEvent(
							this.displayPane,
							HyperlinkEvent.EventType.ACTIVATED, url, null,
							element);
					this.displayPane.fireHyperlinkUpdate(linkEvent);
				}
				
				removeHyperlinkFocus();
				this.focusedHyperlinkIndex = FOCUS_UNDEFINED;
				break;
			default: 
				// nothig to do
				break;
			}
		}
	}

	private void setHyperlinkFocus() {

		AccessibleHypertext accessibleHypertext = (AccessibleHypertext) this.displayPane
				.getAccessibleContext().getAccessibleText();
		AccessibleHyperlink link = accessibleHypertext
				.getLink(this.focusedHyperlinkIndex);		
		
		if (link != null) {
			
			MutableAttributeSet style = new SimpleAttributeSet();
			StyleConstants.setForeground(style, Color.RED);
			((DefaultStyledDocument) this.displayPane.getDocument())
					.setCharacterAttributes(link.getStartIndex(), link
							.getEndIndex()
							- link.getStartIndex(), style, false);
		}
	}

	private void removeHyperlinkFocus() {
		Color textColor = Color.BLUE;
		AccessibleHypertext accessibleHypertext = (AccessibleHypertext) this.displayPane
				.getAccessibleContext().getAccessibleText();
		AccessibleHyperlink link = accessibleHypertext
				.getLink(this.focusedHyperlinkIndex);

		if (link != null) {
			
			MutableAttributeSet style = new SimpleAttributeSet();
			StyleConstants.setForeground(style, textColor);
			((DefaultStyledDocument) this.displayPane.getDocument())
					.setCharacterAttributes(link.getStartIndex(), link
							.getEndIndex()
							- link.getStartIndex(), style, false);
		}
	}

}
