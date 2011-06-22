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

import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.bku.slcommands.SLCommand;
import at.gv.egiz.bku.slcommands.SLSourceContext;
import at.gv.egiz.bku.slcommands.SLTargetContext;
import at.gv.egiz.bku.slexceptions.SLException;

/**
 * Facade for the access controller
 */
public class SecurityManagerFacade {

	private final Logger log = LoggerFactory.getLogger(SecurityManagerFacade.class);

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
			} catch (SLException e) {
				log.error("Check failed.", e);
				return false;
			}
		} else {
			log.warn("No input chain defined.");
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
			} catch (SLException e) {
				log.error("Check failed.", e);
				return false;
			}
		} else {
			log.warn("No output chain defined.");
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
			log.error("Failed to initialize AccessControllerFactory.", e);
		}
		inputFilter = fab.getChainChecker(AccessControllerFactory.INPUT_CHAIN);
		outputFilter = fab.getChainChecker(AccessControllerFactory.OUTPUT_CHAIN);
	}
}
