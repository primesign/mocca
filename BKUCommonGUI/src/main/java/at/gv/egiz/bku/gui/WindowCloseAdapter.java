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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class WindowCloseAdapter extends WindowAdapter {

  private final Logger log = LoggerFactory.getLogger(WindowCloseAdapter.class);

  protected ActionListener closeListener;
  protected String closeCommand;

  void registerListener(ActionListener closeListener, String closeCommand) {
    log.debug("Register close listener for action command {}.", closeCommand);
    this.closeListener = closeListener;
    this.closeCommand = closeCommand;
  }

  @Override
  public void windowClosing(WindowEvent e) {
    log.debug("Received window closing event: {}.", e.paramString());

    if (closeListener != null) {
      log.debug("Notifying closeListener ...");
      closeListener.actionPerformed(new ActionEvent(e.getSource(), e.getID(), closeCommand));
    }
  }


}
