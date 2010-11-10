/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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

    oldVersion = "1";
    minVersion = "2";
    expResult = true;
    result = Configurator.updateRequired(oldVersion, minVersion);
    assertEquals(expResult, result);
  }

  /**
   * Test of backupAndDelete method, of class Configurator.
   */
  @Ignore
  @Test
  public void testBackupAndDelete() throws Exception {
    System.out.println("backupAndDelete");
    File dir = null;
    URI relativeTo = null;
    ZipOutputStream zip = null;
    Configurator.backupAndDelete(dir, relativeTo, zip);
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