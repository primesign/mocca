/*
 * Copyright 2015 Datentechnik Innovation GmbH and Prime Sign GmbH, Austria
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



package at.gv.egiz.bku.slcommands.impl.cms;

import iaik.asn1.CodingException;
import iaik.cms.CMSException;
import iaik.cms.CMSSignatureException;
import iaik.cms.ContentInfo;
import iaik.cms.SecurityProvider;
import iaik.cms.SignedData;
import iaik.cms.SignerInfo;
import iaik.x509.X509ExtensionException;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

import at.buergerkarte.namespaces.securitylayer._1_2_3.CMSDataObjectOptionalMetaType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.ExcludedByteRangeType;
import at.gv.egiz.bku.slexceptions.SLCommandException;
import at.gv.egiz.bku.utils.urldereferencer.URLDereferencer;
import at.gv.egiz.stal.STAL;

/**
 * This class represents a CMS-Signature as to be created by the
 * security layer command <code>BulkSignatureRequest</code>.
 * 
 * @author szoescher
 */
public class BulkSignature extends Signature {

  public final static String ID_AA_ETS_MIMETYPE = "0.4.0.1733.2.1";

  public BulkSignature(CMSDataObjectOptionalMetaType dataObject, String structure,
      X509Certificate signingCertificate, Date signingTime, URLDereferencer urlDereferencer,
      boolean useStrongHash)
          throws NoSuchAlgorithmException, CertificateEncodingException,
          CertificateException, X509ExtensionException, InvalidParameterException,
          CodingException, SLCommandException, IOException, CMSException {
		super(dataObject, structure, signingCertificate, signingTime, urlDereferencer, useStrongHash);
  }

  /**
   * Additionally to the <code>sign()<code> method from the supertype, 
   * contains a additional parameter to set a custom securityProvider.
   * @param securityProvider The Security Provider that handles the sign request.
   */
  public byte[] sign(SecurityProvider securityProvider, STAL stal, String keyboxIdentifier) throws CMSException, CMSSignatureException, SLCommandException {
	    signedData.setSecurityProvider(securityProvider);
	    setSignerInfo();
	    if (digestValue != null) {
	      try {
	        signedData.setMessageDigest(digestAlgorithm, digestValue);
	      } catch (NoSuchAlgorithmException e) {
	        throw new CMSSignatureException(e);
	      }
	    }
	    ContentInfo contentInfo = new ContentInfo(signedData);
	    return contentInfo.getEncoded();
	  }
  
  
  public ExcludedByteRangeType getExcludedByteRange() {
		return excludedByteRange;
	}
  
  public SignerInfo getSignerInfo() {
		return signerInfo;
	}

	public void setSignerInfo(SignerInfo signerInfo) {
		this.signerInfo = signerInfo;
	}
	
	
	public SignedData getSignedData()  {
	  return signedData;
	}
	
	public byte[] getEncoded() throws CMSException{
	    ContentInfo contentInfo = new ContentInfo(signedData);
	    return contentInfo.getEncoded();
	}
  
	
}

