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
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.cert.X509Certificate;

import at.gv.egiz.bku.slcommands.SLResult;

/**
 * Transmit a security layer result to DataURL via HTTP POST, encoded as multipart/form-data. 
 * The HTTP header user-agent is set to <em>citizen-card-environment/1.2 BKU2 1.0</em>.
 * The form-parameter ResponseType is set to <em>HTTP-Security-Layer-RESPONSE</em>.
 * All other headers/parameters are set by the caller.
 * 
 * @author clemens
 */
public interface DataUrlConnection {

    public static final String FORMPARAM_RESPONSETYPE = "ResponseType";
    public static final String DEFAULT_RESPONSETYPE = "HTTP-Security-Layer-RESPONSE";
    public static final String FORMPARAM_XMLRESPONSE = "XMLResponse";
    public static final String FORMPARAM_BINARYRESPONSE = "BinaryResponse";
    
    public static final String XML_RESPONSE_ENCODING = "UTF-8";

    
    public String getProtocol();
    
    public URL getUrl();
    
    /**
     * Set a HTTP Header.
     * @param key
     * @param value multiple values are assumed to have the correct formatting (comma-separated list)
     */
    public void setHTTPHeader(String key, String value);

    /**
     * Set a form-parameter.
     * @param name
     * @param data
     * @param contentType may be null
     * @param charSet may be null
     * @param transferEncoding may be null
     */
    public void setHTTPFormParameter(String name, InputStream data, String contentType, String charSet, String transferEncoding);
    
    /**
     * @pre httpHeaders != null
     * @throws java.net.SocketTimeoutException
     * @throws java.io.IOException
     */
    public void connect() throws SocketTimeoutException, IOException;

    public X509Certificate getServerCertificate();

    /**
     * @pre connection != null
     * @throws java.io.IOException
     */
    public void transmit(SLResult slResult) throws IOException;

    public DataUrlResponse getResponse() throws IOException;
}