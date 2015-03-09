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

import iaik.utils.Base64InputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocketFactory;
import javax.xml.XMLConstants;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.bku.conf.MoccaConfigurationFacade;
import at.gv.egiz.bku.slcommands.ErrorResult;
import at.gv.egiz.bku.slcommands.SLCommand;
import at.gv.egiz.bku.slcommands.SLCommandContext;
import at.gv.egiz.bku.slcommands.SLResult;
import at.gv.egiz.bku.slcommands.SLSourceContext;
import at.gv.egiz.bku.slcommands.SLTargetContext;
import at.gv.egiz.bku.slcommands.impl.ErrorResultImpl;
import at.gv.egiz.bku.slexceptions.SLBindingException;
import at.gv.egiz.bku.slexceptions.SLException;
import at.gv.egiz.bku.spring.ConfigurationFactoryBean;
import at.gv.egiz.bku.utils.ConfigurationUtil;
import at.gv.egiz.bku.utils.StreamUtil;
import at.gv.egiz.bku.utils.urldereferencer.StreamData;
import at.gv.egiz.bku.utils.urldereferencer.URIResolverAdapter;
import at.gv.egiz.bku.utils.urldereferencer.URLDereferencer;
import at.gv.egiz.stal.QuitRequest;
import at.gv.egiz.stal.STALRequest;

/**
 * Class performing the HTTP binding as defined by the CCE specification.
 * Currently a huge monolithic class.
 * 
 * @TODO refactor
 */
@SuppressWarnings("unchecked")
public class HTTPBindingProcessorImpl extends AbstractBindingProcessor implements
		HTTPBindingProcessor, FormDataURLSupplier {

	private final Logger log = LoggerFactory.getLogger(HTTPBindingProcessorImpl.class);

	private static enum State {
		INIT, PROCESS, DATAURL, TRANSFORM, FINISHED
	};

	public final static Collection<String> XML_REQ_TRANSFER_ENCODING = Arrays
			.asList(new String[] { "binary", "8bit" });

	protected static String XML_MIME_TYPE = "text/xml";
	protected static String BINARY_MIME_TYPE = "application/octet-stream";

	/**
	 * The citizen card environment identifier for <code>Server</code> and
	 * <code>UserAgent</code> headers.
	 */
	protected static String CITIZEN_CARD_ENVIRONMENT = "citizen-card-environment/1.2";

	/**
	 * The configuration facade used to access the MOCCA configuration.
	 */
	protected ConfigurationFacade configurationFacade = new ConfigurationFacade();

	public class ConfigurationFacade implements MoccaConfigurationFacade {

		public static final String DATAURLCLIENT_MAXHOPS = "DataURLConnection.MaxHops";

		public static final String DATAURL_WHITELIST = "DataURLConnection.Whitelist";

		public static final String USE_STYLESHEETURL_PROPERTY = "UseStylesheetURL";

		public static final String USE_XADES_1_4 = "UseXAdES14";

		public static final String USE_XADES_1_4_BLACKLIST = "UseXAdES14Blacklist";

		public static final String XADES_1_4_BLACKLIST_URL = "http://www.buergerkarte.at/BKU_XAdES_14_blacklist.txt";

		public static final int XADES_1_4_BLACKLIST_EXPIRY = 60*60*24; //1 day

		public static final String ALLOW_OTHER_REDIRECTS = "AllowOtherRedirects";

		public int getMaxDataUrlHops() {
			return configuration.getInt(DATAURLCLIENT_MAXHOPS, 10);
		}

		public String getProductName() {
			return configuration.getString(
					ConfigurationFactoryBean.MOCCA_IMPLEMENTATIONNAME_PROPERTY,
					"MOCCA");
		}

		public String getProductVersion() {
			return configuration.getString(
					ConfigurationFactoryBean.MOCCA_IMPLEMENTATIONVERSION_PROPERTY,
					"UNKNOWN") + (getUseXAdES14() ? "-X14" : "");
		}

		public String getSignatureLayout() {
			String signatureLayout = configuration
					.getString(ConfigurationFactoryBean.SIGNATURE_LAYOUT_PROPERTY);

			if (getUseXAdES14() && "1.0".equals(signatureLayout))
				signatureLayout = "1.1"; //bump SignatureLayout version to prevent PDF-AS from generating invalid signatures

			return signatureLayout;
		}

		public boolean getEnableStylesheetURL() {
			return configuration
					.getBoolean(USE_STYLESHEETURL_PROPERTY, false);
		}

		public List<String> getDataURLWhitelist() {
			return ConfigurationUtil.getStringListFromObjectList(
				configuration.getList(DATAURL_WHITELIST));
		}

		public boolean hasDataURLWhitelist() {
			return configuration.containsKey(DATAURL_WHITELIST);
		}

		public boolean matchesDataURLWhitelist(String dataURL) {
			List<String> dataURLWhitelist = getDataURLWhitelist();
			log.debug("DataURL Whitelist: " + dataURLWhitelist.toString());
			for (String regExp : dataURLWhitelist) {
				log.debug("Matching " + regExp);
				if (dataURL.matches(regExp))
					return true;
			}
			return false;
		}

		public boolean getUseXAdES14() {
			return configuration.getBoolean(USE_XADES_1_4, true);
		}

		public boolean getAllowOtherRedirects() {
			return configuration.getBoolean(ALLOW_OTHER_REDIRECTS, false);
		}
	}
	
	/**
	 * If null everything is ok and the result is taken from the command invoker.
	 */
	protected SLException bindingProcessorError;
	protected SSLSocketFactory sslSocketFactory;
	protected HostnameVerifier hostnameVerifier;
	protected DataUrlResponse dataUrlResponse;
	protected Map<String, String> headerMap = Collections.EMPTY_MAP;
	protected SLCommand slCommand;
	protected Map<String, FormParameter> formParameterMap = new HashMap<String, FormParameter>();
	protected SLSourceContext srcContex = new SLSourceContext();
	protected SLTargetContext targetContext = new SLTargetContext();
	protected URL srcUrl;
	protected State currentState = State.INIT;
	protected Templates templates = null;
	protected String resultContentType = null;
	protected SLResult slResult = null;
	protected int responseCode = 200;
	protected Map<String, String> responseHeaders = Collections.EMPTY_MAP;
	protected boolean finished = false;

	@Override
	public void setUrlDereferencer(URLDereferencer urlDereferencer) {
		super.setUrlDereferencer(new FormDataURLDereferencer(urlDereferencer,
				this));
	}

	/**
	 * @return the sslSocketFactory
	 */
	public SSLSocketFactory getSslSocketFactory() {
		return sslSocketFactory;
	}

	/**
	 * @param sslSocketFactory
	 *            the sslSocketFactory to set
	 */
	public void setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
		this.sslSocketFactory = sslSocketFactory;
	}

	/**
	 * @return the hostnameVerifier
	 */
	public HostnameVerifier getHostnameVerifier() {
		return hostnameVerifier;
	}

	/**
	 * @param hostnameVerifier
	 *            the hostnameVerifier to set
	 */
	public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
		this.hostnameVerifier = hostnameVerifier;
	}

	protected void sendSTALQuit() {
		log.debug("Sending QUIT command to STAL.");
		List<STALRequest> quit = new ArrayList<STALRequest>(1);
		quit.add(new QuitRequest());
		getSTAL().handleRequest(quit);
	}

	protected String getFormParameterAsString(String formParameterName) {
		FormParameter fp = formParameterMap.get(formParameterName);
		return getFormParameterAsString(fp);
	}

	protected String getFormParameterAsString(FormParameter fp) {
		if (fp == null) {
			return null;
		}
		try {
			return StreamUtil.asString(fp.getFormParameterValue(), HttpUtil
					.getCharset(fp.getFormParameterContentType(), true));
		} catch (IOException e) {
			return null;
		}
	}

	protected String getDataUrl() {
		return getFormParameterAsString(FixedFormParameters.DATAURL);
	}

	protected String getStyleSheetUrl() {
		return getFormParameterAsString(FixedFormParameters.STYLESHEETURL);
	}

	protected List<FormParameter> getFormParameters(String parameterNamePostfix) {
		List<FormParameter> resultList = new ArrayList<FormParameter>();
		for (Iterator<String> fpi = formParameterMap.keySet().iterator(); fpi
				.hasNext();) {
			String paramName = fpi.next();
			if (paramName.endsWith(parameterNamePostfix)) {
				resultList.add(formParameterMap.get(paramName));
			}
		}
		return resultList;
	}

	protected List<FormParameter> getTransferHeaders() {
		return getFormParameters("__");
	}

	protected List<FormParameter> getTransferForms() {
		List<FormParameter> resultList = new ArrayList<FormParameter>();
		for (Iterator<String> fpi = formParameterMap.keySet().iterator(); fpi
				.hasNext();) {
			String paramName = fpi.next();
			if ((paramName.endsWith("_")) && (!paramName.endsWith("__"))) {
				resultList.add(formParameterMap.get(paramName));
			}
		}
		return resultList;
	}

	protected void closeDataUrlConnection() {
		log.debug("Closing data url input stream.");
		if (dataUrlResponse == null) {
			return;
		}
		InputStream is = dataUrlResponse.getStream();
		if (is != null) {
			try {
				is.close();
			} catch (IOException e) {
				log.info("Error closing input stream to dataurl server.", e);
			}
		}
	}

	//----------------------------------------------------------------------------
	// ----------- END CONVENIENCE METHODS -----------

	//----------------------------------------------------------------------------
	// -- BEGIN Methods that handle the http binding activities as defined in the
	// activity diagram --

	protected void init() {
		log.info("Starting Bindingprocessor : {}.", id);
		if (bindingProcessorError != null) {
			log.debug("Detected binding processor error, sending quit command.");
			currentState = State.FINISHED;
		} else if (slCommand == null) {
			log.error("SLCommand not set. (consumeRequest not called?)");
			bindingProcessorError = new SLException(2000);
			currentState = State.FINISHED;
		} else {
			currentState = State.PROCESS;
		}
	}

	protected void processRequest() {
		log.info("Entered State: {}, Processing {}.", State.PROCESS, slCommand.getName());
		SLCommandContext commandCtx = new SLCommandContext(
			getSTAL(),
			new FormDataURLDereferencer(urlDereferencer, this),
			getDataUrl(),
			locale);
		commandInvoker.setCommand(commandCtx, slCommand);
		responseCode = 200;
		responseHeaders = Collections.EMPTY_MAP;
		dataUrlResponse = null;
		try {
			commandInvoker.invoke(srcContex);
		} catch (SLException e) {
			log.info("Failed to invoke command.", e);
			bindingProcessorError = e;
			currentState = State.TRANSFORM;
		}
		if (getDataUrl() != null) {
			log.debug("DataUrl set to: {}.", getDataUrl());
			currentState = State.DATAURL;
		} else {
			log.debug("No data url set.");
			currentState = State.TRANSFORM;
		}
	}

	protected void handleDataUrl() {
		String dataURL = getDataUrl();
		log.info("Entered State: {}, DataURL={}.", State.DATAURL, dataURL);
		try {
			if (configurationFacade.hasDataURLWhitelist()) {
				log.debug("Checking DataURL against whitelist");
				if (!configurationFacade.matchesDataURLWhitelist(dataURL))
				{
					log.error("DataURL doesn't match whitelist");
					throw new SLBindingException(2001);
				}
			}

			DataUrl dataUrl = new DataUrl(dataURL);
			HttpsDataURLConnection conn = (HttpsDataURLConnection) dataUrl.openConnection();
			
			// set user agent and signature layout headers
			conn.setHTTPHeader(HttpUtil.HTTP_HEADER_USER_AGENT, getServerHeaderValue());
			conn.setHTTPHeader(HttpUtil.HTTP_HEADER_SIGNATURE_LAYOUT, getSignatureLayoutHeaderValue());
			conn.setHostnameVerifier(hostnameVerifier);
			conn.setSSLSocketFactory(sslSocketFactory);

			// set transfer headers
			for (FormParameter fp : getTransferHeaders()) {
				String paramString = getFormParameterAsString(fp);
				if (paramString == null) {
					log.error("Got empty transfer header, ignoring this.");
				} else {
					String[] keyVal = paramString.split(":", 2);
					String key = keyVal[0];
					String val = null;
					if (keyVal.length == 2) {
						val = keyVal[1];
						val = val.trim();
					} else {
						log.error("Invalid transfer header encoding: {}.", paramString);
						throw new SLBindingException(2005);
					}
					log.debug("Setting header '{}' to value '{}'.", key, val);
					conn.setHTTPHeader(key, val);
				}
			}

			// set transfer form parameters
			for (FormParameter fp : getTransferForms()) {
				String contentTransferEncoding = null;
				String contentType = fp.getFormParameterContentType();
				String charSet = HttpUtil.getCharset(contentType, false);
				if (charSet != null) {
					contentType = contentType.substring(0, contentType
							.lastIndexOf(HttpUtil.SEPARATOR[0]));
				}
				for (Iterator<String> header = fp.getHeaderNames(); header.hasNext();) {
					if (HttpUtil.CONTENT_TRANSFER_ENCODING
							.equalsIgnoreCase(header.next())) {
						contentTransferEncoding = getFormParameterAsString(fp);
					}
				}
				if (log.isDebugEnabled()) {
					Object[] args = {fp.getFormParameterName(), contentType, charSet, contentTransferEncoding};
					log.debug("Setting form parameter '{}'" +
							" (content-type {}, charset {}, content transfer encoding {})", args);
				}
				conn.setHTTPFormParameter(fp.getFormParameterName(), fp
						.getFormParameterValue(), contentType, charSet,
						contentTransferEncoding);
			}

			// connect
			conn.connect();
			// fetch and set SL result
			targetContext.setTargetIsDataURL(true);
			X509Certificate serverCertificate = null;
			if (conn.getServerCertificates() instanceof X509Certificate[]) {
			  serverCertificate = (X509Certificate) conn.getServerCertificates()[0];
			}
			targetContext.setTargetCertificate(serverCertificate);
			targetContext.setTargetUrl(conn.getURL());
			SLResult result = commandInvoker.getResult(targetContext);

			// transfer result
			conn.transmit(result);

			// process Dataurl response
			dataUrlResponse = conn.getResponse();
			int code = dataUrlResponse.getResponseCode();
			log.debug("Received data url response code: {}.", code);

			if (configurationFacade.getAllowOtherRedirects() &&
					(code >= 301) && (code <= 303))
				code = 307;

			switch (code) {
			case 200:
				String contentType = dataUrlResponse.getContentType();
				log.debug("Got dataurl response content type: {}.", contentType);
				if (contentType != null) {
					if ((contentType.startsWith(HttpUtil.APPLICATION_URL_ENCODED))
							|| (contentType.startsWith(HttpUtil.MULTIPART_FORMDATA))) {
						log.debug("Detected SL Request in dataurl response.");
						// process headers and request
						setHTTPHeaders(dataUrlResponse.getResponseHeaders());
						consumeRequestStream(dataUrlResponse.getUrl(), dataUrlResponse.getStream());
						//TODO check for bindingProcessorError
						closeDataUrlConnection();
						srcContex.setSourceCertificate(serverCertificate);
						srcContex.setSourceIsDataURL(true);
						srcContex.setSourceUrl(conn.getURL());
						currentState = State.PROCESS;
					} else if (((contentType.startsWith(HttpUtil.TXT_HTML))
							|| (contentType.startsWith(HttpUtil.TXT_PLAIN))
							|| (contentType.startsWith(HttpUtil.TXT_XML)))
							&& (dataUrlResponse.isHttpResponseXMLOK())) {
						log.info(
								"Dataurl response matches <ok/> with content type: {}.", contentType);
						currentState = State.TRANSFORM;

					} else if ((contentType.startsWith(HttpUtil.TXT_XML))
							&& (!dataUrlResponse.isHttpResponseXMLOK())) {
						log.debug("Detected text/xml  dataurl response with content != <ok/>");
						headerMap.put(HttpUtil.HTTP_HEADER_CONTENT_TYPE, contentType);
						assignXMLRequest(dataUrlResponse.getStream(), HttpUtil.getCharset(
								contentType, true));
						closeDataUrlConnection();
						srcContex.setSourceCertificate(serverCertificate);
						srcContex.setSourceIsDataURL(true);
						srcContex.setSourceUrl(conn.getURL());
						currentState = State.PROCESS;
						// just to be complete, actually not used
						srcContex.setSourceHTTPReferer(dataUrlResponse.getResponseHeaders()
								.get(HttpUtil.HTTP_HEADER_REFERER));
					} else {
						resultContentType = contentType;
						responseHeaders = dataUrlResponse.getResponseHeaders();
						responseCode = dataUrlResponse.getResponseCode();
						currentState = State.FINISHED;
					}
				} else {
					log.info("Content type not set in dataurl response.");
					closeDataUrlConnection();
					throw new SLBindingException(2007);
				}
				break;

			case 307:
				contentType = dataUrlResponse.getContentType();
				if ((contentType != null) && (contentType.startsWith(HttpUtil.TXT_XML))) {
					log.debug("Received dataurl response code 307 with XML content.");
					String location = dataUrlResponse.getResponseHeaders().get(
							HttpUtil.HTTP_HEADER_LOCATION);
					if (location == null) {
						log.error("Did not get a location header for a 307 data url response.");
						throw new SLBindingException(2003);
					}
					// consumeRequestStream(dataUrlResponse.getStream());
					FormParameterStore fp = new FormParameterStore();
					fp.init(location.getBytes(HttpUtil.DEFAULT_CHARSET),
							FixedFormParameters.DATAURL, null, null);
					formParameterMap.put(FixedFormParameters.DATAURL, fp);
					headerMap.put(HttpUtil.HTTP_HEADER_CONTENT_TYPE, contentType);
					assignXMLRequest(dataUrlResponse.getStream(), HttpUtil.getCharset(
							dataUrlResponse.getContentType(), true));
					closeDataUrlConnection();
					srcContex.setSourceCertificate(serverCertificate);
					srcContex.setSourceIsDataURL(true);
					srcContex.setSourceUrl(conn.getURL());
					currentState = State.PROCESS;
					// just to be complete, actually not used
					srcContex.setSourceHTTPReferer(dataUrlResponse.getResponseHeaders()
							.get(HttpUtil.HTTP_HEADER_REFERER));

					responseHeaders = dataUrlResponse.getResponseHeaders();
					responseCode = dataUrlResponse.getResponseCode();
					break;
				}
				log.debug("Received dataurl response code 307 with non XML content: {}.",
						dataUrlResponse.getContentType());
				// Fall through to 301-303!

			case 301:
			case 302:
			case 303:
				responseHeaders = dataUrlResponse.getResponseHeaders();
				responseCode = dataUrlResponse.getResponseCode();
				resultContentType = dataUrlResponse.getContentType();
				currentState = State.FINISHED;
				break;

			default:
				// issue error
				log.info("Unexpected response code from dataurl server: {}.",
						dataUrlResponse.getResponseCode());
				throw new SLBindingException(2007);
			}

		} catch (SLException slx) {
			bindingProcessorError = slx;
			log.error("Error during dataurl communication.", slx);
			resultContentType = HttpUtil.TXT_XML;
			currentState = State.TRANSFORM;
		} catch (SSLHandshakeException hx) {
			bindingProcessorError = new SLException(2010);
			log.info("Error during dataurl communication.", hx);
			resultContentType = HttpUtil.TXT_XML;
			currentState = State.TRANSFORM;
		} catch (IOException e) {
			bindingProcessorError = new SLBindingException(2001);
			log.error("Error while data url handling", e);
			resultContentType = HttpUtil.TXT_XML;
			currentState = State.TRANSFORM;
			return;
		}
	}

	protected void transformResult() {
		log.info("Entered State: {}.", State.TRANSFORM);
		if (bindingProcessorError != null) {
			resultContentType = HttpUtil.TXT_XML;
		} else if (dataUrlResponse != null) {
			resultContentType = dataUrlResponse.getContentType();
		} else {
			targetContext.setTargetIsDataURL(false);
			targetContext.setTargetUrl(srcUrl);
			try {
				slResult = commandInvoker.getResult(targetContext);
				resultContentType = slResult.getMimeType();
				log.debug("Successfully got SLResult from commandinvoker, setting mimetype to: {}.",
								resultContentType);
			} catch (SLException e) {
				log.info("Cannot get result from invoker:", e);
				bindingProcessorError = new SLException(6002);
				resultContentType = HttpUtil.TXT_XML;
			}
		}
		String stylesheetURL = getStyleSheetUrl();
		if (configurationFacade.getEnableStylesheetURL())
			templates = getTemplates(stylesheetURL);
		else
		{
			templates = null;
			if (stylesheetURL != null)
				log.info("Ignoring StylesheetURL ({})", stylesheetURL);
		}
		if (templates != null) {
			log.debug("Output transformation required.");
			resultContentType = templates.getOutputProperties().getProperty("media-type");
			log.debug("Got media type from stylesheet: {}.", resultContentType);
			if (resultContentType == null) {
				log.debug("Setting to default text/xml result conent type.");
				resultContentType = "text/xml";
			}
			log.debug("Deferring sytylesheet processing.");
		}
		currentState = State.FINISHED;
	}

	protected void finished() {
		log.info("Entered State: {}.", State.FINISHED);
		if (bindingProcessorError != null) {
			log.debug("Binding processor error, sending quit command.");
			resultContentType = HttpUtil.TXT_XML;
		}
		sendSTALQuit();
		log.info("Terminating Bindingprocessor : {}.", id);
		finished = true;
	}

	// -- END Methods that handle the http binding activities as defined in the
	// activity diagram --
	//----------------------------------------------------------------------------

	public String getServerHeaderValue() {
		return CITIZEN_CARD_ENVIRONMENT + " "
				+ configurationFacade.getProductName() + "/"
				+ configurationFacade.getProductVersion();
	}

	public String getSignatureLayoutHeaderValue() {
		return configurationFacade.getSignatureLayout();
	}

	/**
	 * Sets the headers of the SL Request. IMPORTANT: make sure to set all headers
	 * before invoking {@link #consumeRequestStream(String, InputStream)}
	 * 
	 * @param aHeaderMap
	 *          if null all header will be cleared.
	 */
	@Override
	public void setHTTPHeaders(Map<String, String> aHeaderMap) {
		headerMap = new HashMap<String, String>();
		// ensure lowercase keys
		if (aHeaderMap != null) {
			for (String s : aHeaderMap.keySet()) {
				if (s != null) {
					headerMap.put(s.toLowerCase(), aHeaderMap.get(s));
					if (s.equalsIgnoreCase(HttpUtil.HTTP_HEADER_REFERER)) {
						String referer = aHeaderMap.get(s);
						log.debug("Got referer header: {}.", referer);
						srcContex.setSourceHTTPReferer(referer);
					}
				}
			}
		}
	}

	public void setSourceCertificate(X509Certificate aCert) {
		srcContex.setSourceCertificate(aCert);
	}

	/**
	 * The HTTPBindingProcessor does not handle redirect URLs. It only provides
	 * the parameter.
	 * 
	 * @return null if redirect url is not set.
	 */
	public String getRedirectURL() {
		String redirectURL = getFormParameterAsString(FixedFormParameters.REDIRECTURL);
		log.debug("Evaluating redirectURL: " + redirectURL);
		if (redirectURL == null || redirectURL.trim().isEmpty() || redirectURL.contains("\r") || redirectURL.contains("\n") ||
				redirectURL.contains("<") || redirectURL.toLowerCase().contains("javascript:"))
			return null;
		return redirectURL;
	}

	public String getFormDataContentType(String aParameterName) {
		FormParameter fp = formParameterMap.get(aParameterName);
		if (fp != null) {
			return fp.getFormParameterContentType();
		}
		return null;
	}

	public InputStream getFormData(String aParameterName) {
		FormParameter fp = formParameterMap.get(aParameterName);
		if (fp != null) {
			final String enc = fp.getHeaderValue("Content-Transfer-Encoding");
			if (enc == null || "binary".equals(enc) || "8bit".equals(enc)) {
				return fp.getFormParameterValue();
			} else if ("base64".equals(enc)) {
				return new Base64InputStream(fp.getFormParameterValue());
			} else {
				return new InputStream() {
					@Override
					public int read() throws IOException {
						throw new IOException("Content-Transfer-Encoding : "
								+ enc + " is not supported.");
					}
				};
			}
		}
		return null;
	}

	protected void assignXMLRequest(InputStream is, String charset)
			throws IOException, SLException {
		Reader r = new InputStreamReader(is, charset);
		StreamSource source = new StreamSource(r);
		slCommand = slCommandFactory.createSLCommand(source);
		log.info("XMLRequest={}. Created new command: {}.",
				slCommand.getName(), slCommand.getClass().getName());
	}

	@Override
	public void process() {
		boolean done = false;
		int hopcounter = 0;
		if (bindingProcessorError != null) {
			currentState = State.FINISHED;
		}
		try {
			while (!done) {
				try {
					switch (currentState) {
					case INIT:
						init();
						break;
					case PROCESS:
						processRequest();
						break;
					case DATAURL:
						handleDataUrl();
						if (++hopcounter > configurationFacade.getMaxDataUrlHops()) {
							log.error("Maximum number ({}) of dataurl hops reached.", 
									configurationFacade.getMaxDataUrlHops());
							bindingProcessorError = new SLBindingException(2000);
							currentState = State.FINISHED;
						}
						break;
					case TRANSFORM:
						transformResult();
						break;
					case FINISHED:
						done = true;
						finished();
						break;
					}
				} catch (RuntimeException rte) {
					throw rte;
				} catch (Exception e) {
					log.error("Caught unexpected exception.", e);
					responseCode = 200;
					resultContentType = HttpUtil.TXT_XML;
					responseHeaders = Collections.EMPTY_MAP;
					bindingProcessorError = new SLException(2000);
					currentState = State.FINISHED;
				}
			}
		} catch (Throwable t) {
			log.error("Caught unexpected exception.", t);
			responseCode = 200;
			resultContentType = HttpUtil.TXT_XML;
			responseHeaders = Collections.EMPTY_MAP;
			bindingProcessorError = new SLException(2000);
			currentState = State.FINISHED;
		}
		log.debug("Terminated http binding processor.");
		finished = true;
	}

	@Override
	public void consumeRequestStream(String url, InputStream is) {
		try {
			this.srcUrl = new URL(url);
			srcContex.setSourceUrl(srcUrl);
			srcContex.setSourceIsDataURL(false);
			log.debug("Start consuming request stream.");
			FormParameter redirectURL = formParameterMap.get(FixedFormParameters.REDIRECTURL);
			formParameterMap.clear();
			if (redirectURL != null)
				formParameterMap.put(FixedFormParameters.REDIRECTURL, redirectURL);
			String ct = headerMap
					.get(HttpUtil.HTTP_HEADER_CONTENT_TYPE.toLowerCase());
			if (ct == null) {
				log.info("No content type set in http header.");
				throw new SLBindingException(2006);
			}
			InputDecoder id = InputDecoderFactory.getDecoder(ct, is);
			if (id == null) {
				log.error("Cannot get inputdecoder for content type {}.", ct);
				throw new SLException(2006);
			}
			for (Iterator<FormParameter> fpi = id.getFormParameterIterator(); fpi
					.hasNext();) {
				FormParameter fp = fpi.next();
				log.debug("Got request parameter with name: {}.", fp.getFormParameterName());
				if (fp.getFormParameterName().equals(FixedFormParameters.XMLREQUEST)) {
					log.debug("Creating XML Request.");
					for (Iterator<String> headerIterator = fp.getHeaderNames(); headerIterator
							.hasNext();) {
						String headerName = headerIterator.next();
						if (HttpUtil.CONTENT_TRANSFER_ENCODING.equalsIgnoreCase(headerName)) {
							String transferEncoding = fp.getHeaderValue(headerName);
							log.debug("Got transfer encoding for xmlrequest: {}.",
									transferEncoding);
							if (XML_REQ_TRANSFER_ENCODING.contains(transferEncoding)) {
								log.debug("Supported transfer encoding: {}.", transferEncoding);
							} else {
								log.error("Transfer encoding '{}' not supported.", transferEncoding);
								throw new SLBindingException(2005);
							}
						}
					}
					String charset = HttpUtil.getCharset(ct, true);
					assignXMLRequest(fp.getFormParameterValue(), charset);
				}
				else {
					if (fp.getFormParameterName().equals(FixedFormParameters.REDIRECTURL)
							&& formParameterMap.containsKey(FixedFormParameters.REDIRECTURL)) {
						log.info("Not updating previously set RedirectURL!");
					}
					else {
						FormParameterStore fps = new FormParameterStore();
						fps.init(fp);
						//if (!fps.isEmpty()) {
							log.debug("Setting form parameter: {}.", fps.getFormParameterName());
							formParameterMap.put(fps.getFormParameterName(), fps);
						//}
					}
				}
			}
			if (slCommand == null) {
				throw new SLBindingException(2004);
			}
		} catch (SLException slx) {
			log.info("Error while consuming input stream.", slx);
			bindingProcessorError = slx;
		} catch (Throwable t) {
			log.info("Error while consuming input stream.", t);
			bindingProcessorError = new SLException(2000);
		} finally {
			try {
				if (is.read() != -1) {
					log.warn("Request input stream not completely read.");
					while (is.read() != -1)
						;
				}
				log.debug("Finished consuming request stream.");
			} catch (IOException e) {
				log.error("Failed to read request input stream.", e);
			}
		}
	}

	@Override
	public String getResultContentType() {
		return resultContentType;
	}

	protected Templates getTemplates(String styleSheetURL) {
		if (styleSheetURL == null) {
			log.debug("Stylesheet URL not set.");
			return null;
		}
		try {
			TransformerFactory factory = TransformerFactory.newInstance();
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			factory.setURIResolver(new URIResolverAdapter(urlDereferencer));
			StreamData sd = urlDereferencer.dereference(styleSheetURL);
			return factory.newTemplates(new StreamSource(sd.getStream()));
		} catch (Exception ex) {
			log.info("Cannot instantiate transformer.", ex);
			bindingProcessorError = new SLException(2002);
			return null;
		}
	}

	protected void handleBindingProcessorError(OutputStream os, String encoding,
			Templates templates) throws IOException {
		log.debug("Writing error as result.");
		ErrorResultImpl error = new ErrorResultImpl(bindingProcessorError, locale);
		Writer writer = writeXMLDeclarationAndProcessingInstruction(os, encoding);
		error.writeTo(new StreamResult(writer), templates, true);
	}

	protected Writer writeXMLDeclarationAndProcessingInstruction(OutputStream os, String encoding) throws IOException {
		if (encoding == null) {
			encoding = HttpUtil.DEFAULT_CHARSET;
		}
		OutputStreamWriter writer = new OutputStreamWriter(os, encoding);
		writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
		writer.write("<?xml-stylesheet type=\"text/css\" href=\"errorresponse.css\"?>\n");
		return writer;
	}
	
	@Override
	public void writeResultTo(OutputStream os, String encoding)
			throws IOException {
		if (encoding == null) {
			encoding = HttpUtil.DEFAULT_CHARSET;
		}
		if (bindingProcessorError != null) {
			log.debug("Detected error in binding processor, writing error as result.");
			handleBindingProcessorError(os, encoding, templates);
			return;
		} else if (dataUrlResponse != null) {
			log.debug("Writing data url response as result.");
			String charEnc = HttpUtil.getCharset(dataUrlResponse.getContentType(),
					true);
			InputStreamReader isr = new InputStreamReader(
					dataUrlResponse.getStream(), charEnc);
			OutputStreamWriter osw = new OutputStreamWriter(os, encoding);
			if (templates == null) {
				StreamUtil.copyStream(isr, osw);
			} else {
				try {
					Transformer transformer = templates.newTransformer();
					transformer.transform(new StreamSource(isr), new StreamResult(osw));
				} catch (TransformerException e) {
					log.error("Exception occured during result transformation.", e);
					// bindingProcessorError = new SLException(2008);
					// handleBindingProcessorError(os, encoding, null);
					return;
				}
			}
			osw.flush();
			isr.close();
		} else if (slResult == null) {
			// result not yet assigned -> must be a cancel
			bindingProcessorError = new SLException(6001);
			handleBindingProcessorError(os, encoding, templates);
			return;
		} else {
			log.debug("Getting result from invoker.");
			boolean fragment = false;
			Writer writer;
			if (slResult instanceof ErrorResult) {
				writer = writeXMLDeclarationAndProcessingInstruction(os,
						encoding);
				fragment = true;
			} else {
				writer = new OutputStreamWriter(os, encoding);
			}
			slResult.writeTo(new StreamResult(writer), templates, fragment);
			writer.flush();
		}
	}

	/**
	 * The response code from the dataurl server or 200 if no dataurl server
	 * created the result
	 * 
	 * @return
	 */
	@Override
	public int getResponseCode() {
		return responseCode;
	}

	/**
	 * All headers from the data url server in case of a direct forward from the
	 * dataurl server.
	 * 
	 * @return
	 */
	@Override
	public Map<String, String> getResponseHeaders() {
		LinkedHashMap<String, String> headers = new LinkedHashMap<String, String>();
		headers.put(HttpUtil.HTTP_HEADER_SERVER, getServerHeaderValue());
		headers.put(HttpUtil.HTTP_HEADER_SIGNATURE_LAYOUT,
				getSignatureLayoutHeaderValue());
		headers.putAll(responseHeaders);
		return headers;
	}

	public boolean isFinished() {
		return finished;
	}

}
