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

package at.gv.egiz.bku.pin.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * common super class providing action listener for all PIN GUIs
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public abstract class AbstractPINProvider implements ActionListener {

  private final Logger log = LoggerFactory.getLogger(AbstractPINProvider.class);

  protected String action;
  protected boolean actionPerformed;

  protected synchronized void waitForAction() throws InterruptedException {
    try {
      while (!actionPerformed) {
        this.wait();
      }
    } catch (InterruptedException e) {
      log.error("[{}] interrupt in waitForAction.", Thread.currentThread().getName());
      throw e;
    }
    actionPerformed = false;
  }

  private synchronized void actionPerformed() {
    actionPerformed = true;
    notify();//All();
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    log.debug("[{}] action performed - {}", Thread.currentThread().getName(), e.getActionCommand());
    action = e.getActionCommand();
    actionPerformed();
  }
}
