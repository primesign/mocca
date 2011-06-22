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


package at.gv.egiz.bku.slxhtml;

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;

import at.gv.egiz.bku.viewer.ValidationException;
import at.gv.egiz.bku.viewer.Validator;
import at.gv.egiz.bku.viewer.ValidatorFactory;

//@Ignore
public class ValidatorTest {
  
  private static Logger log = LoggerFactory.getLogger(ValidatorTest.class);
  
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
    log.info("Validated SLXHTML file '{}' in {}ms.", slxhtmlFile, t1 - t0);
    
  }
  
}
