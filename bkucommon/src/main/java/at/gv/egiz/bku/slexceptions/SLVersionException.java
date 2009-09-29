package at.gv.egiz.bku.slexceptions;

public class SLVersionException extends SLException {

  private static final long serialVersionUID = 1L;

  protected String namespaceURI;
  
  public SLVersionException(String namespaceURI) {
    super(2901, SLExceptionMessages.LEC2901_NOTIMPLEMENTED, new Object[] {namespaceURI});
    this.namespaceURI = namespaceURI;
  }
  
  public SLVersionException(int errorCode, String namespaceURI) {
    super(errorCode);
    this.namespaceURI = namespaceURI;
  }

  public SLVersionException(int errorCode, String namespaceURI, String message, Object[] arguments) {
    super(errorCode, message, arguments);
    this.namespaceURI = namespaceURI;
  }

  public String getNamespaceURI() {
    return namespaceURI;
  }

}
