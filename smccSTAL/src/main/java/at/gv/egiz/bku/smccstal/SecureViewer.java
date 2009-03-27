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
