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

package at.gv.egiz.bku.local.stal;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import javax.swing.JFrame;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.bku.gui.BKUGUIFacade;
import at.gv.egiz.bku.gui.BKUIcons;
import at.gv.egiz.bku.gui.IdentityLinkGUI;
import at.gv.egiz.bku.gui.IdentityLinkGUIFacade;
import at.gv.egiz.bku.local.gui.GUIProxy;
import at.gv.egiz.bku.local.gui.LocalHelpListener;
import at.gv.egiz.bku.viewer.ResourceFontLoader;
import at.gv.egiz.stal.STAL;
import at.gv.egiz.stal.STALFactory;

public class LocalIdentityLinkSTALFactory implements STALFactory {

	private final Logger log = LoggerFactory
			.getLogger(LocalIdentityLinkSTALFactory.class);
	protected static final Dimension PREFERRED_SIZE = new Dimension(318, 200);
	protected URL helpURL;
	protected Locale locale;

	protected Configuration configuration;

	@Override
	public STAL createSTAL() {
		final LocalBKUWorker stal;
	    //http://java.sun.com/docs/books/tutorial/uiswing/misc/focus.html
	    // use undecorated JFrame instead of JWindow,
	    // which creates an invisible owning frame and therefore cannot getFocusInWindow()
	    JFrame dialog = new JFrame("Bürgerkarte");
	    log.debug("AlwaysOnTop supported: {}.", dialog.isAlwaysOnTopSupported());
	    // [#439] make mocca dialog alwaysOnTop
	    dialog.setAlwaysOnTop(true);
	    dialog.setIconImages(BKUIcons.icons);
//	    dialog.setUndecorated(true);
//	    dialog.getRootPane().setWindowDecorationStyle(JRootPane.NONE);

	    if (locale != null) {
	      dialog.setLocale(locale);
	    }
	    LocalHelpListener helpListener = null;
	    if (helpURL != null) {
	      helpListener = new LocalHelpListener(helpURL, locale);
	    } else {
	      log.warn("No HELP URL configured, help system disabled.");
	    }
	    IdentityLinkGUIFacade gui = new IdentityLinkGUI(dialog.getContentPane(),
	            dialog.getLocale(),
	            null,
	            new ResourceFontLoader(),
	            helpListener);
	    BKUGUIFacade proxy = (BKUGUIFacade) GUIProxy.newInstance(gui, dialog, new Class[] { IdentityLinkGUIFacade.class} );
	    stal = new LocalBKUWorker(proxy, dialog);
	    dialog.setPreferredSize(PREFERRED_SIZE);
	    dialog.pack();
	    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	    Dimension frameSize = dialog.getSize();
	    if (frameSize.height > screenSize.height) {
	      frameSize.height = screenSize.height;
	    }
	    if (frameSize.width > screenSize.width) {
	      frameSize.width = screenSize.width;
	    }
	    dialog.setLocation((screenSize.width - frameSize.width) / 2,
	            (screenSize.height - frameSize.height) / 2);
	    return stal;
	}

	@Override
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	/**
	 * spring injects helpURL
	 * 
	 * @param helpURL
	 * @throws MalformedURLException
	 *             if helpURL is not a valid URL
	 */
	public void setHelpURL(String helpURL) throws MalformedURLException {
		this.helpURL = new URL(helpURL);
	}

	/**
	 * @return the configuration
	 */
	public Configuration getConfiguration() {
		return configuration;
	}

	/**
	 * @param configuration
	 *            the configuration to set
	 */
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}
}
