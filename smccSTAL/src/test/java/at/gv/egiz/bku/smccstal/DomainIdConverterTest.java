package at.gv.egiz.bku.smccstal;

import org.junit.Test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertArrayEquals;

/**
 * Unit tests for class {@link DomainIdConverter}.
 *
 * @see DomainIdConverter
 **/
public class DomainIdConverterTest {

  @Test
  public void testConvertDomainIdWithNullDomainId() throws IOException, NoSuchAlgorithmException {
    byte[] byteArray = "asdafs".getBytes("UTF-8");
    byte[] byteArrayTwo = DomainIdConverter.convertDomainId(byteArray, null);

    assertArrayEquals(byteArray, byteArrayTwo);
  }

}