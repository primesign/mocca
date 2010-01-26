/*
* Copyright 2008 Federal Chancellery Austria and
* Graz University of Technology
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
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
