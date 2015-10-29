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

package at.gv.egiz.bku.utils.urldereferencer;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileURLProtocolHandlerImpl implements URLProtocolHandler {
	
	private final Logger log = LoggerFactory.getLogger(FileURLProtocolHandlerImpl.class);
	
  public final static String FILE = "file";
  
  
  @Override
  public StreamData dereference(String url)
      throws IOException {
  	
  	URL u = new URL(url);
  	URLConnection connection = u.openConnection();
  	
    log.trace("Successfully opened connection.");
    return new StreamData(url.toString(), connection.getContentType(), connection.getInputStream());
  	
  }

  @Override
  public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
  	log.warn("not implemented for {}",  this.getClass().getName());
  }

  @Override
  public void setSSLSocketFactory(SSLSocketFactory socketFactory) {
  	log.warn("not implemented for {}",  this.getClass().getName());
  }
  
}
