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
//  public static final String HELP_VERIFY_PIN = "help.pin.verify";
  public static final String TITLE_PINMGMT = "title.pin.mgmt";
  public static final String TITLE_ACTIVATE_PIN = "title.activate.pin";
  public static final String TITLE_CHANGE_PIN = "title.change.pin";
//  public static final String TITLE_VERIFY_PIN = "title.verify.pin";
  public static final String TITLE_UNBLOCK_PIN = "title.unblock.pin";
  public static final String TITLE_ACTIVATE_SUCCESS = "title.activate.success";
  public static final String TITLE_UNBLOCK_SUCCESS = "title.unblock.success";
  public static final String TITLE_CHANGE_SUCCESS = "title.change.success";

  // removed message.* prefix to reuse keys as help keys
  public static final String MESSAGE_ACTIVATE_SUCCESS = "activate.success";
  public static final String MESSAGE_CHANGE_SUCCESS = "change.success";
  public static final String MESSAGE_UNBLOCK_SUCCESS = "unblock.success";
  public static final String MESSAGE_PINMGMT = "pin.mgmt";
//  public static final String MESSAGE_PINPAD = "pinpad";

  public static final String MESSAGE_ACTIVATE_PIN = "activate.pin";
  public static final String MESSAGE_CHANGE_PIN = "change.pin";
  public static final String MESSAGE_UNBLOCK_PIN = "unblock.pin";

  public static final String MESSAGE_ACTIVATE_PINPAD_CURRENT = "activate.pinpad.current";
  public static final String MESSAGE_CHANGE_PINPAD_CURRENT = "change.pinpad.current";
  public static final String MESSAGE_UNBLOCK_PINPAD_CURRENT = "unblock.pinpad.current";
  public static final String MESSAGE_ACTIVATE_PINPAD_NEW = "activate.pinpad.new";
  public static final String MESSAGE_CHANGE_PINPAD_NEW = "change.pinpad.new";
  public static final String MESSAGE_UNBLOCK_PINPAD_NEW = "unblock.pinpad.new";
  public static final String MESSAGE_ACTIVATE_PINPAD_CONFIRM = "activate.pinpad.confirm";
  public static final String MESSAGE_CHANGE_PINPAD_CONFIRM = "change.pinpad.confirm";
  public static final String MESSAGE_UNBLOCK_PINPAD_CONFIRM = "unblock.pinpad.confirm";

  public static final String MESSAGE_ACTIVATE_PINPAD_DIREKT = "activate.pinpad.direct";
  public static final String MESSAGE_CHANGE_PINPAD_DIREKT = "change.pinpad.direct";
  public static final String MESSAGE_UNBLOCK_PINPAD_DIREKT = "unblock.pinpad.direct";

  public static final String LABEL_OLD_PIN = "label.old.pin";
  public static final String LABEL_PUK = "label.puk";
  public static final String LABEL_NEW_PIN = "label.new.pin";
  public static final String LABEL_REPEAT_PIN = "label.repeat.pin";

  public static final String ERR_STATUS = "err.status";
  public static final String ERR_ACTIVATE = "err.activate";
  public static final String ERR_CHANGE = "err.change";
  public static final String ERR_UNBLOCK = "err.unblock";
  public static final String ERR_VERIFY = "err.verify";
  public static final String ERR_RETRIES = "err.retries";
  public static final String ERR_LOCKED = "err.locked";
  public static final String ERR_NOT_ACTIVE = "err.not.active";
  public static final String ERR_PIN_FORMAT = "err.pin.format";
  public static final String ERR_PIN_CONFIRMATION = "err.pin.confirmation";
  public static final String ERR_PIN_OPERATION_ABORTED = "err.pin.operation.aborted";
  public static final String ERR_UNSUPPORTED_CARD = "err.unsupported.card";

  public static final String BUTTON_ACTIVATE = "button.activate";
  public static final String BUTTON_UNBLOCK = "button.unblock";
  public static final String BUTTON_CHANGE = "button.change";
  public static final String BUTTON_VERIFY = "button.verify";

  public static final String STATUS_ACTIVE = "status.active";
  public static final String STATUS_BLOCKED = "status.blocked";
  public static final String STATUS_NOT_ACTIVE = "status.not.active";
  public static final String STATUS_UNKNOWN = "status.unknown";

  public enum STATUS { ACTIV, NOT_ACTIV, BLOCKED, UNKNOWN };
  public enum DIALOG { VERIFY, ACTIVATE, CHANGE, UNBLOCK };

  /**
   * list pins
   */
  public void showPINManagementDialog(Map<PINSpec, STATUS> pins,
          ActionListener activateListener, String activateCmd, String changeCmd, String unblockCmd, String verifyCmd,
          ActionListener cancelListener, String cancelCmd);

  /**
   * "software" pin-entry dialog (activate, change, unblock, verify)
   */
  public void showPINDialog(DIALOG type, PINSpec pinSpec, int retries,
          ActionListener okListener, String okCmd,
          ActionListener cancelListener, String cancelCmd);

  /**
   * <b>direct</b> pinpad pin-entry dialog
   */
  public void showModifyPINDirect(DIALOG type, PINSpec pinSpec, int retries);

  /**
   * <b>start/finish</b> pinpad pin-entry dialog
   */
  public void showEnterCurrentPIN(DIALOG type, PINSpec pinSpec, int retries);

  public void showEnterNewPIN(DIALOG type, PINSpec pinSpec);

  public void showConfirmNewPIN(DIALOG type, PINSpec pinSpec);


  public char[] getOldPin();

  public PINSpec getSelectedPINSpec();
}
