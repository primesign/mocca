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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * An implementation of the IdValueFactory.
 * <p>
 * This IdValueFactory creates <code>xsd:Id</code>-attribute values of the form
 * '<code>&lt;elementName&gt;-&lt;random&gt;-&lt;sequenceNumber&gt;</code>',
 * where
 * <ul>
 * <li><code>&lt;elementName&gt;</code> is the name provided at
 * {@link #createIdValue(String)},</li>
 * <li><code>&lt;random&gt;</code> is a random generated fixed value for an
 * instance of this IdValueFactory and</li>
 * <li><code>&lt;sequenceNumber&gt;</code> is the sequence number of the value
 * generated for a given <code>elementName</code> by an instance of this
 * IdValueFactory.</li>
 * </ul>
 * </p>
 * 
 * @author mcentner
 */
public class IdValueFactoryImpl implements IdValueFactory {

  /**
   * A generator for <code>xsd:Id</code>-attribute values.
   * 
   * @author mcentner
   */
  private class IdGenerator {

    /**
     * The salt.
     */
    private String salt;

    /**
     * The element name.
     */
    private String elementName;

    /**
     * The sequence number.
     */
    private int i = 0;

    /**
     * Creates a new instance of this IdGenerator with the given
     * <code>elementName</code> and <code>salt</code> value.
     * 
     * @param elementName the element name
     * @param salt the salt valeu
     */
    private IdGenerator(String elementName, String salt) {
      super();
      this.elementName = elementName;
      this.salt = salt;
    }

    /**
     * @return returns the next <code>xsd:Id</code>-attribute value.
     */
    public String getNextId() {
      return elementName + "-" + salt + "-" + Integer.toString(++i);
    }

  }

  /**
   * A map of element names to <code>xsd:Id</code>-value generators.
   */
  private Map<String, IdGenerator> generators = new HashMap<String, IdGenerator>();

  /**
   * The seed value.
   */
  private String seed;

  /**
   * Creates a new instance of this IdValueFactory.
   */
  public IdValueFactoryImpl() {

    Random random = new Random();
    int rand = random.nextInt();
    seed = Integer.toHexString(rand);

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * at.gv.egiz.bku.slcommands.impl.IdValueFactory#createIdValue(java.lang.String
   * )
   */
  public String createIdValue(String elementName) {

    IdGenerator generator = generators.get(elementName);
    if (generator == null) {
      generator = new IdGenerator(elementName, seed);
      generators.put(elementName, generator);
    }
    return generator.getNextId();

  }

}
