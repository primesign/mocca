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
package at.gv.egiz.bku.viewer;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ValidatorFactory {
  
  /**
   * Logging facility.
   */
  protected static Log log = LogFactory.getLog(ValidatorFactory.class);
  
  private static final Class<Validator> VALIDATOR_CLASS = Validator.class;
  
  private static final String SERVICE_ID = "META-INF/services/" + VALIDATOR_CLASS.getName();

  /**
   * Creates a new Validator for the given <code>mimeType</code>.
   * 
   * @param mimeType
   * 
   * @return
   * 
   * @throws IllegalArgumentException
   *           if no Validator for the <code>mimeType</code> could be found
   */
  public static Validator newValidator(String mimeType) throws IllegalArgumentException {
    
    ClassLoader classLoader = ValidatorFactory.class.getClassLoader();
    ValidatorFactory factory = new ValidatorFactory(classLoader);
    
    Validator validator = factory.createValidator(mimeType);
    
    if (validator == null) {
      throw new IllegalArgumentException("Validator for '" + mimeType
          + "' could not be found.");
    }

    return validator;
    
  }
  
  private ClassLoader classLoader;
  
  /**
   * Private constructor.
   * 
   * @param classLoader must not be <code>null</code>
   */
  private ValidatorFactory(ClassLoader classLoader) {
    
    if (classLoader == null) {
      throw new NullPointerException("Argument 'classLoader' must no be null.");
    }
    
    this.classLoader = classLoader;
    
  }

  private Validator createValidator(String mimeType) {
    
    Iterator<URL> serviceIterator = createServiceIterator();
    while (serviceIterator.hasNext()) {
      URL url = serviceIterator.next();
      
      Properties properties = new Properties();
      try {
        properties.load(url.openStream());
      } catch (IOException e) {
        log.error("Failed to load service properties " + url.toExternalForm());
        continue;
      }
      String className = properties.getProperty(mimeType);
      if (className != null) {
        try {
          return createValidatorInstance(className);
        } catch (Exception e) {
          continue;
        }
      }
      
    }
    
    return null;
    
  }
  
  private Validator createValidatorInstance(String className)
      throws ClassNotFoundException, InstantiationException,
      IllegalAccessException {
    
    try {
      Class<?> implClass = classLoader.loadClass(className);
      return (Validator) implClass.newInstance();
    } catch (ClassNotFoundException e) {
      log.error("Validator class '" + className + "' not found.", e);
      throw e;
    } catch (InstantiationException e) {
      log.error("Faild to initialize validator class '" + className + "'.", e);
      throw e;
    } catch (IllegalAccessException e) {
      log.error("Faild to initialize validator class '" + className + "'.", e);
      throw e;
    } catch (ClassCastException e) {
      log.error("Class '" + className + "' is not a validator implementation.", e);
      throw e;
    }
    
  }
  
  private Iterator<URL> createServiceIterator() {
    
    try {
      final Enumeration<URL> resources = classLoader.getResources(SERVICE_ID);
      return new Iterator<URL> () {

        @Override
        public boolean hasNext() {
          return resources.hasMoreElements();
        }

        @Override
        public URL next() {
          return resources.nextElement();
        }

        @Override
        public void remove() {
          throw new UnsupportedOperationException();
        }
        
      };
    } catch (IOException e) {
      log.error("Failed to enumerate resources " + SERVICE_ID);
      List<URL> list = Collections.emptyList(); 
      return list.iterator();
    }
    
  }
  
}
