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

import iaik.asn1.CodingException;
import iaik.utils.StreamCopier;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.UUID;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class Configurator {

  /**
   * MOCCA configuration
   * configurations with less than this (major) version will be backed up and updated
   * TLS Certificate will be recreated
   * allowed: MAJOR[.MINOR[.X[-SNAPSHOT]]]
   */
  public static final String MIN_CONFIG_VERSION = "1.3.4-SNAPSHOT";
  /**
   * configurations with less than this (major) version will be backed up and updated
   * TLS Certificate will *NOT* be recreated
   * allowed: MAJOR[.MINOR[.X[-SNAPSHOT]]]
   */
  public static final String UPDATE_CONFIG_VERSION = "1.3.9-SNAPSHOT";

  public static final String BKU_USER_DIR = ".mocca/";
  public static final String CONFIG_DIR = BKU_USER_DIR + "conf/";
  public static final String CERTS_DIR = BKU_USER_DIR + "certs/";
  public static final String VERSION_FILE = ".version";
  public static final String UNKOWN_VERSION = "unknown";
  public static final String CONF_TEMPLATE_FILE = "conf-tmp.zip";
  public static final String CONF_TEMPLATE_RESOURCE = "at/gv/egiz/bku/webstart/conf/conf.zip";
  public static final String CERTIFICATES_PKG = "at/gv/egiz/bku/certs";

  /**
   * MOCCA TLS certificate
   */
  public static final String KEYSTORE_FILE = "keystore.ks";
  public static final String PASSWD_FILE = ".secret";

  private static final Logger log = LoggerFactory.getLogger(Configurator.class);

  /** currently installed configuration version */
  private String version;
  private String certsVersion;
  /** whether a new MOCCA TLS cert was created during initialization */
  private boolean certRenewed = false;

  /**
   * Checks whether the config directory already exists and creates it otherwise.
   * @param configDir the config directory to be created
   * @throws IOException config/certificate creation failed
   * @throws GeneralSecurityException if MOCCA TLS certificate could not be created
   * @throws CodingException if MOCCA TLS certificate could not be created
   */
  public void ensureConfiguration() throws IOException, CodingException, GeneralSecurityException {
    File configDir = new File(System.getProperty("user.home") + '/' + CONFIG_DIR);
    if (configDir.exists()) {
      if (configDir.isFile()) {
        log.error("invalid config directory: " + configDir);
        throw new IOException("invalid config directory: " + configDir);
      } else {
        version = readVersion(new File(configDir, VERSION_FILE));
        if (log.isDebugEnabled()) {
          log.debug("config directory " + configDir + ", version " + version);
        }
        if (updateRequired(version, MIN_CONFIG_VERSION)) {
          File moccaDir = configDir.getParentFile();
          File zipFile = new File(moccaDir, "conf-" + version + ".zip");
          ZipOutputStream zipOS = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
          log.info("backup configuration to " + zipFile);
          backup(configDir, moccaDir.toURI(), zipOS, true);
          zipOS.close();
          initConfig(configDir);
        } else if (updateRequired(version, UPDATE_CONFIG_VERSION)) {
          File moccaDir = configDir.getParentFile();
          File zipFile = new File(moccaDir, "conf-" + version + ".zip");
          ZipOutputStream zipOS = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
          log.info("backup configuration to " + zipFile);
          backup(configDir, moccaDir.toURI(), zipOS, false);
          zipOS.close();
          updateConfig(configDir);
        }
        if (caCertificateUpdateRequired()) {
          log.info("Creating new CA certificate");
          createKeyStore(configDir);
          certRenewed = true;
        }
      }
    } else {
      initConfig(configDir);
    }
    // re-configure logging
    // TODO: move to appropriate place
    String log4jconfig = configDir.getPath() + File.separatorChar + "log4j.properties";
    log.debug("Reconfiguring logging with " + log4jconfig);
    PropertyConfigurator.configureAndWatch(log4jconfig);
  }

  /**
   * To be replaced by TSLs in IAIK-PKI
   * @throws IOException
   */
  public void ensureCertificates() throws IOException {
    File certsDir = new File(System.getProperty("user.home") + '/' + CERTS_DIR);
    if (certsDir.exists()) {
      if (certsDir.isFile()) {
        log.error("invalid certificate store directory: " + certsDir);
        throw new IOException("invalid config directory: " + certsDir);
      } else {
        certsVersion = readVersion(new File(certsDir, VERSION_FILE));
        if (log.isDebugEnabled()) {
          log.debug("certificate-store directory " + certsDir + ", version " + certsVersion);
        }
        String newCertsVersion = getCertificatesVersion();
        if (updateRequired(certsVersion, newCertsVersion)) {
          File moccaDir = certsDir.getParentFile();
          File zipFile = new File(moccaDir, "certs-" + certsVersion + ".zip");
          ZipOutputStream zipOS = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
          log.info("backup certificates to " + zipFile);
          backup(certsDir, moccaDir.toURI(), zipOS, true);
          zipOS.close();

          createCerts(certsDir, newCertsVersion);
          certsVersion = newCertsVersion;
        }
      }
    } else {
      String newCertsVersion = getCertificatesVersion();
      createCerts(certsDir, newCertsVersion);
      certsVersion = newCertsVersion;
    }
  }

  /**
   * 
   * @return whether a new MOCCA TLS certificate has been created during initialization
   */
  public boolean isCertRenewed() {
    return certRenewed;
  }

  /**
   * @return The first valid (not empty, no comment) line of the version file or
   * "unknown" if version file cannot be read or does not contain such a line.
   */
  protected static String readVersion(File versionFile) {
    if (versionFile.exists() && versionFile.canRead()) {
      BufferedReader versionReader = null;
      try {
        versionReader = new BufferedReader(new FileReader(versionFile));
        String version;
        while ((version = versionReader.readLine().trim()) != null) {
          if (version.length() > 0 && !version.startsWith("#")) {
            log.trace("configuration version from " + versionFile + ": " + version);
            return version;
          }
        }
      } catch (IOException ex) {
        log.error("failed to read configuration version from " + versionFile, ex);
      } finally {
        try {
          versionReader.close();
        } catch (IOException ex) {
        }
      }
    }
    log.debug("unknown configuration version");
    return UNKOWN_VERSION;
  }

  /**
   * Temporary workaround, replace with TSLs in IAIK-PKI.
   * Retrieves version from BKUCertificates.jar Manifest file. 
   * The (remote) resource URL will be handled by the JNLP loader, 
   * and the resource retrieved from the cache.
   *
   * @return
   * @throws IOException
   */
  private static String getCertificatesVersion() throws IOException {
    String certsResourceVersion = null;
    URL certsURL = Configurator.class.getClassLoader().getResource(CERTIFICATES_PKG);
    if (certsURL != null) {
      StringBuilder url = new StringBuilder(certsURL.toExternalForm());
      url = url.replace(url.length() - CERTIFICATES_PKG.length(), url.length(), "META-INF/MANIFEST.MF");
      log.trace("retrieve certificates resource version from " + url);
      certsURL = new URL(url.toString());
      Manifest certsManifest = new Manifest(certsURL.openStream());
      Attributes atts = certsManifest.getMainAttributes();
      if (atts != null) {
        certsResourceVersion = atts.getValue("Implementation-Version");
        log.debug("certs resource version: " + certsResourceVersion);
      }
    } else {
      log.error("Failed to retrieve certificates resource " + CERTIFICATES_PKG);
      throw new IOException("Failed to retrieve certificates resource " + CERTIFICATES_PKG);
    }
    return certsResourceVersion;
  }

  /**
   * if unknown old, update in any case
   * if known old and unknown min, don't update
   * 
   * VERSION := MAJOR[-SNAPSHOT]-rREV
   * MAJOR   := [0-9\.]*[-BRANCH[-BRANCHVERSION]]
   *
   * assume dots '.' appear in major version only (not after "-SNAPSHOT")
   *
   * @param oldVersion
   * @param minVersion
   * @return
   */
  protected static boolean updateRequired(String oldVersion, String minVersion) {
    log.debug("comparing " + oldVersion + " to " + minVersion);
    if (oldVersion != null && !UNKOWN_VERSION.equals(oldVersion)) {
      if (minVersion != null && !UNKOWN_VERSION.equals(minVersion)) {
        int fromInd = 0;
        int nextIndOld, nextIndMin;
        int xOld, xMin;

        // assume dots '.' appear in major version only (not after "-SNAPSHOT")
        while ((nextIndOld = oldVersion.indexOf('.', fromInd)) > 0) {
          nextIndMin = minVersion.indexOf('.', fromInd);
          if (nextIndMin < 0) {
            log.debug("installed version newer than minimum required (newer minor version)");
          }
          xOld = Integer.valueOf(oldVersion.substring(fromInd, nextIndOld));
          xMin = Integer.valueOf(minVersion.substring(fromInd, nextIndMin));
          if (xMin > xOld) {
            log.debug("update required");
            return true;
          } else if (xMin < xOld) {
            log.debug("installed version newer than minimum required");
            return false;
          }
          fromInd = nextIndOld + 1;
        }

        // compare last digit of major
        boolean preRelease = true;
        int majorEndOld = oldVersion.indexOf("-SNAPSHOT"); // 1.0.10-SNAPSHOT-r438, 1.2.12-pinguin-1-SNAPSHOT-r635
        if (majorEndOld < 0) {
          preRelease = false;
          majorEndOld = oldVersion.lastIndexOf('-'); // 1.0.10-r439, 1.2.12-pinguin-1-r635, 1.3.0-RC2-r611
          if (majorEndOld < 0) {
            majorEndOld = oldVersion.length();
          }
        }

        boolean releaseRequired = false;
        int majorEndMin = minVersion.indexOf("-SNAPSHOT");
        if (majorEndMin < 0) {
          releaseRequired = true;
          majorEndMin = minVersion.lastIndexOf('-');
          if (majorEndMin < 0) {
            majorEndMin = minVersion.length();
          }
        }

        try {
            xOld = Integer.valueOf(oldVersion.substring(fromInd, majorEndOld));
        } catch (NumberFormatException ex) {
            log.warn("{} seems to be a branch version, do not update", oldVersion);
            log.debug(ex.getMessage(), ex);
            return false;
        }
        boolean hasMoreDigitsMin = true;
        nextIndMin = minVersion.indexOf('.', fromInd);
        if (nextIndMin < 0) {
          hasMoreDigitsMin = false;
          nextIndMin = majorEndMin;
        }
        xMin = Integer.valueOf(minVersion.substring(fromInd, nextIndMin));
        if (xMin > xOld) {
          log.debug("update required");
          return true;
        } else if (xMin < xOld) {
          log.debug("installed version newer than minimum required");
          return false;
        } else if (hasMoreDigitsMin) { // xMin == xOld
          log.debug("update required (newer minor version required)");
          return true;
        } else if (preRelease && releaseRequired) {
          log.debug("pre-release installed but release required");
          return true;
        } else {
          log.debug("exact match, no updated required");
          return false;
        }
      }
      log.debug("unknown minimum version, do not update");
      return false;
    }
    log.debug("no old version, update required");
    return true;
  }

  private static boolean caCertificateUpdateRequired() {
    String configDir = System.getProperty("user.home") + '/' + CONFIG_DIR;
    File keystoreFile = new File(configDir, KEYSTORE_FILE);
    File passwdFile = new File(configDir, PASSWD_FILE);
    String passwd;
    try {
      passwd = Container.readPassword(passwdFile);
    } catch (IOException e) {
      log.error("Error reading password file", e);
      return true;
    }
    X509Certificate cert = (X509Certificate) Container.getCACertificate(keystoreFile, passwd.toCharArray());
    try {
      cert.checkValidity();
    } catch (CertificateExpiredException e) {
      log.warn("CA Certificate expired");
      return true;
    } catch (CertificateNotYetValidException e) {
      log.error("CA Certificate not yet valid");
      return true;
    }
    return false;
  }

  protected static void backup(File dir, URI relativeTo, ZipOutputStream zip, boolean doDelete) throws IOException {
    if (dir.isDirectory()) {
      File[] subDirs = dir.listFiles();
      for (File subDir : subDirs) {
        backup(subDir, relativeTo, zip, doDelete);
        if (doDelete)
          subDir.delete();
      }
    } else {
      URI relativePath = relativeTo.relativize(dir.toURI());
      ZipEntry entry = new ZipEntry(relativePath.toString());
      zip.putNextEntry(entry);
      BufferedInputStream entryIS = new BufferedInputStream(new FileInputStream(dir));
      new StreamCopier(entryIS, zip).copyStream();
      entryIS.close();
      zip.closeEntry();
      if (doDelete)
        dir.delete();
    }
  }

  /**
   * update MOCCA local configuration
   * @throws IOException config creation failed
   */
  protected void updateConfig(File configDir) throws IOException {
    createConfig(configDir, Launcher.version);
    version = Launcher.version;
  }

  /**
   * set up a new MOCCA local configuration
   * (not to be called directly, call ensureConfiguration())
   * @throws IOException config/certificate creation failed
   * @throws GeneralSecurityException if MOCCA TLS certificate could not be created
   * @throws CodingException if MOCCA TLS certificate could not be created
   */
  protected void initConfig(File configDir) throws IOException, GeneralSecurityException, CodingException {
    updateConfig(configDir);
    createKeyStore(configDir);
    certRenewed = true;
  }

  private static void createConfig(File configDir, String version) throws IOException {
    if (log.isDebugEnabled()) {
      log.debug("creating configuration version " + Launcher.version + " in " + configDir);
    }
    configDir.mkdirs();
    File confTemplateFile = new File(configDir, CONF_TEMPLATE_FILE);
    InputStream is = Configurator.class.getClassLoader().getResourceAsStream(CONF_TEMPLATE_RESOURCE);
    OutputStream os = new BufferedOutputStream(new FileOutputStream(confTemplateFile));
    new StreamCopier(is, os).copyStream();
    os.close();
    unzip(confTemplateFile, configDir);
    confTemplateFile.delete();
    writeVersionFile(new File(configDir, VERSION_FILE), version);
  }

  /**
   * set up a new MOCCA local certStore
   * @throws IOException config/certificate creation failed
   * @throws GeneralSecurityException if MOCCA TLS certificate could not be created
   * @throws CodingException if MOCCA TLS certificate could not be created
   */
  private static void createCerts(File certsDir, String certsVersion) throws IOException {
    if (log.isDebugEnabled()) {
      log.debug("creating certificate-store " + certsDir + ", version " + certsVersion);
    }
    URL certsURL = Configurator.class.getClassLoader().getResource(CERTIFICATES_PKG);
    if (certsURL != null) {
      StringBuilder url = new StringBuilder(certsURL.toExternalForm());
      url = url.replace(url.length() - CERTIFICATES_PKG.length(), url.length(), "META-INF/MANIFEST.MF");
      log.trace("retrieve certificate resource names from " + url);
      certsURL = new URL(url.toString());
      Manifest certsManifest = new Manifest(certsURL.openStream());
      certsDir.mkdirs();
      Iterator<String> entries = certsManifest.getEntries().keySet().iterator();
      while (entries.hasNext()) {
        String entry = entries.next();
        if (entry.startsWith(CERTIFICATES_PKG)) {
          String f = entry.substring(CERTIFICATES_PKG.length()); // "/trustStore/..."
          new File(certsDir, f.substring(0, f.lastIndexOf('/'))).mkdirs();
          BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(certsDir, f)));
          log.debug(f);
          new StreamCopier(Configurator.class.getClassLoader().getResourceAsStream(entry), bos).copyStream();
          bos.close();
        } else {
          log.trace("ignore " + entry);
        }
      }
      writeVersionFile(new File(certsDir, VERSION_FILE), certsVersion);
    } else {
      log.error("Failed to retrieve certificates resource " + CERTIFICATES_PKG);
      throw new IOException("Failed to retrieve certificates resource " + CERTIFICATES_PKG);
    }
  }

  private static void unzip(File zipfile, File toDir) throws IOException {
    ZipFile zipFile = new ZipFile(zipfile);
    Enumeration<? extends ZipEntry> entries = zipFile.entries();
    while (entries.hasMoreElements()) {
      ZipEntry entry = entries.nextElement();
      File eF = new File(toDir, entry.getName());
      if (entry.isDirectory()) {
        eF.mkdirs();
        continue;
      }
      File f = new File(eF.getParent());
      f.mkdirs();
      new StreamCopier(zipFile.getInputStream(entry),
              new FileOutputStream(eF)).copyStream();
    }
    zipFile.close();
  }

  private static void writeVersionFile(File versionFile, String version) throws IOException {
    BufferedWriter versionWriter = new BufferedWriter(new FileWriter(versionFile));
    versionWriter.write("# MOCCA Web Start configuration version\n");
    versionWriter.write("# DO NOT MODIFY THIS FILE\n\n");
    versionWriter.write(version);
    versionWriter.close();
  }

  private static void createKeyStore(File configDir) throws IOException, GeneralSecurityException, CodingException {
    char[] password = UUID.randomUUID().toString().toCharArray();
    File passwdFile = new File(configDir, PASSWD_FILE);
    FileWriter passwdWriter = new FileWriter(passwdFile);
    passwdWriter.write(password);
    passwdWriter.close();
    if (!passwdFile.setReadable(false, false) || !passwdFile.setReadable(true, true)) {
      log.error("failed to make " + passwdFile + " owner readable only (certain file-systems do not support owner's permissions)");
    }
    TLSServerCA ca = new TLSServerCA();
    KeyStore ks = ca.generateKeyStore(password);
    File ksFile = new File(configDir, KEYSTORE_FILE);
    FileOutputStream fos = new FileOutputStream(ksFile);
    ks.store(fos, password);
    fos.close();
  }
}
