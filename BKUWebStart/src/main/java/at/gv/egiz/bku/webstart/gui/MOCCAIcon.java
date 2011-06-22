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

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author clemens
 */
public class MOCCAIcon implements StatusNotifier, ActionListener {

	public static final String LABEL_SHUTDOWN = "tray.label.shutdown";
	public static final String LABEL_PIN = "tray.label.pin";
	public static final String LABEL_HELP = "tray.label.help";
	public static final String LABEL_ABOUT = "tray.label.about";
	public static final String TOOLTIP_DEFAULT = "tray.tooltip.default";
	/** action commands for tray menu */
	public static final String SHUTDOWN_COMMAND = "shutdown";
	public static final String PIN_COMMAND = "pin";
	public static final String ABOUT_COMMAND = "about";
	public static final String HELP_COMMAND = "help";
	private static final Logger log = LoggerFactory.getLogger(MOCCAIcon.class);
	protected BKUControllerInterface controller;
	protected TrayIcon trayIcon;
	protected ResourceBundle messages;

	private AboutDialog aboutDialog;

	public MOCCAIcon(BKUControllerInterface controller) {
		this.controller = controller;
		messages = ResourceBundle.getBundle(MESSAGES_RESOURCE, Locale.getDefault());
		this.trayIcon = initTrayIcon();
	}

	private TrayIcon initTrayIcon() {
		if (SystemTray.isSupported()) {
			try {
				// get the SystemTray instance
				SystemTray tray = SystemTray.getSystemTray();
				log.debug("TrayIcon size: " + tray.getTrayIconSize());

				String iconResource;
				if (tray.getTrayIconSize().height < 17) {
					iconResource = TRAYICON_RESOURCE + "16.png";
				} else if (tray.getTrayIconSize().height < 25) {
					iconResource = TRAYICON_RESOURCE + "24.png";
				} else if (tray.getTrayIconSize().height < 33) {
					iconResource = TRAYICON_RESOURCE + "32.png";
				} else {
					iconResource = TRAYICON_RESOURCE + "48.png";
				}
				Image image = ImageIO.read(getClass().getResourceAsStream(iconResource));

				PopupMenu menu = new PopupMenu();

				MenuItem helpItem = new MenuItem(messages.getString(LABEL_HELP));
				helpItem.addActionListener(this);
				helpItem.setActionCommand(HELP_COMMAND);
				menu.add(helpItem);

				MenuItem pinItem = new MenuItem(messages.getString(LABEL_PIN));
				pinItem.addActionListener(this);
				pinItem.setActionCommand(PIN_COMMAND);
				menu.add(pinItem);

				MenuItem shutdownItem = new MenuItem(messages.getString(LABEL_SHUTDOWN));
				shutdownItem.addActionListener(this);
				shutdownItem.setActionCommand(SHUTDOWN_COMMAND);
				menu.add(shutdownItem);

				menu.addSeparator();

				MenuItem aboutItem = new MenuItem(messages.getString(LABEL_ABOUT));
				aboutItem.setActionCommand(ABOUT_COMMAND);
				aboutItem.addActionListener(this);
				menu.add(aboutItem);

				TrayIcon ti = new TrayIcon(image, messages.getString(TOOLTIP_DEFAULT), menu);
				ti.setImageAutoSize(true);
				ti.addActionListener(this);
				tray.add(ti);
				return ti;
			} catch (AWTException ex) {
				log.error("Failed to init tray icon", ex);
			} catch (IOException ex) {
				log.error("Failed to load tray icon image", ex);
			}
		} else {
			log.error("No system tray support");
		}
		return null;
	}

	@Override
	public void error(String msgKey) {
		if (trayIcon != null) {
			trayIcon.displayMessage(
							messages.getString(CAPTION_ERROR),
							messages.getString(msgKey),
							TrayIcon.MessageType.ERROR);
		} else {
			log.error(messages.getString(msgKey));
		}
	}

	@Override
	public void error(String msgPatternKey, Object... argument) {
		if (trayIcon != null) {
			trayIcon.displayMessage(
							messages.getString(CAPTION_ERROR),
							MessageFormat.format(messages.getString(msgPatternKey), argument),
							TrayIcon.MessageType.ERROR);
		} else {
			log.error(MessageFormat.format(messages.getString(msgPatternKey), argument));
		}
	}

	@Override
	public void info(String msgKey) {
		if (trayIcon != null) {
			trayIcon.displayMessage(
							messages.getString(CAPTION_DEFAULT),
							messages.getString(msgKey),
							TrayIcon.MessageType.INFO);
		} else {
			log.info(messages.getString(msgKey));
		}
	}

	@Override
	public Locale getLocale() {
		return messages.getLocale();
	}

	/**
	 * Listen for TrayMenu actions (display error messages on trayIcon)
	 * @param e
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (SHUTDOWN_COMMAND.equals(e.getActionCommand())) {
			log.debug("shutdown requested via tray menu");
			controller.shutDown();
		} else if (ABOUT_COMMAND.equals(e.getActionCommand())) {
			log.debug("about dialog requested via tray menu");

			if (aboutDialog == null) {
				aboutDialog = new AboutDialog(new JFrame(), true, controller.getVersion());
				aboutDialog.addWindowListener(new WindowAdapter() {

					@Override
					public void windowClosing(java.awt.event.WindowEvent e) {
						aboutDialog.setVisible(false);
					}
				});
			}
			aboutDialog.setLocationByPlatform(true);
			aboutDialog.setVisible(true);

		} else if (PIN_COMMAND.equals(e.getActionCommand())) {
			log.debug("pin management dialog requested via tray menu");
			controller.pinManagement(messages.getLocale());

		} else if (HELP_COMMAND.equals(e.getActionCommand())) {
			log.debug("help page requested via tray menu");
			controller.showHelp(messages.getLocale());

		} else {
			log.error("unknown tray menu command: " + e.getActionCommand());
		}
	}
}
