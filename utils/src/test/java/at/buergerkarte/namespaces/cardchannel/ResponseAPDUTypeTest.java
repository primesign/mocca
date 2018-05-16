package at.buergerkarte.namespaces.cardchannel;

import org.junit.Test;

import java.math.BigInteger;

import static junit.framework.TestCase.assertEquals;

/**
 * Unit tests for class {@link ResponseAPDUType}.
 *
 * @see ResponseAPDUType
 **/
public class ResponseAPDUTypeTest {

  @Test
  public void testGetRc() throws Exception {
    ResponseAPDUType responseAPDUType = new ResponseAPDUType();
    assertEquals(BigInteger.ZERO, responseAPDUType.getRc());
  }

}