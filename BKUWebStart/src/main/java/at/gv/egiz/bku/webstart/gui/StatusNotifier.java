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
    public static final String ERROR_IDENTITY_LINK = "tray.error.identity_link.connect";
    public static final String ERROR_OPEN_URL = "tray.error.open.url";

    
    public void error(String msgKey);

    public void error(String msgPatternKey, Object... argument);

    public void info(String msgKey);

		public Locale getLocale();
}
