package at.gv.egiz.bku.slcommands.impl.cms;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Date;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3._2000._09.xmldsig_.DigestMethodType;

import at.buergerkarte.namespaces.securitylayer._1_2_3.Base64OptRefContentType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.CMSDataObjectRequiredMetaType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.DigestAndRefType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.MetaInfoType;
import at.gv.egiz.stal.dummy.DummySTAL;
import iaik.asn1.ObjectID;
import iaik.asn1.structures.AlgorithmID;
import iaik.cms.InvalidSignatureValueException;
import iaik.cms.SignedData;
import iaik.cms.SignerInfo;
import iaik.security.ecc.provider.ECCProvider;
import iaik.security.provider.IAIK;
import iaik.x509.X509Certificate;

public class SignatureTest {

  private DummySTAL stal = new DummySTAL();
  
  @BeforeClass
  public static void setUpClass() {
    IAIK.addAsProvider();
    ECCProvider.addAsProvider();
  }

  @Test
  public void testSignCMSDataObject() throws Exception {
    
    byte[] plaintext = "Plaintext".getBytes(Charset.forName("UTF-8"));
    
    CMSDataObjectRequiredMetaType dataObject = new CMSDataObjectRequiredMetaType();
    Base64OptRefContentType base64OptRefContentType = new Base64OptRefContentType();
    base64OptRefContentType.setBase64Content(plaintext);
    dataObject.setContent(base64OptRefContentType);
    MetaInfoType metaInfoType = new MetaInfoType();
    metaInfoType.setMimeType("text/plain");
    dataObject.setMetaInfo(metaInfoType);

    Signature signature = new Signature(dataObject, "detached", stal.getCert(), new Date(), null, true);
    byte[] cmsSignature = signature.sign(stal, "SecureSignatureKeypair");
    
    SignedData signedData = new SignedData(new ByteArrayInputStream(cmsSignature));
    signedData.setContent(plaintext);
    assertEquals(ObjectID.pkcs7_data, signedData.getEncapsulatedContentType());
    SignerInfo[] signerInfos = signedData.getSignerInfos();
    assertEquals(1, signerInfos.length);
    SignerInfo signerInfo = signerInfos[0];
    signedData.verify((X509Certificate) stal.getCert());
    assertEquals(AlgorithmID.sha1, signerInfo.getDigestAlgorithm());
    assertEquals(AlgorithmID.sha1WithRSAEncryption, signerInfo.getSignatureAlgorithm());
    
    System.out.println(AlgorithmID.sha1);
    
  }

  @Test
  public void testSignCMSReferenceSha1() throws Exception {
    testSignCMSReference(AlgorithmID.sha1);
  }

  //TODO Why doesn't it work this way??
  @Test(expected = InvalidSignatureValueException.class)
  public void testSignCMSReferenceSha256() throws Exception {
    testSignCMSReference(AlgorithmID.sha256);
  }
  
  private void testSignCMSReference(AlgorithmID digestAlgorithmID) throws Exception {
    
    byte[] plaintext = "Plaintext".getBytes(Charset.forName("UTF-8"));
    
    MessageDigest messageDigest = MessageDigest.getInstance(digestAlgorithmID.getImplementationName());
    byte[] digestValue = messageDigest.digest(plaintext);
    
    CMSDataObjectRequiredMetaType dataObject = new CMSDataObjectRequiredMetaType();
    DigestAndRefType digestAndRefType = new DigestAndRefType();
    DigestMethodType digestMethodType = new DigestMethodType();
    digestMethodType.setAlgorithm("URN:OID:" + digestAlgorithmID.getAlgorithm().getID());
    digestAndRefType.setDigestMethod(digestMethodType);
    digestAndRefType.setDigestValue(digestValue);
    dataObject.setDigestAndRef(digestAndRefType);
    MetaInfoType metaInfoType = new MetaInfoType();
    metaInfoType.setMimeType("text/plain");
    dataObject.setMetaInfo(metaInfoType);

    Signature signature = new Signature(dataObject, "detached", stal.getCert(), new Date(), null, true);
    byte[] cmsSignature = signature.sign(stal, "SecureSignatureKeypair");
    
    SignedData signedData = new SignedData(new ByteArrayInputStream(cmsSignature));
    signedData.setContent(plaintext);
    assertEquals(ObjectID.pkcs7_data, signedData.getEncapsulatedContentType());
    SignerInfo[] signerInfos = signedData.getSignerInfos();
    assertEquals(1, signerInfos.length);
    SignerInfo signerInfo = signerInfos[0];
    signedData.verify((X509Certificate) stal.getCert());
    assertEquals(digestAlgorithmID, signerInfo.getDigestAlgorithm());
    assertEquals(AlgorithmID.sha1WithRSAEncryption, signerInfo.getSignatureAlgorithm());
    
  }

  @Test
  public void test() throws URISyntaxException {
    
    String oid = null;
    URI uri = new URI("URN:OID:1.3.14.3.2.26");
    String scheme = uri.getScheme();
    if ("URN".equalsIgnoreCase(scheme)) {
      String schemeSpecificPart = uri.getSchemeSpecificPart().toLowerCase();
      if (schemeSpecificPart.startsWith("oid:")) {
        oid = schemeSpecificPart.substring(4, schemeSpecificPart.length());
      }
    }
    assertEquals("1.3.14.3.2.26", oid);
    
  }
  
}
