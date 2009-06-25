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
package at.gv.egiz.bku.slxhtml;

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Ignore;
import org.junit.Test;

import at.gv.egiz.bku.viewer.ValidationException;
import at.gv.egiz.bku.viewer.Validator;
import at.gv.egiz.bku.viewer.ValidatorFactory;

//@Ignore
public class ValidatorTest {
  
  private static Log log = LogFactory.getLog(ValidatorTest.class);
  
  @Test
  public void testGetInstance() {
    
    Validator validator = ValidatorFactory.newValidator("application/xhtml+xml");
    
    assertNotNull(validator);
    
  }

  @Test
  public void testValidate() throws ValidationException {
    
    String slxhtmlFile = "at/gv/egiz/bku/slxhtml/zugang.xhtml";
    
    Validator validator = ValidatorFactory.newValidator("application/xhtml+xml");

    ClassLoader cl = ValidatorTest.class.getClassLoader();
    InputStream slxhtml = cl.getResourceAsStream(slxhtmlFile);
    long t0 = System.currentTimeMillis();
    try {
      validator.validate(slxhtml, null);
    } catch (ValidationException e) {
      e.printStackTrace();
      throw e;
    }
    long t1 = System.currentTimeMillis();
    log.info("Validated SLXHTML file '" + slxhtmlFile + "' in " + (t1 - t0) + "ms.");
    
  }
  
}
