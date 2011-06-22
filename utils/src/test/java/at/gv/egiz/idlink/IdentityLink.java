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


package at.gv.egiz.idlink;

import iaik.xml.crypto.XSecProvider;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import oasis.names.tc.saml._1_0.assertion.AssertionType;
import oasis.names.tc.saml._1_0.assertion.AttributeStatementType;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;

import at.gv.egiz.xmldsig.KeyTypeNotSupportedException;

public class IdentityLink {
  
  private static String keyStoreType = "PKCS12";
  
  private static String keyStoreFile = "at/gv/egiz/idlink/IdentityLinkTest.p12";
  
  private static String keyStorePassword = "mocca";
  
  private static String[] certificateFiles = new String [] {
    "at/gv/egiz/idlink/certified.cer",
    "at/gv/egiz/idlink/secure.cer"
  };
  
  private static PublicKey[] publicKeys;
  
  private static X509Certificate signerCert;
  
  private static PrivateKey signerKey;

  @BeforeClass 
  public static void setupClass() throws NoSuchAlgorithmException, IOException,
      InvalidKeySpecException, KeyStoreException, CertificateException,
      UnrecoverableKeyException {
    
    XSecProvider.addAsProvider(false);
    
    ClassLoader classLoader = IdentityLink.class.getClassLoader();

    CertificateFactory certificateFactory = CertificateFactory.getInstance("X509");
    
    List<PublicKey> keys = new ArrayList<PublicKey>();
    for (String certificateFile : certificateFiles) {
      
      InputStream certStream = classLoader.getResourceAsStream(certificateFile);
      X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(certStream);
      keys.add(cert.getPublicKey());
      
    }
    
    publicKeys = keys.toArray(new PublicKey[0]);
    
    KeyStore keyStore = KeyStore.getInstance(keyStoreType);
    keyStore.load(classLoader.getResourceAsStream(keyStoreFile), keyStorePassword.toCharArray());
    
    Enumeration<String> aliases = keyStore.aliases();
    while (aliases.hasMoreElements()) {
      String alias = (String) aliases.nextElement();
      if (keyStore.isKeyEntry(alias)) {
        signerKey = (PrivateKey) keyStore.getKey(alias, keyStorePassword.toCharArray());
        signerCert = (X509Certificate) keyStore.getCertificate(alias);
      }
    }
    

  }
  
  @Test
  public void testCreateIdentityLink() throws KeyTypeNotSupportedException, ParserConfigurationException, JAXBException, TransformerException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, XMLSignatureException, MarshalException, FileNotFoundException {
    
    IdentityLinkFactory factory = IdentityLinkFactory.getInstance();
    
    AttributeStatementType attributeStatement = factory.createAttributeStatement(
        "3utiDdA4KaodrJOeMqu9PA==", 
        "urn:publicid:gv.at:baseid", 
        "Max Moritz", 
        "Mustermann-Fall", 
        "1900-01-01",
        publicKeys
        );

    Calendar calendar = Calendar.getInstance();
    calendar.clear();
    calendar.set(2007, 8, 29, 18, 0, 0);
    
    JAXBElement<AssertionType> assertion = factory.createAssertion(
        "bka.gv.at-2007-08-29T16.41.17.442", 
        calendar.getTime(), 
        "http://www.bka.gv.at/datenschutz/Stammzahlenregisterbehoerde", 
        1L,
        0L,
        attributeStatement);
    
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    Document doc = dbf.newDocumentBuilder().newDocument();
    
    factory.marshallIdentityLink(assertion, doc, null);
    
    factory.signIdentityLink(doc.getDocumentElement(), signerCert, signerKey);
    
  }

}
