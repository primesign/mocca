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


package at.gv.egiz.bku.slcommands.impl.xsect;

import java.util.HashMap;
import java.util.Map;
import java.security.SecureRandom;

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

    SecureRandom random = new SecureRandom();
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
