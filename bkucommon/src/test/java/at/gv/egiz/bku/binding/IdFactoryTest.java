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


package at.gv.egiz.bku.binding;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class IdFactoryTest {
  
  @Before
  public void setUp() {
    IdFactory.getInstance().setNumberOfBits(168);
    
  }
  
  @Test
  public void testWithString() {
    String testString = "Hansi";
    Id hansi = IdFactory.getInstance().createId(testString);
    assertEquals(hansi.toString(), testString);
  }

  @Test(expected = NullPointerException.class)
  public void testFactory() {
    IdFactory.getInstance().setSecureRandom(null);
  }
  
  @Test
  public void testRandom() {
    IdFactory fab = IdFactory.getInstance();
    Id id = fab.createId();
    assertEquals(id.toString().length(), 28);
    fab.setNumberOfBits(24);
    id = fab.createId();
    assertEquals(id.toString().length(), 4);
  }
  
  @Test
  public void testEquals() {
    String idString = "Hansi";
    IdFactory fab = IdFactory.getInstance();
    Id id1 = fab.createId(idString);
    Id id2 = fab.createId(idString);
    assertEquals(id1, id2);
    assertEquals(id1.hashCode(), id2.hashCode());
  }
}
