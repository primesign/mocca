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

import at.gv.egiz.stal.HashDataInput;
import java.security.PrivateKey;

import at.gv.egiz.stal.STAL;
//import at.gv.egiz.stal.HashDataInputCallback;
import java.util.List;

/**
 * This class implements a private key used by the {@link STALSignature} class. 
 * 
 * @author mcentner
 */
public class STALPrivateKey implements PrivateKey {

  private static final long serialVersionUID = 1L;

  /**
   * The STAL implementation.
   */
  private STAL stal;
  
  /**
   * The callback interface for obtaining the hash input data.
   */
//  private HashDataInputCallback hashDataInputCallback;
  

  private List<DataObject> dataObjects;
  
  /**
   * The keybox identifier.
   */
  private String keyboxIdentifier;
  
  /**
   * The signature algorithm.
   */
  private String algorithm;

  /**
   * Creates a new instance of this <code>STALPrivateKey</code> with the given
   * <code>stal</code> implementation, signature <code>algorithm</code>,
   * <code>keyboxIdentifier</code> and <code>hashDataInputCallback</code>
   * interface.
   * 
   * @param stal
   *          the STAL implementation
   * @param algorithm
   *          the signature algorithm
   * @param keyboxIdentifier
   *          the keybox identifier
   * @param hashDataInputCallback
   *          the interface for obtaining the has input data
   */
  public STALPrivateKey(STAL stal,
      String algorithm, String keyboxIdentifier, List<DataObject> dataObjects) {
    super();
    this.keyboxIdentifier = keyboxIdentifier;
    this.dataObjects = dataObjects;
    this.stal = stal;
    this.algorithm = algorithm;
  }

  /* (non-Javadoc)
   * @see java.security.Key#getAlgorithm()
   */
  @Override
  public String getAlgorithm() {
    return algorithm;
  }

  /* (non-Javadoc)
   * @see java.security.Key#getEncoded()
   */
  @Override
  public byte[] getEncoded() {
    throw new UnsupportedOperationException("STALPrivateKey does not support the getEncoded() method.");
  }

  /* (non-Javadoc)
   * @see java.security.Key#getFormat()
   */
  @Override
  public String getFormat() {
    return null;
  }

  /**
   * @return the STAL implementation
   */
  public STAL getStal() {
    return stal;
  }

  /**
   * @return the interface for obtaining the hash data input
   */
  public List<DataObject> getDataObjects() {
      
    return dataObjects;
  }

  /**
   * @return the keybox identifier
   */
  public String getKeyboxIdentifier() {
    return keyboxIdentifier;
  }
  
}
