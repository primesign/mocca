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


package at.gv.egiz.bku.local.stal;

import iaik.me.security.CryptoException;
import iaik.me.security.MessageDigest;

import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.bku.gui.BKUGUIFacade;
import at.gv.egiz.bku.gui.hashdata.HashDataInputLoader;
import at.gv.egiz.bku.gui.viewer.SecureViewer;
import at.gv.egiz.bku.slcommands.impl.DataObjectHashDataInput;
import at.gv.egiz.bku.slcommands.impl.cms.ReferencedHashDataInput;
import at.gv.egiz.stal.HashDataInput;
import at.gv.egiz.stal.SignatureInfo;
import at.gv.egiz.stal.hashdata.StubHashDataInput;
import at.gv.egiz.stal.impl.ByteArrayHashDataInput;
import at.gv.egiz.stal.signedinfo.ReferenceType;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class LocalSecureViewer implements SecureViewer, HashDataInputLoader {

  private final Logger log = LoggerFactory.getLogger(LocalSecureViewer.class);
  private List<HashDataInput> hashDataInputs = Collections.emptyList();

  protected BKUGUIFacade gui;

  public LocalSecureViewer(BKUGUIFacade gui) {
    this.gui = gui;
  }

  public void setDataToBeSigned(List<HashDataInput> dataToBeSigned) {
    this.hashDataInputs = dataToBeSigned;
  }

  /**
   *
   * @param dsigReferences
   * @throws java.lang.Exception
   */
  @Override
  public void displayDataToBeSigned(SignatureInfo signedInfo,
          ActionListener okListener, String okCommand)
          throws Exception {
    
      log.info("Retrieve data to be signed for dsig:SignedInfo {}.", signedInfo.getId());
      List<HashDataInput> hdi = getHashDataInputs(signedInfo);
      List<HashDataInput> verifiedDataToBeSigned = verifyHashDataInput(signedInfo.getReference(), hdi.get(0));

    ArrayList<HashDataInput> selectedHashDataInputs = new ArrayList<HashDataInput>();
    selectedHashDataInputs.addAll(verifiedDataToBeSigned);
      
      
    if (selectedHashDataInputs.size() < 1) {
      log.error("dsig:SignedInfo does not contain a data reference.");
      throw new Exception("dsig:SignedInfo does not contain a data reference.");
    }
    gui.showSecureViewer(selectedHashDataInputs, okListener, okCommand, this);
  }


  private HashDataInput ensureCachedHashDataInput(HashDataInput hashDataInput)
          throws IOException {
    if (!(hashDataInput instanceof DataObjectHashDataInput)) {
      
      log.warn("Expected DataObjectHashDataInput for LocalSignRequestHandler, got {}.",
              hashDataInput.getClass().getName());

      InputStream hdIs = hashDataInput.getHashDataInput();
      ByteArrayOutputStream baos = new ByteArrayOutputStream(hdIs.available());
      int b;
      while ((b = hdIs.read()) != -1) {
        baos.write(b);
      }
      hashDataInput = new ByteArrayHashDataInput(baos.toByteArray(),
              hashDataInput.getReferenceId(),
              hashDataInput.getMimeType(),
              hashDataInput.getEncoding(),
              hashDataInput.getFilename());
    }
    return hashDataInput;
  }

  @Override
  public void displayDataToBeSigned(List<SignatureInfo> signedInfo, ActionListener okListener, String okCommand)
      throws DigestException, Exception {
    log.warn("Called displayDataToBeSigned");
    ArrayList<HashDataInput> selectedHashDataInputs = new ArrayList<HashDataInput>();
    

    for (SignatureInfo nextSignedInfo : signedInfo) {
      selectedHashDataInputs.addAll(addEmptyHashDataInputs(nextSignedInfo));
    }

    gui.showSecureViewer(selectedHashDataInputs, okListener, okCommand, this);

  }
  
  
  
  private Collection<? extends HashDataInput> addEmptyHashDataInputs(SignatureInfo signedInfo) throws Exception {
    if (signedInfo.getReference().size() == 0) {
      log.error("No hashdata input selected to be displayed: null.");
      throw new Exception("No HashData Input selected to be displayed.");
    }

    ArrayList<HashDataInput> selectedHashDataInputs = new ArrayList<HashDataInput>();
    for (ReferenceType dsigRef : signedInfo.getReference()) {

      if (dsigRef.getType() == null) {        
        selectedHashDataInputs.add(new StubHashDataInput(dsigRef, signedInfo.getDisplayName(), signedInfo.getMimeType()));
      }
    }
    return selectedHashDataInputs;
  }
  
  @Override
  public HashDataInput getHashDataInput(HashDataInput hashDataInput) throws Exception {

		if (hashDataInput instanceof StubHashDataInput) {
			String referenceId = hashDataInput.getReferenceId();
			byte[] digest = hashDataInput.getDigest();
			if (referenceId != null || digest != null) {
				boolean hdiAvailable = false;

				for (HashDataInput currentHashDataInput : hashDataInputs) {

					if (Arrays.equals(digest, currentHashDataInput.getDigest())) {
						log.debug("Display hashdata input for dsig:SignedReference {}.", referenceId);

						if (currentHashDataInput instanceof ReferencedHashDataInput) {

							ReferenceType reference = ((StubHashDataInput) hashDataInput).getReference();
							return verifyHashDataInput(Arrays.asList(reference), currentHashDataInput).get(0);

						} else {
							return (ensureCachedHashDataInput(currentHashDataInput));
						}

					}
				}

				if (!hdiAvailable) {
					for (HashDataInput currentHashDataInput : hashDataInputs) {
						if (referenceId.equals(hashDataInput.getReferenceId())) {
							log.debug("Display hashdata input for dsig:SignedReference {}.", referenceId);
							if (currentHashDataInput instanceof ReferencedHashDataInput) {

								ReferenceType reference = ((StubHashDataInput) hashDataInput).getReference();
								return verifyHashDataInput(Arrays.asList(reference), currentHashDataInput).get(0);

							} else {
								return (ensureCachedHashDataInput(currentHashDataInput));
							}
						}
					}
        }

        if (!hdiAvailable) {
          log.error("No hashdata input for dsig:SignedReference {}.", referenceId);
          throw new Exception("No HashDataInput available for dsig:SignedReference " + referenceId);
        }
      } else {
        throw new Exception("Cannot get HashDataInput for dsig:Reference without Id or digest attribute");
      }
    }
    return hashDataInput;
  }
  

	public List<HashDataInput> getHashDataInputs(SignatureInfo signedInfo) throws Exception {

    ArrayList<HashDataInput> selectedHashDataInputs = new ArrayList<HashDataInput>();

    if (signedInfo.getReference().size() == 0) {
      log.error("No hashdata input selected to be displayed: null.");
      throw new Exception("No HashData Input selected to be displayed.");
    }

    for (ReferenceType dsigRef : signedInfo.getReference()) {
      // don't get Manifest, QualifyingProperties, ...
      if (dsigRef.getType() == null) {
        HashDataInput emptyHashDataInput = new StubHashDataInput(dsigRef, signedInfo.getDisplayName(),
            signedInfo.getMimeType());
        

        selectedHashDataInputs.add(getHashDataInput(emptyHashDataInput));

      }
    }
    return selectedHashDataInputs;
  }
	
	 private List<HashDataInput> verifyHashDataInput(List<ReferenceType> signedReferences, HashDataInput hashDataInput)
       throws DigestException, NoSuchAlgorithmException, Exception {

 ArrayList<HashDataInput> verifiedHashDataInputs = new ArrayList<HashDataInput>();

 for (ReferenceType signedRef : signedReferences) {
   if (signedRef.getType() == null) {
     log.info("Verifying digest for signed reference {}.", signedRef.getId());

     String signedRefId = signedRef.getId();
     byte[] signedDigest = signedRef.getDigestValue();
     String signedDigestAlg = null;
     if (signedRef.getDigestMethod() != null) {
       signedDigestAlg = signedRef.getDigestMethod().getAlgorithm();
     } else {
       throw new NoSuchAlgorithmException("Failed to verify digest value for reference " + signedRefId + ": no digest algorithm");
     }

     
     if (hashDataInput == null) {
       throw new Exception("No hashdata input for reference " + signedRefId + " returned by service");
     }

     byte[] hdi = IOUtils.toByteArray(hashDataInput.getHashDataInput());
     String mimeType = hashDataInput.getMimeType();
     String encoding = hashDataInput.getEncoding();
     String filename = hashDataInput.getFilename();

     if (hdi == null) {
       throw new Exception("No hashdata input for reference " + signedRefId + " provided by service");
     }
     if (log.isDebugEnabled()) {
       log.debug("Digesting reference " + signedRefId + " (" + mimeType + ";" + encoding + ")");
     }

     byte[] hashDataInputDigest;
     if ((signedRef.getURI() != null) && signedRef.getURI().startsWith("CMSExcludedByteRange:")) {
       String range = signedRef.getURI().substring(21);
       int sep = range.indexOf('-');
       int from = Integer.parseInt(range.substring(0, sep));
       int to = Integer.parseInt(range.substring(sep+1));

       Arrays.fill(hdi, from, to+1, (byte)0);
       

       byte[] hashData = new byte[hdi.length - ((to+1) - from)];
       if (from > 0)
         System.arraycopy(hdi, 0, hashData, 0, from);
       if ((to+1) < hdi.length)
         System.arraycopy(hdi, to+1, hashData, from, hdi.length - (to+1));
       hashDataInputDigest = digest(hashData, signedDigestAlg);
     } else {
       hashDataInputDigest = digest(hdi, signedDigestAlg);
     }

     log.debug("Comparing digest to claimed digest value for reference {}.", signedRefId);
     if (!Arrays.equals(hashDataInputDigest, signedDigest)) {
       log.error("Bad digest value for reference {}.", signedRefId);
       throw new DigestException("Bad digest value for reference " + signedRefId);
     }

     verifiedHashDataInputs.add(new ByteArrayHashDataInput(hdi, signedRefId, mimeType, encoding, filename));
   }
 }

 return verifiedHashDataInputs;
}

  private byte[] digest(byte[] hashDataInput, String mdAlg) throws NoSuchAlgorithmException {
    if ("http://www.w3.org/2000/09/xmldsig#sha1".equals(mdAlg)) {
      mdAlg = "SHA-1";
    } else if ("http://www.w3.org/2001/04/xmlenc#sha256".equals(mdAlg)) {
      mdAlg = "SHA-256";
    } else if ("http://www.w3.org/2001/04/xmlenc#sha224".equals(mdAlg)) {
      mdAlg = "SHA-224";
    } else if ("http://www.w3.org/2001/04/xmldsig-more#sha224".equals(mdAlg)) {
      mdAlg = "SHA-224";
    } else if ("http://www.w3.org/2001/04/xmldsig-more#sha384".equals(mdAlg)) {
      mdAlg = "SHA-384";
    } else if ("http://www.w3.org/2001/04/xmlenc#sha512".equals(mdAlg)) {
      mdAlg = "SHA-512";
    } else if ("http://www.w3.org/2001/04/xmldsig-more#md2".equals(mdAlg)) {
      mdAlg = "MD2";
    } else if ("http://www.w3.org/2001/04/xmldsig-more#md5".equals(mdAlg)) {
      mdAlg = "MD5";
    } else if ("http://www.w3.org/2001/04/xmlenc#ripemd160".equals(mdAlg)) {
      mdAlg = "RIPEMD160";
    } else {
      throw new NoSuchAlgorithmException("Failed to verify digest value: unsupported digest algorithm " + mdAlg);
    }

    MessageDigest md;
    try {
      md = MessageDigest.getInstance(mdAlg);
    } catch (CryptoException e) {
      throw new NoSuchAlgorithmException(e);
    }
    return md.digest(hashDataInput);
  }
  
}
