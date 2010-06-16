/*
* Copyright 2009 Federal Chancellery Austria and
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

package at.gv.egiz.smcc.test.spring;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class ByteArrayPropertyEditorTest {

  public byte[] bytes = { (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03,
      (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08,
      (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d,
      (byte) 0x0e, (byte) 0x0f, (byte) 0xf0, (byte) 0xe0, (byte) 0xd0,
      (byte) 0xc0, (byte) 0xb0, (byte) 0xa0, (byte) 0x90, (byte) 0x80,
      (byte) 0x70, (byte) 0x60, (byte) 0x50, (byte) 0x40, (byte) 0x30,
      (byte) 0x20, (byte) 0x10, (byte) 0x00 };
  
  @Test
  public void testByteArrayPropertyEditor() {
    
    ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(
        "at/gv/egiz/smcc/spring/PropertyEditorTest.xml");
    
    ByteArrayPropertyDependable testBean = (ByteArrayPropertyDependable) applicationContext
        .getBean("testBean", ByteArrayPropertyDependable.class);
    assertNotNull(testBean);
    
    assertArrayEquals(bytes, testBean.getBytes());
    
    System.out.println("" + byte[].class);
    
  }
  
  
}
