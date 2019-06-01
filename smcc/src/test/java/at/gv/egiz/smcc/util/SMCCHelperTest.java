package at.gv.egiz.smcc.util;

import org.junit.Test;

import javax.smartcardio.CardException;

import static org.junit.Assert.assertArrayEquals;

/**
 * Unit tests for class {@link SMCCHelper}.
 *
 * @see SMCCHelper
 **/
public class SMCCHelperTest {

  @Test
  public void testToByteArray() throws Exception {
    assertArrayEquals(new byte[] {0, 0}, SMCCHelper.toByteArray(0));
    assertArrayEquals(new byte[] {0, 1}, SMCCHelper.toByteArray(1));
    assertArrayEquals(new byte[] {0, 4}, SMCCHelper.toByteArray(4));
    assertArrayEquals(new byte[] {0, 9}, SMCCHelper.toByteArray(9));
    assertArrayEquals(new byte[] {0, 99}, SMCCHelper.toByteArray(99));
    assertArrayEquals(new byte[] {0, 83}, SMCCHelper.toByteArray(0123));
    assertArrayEquals(new byte[] {86, 107}, SMCCHelper.toByteArray(22123));
  }

  @Test(expected = CardException.class)
  public void testToByteArrayWithTooBigInput() throws Exception {
    SMCCHelper.toByteArray(222123);
  }

}