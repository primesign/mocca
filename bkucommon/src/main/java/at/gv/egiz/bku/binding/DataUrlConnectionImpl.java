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
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocketFactory;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.bku.binding.multipart.InputStreamPartSource;
import at.gv.egiz.bku.binding.multipart.SLResultPart;
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
public class DataUrlConnectionImpl extends HttpsDataURLConnection {

  private final Logger log = LoggerFactory.getLogger(DataUrlConnectionImpl.class);

  public static final byte[] B_DEFAULT_RESPONSETYPE = DEFAULT_RESPONSETYPE
      .getBytes(Charset.forName("UTF-8"));

  /**
   * Supported protocols are HTTP and HTTPS.
   */
  public final static Protocol[] SUPPORTED_PROTOCOLS = { Protocol.HTTP,
      Protocol.HTTPS };

  /**
   * Use <code>application/x-www-form-urlencoded</code> instead of standard
   * conform <code>application/x-www-form-urlencoded</code>.
   */
  protected boolean urlEncoded = true;

  /**
   * The URLConnection used for communication with the DataURL server.
   */
  private HttpURLConnection connection;

  /**
   * The HTTP form parameters.
   */
  protected List<HTTPFormParameter> httpFormParameter = new ArrayList<HTTPFormParameter>();

  /**
   * The boundary for multipart/form-data requests.
   */
  protected String boundary;

  /**
   * The response of the DataURL server.
   */
  protected DataUrlResponse response;

  /**
   * Constructs a new instance of this DataUrlConnection implementation.
   * 
   * @param url the URL 
   * 
   * @throws IOException if an I/O exception occurs
   */
  public DataUrlConnectionImpl(URL url) throws IOException {
    super(url);
    
    Protocol protocol = null;
    for (int i = 0; i < SUPPORTED_PROTOCOLS.length; i++) {
      if (SUPPORTED_PROTOCOLS[i].toString().equalsIgnoreCase(url.getProtocol())) {
        protocol = SUPPORTED_PROTOCOLS[i];
        break;
      }
    }
    if (protocol == null) {
      throw new SLRuntimeException("Protocol " + url.getProtocol()
          + " not supported for data url.");
    }
    connection = (HttpURLConnection) url.openConnection();
    connection.setInstanceFollowRedirects(false);
    
    connection.setDoOutput(true);

    
    boundary = "--" + IdFactory.getInstance().createId().toString();
  }

  @Override
  public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
    if (connection instanceof HttpsURLConnection) {
      ((HttpsURLConnection) connection).setHostnameVerifier(hostnameVerifier);
    }
  }

  @Override
  public void setSSLSocketFactory(SSLSocketFactory socketFactory) {
    if (connection instanceof HttpsURLConnection) {
      ((HttpsURLConnection) connection).setSSLSocketFactory(socketFactory);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see at.gv.egiz.bku.binding.DataUrlConnection#connect()
   */
  public void connect() throws SocketTimeoutException, IOException {
    // Transfer-Encoding: chunked is problematic ...
    // e.g. https://issues.apache.org/bugzilla/show_bug.cgi?id=37794
    // ... therefore disabled.
    // connection.setChunkedStreamingMode(5*1024);
    if (urlEncoded) {
      log.debug("Setting DataURL Content-Type to {}.",
          HttpUtil.APPLICATION_URL_ENCODED);
      connection.addRequestProperty(HttpUtil.HTTP_HEADER_CONTENT_TYPE,
          HttpUtil.APPLICATION_URL_ENCODED);
    } else {
      log.debug("Setting DataURL Content-Type to {}.",
          HttpUtil.MULTIPART_FORMDATA_BOUNDARY);
      connection.addRequestProperty(HttpUtil.HTTP_HEADER_CONTENT_TYPE,
          HttpUtil.MULTIPART_FORMDATA + HttpUtil.SEPARATOR[0]
              + HttpUtil.MULTIPART_FORMDATA_BOUNDARY + "=" + boundary);
    }
    log.trace("Connecting to URL '{}'.", url);
    connection.connect();
  }

  /* (non-Javadoc)
   * @see at.gv.egiz.bku.binding.HttpsDataURLConnection#getServerCertificates()
   */
  @Override
  public Certificate[] getServerCertificates()
      throws SSLPeerUnverifiedException, IllegalStateException {
    if (connection instanceof HttpsURLConnection) {
      return ((HttpsURLConnection) connection).getServerCertificates();
    } else {
      return null;
    }
  }

  /* (non-Javadoc)
   * @see at.gv.egiz.bku.binding.HttpDataURLConnection#setHTTPHeader(java.lang.String, java.lang.String)
   */
  @Override
  public void setHTTPHeader(String name, String value) {
    connection.setRequestProperty(name, value);
  }

  /* (non-Javadoc)
   * @see at.gv.egiz.bku.binding.HttpDataURLConnection#setHTTPFormParameter(java.lang.String, java.io.InputStream, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void setHTTPFormParameter(String name, InputStream data,
      String contentType, String charSet, String transferEncoding) {
    // if a content type is specified we have to switch to multipart/form-data
    // encoding
    if (contentType != null && contentType.length() > 0) {
      urlEncoded = false;
    }
    httpFormParameter.add(new HTTPFormParameter(name, data, contentType,
        charSet, transferEncoding));
  }

  /* (non-Javadoc)
   * @see at.gv.egiz.bku.binding.DataUrlConnection#transmit(at.gv.egiz.bku.slcommands.SLResult)
   */
  @Override
  public void transmit(SLResult slResult) throws IOException {
    log.trace("Sending data.");
    if (urlEncoded) {
      //
      // application/x-www-form-urlencoded (legacy, SL < 1.2)
      //

      OutputStream os = connection.getOutputStream();
      OutputStreamWriter streamWriter = new OutputStreamWriter(os,
          HttpUtil.DEFAULT_CHARSET);

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
        InputStreamReader reader = new InputStreamReader(formParameter
            .getData(), (formParameter.getCharSet() != null) ? formParameter
            .getCharSet() : "UTF-8"); // assume request was
                                      // application/x-www-form-urlencoded,
                                      // formParam therefore UTF-8
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
      Part.sendParts(os, parts.toArray(new Part[parts.size()]), boundary
          .getBytes());
      os.close();

    }

    // MultipartRequestEntity PostMethod
    InputStream is = null;
    try {
      is = connection.getInputStream();
    } catch (IOException iox) {
      log.info("Failed to get InputStream of HTTPUrlConnection.", iox);
    }
    log.trace("Reading response.");
    response = new DataUrlResponse(url.toString(), connection.getResponseCode(),
        is);
    Map<String, String> responseHttpHeaders = new HashMap<String, String>();
    Map<String, List<String>> httpHeaders = connection.getHeaderFields();
    for (Iterator<String> keyIt = httpHeaders.keySet().iterator(); keyIt
        .hasNext();) {
      String key = keyIt.next();
      StringBuffer value = new StringBuffer();
      for (String val : httpHeaders.get(key)) {
        value.append(val);
        value.append(HttpUtil.SEPARATOR[0]);
      }
      String valString = value.substring(0, value.length() - 1);
      if ((key != null) && (value.length() > 0)) {
        responseHttpHeaders.put(key, valString);
      }
    }
    response.setResponseHttpHeaders(responseHttpHeaders);
  }

  @Override
  public DataUrlResponse getResponse() throws IOException {
    return response;
  }

  public class HTTPFormParameter {

    private String name;

    private InputStream data;

    private String contentType;

    private String charSet;

    private String transferEncoding;

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
     * @param name
     *          the name to set
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
     * @param data
     *          the data to set
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
     * @param contentType
     *          the contentType to set
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
     * @param charSet
     *          the charSet to set
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
     * @param transferEncoding
     *          the transferEncoding to set
     */
    public void setTransferEncoding(String transferEncoding) {
      this.transferEncoding = transferEncoding;
    }

  }
}
