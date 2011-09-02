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


package moaspss;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Validator;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;

import moaspss.generated.ContentOptionalRefType;
import moaspss.generated.InputDataType;
import moaspss.generated.MOAFault;
import moaspss.generated.ObjectFactory;
import moaspss.generated.SignatureVerificationPortType;
import moaspss.generated.SignatureVerificationService;
import moaspss.generated.VerifyXMLSignatureRequestType;
import moaspss.generated.VerifyXMLSignatureResponseType;
import moaspss.generated.VerifyXMLSignatureRequestType.VerifySignatureInfo;

import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import com.sun.xml.bind.api.Bridge;
import com.sun.xml.bind.api.BridgeContext;
import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.bind.api.RawAccessor;
import com.sun.xml.bind.api.TypeReference;
import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.developer.JAXBContextFactory;
import com.sun.xml.ws.developer.UsesJAXBContextFeature;

@SuppressWarnings("deprecation")
public class MOASPClient {
  
  private static class JAXBContextHolder {
    
    private static final JAXBContext context;

    static {
      try {
          context = JAXBRIContext.newInstance(VerifyXMLSignatureRequestType.class.getPackage().getName());
      } catch (JAXBException e) {
          throw new RuntimeException("Failed to setup JAXBContext.", e);
      }
    }
    
  }
  
  public static JAXBContext getJAXBContext() {
    return JAXBContextHolder.context;
  }

  public static class ClientJAXBContextFactory implements JAXBContextFactory {

    public JAXBRIContext createJAXBContext(final SEIModel sei,
        @SuppressWarnings("rawtypes") final List<Class> classesToBind, final List<TypeReference> typeReferences)
            throws JAXBException {

      System.out.println("Create Context");
      
      return new JAXBRIContext() {
        
        JAXBRIContext context = JAXBRIContext.newInstance(classesToBind.toArray
            (new Class[classesToBind.size()]),
            typeReferences, null, sei.getTargetNamespace(), false, null);
        
        @Override
        public Validator createValidator() throws JAXBException {
          return context.createValidator();
        }
        
        @Override
        public Unmarshaller createUnmarshaller() throws JAXBException {
          return context.createUnmarshaller();
        }
        
        @Override
        public Marshaller createMarshaller() throws JAXBException {
          Marshaller marshaller = context.createMarshaller();
          ClientNamespacePrefixMapper pm = new ClientNamespacePrefixMapper();
          System.out.println(pm.toString());
          marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", pm);
          return marshaller;
        }
        
        @Override
        public boolean hasSwaRef() {
          return context.hasSwaRef();
        }
        
        @Override
        public QName getTypeName(TypeReference arg0) {
          return context.getTypeName(arg0);
        }
        
        @Override
        public List<String> getKnownNamespaceURIs() {
          return context.getKnownNamespaceURIs();
        }
        
        @Override
        public <B, V> RawAccessor<B, V> getElementPropertyAccessor(Class<B> arg0,
            String arg1, String arg2) throws JAXBException {
          return context.getElementPropertyAccessor(arg0, arg1, arg2);
        }
        
        @Override
        public QName getElementName(Object arg0) throws JAXBException {
          return context.getElementName(arg0);
        }
        
        @Override
        public String getBuildId() {
          return context.getBuildId();
        }
        
        @Override
        public void generateSchema(SchemaOutputResolver arg0) throws IOException {
          context.generateSchema(arg0);
        }
        
        @Override
        public void generateEpisode(Result arg0) {
          context.generateEpisode(arg0);
        }
        
        @Override
        public BridgeContext createBridgeContext() {
          return context.createBridgeContext();
        }
        
        @Override
        public Bridge<?> createBridge(TypeReference arg0) {
          return context.createBridge(arg0);
        }
      };
      
    }

  }
  
  public static class ClientNamespacePrefixMapper extends NamespacePrefixMapper {

    protected static final Map<String, String> prefixMap = new HashMap<String, String>();
    
    static {
      prefixMap.put("http://www.w3.org/2001/XMLSchema-instance", "xsi");
      prefixMap.put("http://reference.e-government.gv.at/namespace/moa/20020822#", "moa");
      prefixMap.put("http://www.w3.org/2000/09/xmldsig#", "dsig");
      prefixMap.put("http://uri.etsi.org/01903/v1.1.1#", "xades");
    }
    
    
    @Override
    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {

      String prefix = prefixMap.get(namespaceUri);
      
      return (prefix != null) ? prefix : suggestion;
    }

    /**
     * Returns a list of namespace URIs that should be declared
     * at the root element.
     * <p>
     * By default, the JAXB RI produces namespace declarations only when
     * they are necessary, only at where they are used. Because of this
     * lack of look-ahead, sometimes the marshaller produces a lot of
     * namespace declarations that look redundant to human eyes. For example,
     */
    @Override
    public String[] getPreDeclaredNamespaceUris() {
      return new String[]{ "http://www.w3.org/2000/09/xmldsig#" };
    }
  }

  
  private SignatureVerificationPortType port;

  public MOASPClient() {
    QName serviceName = new QName("http://reference.e-government.gv.at/namespace/moa/wsdl/20020822#", "SignatureVerificationService");
    
    URL wsdlURL = MOASPClient.class.getClassLoader().getResource("MOA-SPSS-1.3.wsdl");
    
    SignatureVerificationService service = new SignatureVerificationService(wsdlURL, serviceName);

    UsesJAXBContextFeature feature = new UsesJAXBContextFeature(ClientJAXBContextFactory.class);
    
    port = service.getSignatureVerificationPort(feature);
  }
  
  public JAXBElement<VerifyXMLSignatureResponseType> verifySignature(Node node,
      String signatureLocation, String trustProfileId) throws JAXBException,
      IOException, ClassCastException, ClassNotFoundException,
      InstantiationException, IllegalAccessException {
    
    DOMImplementationLS domImpl = (DOMImplementationLS) DOMImplementationRegistry
        .newInstance().getDOMImplementation("LS");
    
    LSSerializer serializer = domImpl.createLSSerializer();
    
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    LSOutput output = domImpl.createLSOutput();
    output.setByteStream(bos);
    serializer.write(node, output);
    
    ObjectFactory factory = new ObjectFactory();

    ContentOptionalRefType contentOptionalRefType = factory.createContentOptionalRefType();
    contentOptionalRefType.setBase64Content(bos.toByteArray());

    VerifySignatureInfo verifySignatureInfo = factory.createVerifyXMLSignatureRequestTypeVerifySignatureInfo();
    verifySignatureInfo.setVerifySignatureEnvironment(contentOptionalRefType);
    verifySignatureInfo.setVerifySignatureLocation(signatureLocation);
    
    VerifyXMLSignatureRequestType verifyXMLSignatureRequestType = factory.createVerifyXMLSignatureRequestType();
    verifyXMLSignatureRequestType.setVerifySignatureInfo(verifySignatureInfo);
    verifyXMLSignatureRequestType.setTrustProfileID(trustProfileId);
    verifyXMLSignatureRequestType.setReturnHashInputData(Boolean.TRUE);
    
    VerifyXMLSignatureResponseType resp = null;
    try {
      resp = port.verifyXMLSignature(verifyXMLSignatureRequestType);
    } catch (MOAFault e) {
      e.printStackTrace();
    }
    
    JAXBElement<VerifyXMLSignatureResponseType> verifyXMLSignatureResponse = factory.createVerifyXMLSignatureResponse(resp);
    
    Marshaller marshaller = getJAXBContext().createMarshaller();
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    marshaller.marshal(verifyXMLSignatureResponse, System.out);
    
    List<InputDataType> hashInputData = resp.getHashInputData();
    for (InputDataType inputDataType : hashInputData) {
      System.out.println("------------------------------------------");
      System.out.println("HashInputData: " + inputDataType.getPartOf() + " " + inputDataType.getReferringSigReference());
      System.out.println("------------------------------------------");
      System.out.write(inputDataType.getBase64Content());
      System.out.println();
    }
    
    return verifyXMLSignatureResponse;
  }
}
