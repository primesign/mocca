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
package at.gv.egiz.smcc;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 *
 * @author mcentner
 */
public class PINSpec {

    int minLength_ = 0;
    
    int maxLength_ = -1;
    
    String rexepPattern_;
    
    String resourceBundleName_;
    
    String name_;

    byte kid_;

    byte[] context_aid_;

    /**
     *
     * @param minLenght
     * @param maxLength
     * @param rexepPattern
     * @param resourceBundle
     * @param name
     * @param kid the keyId for this pin
     */
    public PINSpec(int minLenght, int maxLength, String rexepPattern, 
        String resourceBundleName, String name, byte kid, byte[] contextAID) {
        
        minLength_ = minLenght;
        maxLength_ = maxLength;
        rexepPattern_ = rexepPattern;
        resourceBundleName_ = resourceBundleName;
        name_ = name;
        kid_ = kid;
        context_aid_ = contextAID;
    }

    public PINSpec(int minLenght, int maxLength, String rexepPattern, 
        String name, byte kid, byte[] contextAID) {
        
        minLength_ = minLenght;
        maxLength_ = maxLength;
        rexepPattern_ = rexepPattern;
        name_ = name;
        kid_ = kid;
        context_aid_ = contextAID;
    }
    
    public String getLocalizedName() {
      
      if (resourceBundleName_ != null) {
        ResourceBundle resourceBundle = ResourceBundle.getBundle(resourceBundleName_);
        return resourceBundle.getString(name_);
      } else {
        return name_;
      }
        
    }
    
    public String getLocalizedName(Locale locale) {
      
      if (resourceBundleName_ != null) {
        ResourceBundle resourceBundle = ResourceBundle.getBundle(resourceBundleName_, locale);
        return resourceBundle.getString(name_);
      } else {
        return name_;
      }
      
    }

    public int getMaxLength() {
        return maxLength_;
    }

    public int getMinLength() {
        return minLength_;
    }

    public String getRexepPattern() {
        return rexepPattern_;
    }

    public byte getKID() {
      return kid_;
    }

    public byte[] getContextAID() {
      return context_aid_;
    }
    
    
}
