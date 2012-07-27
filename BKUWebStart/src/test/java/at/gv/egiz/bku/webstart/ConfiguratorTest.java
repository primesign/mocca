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

import java.io.File;
import java.net.URI;
import java.util.zip.ZipOutputStream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author clemens
 */
public class ConfiguratorTest {


    public ConfiguratorTest() {
    }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

  /**
   * Test of ensureConfiguration method, of class Configurator.
   */
  @Ignore
  @Test
  public void testEnsureConfiguration() throws Exception {
    System.out.println("ensureConfiguration");
    Configurator instance = new Configurator();
    instance.ensureConfiguration();
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of ensureCertificates method, of class Configurator.
   */
  @Ignore
  @Test
  public void testEnsureCertificates() throws Exception {
    System.out.println("ensureCertificates");
    Configurator instance = new Configurator();
    instance.ensureCertificates();
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of isCertRenewed method, of class Configurator.
   */
  @Ignore
  @Test
  public void testIsCertRenewed() {
    System.out.println("isCertRenewed");
    Configurator instance = new Configurator();
    boolean expResult = false;
    boolean result = instance.isCertRenewed();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of readVersion method, of class Configurator.
   */
  @Ignore
  @Test
  public void testReadVersion() {
    System.out.println("readVersion");
    File versionFile = null;
    String expResult = "";
    String result = Configurator.readVersion(versionFile);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of updateRequired method, of class Configurator.
   */
  @Test
  public void testUpdateRequired() {
    System.out.println("updateRequired");
    String oldVersion = "1.0.9-SNAPSHOT-r123";
    String minVersion = "1.0.9-SNAPSHOT";
    boolean expResult = false;
    boolean result = Configurator.updateRequired(oldVersion, minVersion);
    assertEquals(expResult, result);

    oldVersion = "1.0.9-SNAPSHOT-r123";
    minVersion = "1.0.9";
    expResult = true;
    result = Configurator.updateRequired(oldVersion, minVersion);
    assertEquals(expResult, result);

    oldVersion = "1.0.9-SNAPSHOT-r123";
    minVersion = "1.0.10-r432";
    expResult = true;
    result = Configurator.updateRequired(oldVersion, minVersion);
    assertEquals(expResult, result);

    oldVersion = "1.0.9-r123";
    minVersion = "1.0.10-SNAPSHOT";
    expResult = true;
    result = Configurator.updateRequired(oldVersion, minVersion);
    assertEquals(expResult, result);

    oldVersion = "1.0.9-r123";
    minVersion = "1.0.9-SNAPSHOT";
    expResult = false;
    result = Configurator.updateRequired(oldVersion, minVersion);
    assertEquals(expResult, result);

    oldVersion = "1.0.9-SNAPSHOT";
    minVersion = "1.0.9-r349";
    expResult = true;
    result = Configurator.updateRequired(oldVersion, minVersion);
    assertEquals(expResult, result);

    oldVersion = "1.0.9-SNAPSHOT-r123";
    minVersion = "1.0.10-SNAPSHOT";
    expResult = true;
    result = Configurator.updateRequired(oldVersion, minVersion);
    assertEquals(expResult, result);

    oldVersion = "1.0.9";
    minVersion = "1.0.9.1-SNAPSHOT";
    expResult = true;
    result = Configurator.updateRequired(oldVersion, minVersion);
    assertEquals(expResult, result);

    oldVersion = "1.0.9";
    minVersion = "1.0.8.99-SNAPSHOT";
    expResult = false;
    result = Configurator.updateRequired(oldVersion, minVersion);
    assertEquals(expResult, result);

    oldVersion = "1.2.3-r123";
    minVersion = "1.2.4-r124";
    expResult = true;
    result = Configurator.updateRequired(oldVersion, minVersion);
    assertEquals(expResult, result);

    oldVersion = "1.2.3-SNAPSHOT-r123";
    minVersion = "1.2.4-SNAPSHOT";
    expResult = true;
    result = Configurator.updateRequired(oldVersion, minVersion);
    assertEquals(expResult, result);

    oldVersion = "1.2.13-r637";
    minVersion = "1.3.0";
    expResult = true;
    result = Configurator.updateRequired(oldVersion, minVersion);
    assertEquals(expResult, result);

    //no update for branch versions
    oldVersion = "1.2.13-pinguin-1";
    minVersion = "1.2.14";
    expResult = false;
    result = Configurator.updateRequired(oldVersion, minVersion);
    assertEquals(expResult, result);

    //... but for major version changes
    oldVersion = "1.2.13-pinguin-1";
    minVersion = "1.3.0";
    expResult = true;
    result = Configurator.updateRequired(oldVersion, minVersion);
    assertEquals(expResult, result);

    oldVersion = "1.3.0-RC2-r611";
    minVersion = "1.3.0";
    expResult = false;
    result = Configurator.updateRequired(oldVersion, minVersion);
    assertEquals(expResult, result);

    oldVersion = "1";
    minVersion = "2";
    expResult = true;
    result = Configurator.updateRequired(oldVersion, minVersion);
    assertEquals(expResult, result);
  }

  /**
   * Test of backup method, of class Configurator.
   */
  @Ignore
  @Test
  public void testBackup() throws Exception {
    System.out.println("backup");
    File dir = null;
    URI relativeTo = null;
    ZipOutputStream zip = null;
    Configurator.backup(dir, relativeTo, zip, true);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of initConfig method, of class Configurator.
   */
  @Ignore
  @Test
  public void testInitConfig() throws Exception {
    System.out.println("initConfig");
    File configDir = null;
    Configurator instance = new Configurator();
    instance.initConfig(configDir);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

}