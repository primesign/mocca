package at.gv.egiz.bku.spring;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.configuration.Configuration;
import org.springframework.beans.factory.FactoryBean;

import at.gv.egiz.bku.conf.MoccaConfigurationFacade;
import at.gv.egiz.bku.utils.urldereferencer.FileURLProtocolHandlerImpl;
import at.gv.egiz.bku.utils.urldereferencer.URLDereferencerImpl;

public class URLDereferencerFactoryBean implements FactoryBean {

  private HostnameVerifier hostnameVerifier;
  private SSLSocketFactory sslSocketFactory;

  protected final ConfigurationFacade configurationFacade = new ConfigurationFacade();

  public class ConfigurationFacade implements MoccaConfigurationFacade {

      private Configuration configuration;
      public static final String ENABLE_FILEURIS = "enableFileURIs";

      public boolean isEnableFileURIs() {
          return configuration.getBoolean(ENABLE_FILEURIS, false);
      }
      
  }
  
  public void setConfiguration(Configuration configuration) {
    configurationFacade.configuration = configuration;
  }
  
	@Override
	public Object getObject() throws Exception {
		
	  URLDereferencerImpl urlDereferencer = URLDereferencerImpl.getInstance();
    urlDereferencer.setHostnameVerifier(hostnameVerifier);
    urlDereferencer.setSSLSocketFactory(sslSocketFactory);

	  if(!configurationFacade.isEnableFileURIs()) {
	    urlDereferencer.registerHandler(FileURLProtocolHandlerImpl.FILE, new FileURLProtocolHandlerImpl());
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
	
}
