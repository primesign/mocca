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

package at.gv.egiz.bku.webstart.gui;

import java.util.Locale;

/**
 *
 * @author clemens
 */
public interface StatusNotifier {

    /** no leading slash for messages, but for image */
    public static final String MESSAGES_RESOURCE = "at/gv/egiz/bku/webstart/messages";
    public static final String TRAYICON_RESOURCE = "/at/gv/egiz/bku/webstart/chip";
    /** resource bundle messages */
    public static final String CAPTION_DEFAULT = "tray.caption.default";
    public static final String CAPTION_ERROR = "tray.caption.error";
    public static final String MESSAGE_START = "tray.message.start";
    public static final String MESSAGE_START_OFFLINE = "tray.message.start.offline";
    public static final String MESSAGE_CONFIG = "tray.message.config";
    public static final String MESSAGE_CERTS = "tray.message.certs";
    public static final String MESSAGE_FINISHED = "tray.message.finished";
    public static final String MESSAGE_SHUTDOWN = "tray.message.shutdown";
    public static final String ERROR_START = "tray.error.start";
    public static final String ERROR_BIND = "tray.error.bind";
    public static final String ERROR_CONFIG = "tray.error.config";
    public static final String ERROR_PIN = "tray.error.pin.connect";
    public static final String ERROR_OPEN_URL = "tray.error.open.url";

    
    public void error(String msgKey);

    public void error(String msgPatternKey, Object... argument);

    public void info(String msgKey);

		public Locale getLocale();
}
