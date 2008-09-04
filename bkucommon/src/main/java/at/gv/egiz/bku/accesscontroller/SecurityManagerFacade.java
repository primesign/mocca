package at.gv.egiz.bku.accesscontroller;

import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.bku.slcommands.SLCommand;
import at.gv.egiz.bku.slcommands.SLSourceContext;
import at.gv.egiz.bku.slcommands.SLTargetContext;

/**
 * Facade for the access controller
 */
public class SecurityManagerFacade {

	private static Log log = LogFactory.getLog(SecurityManagerFacade.class);

	private boolean allowUnmatched = false;
	private ChainChecker inputFilter = null;
	private ChainChecker outputFilter = null;
	
	public boolean mayInvokeCommand(SLCommand cmd, SLSourceContext ctx) {
		if (inputFilter != null) {
			AuthenticationClass ac = AuthenticationClassifier.getAuthenticationClass(
					ctx.isSourceIsDataURL(), ctx.getSourceUrl(), ctx
							.getSourceCertificate());
			AccessCheckerContext acc = new AccessCheckerContext(cmd, ac, ctx
					.getSourceUrl().toString());
			try {
				ChainResult cr = inputFilter.check(acc);
				if (cr.matchFound()) {
					if (cr.getAction() == Action.ALLOW) {
						return true;
					} else {
						return false;
					}
				} else {
					return allowUnmatched;
				}
			} catch (Exception e) {
				log.error(e);
				return false;
			}
		} else {
			log.warn("No input chain defined");
			return allowUnmatched;
		}
	}

	public boolean maySendResult(SLCommand cmd, SLTargetContext ctx) {
		if (outputFilter != null) {
			AuthenticationClass ac = AuthenticationClassifier.getAuthenticationClass(
					ctx.isTargetIsDataURL(), ctx.getTargetUrl(), ctx
							.getTargetCertificate());
			AccessCheckerContext acc = new AccessCheckerContext(cmd, ac, ctx
					.getTargetUrl().toString());
			try {
				ChainResult cr = outputFilter.check(acc);
				if (cr.matchFound()) {
					if (cr.getAction() == Action.ALLOW) {
						return true;
					} else {
						return false;
					}
				} else {
					return allowUnmatched;
				}
			} catch (Exception e) {
				log.error(e);
				return false;
			}
		} else {
			log.warn("No output chain defined");
			return allowUnmatched;
		}
	}

	/**
	 * Default policy if not match was found
	 * 
	 * @param allow
	 */
	public void setAllowUnmatched(boolean allow) {
		this.allowUnmatched = allow;
	}

	public void init(InputStream is) {
		inputFilter = null;
		outputFilter = null;
		AccessControllerFactory fab = AccessControllerFactory.getInstance();
		try {
			fab.init(is);
		} catch (JAXBException e) {
			log.error(e);
		}
		inputFilter = fab.getChainChecker(AccessControllerFactory.INPUT_CHAIN);
		outputFilter = fab.getChainChecker(AccessControllerFactory.OUTPUT_CHAIN);
	}
}
