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
import java.io.Reader;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.net.ssl.SSLHandshakeException;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.bku.slcommands.SLCommand;
import at.gv.egiz.bku.slcommands.SLCommandContext;
import at.gv.egiz.bku.slcommands.SLCommandFactory;
import at.gv.egiz.bku.slcommands.SLCommandInvoker;
import at.gv.egiz.bku.slcommands.SLResult;
import at.gv.egiz.bku.slcommands.SLSourceContext;
import at.gv.egiz.bku.slcommands.SLTargetContext;
import at.gv.egiz.bku.slcommands.impl.ErrorResultImpl;
import at.gv.egiz.bku.slexceptions.SLBindingException;
import at.gv.egiz.bku.slexceptions.SLException;
import at.gv.egiz.bku.slexceptions.SLRuntimeException;
import at.gv.egiz.bku.utils.StreamUtil;
import at.gv.egiz.bku.utils.binding.Protocol;
import at.gv.egiz.bku.utils.urldereferencer.FormDataURLSupplier;
import at.gv.egiz.bku.utils.urldereferencer.SimpleFormDataContextImpl;
import at.gv.egiz.bku.utils.urldereferencer.StreamData;
import at.gv.egiz.bku.utils.urldereferencer.URIResolverAdapter;
import at.gv.egiz.bku.utils.urldereferencer.URLDereferencer;
import at.gv.egiz.bku.utils.urldereferencer.URLDereferencerContext;
import at.gv.egiz.stal.QuitRequest;
import at.gv.egiz.stal.STALRequest;

/**
 * Class performing the HTTP binding as defined by the CCE specification.
 * Currently a huge monolithic class.
 * 
 * @TODO refactor
 */
@SuppressWarnings("unchecked")
public class HTTPBindingProcessor extends AbstractBindingProcessor implements
		FormDataURLSupplier {

	private static Log log = LogFactory.getLog(HTTPBindingProcessor.class);

	private static enum State {
		INIT, PROCESS, DATAURL, TRANSFORM, FINISHED
	};

	public final static Collection<String> XML_REQ_TRANSFER_ENCODING = Arrays
			.asList(new String[] { "binary" });

	/**
	 * Defines the maximum number of dataurl connects that are allowed within a
	 * single SL Request processing.
	 */
	protected static int MAX_DATAURL_HOPS = 10;

	protected static String XML_MIME_TYPE = "text/xml";
	protected static String BINARY_MIME_TYPE = "application/octet-stream";

	/**
	 * If null everything is ok and the result is taken from the command invoker.
	 */
	protected SLException bindingProcessorError;
	protected SLCommandInvoker commandInvoker;
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
	protected Locale locale = Locale.getDefault();
	protected boolean finished = false;

	/**
	 * 
	 * @param id
	 *          may be null. In this case a new session id will be created.
	 * @param cmdInvoker
	 *          must not be null;
	 */
	public HTTPBindingProcessor(String id, SLCommandInvoker cmdInvoker, URL source) {
		super(id);
		this.srcUrl = source;
		Protocol protocol = Protocol.fromString(source.getProtocol());
		if ((protocol != Protocol.HTTP) && (protocol != Protocol.HTTPS)) {
			throw new SLRuntimeException("Protocol not supported: " + protocol);
		}
		if (cmdInvoker == null) {
			throw new NullPointerException("Commandinvoker cannot be set to null");
		}
		commandInvoker = cmdInvoker;
		srcContex.setSourceUrl(source);
		srcContex.setSourceIsDataURL(false);
	}

	//----------------------------------------------------------------------------
	// ----------- BEGIN CONVENIENCE METHODS -----------

	protected void sendSTALQuit() {
		log.info("Sending QUIT command to STAL");
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
		log.debug("Closing data url input stream");
		if (dataUrlResponse == null) {
			return;
		}
		InputStream is = dataUrlResponse.getStream();
		if (is != null) {
			try {
				is.close();
			} catch (IOException e) {
				log.info("Error closing input stream to dataurl server:" + e);
			}
		}
	}

	//----------------------------------------------------------------------------
	// ----------- END CONVENIENCE METHODS -----------

	//----------------------------------------------------------------------------
	// -- BEGIN Methods that handle the http binding activities as defined in the
	// activity diagram --

	protected void init() {
		log.info("Starting Bindingprocessor in Thread: "
				+ Thread.currentThread().getId());
		if (bindingProcessorError != null) {
			log.debug("Detected binding processor error, sending quit command");
			// sendSTALQuit();
			currentState = State.FINISHED;
		} else if (slCommand == null) {
			log.error("SLCommand not set (consumeRequest not called ??)");
			bindingProcessorError = new SLException(2000);
			// sendSTALQuit();
			currentState = State.FINISHED;
		} else {
			currentState = State.PROCESS;
		}
	}

	protected void processRequest() {
		log.debug("Entered State: " + State.PROCESS);
		log.debug("Processing command: " + slCommand);
		commandInvoker.setCommand(slCommand);
		responseCode = 200;
		responseHeaders = Collections.EMPTY_MAP;
		dataUrlResponse = null;
		try {
			commandInvoker.invoke(srcContex);
		} catch (SLException e) {
			log.info("Caught exception: " + e);
			bindingProcessorError = e;
			currentState = State.TRANSFORM;
		}
		if (getDataUrl() != null) {
			log.debug("Data Url set to: " + getDataUrl());
			currentState = State.DATAURL;
		} else {
			log.debug("No data url set");
			currentState = State.TRANSFORM;
		}
	}

	protected void handleDataUrl() {
		log.debug("Entered State: " + State.DATAURL);
		try {
			DataUrl dataUrl = new DataUrl(getDataUrl());
			DataUrlConnection conn = dataUrl.openConnection();

			// set transfer headers
			for (FormParameter fp : getTransferHeaders()) {
				String paramString = getFormParameterAsString(fp);
				if (paramString == null) {
					log.error("Got empty transfer header, ignoring this");
				} else {
					String[] keyVal = paramString.split(":", 2);
					String key = keyVal[0];
					String val = null;
					if (keyVal.length == 2) {
						val = keyVal[1];
					}
					val = val.trim();
					log.debug("Setting header " + key + " to value " + val);
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
							.lastIndexOf(HttpUtil.SEPERATOR[0]));
				}
				for (Iterator<String> header = fp.getHeaderNames(); header.hasNext();) {
					if (HttpUtil.CONTENT_TRANSFER_ENCODING
							.equalsIgnoreCase(header.next())) {
						contentTransferEncoding = getFormParameterAsString(fp);
					}
				}
				log.debug("Setting form: " + fp.getFormParameterName()
						+ " contentType: " + contentType + " charset: " + charSet
						+ " contentTransferEncoding: " + contentTransferEncoding);
				conn.setHTTPFormParameter(fp.getFormParameterName(), fp
						.getFormParameterValue(), contentType, charSet,
						contentTransferEncoding);
			}

			// connect
			conn.connect();
			// fetch and set SL result
			targetContext.setTargetIsDataURL(true);
			targetContext.setTargetCertificate(conn.getServerCertificate());
			targetContext.setTargetUrl(conn.getUrl());
			SLResult result = commandInvoker.getResult(targetContext);

			// transfer result
			conn.transmit(result);

			// process Dataurl response
			dataUrlResponse = conn.getResponse();
			log.debug("Received data url response code: "
					+ dataUrlResponse.getResponseCode());

			switch (dataUrlResponse.getResponseCode()) {
			case 200:
				String contentType = dataUrlResponse.getContentType();
				log.debug("Got dataurl response content type: " + contentType);
				if (contentType != null) {
					if ((contentType.startsWith(HttpUtil.APPLICATION_URL_ENCODED))
							|| (contentType.startsWith(HttpUtil.MULTIPART_FOTMDATA))) {
						log.debug("Detected SL Request in dataurl response");
						// process headers and request
						setHTTPHeaders(dataUrlResponse.getResponseHeaders());
						consumeRequestStream(dataUrlResponse.getStream());
						closeDataUrlConnection();
						srcContex.setSourceCertificate(conn.getServerCertificate());
						srcContex.setSourceIsDataURL(true);
						srcContex.setSourceUrl(conn.getUrl());
						currentState = State.PROCESS;
					} else if (((contentType.startsWith(HttpUtil.TXT_HTML))
							|| (contentType.startsWith(HttpUtil.TXT_PLAIN)) || (contentType
							.startsWith(HttpUtil.TXT_XML)))
							&& (dataUrlResponse.isHttpResponseXMLOK())) {
						log.info("Dataurl response matches <ok/> with content type: "
								+ contentType);
						currentState = State.TRANSFORM;

					} else if ((contentType.startsWith(HttpUtil.TXT_XML))
							&& (!dataUrlResponse.isHttpResponseXMLOK())) {
						log
								.debug("Detected text/xml  dataurl response with content != <ok/>");
						headerMap.put(HttpUtil.HTTP_HEADER_CONTENT_TYPE, contentType);
						assignXMLRequest(dataUrlResponse.getStream(), HttpUtil.getCharset(
								contentType, true));
						closeDataUrlConnection();
						srcContex.setSourceCertificate(conn.getServerCertificate());
						srcContex.setSourceIsDataURL(true);
						srcContex.setSourceUrl(conn.getUrl());
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
					log.debug("Content type not set in dataurl response");
					closeDataUrlConnection();
					throw new SLBindingException(2007);
				}

				break;
			case 307:
				contentType = dataUrlResponse.getContentType();
				if ((contentType != null) && (contentType.startsWith(HttpUtil.TXT_XML))) {
					log.debug("Received dataurl response code 307 with XML content");
					String location = dataUrlResponse.getResponseHeaders().get(
							HttpUtil.HTTP_HEADER_LOCATION);
					if (location == null) {
						log
								.error("Did not get a location header for a 307 data url response");
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
					srcContex.setSourceCertificate(conn.getServerCertificate());
					srcContex.setSourceIsDataURL(true);
					srcContex.setSourceUrl(conn.getUrl());
					currentState = State.PROCESS;
					// just to be complete, actually not used
					srcContex.setSourceHTTPReferer(dataUrlResponse.getResponseHeaders()
							.get(HttpUtil.HTTP_HEADER_REFERER));

				} else {
					log.debug("Received dataurl response code 307 non XML content: "
							+ dataUrlResponse.getContentType());
					resultContentType = dataUrlResponse.getContentType();
					currentState = State.FINISHED;
				}
				responseHeaders = dataUrlResponse.getResponseHeaders();
				responseCode = dataUrlResponse.getResponseCode();
				break;

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
				log.info("Unexpected response code from dataurl server: "
						+ dataUrlResponse.getResponseCode());
				throw new SLBindingException(2007);
			}

		} catch (SLException slx) {
			bindingProcessorError = slx;
			log.error("Error during dataurl communication");
			resultContentType = HttpUtil.TXT_XML;
			currentState = State.TRANSFORM;
		} catch (SSLHandshakeException hx) {
			bindingProcessorError = new SLException(2010);
			log.info("Error during dataurl communication", hx);
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
		log.debug("Entered State: " + State.TRANSFORM);
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
				log
						.debug("Successfully got SLResult from commandinvoker, setting mimetype to: "
								+ resultContentType);
			} catch (SLException e) {
				log.info("Cannot get result from invoker:", e);
				bindingProcessorError = new SLException(6002);
				resultContentType = HttpUtil.TXT_XML;
			}
		}
		templates = getTemplates(getStyleSheetUrl());
		if (templates != null) {
			log.debug("Output transformation required");
			resultContentType = templates.getOutputProperties().getProperty("media-type");
			log.debug("Got media type from stylesheet: " + resultContentType);
			if (resultContentType == null) {
				log.debug("Setting to default text/xml result conent type");
				resultContentType = "text/xml";
			}
			log.debug("Deferring sytylesheet processing");
		}
		currentState = State.FINISHED;
	}

	protected void finished() {
		log.debug("Entered State: " + State.FINISHED);
		if (bindingProcessorError != null) {
			log.debug("Binding processor error, sending quit command");
			resultContentType = HttpUtil.TXT_XML;
		}
		sendSTALQuit();
		log.info("Terminating Bindingprocessor; Thread: "
				+ Thread.currentThread().getId());
		finished = true;
	}

	// -- END Methods that handle the http binding activities as defined in the
	// activity diagram --
	//----------------------------------------------------------------------------

	/**
	 * Sets the headers of the SL Request. IMPORTANT: make sure to set all headers
	 * before invoking {@link #consumeRequestStream(InputStream)}
	 * 
	 * @param aHeaderMap
	 *          if null all header will be cleared.
	 */
	public void setHTTPHeaders(Map<String, String> aHeaderMap) {
		headerMap = new HashMap<String, String>();
		// ensure lowercase keys
		if (aHeaderMap != null) {
			for (String s : aHeaderMap.keySet()) {
				if (s != null) {
					headerMap.put(s.toLowerCase(), aHeaderMap.get(s));
					if (s.equalsIgnoreCase(HttpUtil.HTTP_HEADER_REFERER)) {
						String referer = aHeaderMap.get(s);
						log.debug("Got referer header: " + referer);
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
		return getFormParameterAsString(FixedFormParameters.REDIRECTURL);
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
			return fp.getFormParameterValue();
		}
		return null;
	}

	protected void assignXMLRequest(InputStream is, String charset)
			throws IOException, SLException {
		Reader r = new InputStreamReader(is, charset);
		StreamSource source = new StreamSource(r);
		SLCommandContext commandCtx = new SLCommandContext();
		commandCtx.setSTAL(getSTAL());
		commandCtx.setURLDereferencerContext(new SimpleFormDataContextImpl(this));
		commandCtx.setLocale(locale);
		slCommand = SLCommandFactory.getInstance().createSLCommand(source,
				commandCtx);
		log.debug("Created new command: " + slCommand);
	}

	@Override
	public void run() {
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
						if (++hopcounter > MAX_DATAURL_HOPS) {
							log.error("Maximum number of dataurl hops reached");
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
				} catch (Exception t) {
					log.error("Caught unexpected exception", t);
					responseCode = 200;
					resultContentType = HttpUtil.TXT_XML;
					responseHeaders = Collections.EMPTY_MAP;
					bindingProcessorError = new SLException(2000);
					currentState = State.FINISHED;
				}
			}
		} catch (Throwable t) {
			log.error("Caught unexpected exception", t);
			responseCode = 200;
			resultContentType = HttpUtil.TXT_XML;
			responseHeaders = Collections.EMPTY_MAP;
			bindingProcessorError = new SLException(2000);
			currentState = State.FINISHED;
		}
		log.debug("Terminated http binding processor");
		finished = true;
	}

	@Override
	public void consumeRequestStream(InputStream is) {
		try {
			log.debug("Start consuming request stream");
			formParameterMap.clear();
			String cl = headerMap
					.get(HttpUtil.HTTP_HEADER_CONTENT_TYPE.toLowerCase());
			if (cl == null) {
				log.info("No content type set in http header");
				throw new SLBindingException(2006);
			}
			InputDecoder id = InputDecoderFactory.getDecoder(cl, is);
			id.setContentType(cl);
			if (id == null) {
				log.error("Cannot get inputdecoder for is");
				throw new SLException(2006);
			}
			for (Iterator<FormParameter> fpi = id.getFormParameterIterator(); fpi
					.hasNext();) {
				FormParameter fp = fpi.next();
				log.debug("Got request parameter with name: "
						+ fp.getFormParameterName());
				if (fp.getFormParameterName().equals(FixedFormParameters.XMLREQUEST)) {
					log.debug("Creating XML Request");
					for (Iterator<String> headerIterator = fp.getHeaderNames(); headerIterator
							.hasNext();) {
						String headerName = headerIterator.next();
						if (HttpUtil.CONTENT_TRANSFER_ENCODING.equalsIgnoreCase(headerName)) {
							String transferEncoding = fp.getHeaderValue(headerName);
							log.debug("Got transfer encoding for xmlrequest: "
									+ transferEncoding);
							if (XML_REQ_TRANSFER_ENCODING.contains(transferEncoding)) {
								log.debug("Supported transfer encoding: " + transferEncoding);
							} else {
								log
										.error("Transferencoding not supported: "
												+ transferEncoding);
								throw new SLBindingException(2005);
							}
						}
					}
					String charset = HttpUtil.getCharset(cl, true);
					assignXMLRequest(fp.getFormParameterValue(), charset);
				} else {
					FormParameterStore fps = new FormParameterStore();
					fps.init(fp);
					if (!fps.isEmpty()) {
						log.debug("Setting form parameter: " + fps.getFormParameterName());
						formParameterMap.put(fps.getFormParameterName(), fps);
					}
				}
			}
			if (slCommand == null) {
				throw new SLBindingException(2004);
			}
			if (is.read() != -1) {
				log.error("Request input stream not completely read");
				// consume rest of stream, should never occur
				throw new SLRuntimeException(
						"request input stream not consumed till end");
			}
		} catch (SLException slx) {
			log.info("Error while consuming input stream " + slx);
			bindingProcessorError = slx;
		} catch (Throwable t) {
			log.info("Error while consuming input stream " + t, t);
			bindingProcessorError = new SLException(2000);
		} finally {
			try {
				while (is.read() != -1)
					;
			} catch (IOException e) {
				log.error(e);
			}
		}
	}

	@Override
	public String getResultContentType() {
		return resultContentType;
	}

	protected Templates getTemplates(String styleSheetURL) {
		if (styleSheetURL == null) {
			log.debug("Stylesheet URL not set");
			return null;
		}
		try {
			URLDereferencerContext urlCtx = new SimpleFormDataContextImpl(this);
			URIResolver resolver = new URIResolverAdapter(URLDereferencer
					.getInstance(), urlCtx);
			TransformerFactory factory = TransformerFactory.newInstance();
			factory.setURIResolver(resolver);
			StreamData sd = URLDereferencer.getInstance().dereference(styleSheetURL,
					urlCtx);
			return factory.newTemplates(new StreamSource(sd.getStream()));
		} catch (Exception ex) {
			log.info("Cannot instantiate transformer", ex);
			bindingProcessorError = new SLException(2002);
			return null;
		}
	}

	protected void handleBindingProcessorError(OutputStream os, String encoding,
			Templates templates) throws IOException {
		log.debug("Writing error as result");
		ErrorResultImpl error = new ErrorResultImpl(bindingProcessorError, locale);
		error.writeTo(new StreamResult(new OutputStreamWriter(os, encoding)), templates);
	}

	@Override
	public void writeResultTo(OutputStream os, String encoding)
			throws IOException {
		if (encoding == null) {
			encoding = HttpUtil.DEFAULT_CHARSET;
		}
		if (bindingProcessorError != null) {
			log.debug("Detected error in binding processor, writing error as result");
			handleBindingProcessorError(os, encoding, templates);
			return;
		} else if (dataUrlResponse != null) {
			log.debug("Writing data url response  as result");
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
					log.fatal("Exception occured during result transformation", e);
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
			log.debug("Getting result from invoker");
			OutputStreamWriter osw = new OutputStreamWriter(os, encoding);
			slResult.writeTo(new StreamResult(osw), templates);
			osw.flush();
		}
	}

	/**
	 * The response code from the dataurl server or 200 if no dataurl server
	 * created the result
	 * 
	 * @return
	 */
	public int getResponseCode() {
		return responseCode;
	}

	/**
	 * All headers from the data url server in case of a direct forward from the
	 * dataurl server.
	 * 
	 * @return
	 */
	public Map<String, String> getResponseHeaders() {
		return responseHeaders;
	}

	@Override
	public void setLocale(Locale locale) {
		if (locale == null) {
			throw new NullPointerException("Locale must not be set to null");
		}
		this.locale = locale;
	}

	@Override
  public boolean isFinished() {
    return finished;
  }
}