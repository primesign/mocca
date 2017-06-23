package at.gv.egiz.bku.online.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.dom.DOMUtils;


public class StalSecurityFilter implements Filter {

	private static Logger log = LoggerFactory.getLogger(StalSecurityFilter.class);
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		log.info("Initialize STAL Service security filter");
		
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		if (request instanceof HttpServletRequest) {
			try {
				MoccaHttpServletRequestWrapper stalHttpReq = new MoccaHttpServletRequestWrapper((HttpServletRequest) request);		
				
				if (stalHttpReq.isInputStreamAvailable()) {
					log.trace("Validate STAL request ... ");
					DOMUtils.validateXMLAgainstXXEAndSSRFAttacks(stalHttpReq.getInputStream());
					log.trace("Validate of STAL request completed");
					
				}
			
				chain.doFilter(stalHttpReq, response);
				
			} catch (XMLStreamException e) {
				log.error("XML data validation FAILED with msg: " + e.getMessage(), e);
				sendErrorToResponse(e, response);
					
			} catch (IOException e) {
				log.error("Can not process InputStream from STAL request");
				sendErrorToResponse(e, response);
				
			}				
						
		} else {
			log.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			log.warn("STAL request is processed WITHOUT security checks!!!!");
			log.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			chain.doFilter(request, response);
			
		}
	}

	@Override
	public void destroy() {
		
	}

	private void sendErrorToResponse(Exception e, ServletResponse response) throws IOException {
		if (response instanceof HttpServletResponse) {
			((HttpServletResponse)response).sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			
		} else
			log.error("Can not response with http error message");
		
	}
		
}
