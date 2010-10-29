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

public abstract class AbstractCardTestBase {
  
  public static byte[] MOCCA = { 'M', 'O', 'C', 'C', 'A' };

  protected ApplicationContext applicationContext;
  
  protected Card card;
  
  protected SignatureCard signatureCard;
  
  @BeforeClass
  public static void setupClass() {
//    IAIK.addAsJDK14Provider();
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
