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


package at.gv.egiz.bku.slxhtml.css;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import org.junit.Test;

import at.gv.egiz.bku.viewer.ValidationException;

public class CssValidatorTest {

  @Test
  public void testProperties() throws IOException {

    ClassLoader cs = CssValidatorTest.class.getClassLoader();
    InputStream is = cs.getResourceAsStream("org/w3c/css/properties/SLXHTMLProperties.properties");
    
    assertNotNull(is);
    
    Properties cssProperties = new Properties();
    cssProperties.load(is);
    
    Set<String> names = cssProperties.stringPropertyNames();
    for (String name : names) {
      String className = cssProperties.getProperty(name);
      try {
        Class.forName(className);
      } catch (ClassNotFoundException e) {
        fail("Implementation class '" + className + "' for property '" + name + "' not found.");
      }
      
    }
    
  }
  
  @Test(expected=ValidationException.class)
  public void testValidator() throws UnsupportedEncodingException, ValidationException {
    
    String css = "@charset \"ABCDEFG\";\n" +
    		" @import url(http://test.abc/test); * { color: black }";
    ByteArrayInputStream input = new ByteArrayInputStream(css.getBytes("UTF-8"));
    
    CSSValidatorSLXHTML validator = new CSSValidatorSLXHTML();
    
    Locale locale = new Locale("de");
    
    validator.validate(input, locale, "Test", 10);
    
  }
  
  
}
