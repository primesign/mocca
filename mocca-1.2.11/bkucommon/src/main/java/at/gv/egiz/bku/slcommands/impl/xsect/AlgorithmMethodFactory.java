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
package at.gv.egiz.bku.slcommands.impl.xsect;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;

import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.SignatureMethod;

/**
 * A factory for creating {@link AlgorithmMethod}s.
 * 
 * @author mcentner
 */
public interface AlgorithmMethodFactory {

  /**
   * Creates a new DigestMethod for the given <code>signatureContext</code>.
   * 
   * @param signatureContext
   *          the signature context
   * 
   * @return a DigestMethod for the given <code>signatureContext</code>
   * 
   * @throws NoSuchAlgorithmException
   * @throws InvalidAlgorithmParameterException
   */
  public DigestMethod createDigestMethod(SignatureContext signatureContext)
      throws NoSuchAlgorithmException, InvalidAlgorithmParameterException;

  /**
   * Creates a new SignatureMethod for the given <code>signatureContext</code>.
   * 
   * @param signatureContext
   *          the signature context
   * 
   * @return a SignatureMethod for the given <code>signatureContext</code>
   * 
   * @throws NoSuchAlgorithmException
   * @throws InvalidAlgorithmParameterException
   */
  public SignatureMethod createSignatureMethod(SignatureContext signatureContext)
      throws NoSuchAlgorithmException, InvalidAlgorithmParameterException;

  /**
   * Creates a new CanonicalizationMethod for the given
   * <code>signatureContext</code>.
   * 
   * @param signatureContext
   *          the signature context
   * 
   * @return a CanonicalizationMethod for the given
   *         <code>signatureContext</code>
   * 
   * @throws NoSuchAlgorithmException
   * @throws InvalidAlgorithmParameterException
   */
  public CanonicalizationMethod createCanonicalizationMethod(
      SignatureContext signatureContext) throws NoSuchAlgorithmException,
      InvalidAlgorithmParameterException;

}
