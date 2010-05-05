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
package at.gv.egiz.bku.binding;

import iaik.utils.Base64OutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation that uses a Base64 representation for self generated Ids.
 * @author wbauer
 *
 */
public class IdImpl implements at.gv.egiz.bku.binding.Id {

  private final Logger log = LoggerFactory.getLogger(IdImpl.class);
  
  private String idString;

  public IdImpl(int bitNumber, SecureRandom random) {
    int byteSize = bitNumber/8;
    if (bitNumber % 8 != 0) {
      byteSize++;
    }
    byte[] randomBytes = new byte[byteSize];
    random.nextBytes(randomBytes);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    Base64OutputStream b64 = new Base64OutputStream(baos);
    try {
      b64.write(randomBytes);
      b64.flush();
      b64.close();
      idString = new String(baos.toByteArray());
    } catch (IOException e) {
      log.error("Cannot create secure id.", e);
    }
  }

  public IdImpl(String idString) {
    if (idString == null) {
      throw new NullPointerException("Provided idstring must not be null");
    }
    this.idString = idString;
  }

  @Override
  public String toString() {
    return idString;
  }
  
  @Override
  public int hashCode() {
    return idString.hashCode();
  }
  
  @Override
  public boolean equals(Object other) {
    if (other instanceof Id) {
      Id otherId = (Id)other;
      return otherId.toString().equals(idString);
    } else {
      return false;
    }
  }
}
