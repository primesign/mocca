/*
 * Copyright 2015 Datentechnik Innovation GmbH and Prime Sign GmbH, Austria
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

package at.gv.egiz.stal.hashdata;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import at.gv.egiz.stal.HashDataInput;
import at.gv.egiz.stal.signedinfo.ReferenceType;

/**
 * A StabHashDataInput is used as a placeholder at client side.
 * The reference is used to load the corresponding HashDataInput from STAL.
 * @author szoescher
 */
public class StubHashDataInput implements HashDataInput {

  private byte[] data;
  private String mimeType;
  private ReferenceType reference;
  private String fileName;


  public StubHashDataInput(ReferenceType reference, String fileName, String mimeType) {
    this.mimeType = mimeType;
    this.fileName = fileName;
    this.reference = reference;
  }

  @Override
  public String getReferenceId() {
    if (reference != null) {
      return reference.getId();
    }
    return null;
  }

  @Override
  public String getMimeType() {
    return mimeType;
  }

  @Override
  public String getEncoding() {
    return null;
  }

  @Override
  public String getFilename() {
    if (fileName != null) {
      return fileName;
    }
    return DEFAULT_FILENAME;
  }

  @Override
  public InputStream getHashDataInput() {

    if (data != null) {
      return new ByteArrayInputStream(data);
    }

    return null;
  }

  @Override
  public byte[] getDigest() {
    if (reference != null) {
      return reference.getDigestValue();
    }
    return null;
  }

  public void setFilename(String fileName) {
    this.fileName = fileName;
  }

  public ReferenceType getReference() {
    return reference;
  }	

}
