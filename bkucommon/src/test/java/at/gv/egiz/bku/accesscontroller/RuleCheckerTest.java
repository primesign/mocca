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

import org.junit.Before;
import org.junit.Test;

import at.gv.egiz.bku.accesscontroller.RuleChecker.PEER_TYPE;
import at.gv.egiz.bku.slcommands.impl.InfoboxReadCommandImpl;
import at.gv.egiz.bku.slcommands.impl.NullOperationCommandImpl;
import static org.junit.Assert.*;

public class RuleCheckerTest {

	protected RuleChecker onlyAuthChecker;
	protected RuleChecker onlyCmdChecker;
	protected RuleChecker onlyPeerChecker;

	@Before
	public void setUp() {
		onlyAuthChecker = new RuleChecker("OnlyAuthChecker");
		onlyAuthChecker.setAction("allow");
		onlyAuthChecker.setUserAction("none");
		onlyAuthChecker.setAuthenticationClass("pseudoanonymous");
		onlyCmdChecker = new RuleChecker("OnlyCmdChecker");
		onlyCmdChecker.setAction("allow");
		onlyCmdChecker.setCommandName("InfoboxReadRequest");
		onlyPeerChecker = new RuleChecker("OnlyPeerChecker");
		onlyPeerChecker.setAction("allow");
		onlyPeerChecker.setPeerId("https://129.27.142..*", PEER_TYPE.URL);
	}

	@Test
	public void testAuthClass() {
		AccessCheckerContext ctx = new AccessCheckerContext(null,
				AuthenticationClass.ANONYMOUS, null);
		RuleResult rr = onlyAuthChecker.check(ctx);
		assertFalse(rr.matchFound());
		ctx = new AccessCheckerContext(null, AuthenticationClass.PSEUDO_ANONYMOUS,
				null);
		rr = onlyAuthChecker.check(ctx);
		assertTrue(rr.matchFound());
		ctx = new AccessCheckerContext(null, AuthenticationClass.CERTIFIED, null);
		rr = onlyAuthChecker.check(ctx);
		assertTrue(rr.matchFound());
	}

	@Test
	public void testCmd() {
		AccessCheckerContext ctx = new AccessCheckerContext(
				new InfoboxReadCommandImpl(), null, null);
		RuleResult rr = onlyCmdChecker.check(ctx);
		assertTrue(rr.matchFound());
		onlyCmdChecker.setCommandName("Info.*");
		rr = onlyCmdChecker.check(ctx);
		assertTrue(rr.matchFound());
		ctx = new AccessCheckerContext(new NullOperationCommandImpl(), null, null);
		rr = onlyCmdChecker.check(ctx);
		assertFalse(rr.matchFound());
		onlyCmdChecker.setCommandName(".*");
		rr = onlyCmdChecker.check(ctx);
		assertTrue(rr.matchFound());
	}

	@Test
	public void testPeerId() {
		AccessCheckerContext ctx = new AccessCheckerContext(null, null,
				"https://129.27.142.55:80/index.html");
		RuleResult rr = onlyPeerChecker.check(ctx);
		assertTrue(rr.matchFound());

		ctx = new AccessCheckerContext(null, null,
				"https://129.27.14.20:80/index.html");
		rr = onlyPeerChecker.check(ctx);
		assertFalse(rr.matchFound());
		
		onlyPeerChecker.setPeerId(".*.buergerkarte..*", PEER_TYPE.HOST);
		ctx = new AccessCheckerContext(null, null,
		"https://129.27.142.55:80/index.html");
		rr = onlyPeerChecker.check(ctx);
		assertTrue(rr.matchFound());
		
		onlyPeerChecker.setPeerId("129.27.142..*", PEER_TYPE.IP);
		ctx = new AccessCheckerContext(null, null, "https://www.buergerkarte.at:80/");
		rr = onlyPeerChecker.check(ctx);
		assertTrue(rr.matchFound());
	}

}
