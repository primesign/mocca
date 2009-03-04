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

import at.gv.egiz.smcc.PINSpec;
import java.awt.event.ActionListener;
import java.util.Map;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public interface PINManagementGUIFacade extends BKUGUIFacade {

  public static final String HELP_PINMGMT = "help.pin.mgmt";
  public static final String TITLE_PINMGMT = "title.pin.mgmt";
  public static final String TITLE_ACTIVATE_PIN = "title.activate.pin";
  public static final String TITLE_CHANGE_PIN = "title.change.pin";
  public static final String TITLE_UNBLOCK_PIN = "title.unblock.pin";
  public static final String TITLE_ACTIVATE_SUCCESS = "title.activate.success";
  public static final String TITLE_CHANGE_SUCCESS = "title.change.success";
  public static final String MESSAGE_ACTIVATE_SUCCESS = "message.activate.success";
  public static final String MESSAGE_CHANGE_SUCCESS = "message.change.success";
  public static final String MESSAGE_PINMGMT = "message.pin.mgmt";
  public static final String MESSAGE_ACTIVATE_PIN = "message.activate.pin";
  public static final String MESSAGE_CHANGE_PIN = "message.change.pin";
  public static final String MESSAGE_UNBLOCK_PIN = "message.unblock.pin";
  public static final String LABEL_OLD_PIN = "label.old.pin";
  public static final String LABEL_NEW_PIN = "label.new.pin";
  public static final String LABEL_REPEAT_PIN = "label.repeat.pin";

  public static final String ERR_ACTIVATE = "err.activate";
  public static final String ERR_CHANGE = "err.change";
  public static final String ERR_UNBLOCK = "err.unblock";
  public static final String ERR_RETRIES = "err.retries";

  public static final String BUTTON_ACTIVATE = "button.activate";
  public static final String BUTTON_UNBLOCK = "button.unblock";
  public static final String BUTTON_CHANGE = "button.change";

  public static final String STATUS_ACTIVE = "status.active";
  public static final String STATUS_BLOCKED = "status.blocked";
  public static final String STATUS_NOT_ACTIVE = "status.not.active";
  public static final String STATUS_UNKNOWN = "status.unknown";

  public enum STATUS { ACTIV, NOT_ACTIV, BLOCKED, UNKNOWN };

  public void showPINManagementDialog(Map<PINSpec, STATUS> pins,
          ActionListener activateListener, String activateCmd, String changeCmd, String unblockCmd,
          ActionListener cancelListener, String cancelCmd);

  public void showActivatePINDialog(PINSpec pin,
          ActionListener okListener, String okCmd,
          ActionListener cancelListener, String cancelCmd);

  public void showChangePINDialog(PINSpec pin,
          ActionListener okListener, String okCmd,
          ActionListener cancelListener, String cancelCmd);

  public void showUnblockPINDialog(PINSpec pin,
          ActionListener okListener, String okCmd,
          ActionListener cancelListener, String cancelCmd);

  public char[] getOldPin();

  public PINSpec getSelectedPINSpec();
}
