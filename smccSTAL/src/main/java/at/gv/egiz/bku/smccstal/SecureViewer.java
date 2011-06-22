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


package at.gv.egiz.bku.smccstal;

import at.gv.egiz.stal.signedinfo.SignedInfoType;
import java.awt.event.ActionListener;
import java.security.DigestException;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public interface SecureViewer {

  /**
   * Displays the hashdata inputs for all provided dsig:SignedReferences.
   * Implementations may verify the digest value if necessary.
   * (LocalSignRequestHandler operates on DataObjectHashDataInput,
   * other SignRequestHandlers should cache the HashDataInputs obtained by webservice calls,
   * or simply forward to a HashDataInputServlet.)
   * @param signedReferences The caller may select a subset of the references in SignedInfo to be displayed.
   * @throws java.security.DigestException if digest values are verified and do not correspond
   * (or any other digest computation error occurs)
   * @throws java.lang.Exception
   */
  void displayDataToBeSigned(SignedInfoType signedInfo,
          ActionListener okListener, String okCommand)
        throws DigestException, Exception;
}
