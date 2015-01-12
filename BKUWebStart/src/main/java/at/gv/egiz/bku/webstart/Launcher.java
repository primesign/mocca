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


package at.gv.egiz.bku.webstart;

import at.gv.egiz.bku.webstart.autostart.Autostart;
import at.gv.egiz.bku.webstart.gui.StatusNotifier;
import at.gv.egiz.bku.webstart.gui.BKUControllerInterface;
import at.gv.egiz.bku.webstart.gui.MOCCAIcon;
import iaik.asn1.CodingException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Locale;

import javax.jnlp.UnavailableServiceException;


//import com.sun.javaws.security.JavaWebStartSecurity;
import java.awt.Desktop;
import java.awt.SplashScreen;
import java.net.BindException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.security.GeneralSecurityException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;

import org.mortbay.util.MultiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Launcher implements BKUControllerInterface {

	public static final String WEBAPP_RESOURCE = "BKULocal.war";
	public static final String CERTIFICATES_RESOURCE = "BKUCertificates.jar";
	public static final String WEBAPP_FILE = "BKULocal.war";
	/** no leading slash for messages, but for image */
	public static final String MESSAGES_RESOURCE = "at/gv/egiz/bku/webstart/messages";
	public static final String TRAYICON_RESOURCE = "/at/gv/egiz/bku/webstart/chip";
	private static final String WEBSTART_FILENAME = "mocca.jnlp";
	private static Logger log = LoggerFactory.getLogger(Launcher.class);
	/** local bku uri */
	public static final URL HTTP_SECURITY_LAYER_URL;
	public static final URL HTTPS_SECURITY_LAYER_URL;
	public static final URL INSTALL_CERT_URL;
	public static final URL PIN_MANAGEMENT_URL;
	public static final URL IDENTITY_LINK_URL;
	public static final URL GETCERTIFICATE_URL;
	public static final URL HELP_URL;
	public static final URL HARDWAREINFO_URL;

	static {
		URL http = null;
		URL https = null;
		URL pin = null;
		URL ident = null;
		URL getcertificate = null;
		URL hardwareinfo = null;
		URL cert = null;
		URL help = null;
		try {
			http = new URL("http://localhost:" + Integer.getInteger(Container.HTTP_PORT_PROPERTY, 3495).intValue() + '/');
			https = new URL("https://localhost:" + Integer.getInteger(Container.HTTPS_PORT_PROPERTY, 3496).intValue() + '/');
			pin = new URL(http, "/PINManagement");
			getcertificate = new URL(http, "/GetCertificate");
			hardwareinfo = new URL(http, "/GetHardwareinfo");
			cert = new URL(http, "/ca.crt");
			help = new URL(http, "/help/");
			ident = new URL(http, "/IdentityLink");
		} catch (MalformedURLException ex) {
			log.error("Failed to create URL.", ex);
		} finally {
			HTTP_SECURITY_LAYER_URL = http;
			HTTPS_SECURITY_LAYER_URL = https;
			PIN_MANAGEMENT_URL = pin;
			IDENTITY_LINK_URL = ident;
			GETCERTIFICATE_URL = getcertificate;
			HARDWAREINFO_URL = hardwareinfo;
			INSTALL_CERT_URL = cert;
			HELP_URL = help;
		}
	}
	public static final String version;

	static {
		String tmp = Configurator.UNKOWN_VERSION;
		try {
			String bkuWebStartJar = getJarLocation(Launcher.class);
			URL manifestURL = new URL("jar:" + bkuWebStartJar + "!/META-INF/MANIFEST.MF");
			if (log.isTraceEnabled()) {
				log.trace("read version information from " + manifestURL);
			}
			Manifest manifest = new Manifest(manifestURL.openStream());
			Attributes atts = manifest.getMainAttributes();
			if (atts != null) {
				tmp = atts.getValue("Implementation-Build");
			}
		} catch (IOException ex) {
			log.error("failed to read version", ex);
		} catch (URISyntaxException ex) {
			log.error("failed to read version", ex);
		} finally {
			version = tmp;
			log.info("BKU Web Start " + version);
		}
	}
	private Configurator config;
	private Container server;
	private BasicService basicService;
	private StatusNotifier status;
	private Autostart autostart;

	private static URL codeBase;

	public Launcher() {
		log.info("Initializing Launcher");

		// SocketPerm * required (DataURL), FilePermission * write (JFileChooser) required,
		// jetty does not allow fine-grained permission config (codeBase?)
		// ie. we don't need a security manager
		log.trace("disabling (JNLP) security manager");
		System.setSecurityManager(null);

		autostart = new Autostart();
		status = new MOCCAIcon(this);
	}

	public void launch() throws Exception {
		initStart();
		try {
			initConfig();
		} catch (Exception ex) {
			log.error("Failed to initialize configuration", ex);
			status.error(StatusNotifier.ERROR_CONFIG);
			throw ex;
		}
		try {
			startServer();
			initFinished();
		} catch (BindException ex) {
			log.error("Failed to launch server, " + ex.getMessage(), ex);
			status.error(StatusNotifier.ERROR_BIND);
			throw ex;
		} catch (MultiException ex) {
			log.error("Failed to launch server, " + ex.getMessage(), ex);
			if (ex.getThrowable(0) instanceof BindException) {
				status.error(StatusNotifier.ERROR_BIND);
			} else {
				status.error(StatusNotifier.ERROR_START);
			}
			throw ex;
		} catch (Exception ex) {
			ex.printStackTrace();
			log.error("Failed to launch server, " + ex.getMessage(), ex);
			status.error(StatusNotifier.ERROR_START);
			throw ex;
		}
	}

	private void browse(URL url) throws IOException, URISyntaxException {
		// don't use basicService.showDocument(), which causes a java ssl warning dialog
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			if (desktop.isSupported(Desktop.Action.BROWSE)) {
				desktop.browse(url.toURI());
				return;
			}
		}
		throw new IOException("current platform does not support Java Desktop API");
	}

	private static String getJarLocation(Class<?> c) throws URISyntaxException,
	UnsupportedEncodingException {
		CodeSource codeSource = c.getProtectionDomain().getCodeSource();

		String loc;

		if (codeSource.getLocation() != null) {
			loc = codeSource.getLocation().toString();
		} else {
			loc = c.getResource(c.getSimpleName() + ".class").toString();
		}
		if (loc.startsWith("jar:")) {
			loc = loc.substring(loc.indexOf(":") + 1, loc.indexOf("!"));
			loc = URLDecoder.decode(loc, "UTF-8");
		}
		return loc;
	}

	private void initStart() {
		try {
			status.info(StatusNotifier.MESSAGE_START);
			basicService = (BasicService) ServiceManager.lookup("javax.jnlp.BasicService");
			codeBase = basicService.getCodeBase();
			autostart.setWebstartName(codeBase + WEBSTART_FILENAME);
			if (basicService.isOffline()) {
				log.info("launching MOCCA Web Start offline");
			} else {
				log.info("launching MOCCA Web Start online");
			}
		} catch (UnavailableServiceException ex) {
			log.info("Failed to obtain JNLP service: " + ex.getMessage());
		}
	}

	private void initConfig() throws IOException, CodingException, GeneralSecurityException {
		config = new Configurator();
		config.ensureConfiguration();
		config.ensureCertificates();
	}

	private void startServer() throws Exception {
		log.info("init servlet container and MOCCA webapp");
		server = new Container();
		server.init(status.getLocale());
		server.start();
	}

	private void initFinished() {
		try {
			status.info(StatusNotifier.MESSAGE_FINISHED);
			// standalone (non-webstart) version has splashscreen
			if (SplashScreen.getSplashScreen() != null) {
				try {
					SplashScreen.getSplashScreen().close();
				} catch (IllegalStateException ex) {
					log.warn("Failed to close splash screen: " + ex.getMessage());
				}
			}
			if (config.isCertRenewed()) {
				try {
					if ("".equals(status.getLocale().getLanguage())) {
						browse(HTTP_SECURITY_LAYER_URL);
					} else {
						browse(new URL(HTTP_SECURITY_LAYER_URL, status.getLocale().getLanguage()));
					}
				} catch (Exception ex) {
					log.error("failed to open system browser, install TLS certificate manually: " + HTTPS_SECURITY_LAYER_URL, ex);
				}
			}
			log.info("BKU successfully started");
			server.join();
		} catch (InterruptedException e) {
			log.warn("failed to join server: " + e.getMessage(), e);
		}
	}

	@Override
	public void shutDown() {
		log.info("Shutting down server");
		status.info(StatusNotifier.MESSAGE_SHUTDOWN);
		if ((server != null) && (server.isRunning())) {
			try {
				if (server.isRunning()) {
					server.stop();
				}
			} catch (Exception e) {
				log.debug(e.toString());
			} finally {
				if (server.isRunning()) {
					server.destroy();
				}
			}
		}
		System.exit(0);
	}

	public static void main(String[] args) throws InterruptedException, IOException {
		try {
			Launcher launcher = new Launcher();
			launcher.launch();
		} catch (Exception ex) {
			ex.printStackTrace();
			log.debug("Caught exception " + ex.getMessage(), ex);
			log.info("waiting to shutdown...");
			Thread.sleep(5000);
			log.info("exit");
			System.exit(-1000);
		}
	}

	@Override
	public void showHelp(Locale locale) {
		try {
			if ("".equals(locale.getLanguage())) {
				browse(HELP_URL);
			} else {
				browse(new URL(HELP_URL, locale.getLanguage()));
			}
		} catch (Exception ex) {
			log.error("Failed to open " + HELP_URL, ex);
			status.error(StatusNotifier.ERROR_OPEN_URL, HELP_URL);
		}

	}

	@Override
	public void pinManagement(Locale locale) {
		new Thread(new PINManagementInvoker(status)).start();
	}

	@Override
	public String getVersion() {
		return version;
	}

	@Override
	public boolean isAutostartPossible() {
		return autostart.isPossible();
	}

	@Override
	public boolean isAutostartEnabled() {
		return autostart.isEnabled();
	}

	@Override
	public boolean setAutostart(boolean doAutostart) {
		return autostart.set(doAutostart);
	}

	@Override
	public void getIdentityLink(Locale locale) {
		new Thread(new IdentityLinkInvoker(status)).start();
	}

	@Override
	public void getCertificate(Locale locale) {
		new Thread(new GetCertificateInvoker(status)).start();
	}

	@Override
	public void hardwareInfo(Locale locale) {
		new Thread(new HardwareInfoInvoker(status)).start();
	}
}
