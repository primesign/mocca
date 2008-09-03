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
				"https://129.27.142.20:80/index.html");
		RuleResult rr = onlyPeerChecker.check(ctx);
		assertTrue(rr.matchFound());

		ctx = new AccessCheckerContext(null, null,
				"https://129.27.14.20:80/index.html");
		rr = onlyPeerChecker.check(ctx);
		assertFalse(rr.matchFound());
		
		onlyPeerChecker.setPeerId(".*.iaik..*", PEER_TYPE.HOST);
		ctx = new AccessCheckerContext(null, null,
		"https://129.27.142.20:80/index.html");
		rr = onlyPeerChecker.check(ctx);
		assertTrue(rr.matchFound());
		
		onlyPeerChecker.setPeerId("129.27.142..*", PEER_TYPE.IP);
		ctx = new AccessCheckerContext(null, null, "https://www.iaik.tugraz.at:80/");
		rr = onlyPeerChecker.check(ctx);
		assertTrue(rr.matchFound());
	}

}
