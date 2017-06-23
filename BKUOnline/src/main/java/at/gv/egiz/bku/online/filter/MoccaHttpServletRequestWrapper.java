package at.gv.egiz.bku.online.filter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import at.gv.egiz.bku.binding.HttpUtil;


public class MoccaHttpServletRequestWrapper extends HttpServletRequestWrapper {

	private static Logger log = LoggerFactory.getLogger(MoccaHttpServletRequestWrapper.class);
	
	private final byte[] body;
	private final String charset;
	
	public MoccaHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
		super(request);
				
		String ct = request.getHeader(HttpUtil.HTTP_HEADER_CONTENT_TYPE.toLowerCase());
		charset = HttpUtil.getCharset(ct, true);
		
		byte[] result = null;
		try {
			 result = IOUtils.toByteArray(request.getReader(), charset);
						
		} catch (IOException e) {
			log.error("Can not copy input stream!!!!!", e);
			throw new IOException("Can not copy input stream!!!!!", e);
			
		} finally {
			body = result;
			
		}
	}
	
	public boolean isInputStreamAvailable() {
		return (body != null && body.length > 0);
		
	}
	
	@Override
	public BufferedReader getReader() throws IOException {
		return new BufferedReader(new InputStreamReader(getInputStream(), charset));
	    
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body);
		return new ServletInputStream() {

			@Override
			public int read() throws IOException {
				return byteArrayInputStream.read();
			}
        
		};
					
	}
}
