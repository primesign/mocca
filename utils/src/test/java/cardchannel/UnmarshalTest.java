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
package cardchannel;

import at.buergerkarte.namespaces.cardchannel.ObjectFactory;
import at.buergerkarte.namespaces.cardchannel.ResetType;
import at.buergerkarte.namespaces.cardchannel.ScriptType;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class UnmarshalTest {

  @Test
  public void unmarshalScript() throws FileNotFoundException, JAXBException {
    JAXBContext ctx = JAXBContext.newInstance(ObjectFactory.class);
    Unmarshaller um = ctx.createUnmarshaller();

    JAXBElement<?> script = (JAXBElement<?>) um.unmarshal(new File("src/test/cardchannel/script.xml"));

    ScriptType scriptT = (ScriptType) script.getValue();
    System.out.println("script " + scriptT.getClass());
    List<Object> resetOrCommandAPDUOrVerifyAPDU = scriptT.getResetOrCommandAPDUOrVerifyAPDU();
    for (Object object : resetOrCommandAPDUOrVerifyAPDU) {
      System.out.println("script contains: " + object.getClass());
    }
  }

  @Test
  @Ignore
  public void marshalScript() throws JAXBException {
    JAXBContext ctx = JAXBContext.newInstance(ObjectFactory.class);
    Marshaller m = ctx.createMarshaller();

    ObjectFactory of = new ObjectFactory();
    ResetType r = of.createResetType();
    ScriptType s = of.createScriptType();
    s.getResetOrCommandAPDUOrVerifyAPDU().add(r);
    JAXBElement<ScriptType> script = of.createScript(s);

    m.marshal(script, System.out);
    
  }
}
