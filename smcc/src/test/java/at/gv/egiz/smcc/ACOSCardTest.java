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
public class ACOSCardTest {

  static ACOSCard card;
  static PINSpec infPin, decPin, sigPin;

    public ACOSCardTest() {
    }

  @BeforeClass
  public static void setUpClass() throws Exception {
    SMCCHelper smccHelper = new SMCCHelper();
      switch (smccHelper.getResultCode()) {
        case SMCCHelper.CARD_FOUND:
          SignatureCard sigCard = smccHelper.getSignatureCard(Locale.GERMAN);
          if (sigCard instanceof ACOSCard) {
            System.out.println("ACOS card found");
            card = (ACOSCard) sigCard;
            List<PINSpec> pinSpecs = card.getPINSpecs();
            infPin = pinSpecs.get(ACOSCard.PINSPEC_INF);
            decPin = pinSpecs.get(ACOSCard.PINSPEC_DEC);
            sigPin = pinSpecs.get(ACOSCard.PINSPEC_SIG);
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
   * Test of verifyPIN method, of class STARCOSCard.
   */
  @Test
  @Ignore
  public void testVerifyPIN_pinpad() throws Exception {
    System.out.println("verifyPIN (pinpad)");
    assertNotNull(card);

    card.verifyPIN(decPin, new PINProvider() {

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
    int retries = card.verifyPIN(decPin.getKID(), null); //"1397".toCharArray());

    System.out.println("VERIFY PIN returned " + retries);
    card.getCard().endExclusive();
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
    int retries = card.changePIN(decPin.getKID(), null, null);

    System.out.println("CHANGE PIN returned " + retries);
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

}