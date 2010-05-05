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

package at.gv.egiz.mocca.id;

import iaik.utils.Base64OutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PersonalIdentifier {
  
  public static final String PREFIX = "urn:publicid:gv.at:";
  
  public static final String BASE_ID = PREFIX + "baseid";
  
  
  protected String type;
  
  protected String value;

  public PersonalIdentifier(String type, String value) {
    this.type = type;
    this.value = value;
  }

  /**
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * @return the value
   */
  public String getValue() {
    return value;
  }
  
  public PersonalIdentifier getDerivedValue(String domainId) {
    
    if (BASE_ID.equals(type)) {
      try {
        MessageDigest md = MessageDigest.getInstance("SHA");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Base64OutputStream bos = new Base64OutputStream(os);
        bos.write(md.digest((value + '+' + domainId).getBytes("ISO-8859-1")));
        bos.flush();
        return new PersonalIdentifier(domainId, os.toString("ASCII"));
      } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException(e);
      } catch (UnsupportedEncodingException e) {
        throw new RuntimeException(e);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return null;

  }
  
  
}
