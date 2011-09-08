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

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXParseException;


import at.gv.egiz.bku.slexceptions.SLCommandException;
import at.gv.egiz.bku.slexceptions.SLExceptionMessages;
import at.gv.egiz.bku.slexceptions.SLRequestException;
import at.gv.egiz.bku.slexceptions.SLRuntimeException;
import at.gv.egiz.bku.slexceptions.SLVersionException;
import at.gv.egiz.bku.utils.DebugReader;
import at.gv.egiz.slbinding.SLUnmarshaller;

public class SLCommandFactory extends SLUnmarshaller {
  
  private final Logger log = LoggerFactory.getLogger(SLCommandFactory.class);

  private static class SLCommandFactoryInstance {
    private static final SLCommandFactory INSTANCE = new SLCommandFactory();
  }
  
  /**
   * The mapping of a requests's qualified name to a concrete command factories.
   */
  private Map<QName, AbstractSLCommandFactory> slCommandFactories = new HashMap<QName, AbstractSLCommandFactory>();

  public void setConcreteFactories(
      Map<QName, AbstractSLCommandFactory> factories) {
    if (log.isDebugEnabled()) {
      StringBuilder sb = new StringBuilder();
      sb.append("Registered sl command factory for");
      for (QName qname : factories.keySet()) {
        sb.append("\n  " + qname + " : " + factories.get(qname).getClass());
      }
      log.debug(sb.toString());
    }
    slCommandFactories = factories;
  }

  /**
   * Get an instance of the <code>SLCommandFactory</code>.
   */
  public synchronized static SLCommandFactory getInstance() {
    return SLCommandFactoryInstance.INSTANCE;
  }

  /**
   * Private constructor used by {@link #getInstance()}.
   */
  private SLCommandFactory() {
    super();
  }

  /**
   * Creates a new <code>SLCommand</code> from the given <code>source</code> and
   * <code>context</code>.
   * 
   * @param source
   *          the <code>StreamSource</code> to unmarshal from
   *          Note that the StreamSource _must_ contain a Reader set with
   *          setReader()
   * @return the <code>SLCommand</code> unmarshalled from the given
   *         <code>source</code>
   * @throws SLRequestException
   *           if unmarshalling fails
   * @throws SLCommandException
   *           if command ist not supported
   * @throws SLRuntimeException
   *           if an unexpected error occurs configuring the unmarshaller, if
   *           unmarshalling fails with an unexpected error or if the
   *           corresponding <code>SLCommand</code> could not be instantiated
   * @throws SLVersionException
   */
  public SLCommand createSLCommand(StreamSource source)
      throws SLCommandException, SLRuntimeException, SLRequestException,
      SLVersionException {

    DebugReader dr = null;
    if (log.isTraceEnabled() && source instanceof StreamSource) {
      if (source.getReader() != null) {
        dr = new DebugReader(source.getReader(),
            "SLCommand unmarshalled from:\n");
        source.setReader(dr);
      }
    }

    Object object;
    try {
      object = unmarshalRequest(source);
    } catch (SLRequestException e) {
      throw e;
    } finally {
      if (dr != null) {
        log.trace(dr.getCachedString());
      }
    }

    if (!(object instanceof JAXBElement<?>)) {
      // invalid request
      log.info("Invalid security layer request.\n{}", object.toString());
      throw new SLRequestException(3002, SLExceptionMessages.EC3002_INVALID,
          new Object[] { object.toString() });
    }

    return createSLCommand((JAXBElement<?>) object);

  }

  /**
   * Creates a new <code>SLCommand</code> from the given <code>element</code>
   * and <code>context</code>.
   * 
   * @param element
   *          the request element
   * @return the <code>SLCommand</code> for for the given <code>element</code>
   * @throws SLCommandException
   *           if command ist not supported
   * @throws SLVersionException
   */
  public SLCommand createSLCommand(JAXBElement<?> element) throws SLCommandException, SLVersionException {
    
    QName qName = element.getName();
    if (SLCommand.NAMESPACE_URI_20020831.equals(qName.getNamespaceURI())
        || SLCommand.NAMESPACE_URI_20020225.equals(qName.getNamespaceURI())) {
      // security layer request version not supported
      log.info("Unsupported security layer request version {}.", qName.getNamespaceURI());
      throw new SLVersionException(qName.getNamespaceURI());
    }

    AbstractSLCommandFactory concreteFactory = slCommandFactories.get(qName);
    if (concreteFactory == null) {
      // command not supported
      log.info("Unsupported command received {}.", qName.toString());
      throw new SLCommandException(4011,
          SLExceptionMessages.EC4011_NOTIMPLEMENTED, new Object[] { qName
              .toString() });
    }

    return concreteFactory.createSLCommand(element);
    
  }

  /**
   * Unmarshalls from the given <code>source</code>.
   * 
   * @see Unmarshaller#unmarshal(Source)
   * 
   *      <em>Note:</em>Could replace JAXB's unmarshal-time validation engine
   *      (see commented code), however, we need a redirect filter.
   * 
   * @param source
   *          the source to unmarshal from
   * @return the object returned by {@link Unmarshaller#unmarshal(Source)}
   * @throws SLRequestException
   *           if unmarshalling fails
   * @throws SLRuntimeException
   *           if an unexpected error occurs configuring the unmarshaller or if
   *           unmarshalling fails with an unexpected error
   */
  protected Object unmarshalRequest(StreamSource source) throws SLRuntimeException,
      SLRequestException {

    try {
      return unmarshal(source);
    } catch (UnmarshalException e) {
      if (log.isDebugEnabled()) {
        log.debug("Failed to unmarshall security layer request.", e);
      } else {
        log.info("Failed to unmarshall security layer request."
            + e.getMessage());
      }

      if (e.getLinkedException() != null) {
        throw new SLRequestException(3002, SLExceptionMessages.EC3002_INVALID,
            new Object[] { e.getMessage() });
      }
      Throwable cause = e.getCause();
      if (cause instanceof SAXParseException) {
        throw new SLRequestException(3000,
            SLExceptionMessages.EC3000_UNCLASSIFIED, new Object[] { cause
                .getMessage() });
      } else {
        throw new SLRequestException(3000,
            SLExceptionMessages.EC3000_UNCLASSIFIED, new Object[] { e });
      }
    } catch (JAXBException e) {
      // unexpected error
      log.error("Failed to unmarshall security layer request.", e);
      throw new SLRuntimeException(e);
    } catch (XMLStreamException e) {
      // unexpected error
      log.error("Failed to unmarshall security layer request.", e);
      throw new SLRuntimeException(e);
    }

  }
}