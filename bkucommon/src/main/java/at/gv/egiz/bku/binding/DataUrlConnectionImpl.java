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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
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

import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.bku.binding.multipart.InputStreamPartSource;
import at.gv.egiz.bku.binding.multipart.SLResultPart;
import at.gv.egiz.bku.conf.Configurator;
import at.gv.egiz.bku.slcommands.SLResult;
import at.gv.egiz.bku.slcommands.SLResult.SLResultType;
import at.gv.egiz.bku.slexceptions.SLRuntimeException;
import at.gv.egiz.bku.utils.URLEncodingWriter;
import at.gv.egiz.bku.utils.binding.Protocol;

/**
 * An implementation of the DataUrlConnectionSPI that supports
 * <code>multipart/form-data</code> encoding and
 * <code>application/x-www-form-urlencoded</code> for compatibility with legacy
 * systems.
 * 
 */
public class DataUrlConnectionImpl implements DataUrlConnectionSPI {

  private final static Log log = LogFactory.getLog(DataUrlConnectionImpl.class);
  
  public static final byte[] B_DEFAULT_RESPONSETYPE = DEFAULT_RESPONSETYPE.getBytes(Charset.forName("UTF-8"));

  /**
   * Supported protocols are HTTP and HTTPS. 
   */
  public final static Protocol[] SUPPORTED_PROTOCOLS = { Protocol.HTTP,
      Protocol.HTTPS };

  /**
   * The X509 certificate of the DataURL server.
   */
  protected X509Certificate serverCertificate;
  
  /**
   * The protocol of the DataURL.
   */
  protected Protocol protocol;
  
  /**
   * Use <code>application/x-www-form-urlencoded</code> instead of
   * standard conform <code>application/x-www-form-urlencoded</code>.
   */
  protected boolean urlEncoded = true;
  
  /**
   * The value of the DataURL.
   */
  protected URL url;
  
  /**
   * The URLConnection used for communication with the DataURL server.
   */
  private HttpURLConnection connection;
  
  /**
   * The HTTP request headers.
   */
  protected Map<String, String> requestHttpHeaders;
  
  /**
   * The HTTP form parameters.
   */
  protected ArrayList<HTTPFormParameter> httpFormParameter;
  
  /**
   * The boundary for multipart/form-data requests.
   */
  protected String boundary;
  
  /**
   * The configuration properties.
   */
  protected Properties config = null;
  
  /**
   * The SSLSocketFactory for HTTPS connections.
   */
  protected SSLSocketFactory sslSocketFactory;
  
  /**
   * The HostnameVerifier for HTTPS connections. 
   */
  protected HostnameVerifier hostnameVerifier;

  /**
   * The response of the DataURL server.
   */
  protected DataUrlResponse result;

  /* (non-Javadoc)
   * @see at.gv.egiz.bku.binding.DataUrlConnection#getProtocol()
   */
  public String getProtocol() {
    if (protocol == null) {
      return null;
    }
    return protocol.toString();
  }

  /* (non-Javadoc)
   * @see at.gv.egiz.bku.binding.DataUrlConnection#connect()
   */
  public void connect() throws SocketTimeoutException, IOException {
    connection = (HttpURLConnection) url.openConnection();
    if (connection instanceof HttpsURLConnection) {
      log.trace("Detected ssl connection");
      HttpsURLConnection https = (HttpsURLConnection) connection;
      if (sslSocketFactory != null) {
        log.debug("Setting custom ssl socket factory for ssl connection");
        https.setSSLSocketFactory(sslSocketFactory);
      } else {
        log.trace("No custom socket factory set");
      }
      if (hostnameVerifier != null) {
        log.debug("Setting custom hostname verifier");
        https.setHostnameVerifier(hostnameVerifier);
      }
    } else {
      log.trace("No secure connection with: " + url + " class="
          + connection.getClass());
    }
    connection.setDoOutput(true);
    // Transfer-Encoding: chunked is problematic ...
    // e.g. https://issues.apache.org/bugzilla/show_bug.cgi?id=37794
    // ... therefore disabled.
    // connection.setChunkedStreamingMode(5*1024);
    if (urlEncoded) {
      log.debug("Setting DataURL Content-Type to "
          + HttpUtil.APPLICATION_URL_ENCODED);
      connection.addRequestProperty(HttpUtil.HTTP_HEADER_CONTENT_TYPE,
          HttpUtil.APPLICATION_URL_ENCODED);
    } else {
      log.debug("Setting DataURL Content-Type to "
          + HttpUtil.MULTIPART_FOTMDATA_BOUNDARY);
      connection.addRequestProperty(HttpUtil.HTTP_HEADER_CONTENT_TYPE,
          HttpUtil.MULTIPART_FOTMDATA + HttpUtil.SEPERATOR[0]
              + HttpUtil.MULTIPART_FOTMDATA_BOUNDARY + "=" + boundary);
    }
    Set<String> headers = requestHttpHeaders.keySet();
    Iterator<String> headerIt = headers.iterator();
    while (headerIt.hasNext()) {
      String name = headerIt.next();
      connection.setRequestProperty(name, requestHttpHeaders.get(name));
    }
    log.trace("Connecting to: " + url);
    connection.connect();
    if (connection instanceof HttpsURLConnection) {
      HttpsURLConnection ssl = (HttpsURLConnection) connection;
      X509Certificate[] certs = (X509Certificate[]) ssl.getServerCertificates();
      if ((certs != null) && (certs.length >= 1)) {
        log.trace("Server certificate: " + certs[0]);
        serverCertificate = certs[0];
      }
    }
  }

  /* (non-Javadoc)
   * @see at.gv.egiz.bku.binding.DataUrlConnection#getServerCertificate()
   */
  public X509Certificate getServerCertificate() {
    return serverCertificate;
  }

  /* (non-Javadoc)
   * @see at.gv.egiz.bku.binding.DataUrlConnection#setHTTPHeader(java.lang.String, java.lang.String)
   */
  public void setHTTPHeader(String name, String value) {
    if (name != null && value != null) {
      requestHttpHeaders.put(name, value);
    }
  }

  /* (non-Javadoc)
   * @see at.gv.egiz.bku.binding.DataUrlConnection#setHTTPFormParameter(java.lang.String, java.io.InputStream, java.lang.String, java.lang.String, java.lang.String)
   */
  public void setHTTPFormParameter(String name, InputStream data,
      String contentType, String charSet, String transferEncoding) {
    // if a content type is specified we have to switch to multipart/formdata encoding
    if (contentType != null && contentType.length() > 0) {
      urlEncoded = false;
    }
    httpFormParameter.add(new HTTPFormParameter(name, data, contentType,
        charSet, transferEncoding));
  }

  
  
  /* (non-Javadoc)
   * @see at.gv.egiz.bku.binding.DataUrlConnection#transmit(at.gv.egiz.bku.slcommands.SLResult)
   */
  public void transmit(SLResult slResult) throws IOException {
    log.trace("Sending data");
    if (urlEncoded) {
      //
      // application/x-www-form-urlencoded (legacy, SL < 1.2)
      //
      
      OutputStream os = connection.getOutputStream();
      OutputStreamWriter streamWriter = new OutputStreamWriter(os, HttpUtil.DEFAULT_CHARSET);

      // ResponseType
      streamWriter.write(FORMPARAM_RESPONSETYPE);
      streamWriter.write("=");
      streamWriter.write(URLEncoder.encode(DEFAULT_RESPONSETYPE, "UTF-8"));
      streamWriter.write("&");
      
      // XMLResponse / Binary Response
      if (slResult.getResultType() == SLResultType.XML) {
        streamWriter.write(DataUrlConnection.FORMPARAM_XMLRESPONSE);
      } else {
        streamWriter.write(DataUrlConnection.FORMPARAM_BINARYRESPONSE);
      }
      streamWriter.write("=");
      streamWriter.flush();
      URLEncodingWriter urlEnc = new URLEncodingWriter(streamWriter);
      slResult.writeTo(new StreamResult(urlEnc), false);
      urlEnc.flush();

      // transfer parameters
      char[] cbuf = new char[512];
      int len;
      for (HTTPFormParameter formParameter : httpFormParameter) {
        streamWriter.write("&");
        streamWriter.write(URLEncoder.encode(formParameter.getName(), "UTF-8"));
        streamWriter.write("=");
        InputStreamReader reader = new InputStreamReader(formParameter.getData(), 
            (formParameter.getCharSet() != null) 
                ? formParameter.getCharSet()
                : null);
        while ((len = reader.read(cbuf)) != -1) {
          urlEnc.write(cbuf, 0, len);
        }
        urlEnc.flush();
      }
      streamWriter.close();
      
    } else {
      //
      // multipart/form-data (conforming to SL 1.2)
      //

      ArrayList<Part> parts = new ArrayList<Part>();

      // ResponseType
      StringPart responseType = new StringPart(FORMPARAM_RESPONSETYPE,
          DEFAULT_RESPONSETYPE, "UTF-8");
      responseType.setTransferEncoding(null);
      parts.add(responseType);
      
      // XMLResponse / Binary Response
      SLResultPart slResultPart = new SLResultPart(slResult,
          XML_RESPONSE_ENCODING);
      if (slResult.getResultType() == SLResultType.XML) {
        slResultPart.setTransferEncoding(null);
        slResultPart.setContentType(slResult.getMimeType());
        slResultPart.setCharSet(XML_RESPONSE_ENCODING);
      } else {
        slResultPart.setTransferEncoding(null);
        slResultPart.setContentType(slResult.getMimeType());
      }
      parts.add(slResultPart);
      
      // transfer parameters
      for (HTTPFormParameter formParameter : httpFormParameter) {
        InputStreamPartSource source = new InputStreamPartSource(null,
            formParameter.getData());
        FilePart part = new FilePart(formParameter.getName(), source,
            formParameter.getContentType(), formParameter.getCharSet());
        part.setTransferEncoding(formParameter.getTransferEncoding());
        parts.add(part);
      }

      OutputStream os = connection.getOutputStream();
      Part.sendParts(os, parts.toArray(new Part[parts.size()]), boundary.getBytes());
      os.close();
      
    }
    
    // MultipartRequestEntity PostMethod
    InputStream is = null;
    try {
      is = connection.getInputStream();
    } catch (IOException iox) {
      log.info(iox);
    }
    log.trace("Reading response");
    result = new DataUrlResponse(url.toString(), connection.getResponseCode(),
        is);
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
    boundary = "--" + IdFactory.getInstance().createId().toString();
    requestHttpHeaders = new HashMap<String, String>();
    
    if (config != null) {
      String version = config.getProperty(Configurator.SIGNATURE_LAYOUT);
      if ((version != null) && (!"".equals(version.trim()))) {
    	log.debug("setting SignatureLayout header to " + version);
        requestHttpHeaders.put(Configurator.SIGNATURE_LAYOUT, version);
      } else {
        log.debug("do not set SignatureLayout header");
      }
      String userAgent = config.getProperty(Configurator.USERAGENT_CONFIG_P, Configurator.USERAGENT_DEFAULT);
      requestHttpHeaders.put(HttpUtil.HTTP_HEADER_USER_AGENT, userAgent);
    } else {
      requestHttpHeaders
          .put(HttpUtil.HTTP_HEADER_USER_AGENT, Configurator.USERAGENT_DEFAULT);

    }

    httpFormParameter = new ArrayList<HTTPFormParameter>();

  }

  @Override
  public DataUrlConnectionSPI newInstance() {
    DataUrlConnectionSPI uc = new DataUrlConnectionImpl();
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
  
  public class HTTPFormParameter {

    private String name; 
    
    private InputStream data;
    
    private String contentType;
    
    private String charSet;
    
    private String transferEncoding;
    
    /**
     * @param name
     * @param data
     * @param contentType
     * @param charSet
     * @param transferEncoding
     */
    public HTTPFormParameter(String name, InputStream data, String contentType,
        String charSet, String transferEncoding) {
      super();
      this.name = name;
      this.data = data;
      this.contentType = contentType;
      this.charSet = charSet;
      this.transferEncoding = transferEncoding;
    }

    /**
     * @return the name
     */
    public String getName() {
      return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
      this.name = name;
    }

    /**
     * @return the data
     */
    public InputStream getData() {
      return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(InputStream data) {
      this.data = data;
    }

    /**
     * @return the contentType
     */
    public String getContentType() {
      return contentType;
    }

    /**
     * @param contentType the contentType to set
     */
    public void setContentType(String contentType) {
      this.contentType = contentType;
    }

    /**
     * @return the charSet
     */
    public String getCharSet() {
      return charSet;
    }

    /**
     * @param charSet the charSet to set
     */
    public void setCharSet(String charSet) {
      this.charSet = charSet;
    }

    /**
     * @return the transferEncoding
     */
    public String getTransferEncoding() {
      return transferEncoding;
    }

    /**
     * @param transferEncoding the transferEncoding to set
     */
    public void setTransferEncoding(String transferEncoding) {
      this.transferEncoding = transferEncoding;
    }

    
    
  }
}