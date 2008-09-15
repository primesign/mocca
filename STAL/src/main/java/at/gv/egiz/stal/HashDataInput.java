/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.gv.egiz.stal;

import java.io.InputStream;

/**
 *
 * @author clemens
 */
public interface HashDataInput {
    
    public String getReferenceId();
    
    public String getMimeType();
    
    public String getEncoding();
    
    public InputStream getHashDataInput();

}
