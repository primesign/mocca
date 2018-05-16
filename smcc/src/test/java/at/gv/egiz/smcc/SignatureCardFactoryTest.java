package at.gv.egiz.smcc;

import org.junit.Test;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertEquals;

/**
 * Unit tests for class {@link SignatureCardFactory}.
 *
 * @see SignatureCardFactory
 **/
public class SignatureCardFactoryTest {

  @Test
  public void testToStringWithZeroByteArrayLength() {
    byte[] byteArray = new byte[0];
    assertEquals("", SignatureCardFactory.toString(byteArray));
  }

  @Test
  public void testToString() {
    byte[] byteArray = "TestString here € \"".getBytes();

    assertEquals("54:65:73:74:53:74:72:69:6e:67:20:68:65:72:65:20:e2:82:ac:20:22",
            SignatureCardFactory.toString(byteArray));
  }

  @Test
  public void testToStringWithNullParameter() {
    assertEquals("",SignatureCardFactory.toString(null));
  }

  @Test
  public void testCreateSignatureCardWithNullParameters() throws CardNotSupportedException {
    SignatureCard signatureCard = SignatureCardFactory.getInstance().createSignatureCard(null, null);

    assertNotNull(signatureCard);
    assertTrue(signatureCard instanceof SWCard);
  }

}