package at.gv.egiz.bku.slcommands.impl.cms;

import java.security.PrivateKey;

/**
 * Dummy PrivateKey implementation for CMS signing using STAL
 * @author tkellner
 */
public class STALPrivateKey implements PrivateKey {

  private static final long serialVersionUID = 1L;

  private String algorithm;
  private String digestAlgorithm;

  public STALPrivateKey(String algorithm, String digestAlgorithm) {
    this.algorithm = algorithm;
    this.digestAlgorithm = digestAlgorithm;
  }

  @Override
  public String getAlgorithm() {
    return algorithm;
  }

  public String getDigestAlgorithm() {
    return digestAlgorithm;
  }

  @Override
  public byte[] getEncoded() {
    throw new UnsupportedOperationException("STALPrivateKey does not support the getEncoded() method.");
  }

  @Override
  public String getFormat() {
    return null;
  }

}
