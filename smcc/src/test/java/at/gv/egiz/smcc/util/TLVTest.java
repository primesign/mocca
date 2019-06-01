package at.gv.egiz.smcc.util;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * Unit tests for class {@link TLV}.
 *
 * @see TLV
 **/
public class TLVTest {

  @Test
  public void testCreateTLV() {
    byte[] byteArray = new byte[4];
    TLV tLV = new TLV(byteArray, (byte)0);

    assertEquals(0, tLV.getTag());
    assertEquals(0, tLV.getLength());
    assertEquals(1, tLV.getLengthFieldLength());

    byteArray[1] = 24;
    tLV = new TLV(byteArray, 1);

    assertEquals(24, tLV.getTag());
    assertEquals(0, tLV.getLength());
    assertEquals(1, tLV.getLengthFieldLength());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFailsToCreateTLVThrowsIllegalArgumentException() {
    new TLV(new byte[0], 1);
  }

}