package at.gv.egiz.org.apache.tomcat.util.http;

import org.junit.Test;

import java.util.Locale;

import static junit.framework.TestCase.assertEquals;

/**
 * Unit tests for class {@link AcceptLanguage}.
 *
 * @see AcceptLanguage
 **/
public class AcceptLanguageTest {

  @Test
  public void testGetLocaleReturnsDefaultGivenNullOrEmptyString() throws Exception {
    assertEquals(Locale.getDefault(), AcceptLanguage.getLocale(null));
    assertEquals(Locale.getDefault(), AcceptLanguage.getLocale(""));

    Locale.setDefault(Locale.CANADA_FRENCH);
    assertEquals(Locale.CANADA_FRENCH, AcceptLanguage.getLocale(null));
    assertEquals(Locale.CANADA_FRENCH, AcceptLanguage.getLocale(""));
  }

}