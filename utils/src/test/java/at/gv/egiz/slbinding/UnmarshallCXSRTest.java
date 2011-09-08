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



package at.gv.egiz.slbinding;

import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test; 

import at.gv.egiz.slbinding.impl.CreateXMLSignatureResponseType;
import static org.junit.Assert.*;

public class UnmarshallCXSRTest {

  @Test
  public void testUnmarshalCreateXMLSignatureResponse() throws XMLStreamException, JAXBException {
    
    ClassLoader cl = UnmarshallCXSRTest.class.getClassLoader();
    InputStream s = cl.getResourceAsStream("at/gv/egiz/slbinding/CreateXMLSignatureResponse.xml");
    
    assertNotNull(s);
    
    SLUnmarshaller unmarshaller = new SLUnmarshaller();
    Object object = unmarshaller.unmarshal(new StreamSource(new InputStreamReader(s)));

    assertTrue(object.getClass().getName(), object instanceof JAXBElement<?>);

    Object value = ((JAXBElement<?>) object).getValue();
    
    assertTrue(value.getClass().getName(), value instanceof CreateXMLSignatureResponseType);
    
  }
  
}
