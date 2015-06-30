/*
 * Copyright 2015 Datentechnik Innovation and Prime Sign GmbH, Austria
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

package at.gv.egiz.bku.slcommands.impl;

import static org.junit.Assert.*;
import iaik.xml.crypto.XSecProvider;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import at.buergerkarte.namespaces.securitylayer._1_2_3.BulkResponseType;
import at.gv.egiz.bku.slcommands.BulkSignatureCommand;
import at.gv.egiz.bku.slcommands.BulkSignatureResult;
import at.gv.egiz.bku.slcommands.SLCommand;
import at.gv.egiz.bku.slcommands.SLCommandContext;
import at.gv.egiz.bku.slcommands.SLCommandFactory;
import at.gv.egiz.bku.slcommands.SLResult;
import at.gv.egiz.bku.slexceptions.SLCommandException;
import at.gv.egiz.bku.slexceptions.SLRequestException;
import at.gv.egiz.bku.slexceptions.SLRuntimeException;
import at.gv.egiz.bku.slexceptions.SLVersionException;
import at.gv.egiz.bku.utils.urldereferencer.URLDereferencer;
import at.gv.egiz.slbinding.SLUnmarshaller;
import at.gv.egiz.stal.STAL;
import at.gv.egiz.stal.STALFactory;

public class BulkSignatureCommandImplTest {

  protected static ApplicationContext appCtx;
  private SLCommandFactory factory;

  private STAL stal;
  
  private URLDereferencer urlDereferencer;
  
  @BeforeClass
  public static void setUpClass() {
    appCtx = new ClassPathXmlApplicationContext("at/gv/egiz/bku/slcommands/testApplicationContext.xml");
    XSecProvider.addAsProvider(true);
  }
  
  @Before
  public void setUp() throws JAXBException {
    Object bean = appCtx.getBean("slCommandFactory");
    assertTrue(bean instanceof SLCommandFactory);
    
    factory = (SLCommandFactory) bean;
    
    bean = appCtx.getBean("stalFactory");
    assertTrue(bean instanceof STALFactory);
    
    stal = ((STALFactory) bean).createSTAL();
    
    bean = appCtx.getBean("urlDereferencer");
    assertTrue(bean instanceof URLDereferencer);
    
    urlDereferencer = (URLDereferencer) bean;
  }
  
	@Test
	public void testCreateCMSSignatureRequest() throws SLCommandException, SLRuntimeException, SLRequestException,
			SLVersionException, JAXBException {
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream("at/gv/egiz/bku/slcommands/bulksignaturerequest/BulkSignatureRequest.xml");
		assertNotNull(inputStream);

		SLCommand command = factory.createSLCommand(new StreamSource(new InputStreamReader(inputStream)));
		assertTrue(command instanceof BulkSignatureCommand);

		Unmarshaller unmarshaller = factory.getJaxbContext().createUnmarshaller();

		SLCommandContext context = new SLCommandContext(stal, urlDereferencer, null);
		SLResult result = command.execute(context);

		assertTrue(result instanceof BulkSignatureResult);

		BulkSignatureResult bulkResult = (BulkSignatureResult) result;

		bulkResult.getContent();
		Object response = unmarshaller.unmarshal(bulkResult.getContent());
		assertTrue(response instanceof BulkResponseType);
		BulkResponseType bulkResponse = (BulkResponseType) response;

		assertNotNull(bulkResponse.getCreateSignatureResponse());
		assertEquals(bulkResponse.getCreateSignatureResponse().size(), 2);

		assertEquals(bulkResponse.getCreateSignatureResponse().get(1).getCreateCMSSignatureResponse().getCMSSignature(),"MIIFygYJKoZIhvcNAQcCoIIFuzCCBbcCAQExCzAJBgUrDgMCGgUAMAsGCSqGSIb3DQEHAaCCA60wggOpMIIDEqADAgECAgZ1TDn0eiIwDQYJKoZIhvcNAQEFBQAwQDEiMCAGA1UEAxMZSUFJSyBUZXN0IEludGVybWVkaWF0ZSBDQTENMAsGA1UEChMESUFJSzELMAkGA1UEBhMCQVQwHhcNMDgwNzI0MTM0NTA0WhcNMTAwNzI0MTM0NTA0WjBVMRIwEAYDVQQqEwlYWFhEYWdtYXIxEzARBgNVBAQMClhYWEvDtnJuZXIxHTAbBgNVBAMMFFhYWERhZ21hciBYWFhLw7ZybmVyMQswCQYDVQQGEwJBVDCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAuzuLn6o5xAMIfOwZmxrvD7WVllx3/w0qQWlIXvH3qdrr1M0N6PoRY2SkE858BYJvwtMPtwlhZk+xMh7yNEfF0Ar9rVuO/ahL5TbUgLkIrOmT/Yn6wI6OUlmmgf2nXPuYaA/kMSCdg4WoomSMsNQq4OfO/YQAhLtsXQ1y7RKd3KMCAwEAAaOCAZcwggGTMA4GA1UdDwEB/wQEAwIGwDAMBgNVHRMBAf8EAjAAMB0GA1UdDgQWBBTTgWe2ksSldD3oPVu/1Oa6ErIu5DBQBgNVHR8ESTBHMEWgQ6BBhj9odHRwOi8vY2EuaWFpay50dWdyYXouYXQvY2Fwc28vY3Jscy9JQUlLVGVzdF9JbnRlcm1lZGlhdGVDQS5jcmwwgaoGCCsGAQUFBwEBBIGdMIGaMEoGCCsGAQUFBzABhj5odHRwOi8vY2EuaWFpay50dWdyYXouYXQvY2Fwc28vT0NTUD9jYT1JQUlLVGVzdF9JbnRlcm1lZGlhdGVDQTBMBggrBgEFBQcwAoZAaHR0cDovL2NhLmlhaWsudHVncmF6LmF0L2NhcHNvL2NlcnRzL0lBSUtUZXN0X0ludGVybWVkaWF0ZUNBLmNlcjA0BgNVHREELTArgSl4eHhkYWdhbWFyLnh4eGtvZXJuZXJAbm9uLmV4aXN0ZW5kLmRvbWFpbjAfBgNVHSMEGDAWgBRool4R2t4vgGtERL+N+mU2gSSnbzANBgkqhkiG9w0BAQUFAAOBgQBsN3XKQn6r4HmrmTy8+/qVApLzZGm7/Lpabo7G6Ek7/9RtpocHVUFCdWYwntBaIg1dYDbUkjNwSNu/dJi6grYE34EHKib2jOZB/2lndgTj4mkxkS1TFRV7Vq5jc0nI9sACrUUZ7WUrmN9BOGTLy5y+MNYhzysdpW8lZ2wUGnsp6zGCAeUwggHhAgEBMEowQDEiMCAGA1UEAxMZSUFJSyBUZXN0IEludGVybWVkaWF0ZSBDQTENMAsGA1UEChMESUFJSzELMAkGA1UEBhMCQVQCBnVMOfR6IjAJBgUrDgMCGgUAoIHyMBYGBgQAjUUCATEMDAp0ZXh0L3BsYWluMBgGCSqGSIb3DQEJAzELBgkqhkiG9w0BBwEwHAYJKoZIhvcNAQkFMQ8XDTE1MDYzMDExMDIxOFowIwYJKoZIhvcNAQkEMRYEFBvlEGRlPbmPAe61tMhrwi6aOeXpMHsGCyqGSIb3DQEJEAIMMWwwajBoMGYEFG/vsW2O5DoVw+TKSRuPDigjrUTCME4wRKRCMEAxIjAgBgNVBAMTGUlBSUsgVGVzdCBJbnRlcm1lZGlhdGUgQ0ExDTALBgNVBAoTBElBSUsxCzAJBgNVBAYTAkFUAgZ1TDn0eiIwDQYJKoZIhvcNAQEFBQAEgYAadNdxVokjA686xRWeal3ou8pbtj0FCpfkrDdJbkJZmVDGX+FhIlmNz1K1XOralQc3SCNO2Iwak3JthA2RVgio4QBqt9u2RLtci5n2b4JmtzdWIlq4GoEftJ3PA5vhrVwimkVwxVnDD95J5Bi6+TKJIzIGlWAHuS2XDiVIyv247w==");
		
		result.writeTo(new StreamResult(System.out), false);

	}

  
  
  
}
