/*
* Copyright 2009 Federal Chancellery Austria and
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

package at.gv.egiz.smcc.test.spring;

import java.beans.PropertyEditorSupport;
import java.io.ByteArrayOutputStream;

public class ByteArrayPropertyEditor extends PropertyEditorSupport {

  @Override
  public void setAsText(String text) throws IllegalArgumentException {
    
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    
    int hi = 0, n = 0;
    for (int i = 0; i < text.length(); i++) {
      int digit = Character.digit(text.charAt(i), 16);
      if (digit != -1) {
        if (n++ % 2 == 0) {
          hi = digit << 4; 
        } else {
          os.write(hi + digit);
        }
      }
    }
    
    if (n % 2 != 0) {
      throw new IllegalArgumentException();
    }
    
    setValue(os.toByteArray());
  }
  
}
