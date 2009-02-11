package at.gv.egiz.bku.binding;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.bku.slcommands.SLResult;
import at.gv.egiz.bku.slcommands.SLResult.SLResultType;
import at.gv.egiz.bku.slexceptions.SLRuntimeException;
import at.gv.egiz.bku.utils.binding.Protocol;

/**
 * not thread-safe thus newInsance always returns a new object
 * 
 */
public class LegacyDataUrlConnectionImpl implements DataUrlConnectionSPI {
  
  private final static Log log = LogFactory.getLog(DataUrlConnectionImpl.class);

  public final static Protocol[] SUPPORTED_PROTOCOLS = { Protocol.HTTP,
      Protocol.HTTPS };
  protected X509Certificate serverCertificate;
  protected Protocol protocol;
  protected URL url;
  private HttpURLConnection connection;
  protected Map<String, String> requestHttpHeaders;
  protected Map<String, String> formParams;
  protected String boundary;
  protected Properties config = null;
  protected SSLSocketFactory sslSocketFactory;
  protected HostnameVerifier hostnameVerifier;

  protected DataUrlResponse result;

  public String getProtocol() {
    if (protocol == null) {
      return null;
    }
    return protocol.toString();
  }

  /**
   * opens a connection sets the headers gets the server certificate
   * 
   * @throws java.net.SocketTimeoutException
   * @throws java.io.IOException
   * @pre url != null
   * @pre httpHeaders != null
   */
  public void connect() throws SocketTimeoutException, IOException {
    connection = (HttpURLConnection) url.openConnection();
    if (connection instanceof HttpsURLConnection) {
      HttpsURLConnection https = (HttpsURLConnection) connection;
      if (sslSocketFactory != null) {
        log.debug("Setting custom ssl socket factory for ssl connection");
        https.setSSLSocketFactory(sslSocketFactory);
      }
      if (hostnameVerifier != null) {
        log.debug("Setting custom hostname verifier");
        https.setHostnameVerifier(hostnameVerifier);
      }
    }
    connection.setDoOutput(true);
    Set<String> headers = requestHttpHeaders.keySet();
    Iterator<String> headerIt = headers.iterator();
    while (headerIt.hasNext()) {
      String name = headerIt.next();
      connection.setRequestProperty(name, requestHttpHeaders.get(name));
    }
    log.trace("Connecting to: "+url);
    connection.connect();
    if (connection instanceof HttpsURLConnection) {
      HttpsURLConnection ssl = (HttpsURLConnection) connection;
      X509Certificate[] certs = (X509Certificate[]) ssl.getServerCertificates();
      if ((certs != null) && (certs.length >= 1)) {
        log.trace("Server certificate: "+certs[0]);
        serverCertificate = certs[0];
      }
    }
  }

  public X509Certificate getServerCertificate() {
    return serverCertificate;
  }

  public void setHTTPHeader(String name, String value) {
    if (name != null && value != null) {
      requestHttpHeaders.put(name, value);
    }
  }

  public void setHTTPFormParameter(String name, InputStream data,
      String contentType, String charSet, String transferEncoding) {
    StringBuilder sb = new StringBuilder();
    try {
      InputStreamReader reader = new InputStreamReader(data, (charSet != null) ? charSet : "UTF-8");
      char[] c = new char[512];
      for (int l; (l = reader.read(c)) != -1;) {
        sb.append(c, 0, l);
      }
    } catch (IOException e) {
      throw new SLRuntimeException("Failed to set HTTP form parameter.", e);
    }
    formParams.put(name, sb.toString());
  }

  /**
   * send all formParameters
   * 
   * @throws java.io.IOException
   */
  public void transmit(SLResult slResult) throws IOException {
    StringWriter writer = new StringWriter();
    slResult.writeTo(new StreamResult(writer));
    formParams.put(
        (slResult.getResultType() == SLResultType.XML) 
            ? DataUrlConnection.FORMPARAM_XMLRESPONSE
            : DataUrlConnection.FORMPARAM_BINARYRESPONSE, 
        writer.toString());

    OutputStream os = connection.getOutputStream();
    OutputStreamWriter streamWriter = new OutputStreamWriter(os, HttpUtil.DEFAULT_CHARSET);

    log.trace("Sending data");
    Iterator<String> keys = formParams.keySet().iterator();
    while(keys.hasNext()) {
      String key = keys.next();
      streamWriter.write(URLEncoder.encode(key, "UTF-8"));
      streamWriter.write("=");
      streamWriter.write(URLEncoder.encode(formParams.get(key), "UTF-8"));
      if (keys.hasNext()) {
        streamWriter.write("&");
      }
    }
    streamWriter.flush();
    os.close();
    
    // MultipartRequestEntity PostMethod
    InputStream is = null;
    try {
      is = connection.getInputStream();
    } catch (IOException iox) {
      log.info(iox);
    }
    log.trace("Reading response");
    result = new DataUrlResponse(url.toString(), connection.getResponseCode(),  is);
    Map<String, String> responseHttpHeaders = new HashMap<String, String>();
    Map<String, List<String>> httpHeaders = connection.getHeaderFields();
    for (Iterator<String> keyIt = httpHeaders.keySet().iterator(); keyIt
        .hasNext();) {
      String key = keyIt.next();
      StringBuffer value = new StringBuffer();
      for (String val : httpHeaders.get(key)) {
        value.append(val);
        value.append(HttpUtil.SEPERATOR[0]);
      }
      String valString = value.substring(0, value.length() - 1);
      if ((key != null) && (value.length() > 0)) {
        responseHttpHeaders.put(key, valString);
      }
    }
    result.setResponseHttpHeaders(responseHttpHeaders);
  }

  @Override
  public DataUrlResponse getResponse() throws IOException {
    return result;
  }

  /**
   * inits protocol, url, httpHeaders, formParams
   * 
   * @param url
   *          must not be null
   */
  @Override
  public void init(URL url) {

    for (int i = 0; i < SUPPORTED_PROTOCOLS.length; i++) {
      if (SUPPORTED_PROTOCOLS[i].toString().equalsIgnoreCase(url.getProtocol())) {
        protocol = SUPPORTED_PROTOCOLS[i];
        break;
      }
    }
    if (protocol == null) {
      throw new SLRuntimeException("Protocol " + url.getProtocol()
          + " not supported for data url");
    }
    this.url = url;
    requestHttpHeaders = new HashMap<String, String>();
    if ((config != null)
        && (config.getProperty(USERAGENT_CONFIG_P) != null)) {
      log.debug("setting User-Agent header: " + config.getProperty(USERAGENT_CONFIG_P));
      requestHttpHeaders.put(HttpUtil.HTTP_HEADER_USER_AGENT, config
          .getProperty(USERAGENT_CONFIG_P));
    } else {
      requestHttpHeaders
          .put(HttpUtil.HTTP_HEADER_USER_AGENT, USERAGENT_DEFAULT);

    }
    requestHttpHeaders.put(HttpUtil.HTTP_HEADER_CONTENT_TYPE,
        HttpUtil.APPLICATION_URL_ENCODED);
    
    formParams = new HashMap<String, String>();
  }

  @Override
  public DataUrlConnectionSPI newInstance() {
    DataUrlConnectionSPI uc = new LegacyDataUrlConnectionImpl();
    uc.setConfiguration(config);
    uc.setSSLSocketFactory(sslSocketFactory);
    uc.setHostnameVerifier(hostnameVerifier);
    return uc;
  }

  @Override
  public URL getUrl() {
    return url;
  }

  @Override
  public void setConfiguration(Properties config) {
    this.config = config;
  }
  
  @Override
  public void setSSLSocketFactory(SSLSocketFactory socketFactory) {
    this.sslSocketFactory = socketFactory;
  }
  
  @Override
  public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
    this.hostnameVerifier = hostnameVerifier;
  }
}