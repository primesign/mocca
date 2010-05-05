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

package at.gv.egiz.slbinding;

import java.io.InputStream;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test; 

import at.gv.egiz.slbinding.impl.CreateXMLSignatureResponseType;
import static org.junit.Assert.*;

public class UnmarshallCXSRTest {

  @Test
  public void testUnmarshallCreateXMLSignatureResponse() throws XMLStreamException, JAXBException {
    
    ClassLoader cl = UnmarshallCXSRTest.class.getClassLoader();
    InputStream s = cl.getResourceAsStream("at/gv/egiz/slbinding/CreateXMLSignatureResponse.xml");
    
    assertNotNull(s);
    
    SLUnmarshaller unmarshaller = new SLUnmarshaller();
    Object object = unmarshaller.unmarshal(new StreamSource(s));

    assertTrue(object.getClass().getName(), object instanceof JAXBElement<?>);

    Object value = ((JAXBElement<?>) object).getValue();
    
    assertTrue(value.getClass().getName(), value instanceof CreateXMLSignatureResponseType);
    
  }
  
}
