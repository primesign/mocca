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

package at.gv.egiz.bku.slcommands.impl.cms;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import at.gv.egiz.bku.gui.viewer.MimeTypes;
import at.gv.egiz.stal.HashDataInput;

/**
 *
 * @author szoescher
 */
public class BulkHashDataInput implements HashDataInput {

  public final static String DEFAULT_FILENAME = "SignatureData";

  private byte[] data;
  private byte[] digest;
  private String mimeType;
  private String referenceId;
  private String fileName;

  public BulkHashDataInput(byte[] data, String mimeType) {
    this.data = data;
    this.mimeType = mimeType;
  }

  public BulkHashDataInput(byte[] data, String fileName, String mimeType) {
    this.data = data;
    this.mimeType = mimeType;
    this.fileName = fileName;
  }

  public BulkHashDataInput(String referenceId, String fileName, String mimeType, byte[] digest) {
    this.mimeType = mimeType;
    this.fileName = fileName;
    this.referenceId = referenceId;
    this.digest = digest;
  }

  @Override
  public String getReferenceId() {
    return referenceId;
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
    return DEFAULT_FILENAME + MimeTypes.getExtension(mimeType);
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
    return digest;
  }

  public void setFilename(String fileName) {
    this.fileName = fileName;
  }

  public void setDigest(byte[] digest) {
    this.digest = digest;
  }

  public void setReferenceId(String referenceId) {
    this.referenceId = referenceId;
  }
}
