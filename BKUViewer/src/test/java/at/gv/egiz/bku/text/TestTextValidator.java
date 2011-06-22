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


package at.gv.egiz.bku.text;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.junit.Ignore;
import org.junit.Test;

import at.gv.egiz.bku.viewer.ValidationException;
import at.gv.egiz.bku.viewer.Validator;
import at.gv.egiz.bku.viewer.ValidatorFactory;

public class TestTextValidator {

  public static byte[] generateText(String encoding) throws UnsupportedEncodingException {

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    PrintWriter writer = new PrintWriter(new OutputStreamWriter(bos, encoding));

    writer.write("C0 Controls and Basic Latin  0x0009-0x000A");
    writer.write("\n");
    for (char c = '\t'; c <= '\n'; c++) {
      writer.write(c);
    }
    writer.write("\n");
    // errata: don't include FORM FEED (0x000C)
    writer.write("C0 Controls and Basic Latin 0x000D");
    writer.write("\n");

//    for (char c = '\f'; c <= '\r'; c++) {
//      writer.write(c);
//    }

    writer.write("\r");
    writer.write("\n");
    writer.write("C0 Controls and Basic Latin  0x0020-0x007E");
    writer.write("\n");
    for (char c = '\u0020'; c <= '\u007E'; c++) {
      writer.write(c);
    }
    writer.write("\n");
    writer.write("C1 Controls and Latin-1 Supplement  0x00A1-0x00FF");
    writer.write("\n");
    for (char c = '\u00A1'; c <= '\u00FF'; c++) {
      writer.write(c);
    }
    writer.write("\n");
    writer.write("Latin Extended-A  0x0100-0x017F");
    writer.write("\n");
    for (char c = '\u0100'; c <= '\u017F'; c++) {
      writer.write(c);
    }
    writer.write("\n");
    writer.write("Spacing Modifier Letters  0x02C7");
    writer.write("\n");
    writer.write("\u02C7");
    writer.write("\n");
    writer.write("Spacing Modifier Letters  0x02D8");
    writer.write("\n");
    writer.write("\u02D8");
    writer.write("\n");
    writer.write("Spacing Modifier Letters  0x02D9");
    writer.write("\n");
    writer.write("\u02D9");
    writer.write("\n");
    writer.write("Spacing Modifier Letters  0x02DB");
    writer.write("\n");
    writer.write("\u02DB");
    writer.write("\n");
    writer.write("Spacing Modifier Letters  0x02DD");
    writer.write("\n");
    writer.write("\u02DD");
    writer.write("\n");
    writer.write("General Punctuation   0x2015");
    writer.write("\n");
    writer.write("\u2015");
    writer.write("\n");
    writer.write("Currency Symbols 0x20AC");
    writer.write("\n");
    writer.write("\u20AC");
    writer.flush();

    return bos.toByteArray();

  }

  public void testTextValidation(String encoding) throws ValidationException, UnsupportedEncodingException {

    Validator validator = ValidatorFactory.newValidator("text/plain");

    assertNotNull(validator);

    InputStream is = new ByteArrayInputStream(generateText(encoding));

    assertNotNull(is);

    validator.validate(is, encoding);

  }

  @Test
  public void testUTF8() throws ValidationException, UnsupportedEncodingException {
    testTextValidation("UTF-8");
  }

  @Test
  public void testISO8859_1() throws ValidationException, UnsupportedEncodingException {
    testTextValidation("ISO-8859-1");
  }

  @Test
  public void testISO8859_2() throws ValidationException, UnsupportedEncodingException {
    testTextValidation("ISO-8859-2");
  }

  @Test
  public void testISO8859_3() throws ValidationException, UnsupportedEncodingException {
    testTextValidation("ISO-8859-3");
  }

  @Test
  public void testISO8859_9() throws ValidationException, UnsupportedEncodingException {
    testTextValidation("ISO-8859-9");
  }

  @Ignore
  @Test
  public void testISO8859_10() throws ValidationException, UnsupportedEncodingException {
    testTextValidation("ISO-8859-10");
  }

  @Test
  public void testISO8859_15() throws ValidationException, UnsupportedEncodingException {
    testTextValidation("ISO-8859-15");
  }

  @Test
  public void testPerformance() throws UnsupportedEncodingException, ValidationException {
    Validator validator = ValidatorFactory.newValidator("text/plain");

    assertNotNull(validator);

    //!"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\]^_`abcdefghijklmnopqrstuvwxyz{|}~¡¢£¤¥¦§¨©ª«¬­®¯°±²³´µ¶·¸¹º»¼½¾¿ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿΎΏΐΑΒΓΔΕΖΗΘΙΚΛΜΝΞΟΠΡΣΤΥΦΧΨΩΪΫάέήίΰαβγδεζηθικλμνξοπρςστυφχψωϊϋόύώϐϑϒϓϔϕϖϗϘϙϚϛϜϝϞϟϠϡЀЁЂЃЄЅІЇЈЉЊЋЌЍЎЏАБВГДЕЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдежзийклмнопрстуфхцчшщъыьэюяѐёђѓєѕіїјљњћќѝўџҐґҒғҔҕҖҗҘҙҚқҰұҲҳӀӁӂӃӄӐӑӒӓӔӕӖӗӘәӚӛӜӝӞӟӠӡӢӣӤӥӦӧӨөӪӫӬӭӮӯӰӱӲӳӴӵӶӷӸӹ

    StringBuilder data = new StringBuilder();
    //LATIN
    for (int i = 0x0021; i <= 0x007e; i++) {
      data.append((char) i);
    }
    //LATIN supplement
    for (int i = 0x00A1; i <= 0x00FF; i++) {
      data.append((char) i);
    }
    //GREEK
    for (int i = 0x038e; i <= 0x03a1; i++) {
      data.append((char) i);
    }
    for (int i = 0x03a3; i <= 0x03ce; i++) {
      data.append((char) i);
    }
    for (int i = 0x03d0; i <= 0x03e1; i++) {
      data.append((char) i);
    }
    //CYRILLIC
    for (int i = 0x0400; i <= 0x045f; i++) {
      data.append((char) i);
    }
    for (int i = 0x0490; i <= 0x049b; i++) {
      data.append((char) i);
    }
    for (int i = 0x04b0; i <= 0x04b3; i++) {
      data.append((char) i);
    }
    for (int i = 0x04c0; i <= 0x04c4; i++) {
      data.append((char) i);
    }
    for (int i = 0x04d0; i <= 0x04f9; i++) {
      data.append((char) i);
    }

    StringBuilder aLotOfData = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      aLotOfData.append('\n');
      aLotOfData.append(data);
    }
    String aLotOfText = aLotOfData.toString();
    System.out.println("validating " + aLotOfText.length() + " weird characters: " + aLotOfText);

    InputStream is = new ByteArrayInputStream(aLotOfText.getBytes("UTF-8"));

    assertNotNull(is);

    validator.validate(is, "UTF-8");

  }
}
