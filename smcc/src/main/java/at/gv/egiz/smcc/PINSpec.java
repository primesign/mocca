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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.gv.egiz.smcc;

import java.util.ResourceBundle;

/**
 *
 * @author mcentner
 */
public class PINSpec {

    int minLength_ = 0;
    
    int maxLength_ = -1;
    
    String rexepPattern_;
    
    ResourceBundle resourceBundle_;
    
    String name_;

    public PINSpec(int minLenght, int maxLength, String rexepPattern, 
        ResourceBundle resourceBundle, String name) {
        
        minLength_ = minLenght;
        maxLength_ = maxLength;
        rexepPattern_ = rexepPattern;
        resourceBundle_ = resourceBundle;
        name_ = name;
    }
    
    public PINSpec(int minLenght, int maxLength, String rexepPattern, 
        String name) {
        
        minLength_ = minLenght;
        maxLength_ = maxLength;
        rexepPattern_ = rexepPattern;
        name_ = name;
    }
    
    
    
    public String getLocalizedName() {
        
        return (resourceBundle_ != null) 
            ? resourceBundle_.getString(name_)
            : name_;
        
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
    
    
    
}
