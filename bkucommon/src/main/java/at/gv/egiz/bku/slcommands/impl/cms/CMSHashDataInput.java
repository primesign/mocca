package at.gv.egiz.bku.slcommands.impl.cms;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import at.gv.egiz.stal.HashDataInput;

public class CMSHashDataInput implements HashDataInput {

  private final static String DEFAULT_FILENAME = "SignatureData";
  private final static String CMS_DEF_REFERENCE_ID = "Reference-1";

  private byte[] data;
  private String mimeType;

  public CMSHashDataInput(byte[] data, String mimeType) {
    this.data = data;
    this.mimeType = mimeType;
  }

  @Override
  public String getReferenceId() {
    return CMS_DEF_REFERENCE_ID;
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
    String fileName = DEFAULT_FILENAME;
    String extension = getExtensionForMimeType(mimeType);
    if (extension != null)
      fileName += extension;
    return fileName;
  }

  @Override
  public InputStream getHashDataInput() {
    return new ByteArrayInputStream(data);
  }
  private static String getExtensionForMimeType(String mimeType) {
    if (mimeType.equalsIgnoreCase("application/pdf")) {
      return ".pdf";
    }
    else if (mimeType.equalsIgnoreCase("text/plain")) {
      return ".txt";
    }
    else if (mimeType.equalsIgnoreCase("application/xml")) {
      return ".xml";
    }
    else if (mimeType.equalsIgnoreCase("application/zip")) {
      return ".zip";
    }
    else if (mimeType.equalsIgnoreCase("application/gzip")) {
      return ".gz";
    }
    return null;
  }
}
