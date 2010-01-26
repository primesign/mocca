package at.gv.egiz.bku.accesscontroller;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import at.gv.egiz.bku.slcommands.InfoboxReadCommand;
import at.gv.egiz.bku.slcommands.SLCommandContext;
import at.gv.egiz.bku.slcommands.SLResult;
import at.gv.egiz.bku.slcommands.impl.InfoboxReadCommandImpl;
import at.gv.egiz.bku.slexceptions.SLCommandException;
import at.gv.egiz.bku.slexceptions.SLException;
import static org.junit.Assert.*;

public class ConfigTest {

	public final static String RESOURCE1 = "at/gv/egiz/bku/accesscontroller/AccessControlConfig.xml";
	public final static String RESOURCE2 = "at/gv/egiz/bku/accesscontroller/SimpleChainTest.xml";

	static class MyInfoBox implements InfoboxReadCommand {
		private String domainId;
		private String boxId;
		private String name;

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
		public SLResult execute() {
			return null;
		}
		
		public void setName(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return "InfoboxReadRequest";
		}

		@Override
		public void init(SLCommandContext ctx, Object unmarshalledRequest)
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
