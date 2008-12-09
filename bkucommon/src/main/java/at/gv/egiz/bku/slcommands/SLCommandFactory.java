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
package at.gv.egiz.bku.slcommands;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import at.buergerkarte.namespaces.cardchannel.ObjectFactory;
import at.gv.egiz.bku.slexceptions.SLCommandException;
import at.gv.egiz.bku.slexceptions.SLExceptionMessages;
import at.gv.egiz.bku.slexceptions.SLRequestException;
import at.gv.egiz.bku.slexceptions.SLRuntimeException;
import at.gv.egiz.bku.utils.DebugReader;
import at.gv.egiz.slbinding.RedirectEventFilter;
import at.gv.egiz.slbinding.RedirectUnmarshallerListener;

public class SLCommandFactory {

    /**
     * Schema files required for Security Layer command validation.
     */
    public static final String[] SCHEMA_FILES = new String[]{
        "at/gv/egiz/bku/slcommands/schema/xml.xsd",
        "at/gv/egiz/bku/slcommands/schema/xmldsig-core-schema.xsd",
        "at/gv/egiz/bku/slcommands/schema/Core-1.2.xsd"
    };
    /**
     * Logging facility.
     */
    static Log log = LogFactory.getLog(SLCommandFactory.class);
    /**
     * The instance returned by {@link #getInstance()}.
     */
    private static SLCommandFactory instance;
    /**
     * Schema for Security Layer command validation.
     */
    private Schema slSchema;
    /**
     * The JAXBContext.
     */
    private JAXBContext jaxbContext;
    /**
     * The map of <namespaceURI>:<localName> to implementation class of the
     * corresponding {@link SLCommand}.
     */
    private Map<String, Class<? extends SLCommand>> slRequestTypeMap = new HashMap<String, Class<? extends SLCommand>>();
    
    /**
     * Configures the singleton instance with command implementations
     * @param commandImplMap
     * @throws ClassNotFoundException 
     */
    @SuppressWarnings("unchecked")
    public void setCommandImpl(Map<String, String> commandImplMap) throws ClassNotFoundException {
      ClassLoader cl = getClass().getClassLoader();
      for (String key : commandImplMap.keySet()) {
        Class<? extends SLCommand> impl =  (Class<? extends SLCommand>) cl.loadClass(commandImplMap.get(key));
        log.debug("Registering sl command implementation for :"+key+ "; implementation class: "+impl.getCanonicalName());
        slRequestTypeMap.put(key, impl);
      }
    }

    /**
     * Register an {@link SLCommand} implementation class of a Security Layer
     * command with the given <code>namespaceUri</code> and <code>localname</code>
     * .
     * 
     * @param namespaceUri
     *          the namespace URI of the Security Layer command
     * @param localname
     *          the localname of the Security Layer command
     * @param slCommandClass
     *          the implementation class, or <code>null</code> to deregister a
     *          currently registered class
     */
    public  void setImplClass(String namespaceUri, String localname,
      Class<? extends SLCommand> slCommandClass) {
        if (slCommandClass != null) {
            slRequestTypeMap.put(namespaceUri + ":" + localname, slCommandClass);
        } else {
            slRequestTypeMap.remove(namespaceUri + ":" + localname);
        }
    }

    /**
     * Returns the implementation class of an {@link SLCommand} with the given
     * <code>name</code>, or <code>null</code> if no such class is registered.
     * 
     * @param name
     *          the <code>QName</code> of the Security Layer command
     * @return the implementation class, or <code>null</code> if no class is
     *         registered for the given <code>name</code>
     */
    public Class<? extends SLCommand> getImplClass(QName name) {
        String namespaceURI = name.getNamespaceURI();
        String localPart = name.getLocalPart();
        return slRequestTypeMap.get(namespaceURI + ":" + localPart);
    }

    /**
     * Sets the schema to validate Security Layer commands with.
     * 
     * @param slSchema the schema to validate Security Layer commands with
     */
    public void setSLSchema(Schema slSchema) {
        this.slSchema = slSchema;
    }

    /**
     * @return the jaxbContext
     */
    public JAXBContext getJaxbContext() {
        ensureJaxbContext();
        return jaxbContext;
    }

    /**
     * @param jaxbContext the jaxbContext to set
     */
    public  void setJaxbContext(JAXBContext jaxbContext) {
        this.jaxbContext = jaxbContext;
    }

    /**
     * Initialize the JAXBContext.
     */
    private synchronized void ensureJaxbContext() {
        if (jaxbContext == null) {
            try {
                String slPkg = at.buergerkarte.namespaces.securitylayer._1.ObjectFactory.class.getPackage().getName();
                String xmldsigPkg = org.w3._2000._09.xmldsig_.ObjectFactory.class.getPackage().getName();
                String cardChannelPkg = at.buergerkarte.namespaces.cardchannel.ObjectFactory.class.getPackage().getName();
                setJaxbContext(JAXBContext.newInstance(slPkg + ":" + xmldsigPkg + ":" + cardChannelPkg));
            } catch (JAXBException e) {
                log.error("Failed to setup JAXBContext security layer request.", e);
                throw new SLRuntimeException(e);
            }
        }
    }

    /**
     * Initialize the security layer schema.
     */
    private synchronized void ensureSchema() {
        if (slSchema == null) {
            try {
                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                ClassLoader cl = SLCommandFactory.class.getClassLoader();
                Source[] sources = new Source[SCHEMA_FILES.length];
                for (int i = 0; i < SCHEMA_FILES.length; i++) {
                    String schemaFile = SCHEMA_FILES[i];
                    URL schemaURL = cl.getResource(schemaFile);
                    if (schemaURL == null) {
                        throw new SLRuntimeException("Failed to load schema file " + schemaFile + ".");
                    }
                    log.debug("Schema location: " + schemaURL);
                    sources[i] = new StreamSource(schemaURL.openStream());
                }
                Schema schema = schemaFactory.newSchema(sources);
                log.debug("Schema successfully created.");
                setSLSchema(schema);
            } catch (SAXException e) {
                log.error("Failed to load security layer schema.", e);
                throw new SLRuntimeException("Failed to load security layer schema.", e);
            } catch (IOException e) {
                log.error("Failed to load security layer schema.", e);
                throw new SLRuntimeException("Failed to load security layer schema.", e);
            }

        }
    }

    /**
     * Get an instance of the <code>SLCommandFactory</code>.
     */
    public synchronized static SLCommandFactory getInstance() {
        if (instance == null) {
          instance = new SLCommandFactory();
          instance.ensureJaxbContext();
          instance.ensureSchema();
        }
        return instance;
    }

    /**
     * Private constructor used by {@link #getInstance()}.
     */
    private SLCommandFactory() {
    }

    /**
     * Unmarshalls from the given <code>source</code>.
     * 
     * @see Unmarshaller#unmarshal(Source)
     * 
     * <em>Note:</em>Could replace JAXB's unmarshal-time validation engine (see commented code), however,
     * we need a redirect filter.
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
    protected Object unmarshal(Source source) throws SLRuntimeException,
      SLRequestException {

        Object object;
        try {
            
//            ValidatorHandler validator = slSchema.newValidatorHandler();
//            validator.getContentHandler();
//            
//            SAXParserFactory spf = SAXParserFactory.newInstance();
//            spf.setNamespaceAware(true);
//            XMLReader saxReader = spf.newSAXParser().getXMLReader();
//            //TODO extend validator to implement redirectContentHandler (validate+redirect)
//            saxReader.setContentHandler(validator);
//            //TODO get a InputSource
//            SAXSource saxSource = new SAXSource(saxReader, source);
//            
//            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
//            //turn off duplicate jaxb validation 
//            unmarshaller.setSchema(null);
//            unmarshaller.setListener(listener);
//            unmarshaller.unmarshal(saxSource);
            

            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLEventReader eventReader = inputFactory.createXMLEventReader(source);
            RedirectEventFilter redirectEventFilter = new RedirectEventFilter();
            XMLEventReader filteredReader = inputFactory.createFilteredReader(eventReader, redirectEventFilter);

            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setListener(new RedirectUnmarshallerListener(redirectEventFilter));
            if (slSchema != null) {
                unmarshaller.setSchema(slSchema);
            }
            log.trace("Before unmarshal().");
            object = unmarshaller.unmarshal(filteredReader);
            log.trace("After unmarshal().");
        } catch (UnmarshalException e) {
            if (log.isDebugEnabled()) {
                log.debug("Failed to unmarshall security layer request.", e);
            } else {
                log.info("Failed to unmarshall security layer request." + e.getMessage());
            }
            Throwable cause = e.getCause();
            if (cause instanceof SAXParseException) {
                throw new SLRequestException(3000,
                  SLExceptionMessages.EC3000_UNCLASSIFIED, new Object[]{cause.getMessage()});
            } else {
                throw new SLRequestException(3000,
                  SLExceptionMessages.EC3000_UNCLASSIFIED, new Object[]{e});
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

        return object;

    }

    /**
     * Creates a new <code>SLCommand</code> from the given <code>source</code> and
     * <code>context</code>.
     * 
     * @param source
     *          the <code>Source</code> to unmarshall from
     * @param context
     *          the context for the created <code>SLCommand</code>
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
     */
    @SuppressWarnings("unchecked")
    public SLCommand createSLCommand(Source source, SLCommandContext context)
      throws SLCommandException, SLRuntimeException, SLRequestException {
      
        DebugReader dr = null;
        if (log.isTraceEnabled() && source instanceof StreamSource) {
          StreamSource streamSource = (StreamSource) source;
          if (streamSource.getReader() != null) {
            dr = new DebugReader(streamSource.getReader(), "SLCommand unmarshalled from:\n");
            streamSource.setReader(dr);
          }
        }

        Object object;
        try {
          object = unmarshal(source);
        } catch (SLRequestException e) {
          throw e;
        } finally {
          if (dr != null) {
            log.trace(dr.getCachedString());
          }
        }
        
        if (!(object instanceof JAXBElement)) {
            // invalid request
            log.info("Invalid security layer request. " + object.toString());
            throw new SLRequestException(3002, SLExceptionMessages.EC3002_INVALID,
              new Object[]{object.toString()});
        }

        QName qName = ((JAXBElement) object).getName();
        Class<? extends SLCommand> implClass = getImplClass(qName);
        if (implClass == null) {
            // command not supported
            log.info("Unsupported command received: " + qName.toString());
            throw new SLCommandException(4011,
              SLExceptionMessages.EC4011_NOTIMPLEMENTED, new Object[]{qName.toString()});
        }

        
        
        // try to instantiate
        SLCommand slCommand;
        try {
            slCommand = implClass.newInstance();
            log.debug("SLCommand " + slCommand.getName() + " created.");
        } catch (InstantiationException e) {
            // unexpected error
            log.error("Failed to instantiate security layer command implementation.",
              e);
            throw new SLRuntimeException(e);
        } catch (IllegalAccessException e) {
            // unexpected error
            log.error("Failed to instantiate security layer command implementation.",
              e);
            throw new SLRuntimeException(e);
        }

        slCommand.init(context, (JAXBElement) object);

        return slCommand;

    }
}