package at.gv.egiz.bku.spring;

import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.configuration.Configuration;
import org.springframework.beans.factory.FactoryBean;

import at.gv.egiz.bku.conf.MoccaConfigurationFacade;
import at.gv.egiz.bku.utils.urldereferencer.FileURLProtocolHandlerImpl;
import at.gv.egiz.bku.utils.urldereferencer.HTTPURLProtocolHandlerImpl;
import at.gv.egiz.bku.utils.urldereferencer.URLDereferencerImpl;
import at.gv.egiz.bku.utils.urldereferencer.URLProtocolHandler;

public class URLDereferencerFactoryBean implements FactoryBean {

	URLDereferencerImpl urlDereferencer;
	
  private HostnameVerifier hostnameVerifier;
  private SSLSocketFactory sslSocketFactory;

  
  private Map<String, URLProtocolHandler> handlerMap = new HashMap<String, URLProtocolHandler>();

	
  protected final ConfigurationFacade configurationFacade = new ConfigurationFacade();

  public class ConfigurationFacade implements MoccaConfigurationFacade {

      private Configuration configuration;
      public static final String DISABLE_FILEURI = "disableFileURI";

      public boolean isDisableFileURI() {
          return configuration.getBoolean(DISABLE_FILEURI, true);
      }
  }
  
  
  public void setConfiguration(Configuration configuration) {
    configurationFacade.configuration = configuration;
}
  
	@Override
	public Object getObject() throws Exception {
		
		if(urlDereferencer == null) {
			
			 registerHandlers();
			
			urlDereferencer = new URLDereferencerImpl();
			urlDereferencer.setHostnameVerifier(hostnameVerifier);
			urlDereferencer.setSSLSocketFactory(sslSocketFactory);
			urlDereferencer.setHandlerMap(handlerMap);
			
		}
		return urlDereferencer;
	}

	@Override
	public Class<URLDereferencerImpl> getObjectType() {
		return URLDereferencerImpl.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public HostnameVerifier getHostnameVerifier() {
		return hostnameVerifier;
	}

	public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
		this.hostnameVerifier = hostnameVerifier;
	}

	public SSLSocketFactory getSslSocketFactory() {
		return sslSocketFactory;
	}

	public void setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
		this.sslSocketFactory = sslSocketFactory;
	}
	
	
  protected void registerHandlers() {
    URLProtocolHandler handler = new HTTPURLProtocolHandlerImpl();
    for (String proto : HTTPURLProtocolHandlerImpl.PROTOCOLS) {
      handlerMap.put(proto, handler);
    }
    
    if(!configurationFacade.isDisableFileURI()) {
  	FileURLProtocolHandlerImpl fileHandler = new FileURLProtocolHandlerImpl();
  	handlerMap.put(FileURLProtocolHandlerImpl.FILE, fileHandler);
    }
  }
  

	
	
}
