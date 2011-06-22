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
