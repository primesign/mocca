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
        bos.close();
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
