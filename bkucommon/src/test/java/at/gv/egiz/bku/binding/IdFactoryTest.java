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
