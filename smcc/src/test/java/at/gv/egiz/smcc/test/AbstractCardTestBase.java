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



package at.gv.egiz.smcc.test;

import static org.junit.Assert.assertNotNull;
//import iaik.security.provider.IAIK;

import javax.smartcardio.Card;

import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import at.gv.egiz.smcc.CardNotSupportedException;
import at.gv.egiz.smcc.CardTerminalEmul;
import at.gv.egiz.smcc.SignatureCard;
import at.gv.egiz.smcc.SignatureCardFactory;
import iaik.security.provider.IAIK;

public abstract class AbstractCardTestBase {
  
  public static byte[] MOCCA = { 'M', 'O', 'C', 'C', 'A' };

  protected ApplicationContext applicationContext;
  
  protected Card card;
  
  protected SignatureCard signatureCard;
  
  @BeforeClass
  public static void setupClass() {
    IAIK.addAsJDK14Provider();
  }
  
  @Before
  public void setup() throws CardNotSupportedException {
    applicationContext = new ClassPathXmlApplicationContext(getClass().getSimpleName() + ".xml", getClass());

    card = (Card) applicationContext.getBean("card", Card.class);
    assertNotNull(card);

    SignatureCardFactory factory = SignatureCardFactory.getInstance();
    signatureCard = factory.createSignatureCard(card, new CardTerminalEmul(card));
    assertNotNull(signatureCard);
  }
  
}
