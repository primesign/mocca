/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.gv.egiz.smcc;

import at.gv.egiz.smcc.SignatureCard.KeyboxName;
import at.gv.egiz.smcc.util.SMCCHelper;
import java.util.List;
import java.util.Locale;
import javax.smartcardio.ResponseAPDU;
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
@Ignore
public class STARCOSCardTest {

  static STARCOSCard card;
  static PINSpec cardPin, ssPin;

    public STARCOSCardTest() {
    }

  @BeforeClass
  public static void setUpClass() throws Exception {
    SMCCHelper smccHelper = new SMCCHelper();
      switch (smccHelper.getResultCode()) {
        case SMCCHelper.CARD_FOUND:
          SignatureCard sigCard = smccHelper.getSignatureCard(Locale.GERMAN);
          if (sigCard instanceof STARCOSCard) {
            System.out.println("STARCOS card found");
            card = (STARCOSCard) sigCard;
            List<PINSpec> pinSpecs = card.getPINSpecs();
            cardPin = pinSpecs.get(STARCOSCard.PINSPEC_CARD);
            ssPin = pinSpecs.get(STARCOSCard.PINSPEC_SS);

          } else {
            throw new Exception("not STARCOS card: " + sigCard.toString());
          }
          break;
        default:
          throw new Exception("no card found");
      }
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
   * Test of getCertificate method, of class STARCOSCard.
   */
  @Test
  @Ignore
  public void testGetCertificate() throws Exception {
    System.out.println("getCertificate");
    KeyboxName keyboxName = null;
    STARCOSCard instance = new STARCOSCard();
    byte[] expResult = null;
    byte[] result = instance.getCertificate(keyboxName);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getInfobox method, of class STARCOSCard.
   */
  @Test
  @Ignore
  public void testGetInfobox() throws Exception {
    System.out.println("getInfobox");
    String infobox = "";
    PINProvider provider = null;
    String domainId = "";
    STARCOSCard instance = new STARCOSCard();
    byte[] expResult = null;
    byte[] result = instance.getInfobox(infobox, provider, domainId);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of createSignature method, of class STARCOSCard.
   */
  @Test
  @Ignore
  public void testCreateSignature() throws Exception {
    System.out.println("createSignature");
    byte[] hash = null;
    KeyboxName keyboxName = null;
    PINProvider provider = null;
    STARCOSCard instance = new STARCOSCard();
    byte[] expResult = null;
    byte[] result = instance.createSignature(hash, keyboxName, provider);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of selectFileFID method, of class STARCOSCard.
   */
  @Test
  @Ignore
  public void testSelectFileFID() throws Exception {
    System.out.println("selectFileFID");
    byte[] fid = null;
    STARCOSCard instance = new STARCOSCard();
    ResponseAPDU expResult = null;
    ResponseAPDU result = instance.selectFileFID(fid);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of verifyPIN method, of class STARCOSCard.
   */
  @Test
  @Ignore
  public void testVerifyPIN_pinpad() throws Exception {
    System.out.println("verifyPIN (pinpad)");
    assertNotNull(card);

    card.verifyPIN(cardPin, new PINProvider() {

      @Override
      public char[] providePIN(PINSpec spec, int retries) {
        return null;
      }
    });
  }

  /**
   * Test of verifyPIN method, of class STARCOSCard.
   */
  @Test
  @Ignore
  public void testVerifyPIN_internal() throws Exception {
    System.out.println("verifyPIN (internal)");
    assertNotNull(card);

    card.reset();

    card.getCard().beginExclusive();

    // 0x6700 without sending an APDU prior to send CtrlCmd
    System.out.println("WARNING: this command will fail if no card " +
            "communication took place prior to sending the CtrlCommand");
    int retries = card.verifyPIN(cardPin.getKID(), null); //"1397".toCharArray());

    System.out.println("VERIFY PIN returned " + retries);
    card.getCard().endExclusive();
  }

  /**
   * Test of verifyPIN method, of class STARCOSCard.
   */
  @Test
  @Ignore
  public void testVerifyPIN_byte() throws Exception {
    System.out.println("verifyPIN");
    byte kid = 0;
    STARCOSCard instance = new STARCOSCard();
    int expResult = 0;
    int result = instance.verifyPIN(kid);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of changePIN method, of class STARCOSCard.
   */
  @Test
  @Ignore
  public void testChangePIN() throws Exception {
    System.out.println("changePIN");
    assertNotNull(card);

    card.reset();
    int retries = card.changePIN(cardPin.getKID(), null, null);

    System.out.println("CHANGE PIN returned " + retries);
  }

  /**
   * Test of activatePIN method, of class STARCOSCard.
   */
  @Test
  @Ignore
  public void testActivatePIN() throws Exception {
    System.out.println("activatePIN");
    assertNotNull(card);

    card.reset();
    card.activatePIN(cardPin, new PINProvider() {

      @Override
      public char[] providePIN(PINSpec spec, int retries) throws CancelledException, InterruptedException {
        return null;
      }
    });
  }

  /**
   * Test of encodePINBlock method, of class STARCOSCard.
   */
  @Test
  @Ignore
  public void testEncodePINBlock() throws Exception {
    System.out.println("encodePINBlock");
    char[] pin = null;
    STARCOSCard instance = new STARCOSCard();
    byte[] expResult = null;
    byte[] result = instance.encodePINBlock(pin);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of reset method, of class STARCOSCard.
   */
  @Test
  public void testReset() throws Exception {
    System.out.println("reset");
    assertNotNull(card);
    card.reset();
  }

  /**
   * Test of toString method, of class STARCOSCard.
   */
  @Test
  @Ignore
  public void testToString() {
    System.out.println("toString");
    STARCOSCard instance = new STARCOSCard();
    String expResult = "";
    String result = instance.toString();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

}