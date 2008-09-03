package at.gv.egiz.bku.accesscontroller;

import javax.xml.bind.JAXBException;

import org.junit.Test;

public class ConfigTest {

	public final static String RESOURCE = "at/gv/egiz/bku/accesscontroller/AccessControlConfig.xml";

	@Test
	public void testUnmarshall() throws JAXBException {
		AccessControllerFactory.getInstance().init(
				getClass().getClassLoader().getResourceAsStream(RESOURCE));
	}

}
