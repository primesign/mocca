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


package at.gv.egiz.smcc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExclSignatureCardProxy implements InvocationHandler {
  
  private final Logger log = LoggerFactory.getLogger(ExclSignatureCardProxy.class);

  private static final Method init;
  
  static {
    try {
      init = SignatureCard.class.getMethod("init", new Class<?>[] { Card.class,
          CardTerminal.class });
    } catch (SecurityException e) {
      throw new RuntimeException(e);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }
  
  private SignatureCard signatureCard;
  
  public ExclSignatureCardProxy(SignatureCard signatureCard) {
    this.signatureCard = signatureCard;
  }

  public static SignatureCard newInstance(SignatureCard signatureCard) {
    ArrayList<Class<?>> proxyInterfaces = new ArrayList<Class<?>>();
    proxyInterfaces.add(SignatureCard.class);
    if (PINMgmtSignatureCard.class.isAssignableFrom(signatureCard.getClass())) {
      proxyInterfaces.add(PINMgmtSignatureCard.class);
    }
    ClassLoader loader = signatureCard.getClass().getClassLoader();
    return (SignatureCard) Proxy.newProxyInstance(loader, proxyInterfaces
        .toArray(new Class[proxyInterfaces.size()]),
        new ExclSignatureCardProxy(signatureCard));
  }
  
  @Override
  public Object invoke(Object proxy, Method method, Object[] args)
      throws Throwable {

    Card card = null;
    
    Method target = signatureCard.getClass().getMethod(method.getName(),
        method.getParameterTypes());
    
    if (target.isAnnotationPresent(Exclusive.class)) {
      card = (Card) ((method.equals(init)) 
        ? args[0]
        : signatureCard.getCard());
    }
    
    if (card != null) {
      try {
        log.trace("Invoking method {}() with exclusive access.", method.getName());
        card.beginExclusive();
      } catch (CardException e) {
        log.info("Failed to get exclusive access to signature card {}.", signatureCard);
        throw new SignatureCardException(e);
      }
    }
      
    try {
      return method.invoke(signatureCard, args);
    } catch (InvocationTargetException e) {
      throw e.getTargetException();
    } finally {
      if (card != null) {
        card.endExclusive();
      }
    }
    
  
  }

}
