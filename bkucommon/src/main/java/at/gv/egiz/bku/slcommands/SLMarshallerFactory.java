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


package at.gv.egiz.bku.slcommands;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.bku.slexceptions.SLRuntimeException;
import at.gv.egiz.marshal.MarshallerFactory;

public class SLMarshallerFactory {
  
  private final Logger log = LoggerFactory.getLogger(SLMarshallerFactory.class);

  /**
   * The JAXBContext used for result marshaling.
   * <p>
   * Note: Different contexts are used for marshaling and unmarshaling of
   * security layer requests and responses to avoid propagation of namespace
   * declarations of legacy namespaces into marshaled results.
   * </p>
   * @see #jaxbContextLegacy
   */
  protected static JAXBContext context;

  /**
   * The JAXBContext used for marshaling of of results in the legacy namespace.
   */
  protected static JAXBContext legacyContext;

  // ------------------- initialization on demand idiom -------------------
  // see http://en.wikipedia.org/wiki/Initialization_on_demand_holder_idiom
  // ----------------------------------------------------------------------
  
  /**
   * Private constructor called by {@link SLMarshallerFactoryInstanceHolder}.
   */
  private SLMarshallerFactory() {
    // context is initialized immediately while the legacy context is initialized only on demand
    try {
      String slPkg = at.buergerkarte.namespaces.securitylayer._1_2_3.ObjectFactory.class.getPackage().getName();
      String xmldsigPkg = org.w3._2000._09.xmldsig_.ObjectFactory.class.getPackage().getName();
      String cardChannelPkg = at.buergerkarte.namespaces.cardchannel.ObjectFactory.class.getPackage().getName();
      context = JAXBContext.newInstance(slPkg + ":" + xmldsigPkg + ":" + cardChannelPkg);
    } catch (JAXBException e) {
      log.error("Failed to setup JAXBContext security layer request.", e);
      throw new SLRuntimeException(e);
    }
  }
 
  /**
   * The lazy instance holder for this SLMarshallerFactory.
   */
  private static class SLMarshallerFactoryInstanceHolder {
    /**
     * The instance returned by {@link SLMarshallerFactory#getInstance()}
     */
    private static final SLMarshallerFactory instance = new SLMarshallerFactory();
  }

  /**
   * Get an instance of the <code>SLMarshallerFactory</code>. 
   */
  public static SLMarshallerFactory getInstance() {
    return SLMarshallerFactoryInstanceHolder.instance;
  }

  // ----------------------------------------------------------------------
  
  /**
   * Initialize the JAXBContext for the legacy namespace.
   */
  private static synchronized void ensureLegacyContext() {
    // legacy marshaller is initialized only on demand
    if (legacyContext == null) {
      try {
        String slPkgLegacy1_0 = at.buergerkarte.namespaces.securitylayer._20020225_.ObjectFactory.class.getPackage().getName();
        String slPkgLegacy1_1 = at.buergerkarte.namespaces.securitylayer._20020831_.ObjectFactory.class.getPackage().getName();
        String xmldsigPkg = org.w3._2000._09.xmldsig_.ObjectFactory.class.getPackage().getName();
        String cardChannelPkg = at.buergerkarte.namespaces.cardchannel.ObjectFactory.class.getPackage().getName();
        legacyContext = JAXBContext.newInstance(slPkgLegacy1_0 + ":" + slPkgLegacy1_1 + ":" + xmldsigPkg + ":" + cardChannelPkg);
      } catch (JAXBException e) {
        Logger log = LoggerFactory.getLogger(SLMarshallerFactory.class);
        log.error("Failed to setup JAXBContext security layer request.", e);
        throw new SLRuntimeException(e);
      }
    }
  }

  /**
   * Creates an SL marshaller.
   * 
   * @param formattedOutput
   *          <code>true</code> if the marshaller should produce formated
   *          output, <code>false</code> otherwise
   * @return an SL marshaller
   */
  public Marshaller createMarshaller(boolean formattedOutput) {
    return createMarshaller(formattedOutput, false);
  }

  /**
   * Creates an SL marshaller.
   * 
   * @param formattedOutput
   *          <code>true</code> if the marshaller should produce formated
   *          output, <code>false</code> otherwise
   * @param fragment
   *          <code>true</code> if the marshaller should produce a XML fragment
   *          (omit XML declaration), <code>false</code> otherwise
   * @return an SL marshaller
   */
  public Marshaller createMarshaller(boolean formattedOutput, boolean fragment) {
    try {
      return MarshallerFactory.createMarshaller(context, formattedOutput, fragment);
    } catch (JAXBException e) {
      log.error("Failed to marshall error response.", e);
      throw new SLRuntimeException("Failed to marshall error response.", e);
    }
  }

  /**
   * Creates a legacy SL marshaller.
   * 
   * @param formattedOutput
   *          <code>true</code> if the marshaller should produce formated
   *          output, <code>false</code> otherwise
   * @return a legacy SL marshaller
   */
  public Marshaller createLegacyMarshaller(boolean formattedOutput) {
    return createLegacyMarshaller(formattedOutput, false);
  }
  
  /**
   * Creates a legacy SL marshaller.
   * 
   * @param formattedOutput
   *          <code>true</code> if the marshaller should produce formated
   *          output, <code>false</code> otherwise
   * @param fragment
   *          <code>true</code> if the marshaller should produce a XML fragment
   *          (omit XML declaration), <code>false</code> otherwise
   * @return a legacy SL marshaller
   */
  public Marshaller createLegacyMarshaller(boolean formattedOutput, boolean fragment) {
    try {
      ensureLegacyContext();
      return MarshallerFactory.createMarshaller(legacyContext, formattedOutput, fragment);
    } catch (JAXBException e) {
      log.error("Failed to marshall error response.", e);
      throw new SLRuntimeException("Failed to marshall error response.", e);
    }
  }
  
}
