/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.gv.egiz.bku.smccstal;

import at.gv.egiz.stal.HashDataInput;
import at.gv.egiz.stal.impl.ByteArrayHashDataInput;
import at.gv.egiz.stal.signedinfo.ReferenceType;
import java.security.DigestException;
import java.util.List;
import java.util.Set;

/**
 *
 * @author clemens
 */
public interface CashedHashDataInputResolver {

    /**
     * implementations may verify the hashvalue 
     * @post-condition returned list != null
     * @return
     */
    List<HashDataInput> getCashedHashDataInputs(List<ReferenceType> signedReferences) throws DigestException, Exception; 
}
