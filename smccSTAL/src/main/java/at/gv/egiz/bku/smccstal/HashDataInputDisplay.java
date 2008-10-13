/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.gv.egiz.bku.smccstal;

import at.gv.egiz.stal.signedinfo.ReferenceType;
import java.security.DigestException;
import java.util.List;

/**
 *
 * @author clemens
 */
public interface HashDataInputDisplay {

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
  void displayHashDataInputs(List<ReferenceType> signedReferences) throws DigestException, Exception;
  
}
