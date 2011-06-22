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


package at.gv.egiz.bku.accesscontroller;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import at.gv.egiz.bku.slcommands.InfoboxReadCommand;
import at.gv.egiz.bku.slcommands.SLCommandContext;
import at.gv.egiz.bku.slcommands.SLResult;
import at.gv.egiz.bku.slexceptions.SLCommandException;
import at.gv.egiz.bku.slexceptions.SLException;
import static org.junit.Assert.*;

public class ConfigTest {

	public final static String RESOURCE1 = "at/gv/egiz/bku/accesscontroller/AccessControlConfig.xml";
	public final static String RESOURCE2 = "at/gv/egiz/bku/accesscontroller/SimpleChainTest.xml";

	static class MyInfoBox implements InfoboxReadCommand {
		private String domainId;
		private String boxId;

		public MyInfoBox(String identifier, String domainId) {
			this.boxId = identifier;
			this.domainId = domainId;
		}

		@Override
		public String getIdentityLinkDomainId() {
			return domainId;
		}

		@Override
		public String getInfoboxIdentifier() {
			return boxId;
		}

		@Override
		public SLResult execute(SLCommandContext commandContext) {
			return null;
		}
		
		public void setName(String name) {
		}

		@Override
		public String getName() {
			return "InfoboxReadRequest";
		}

		@Override
		public void init(Object unmarshalledRequest)
				throws SLCommandException {
		}
	}

	@Test
	public void testUnmarshall() throws JAXBException {
		AccessControllerFactory.getInstance().init(
				getClass().getClassLoader().getResourceAsStream(RESOURCE1));
	}

	@Test
	public void testBasicFunction() throws JAXBException, SLException {
		AccessControllerFactory.getInstance().init(
				getClass().getClassLoader().getResourceAsStream(RESOURCE2));
		ChainChecker cc = AccessControllerFactory.getInstance().getChainChecker(
				"InputFilter");
		assertNotNull(cc);

		AccessCheckerContext ctx = new AccessCheckerContext(null,
				AuthenticationClass.ANONYMOUS, null);
		ChainResult cr = cc.check(ctx);
		assertFalse(cr.matchFound());

		ctx = new AccessCheckerContext(new MyInfoBox("IdentityLink", "hansi"),
				AuthenticationClass.CERTIFIED, null);
		cr = cc.check(ctx);
		assertTrue(cr.matchFound());

		ctx = new AccessCheckerContext(new MyInfoBox("Something", "hansi"),
				AuthenticationClass.CERTIFIED, null);
		cr = cc.check(ctx);
		assertFalse(cr.matchFound());
		
		MyInfoBox mib = new MyInfoBox("IdentityLink", "seppl");
		mib.setName("ReadInfoboxSchickSchnack");
		ctx = new AccessCheckerContext(mib,	AuthenticationClass.CERTIFIED, null);
		cr = cc.check(ctx);
		assertTrue(cr.matchFound());
		assertTrue(cr.getAction()==Action.ALLOW);
		
		mib = new MyInfoBox("IdentityLink", null);
		mib.setName("ReadInfoboxSchickSchnack");
		ctx = new AccessCheckerContext(mib,	AuthenticationClass.CERTIFIED, null);
		cr = cc.check(ctx);
		assertTrue(cr.matchFound());
		assertTrue(cr.getAction()==Action.DENY);
	}

}
