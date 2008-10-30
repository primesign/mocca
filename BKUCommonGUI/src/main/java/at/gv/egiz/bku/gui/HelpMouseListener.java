package at.gv.egiz.bku.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HelpMouseListener extends MouseAdapter {

  protected static final Log log = LogFactory.getLog(HelpMouseListener.class);
  
  protected ActionListener externalHelpListener;
  protected String locale;
  protected String topic;

  public HelpMouseListener(ActionListener externalHelpListener) {
    super();
    this.externalHelpListener = externalHelpListener;
  }

  public void setHelpTopic(String topic) {
    log.trace("setting help topic: " + topic);
    this.topic = topic;
  }

  @Override
  public void mouseClicked(MouseEvent arg0) {
    ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, topic);
    externalHelpListener.actionPerformed(e);
  }
}
