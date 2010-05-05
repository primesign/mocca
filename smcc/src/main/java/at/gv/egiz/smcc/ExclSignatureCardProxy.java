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
  
  public static PINMgmtSignatureCard newInstance(PINMgmtSignatureCard signatureCard) {
    return null;
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
