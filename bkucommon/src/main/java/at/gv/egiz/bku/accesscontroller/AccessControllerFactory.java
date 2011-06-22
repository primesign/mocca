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
import java.util.Hashtable;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.bku.accesscontrol.config.AccessControl;
import at.gv.egiz.bku.accesscontrol.config.Chain;
import at.gv.egiz.bku.accesscontrol.config.Command;
import at.gv.egiz.bku.accesscontrol.config.ObjectFactory;
import at.gv.egiz.bku.accesscontrol.config.Param;
import at.gv.egiz.bku.accesscontrol.config.Rule;
import at.gv.egiz.bku.accesscontroller.RuleChecker.PEER_TYPE;
import at.gv.egiz.bku.slexceptions.SLRuntimeException;

public class AccessControllerFactory {

	private static AccessControllerFactory instance = new AccessControllerFactory();
    private static JAXBContext jaxbContext;
	private final Logger log = LoggerFactory.getLogger(AccessControllerFactory.class);
	public static String INPUT_CHAIN = "InputChain";
	public static String OUTPUT_CHAIN = "OutputChain";

	static {
		try {
			jaxbContext = JAXBContext.newInstance(ObjectFactory.class.getPackage()
					.getName());
		} catch (JAXBException e) {
		    Logger log = LoggerFactory.getLogger(AccessControllerFactory.class);
			log.error("Cannot init jaxbContext.", e);
		}
	}

	private Hashtable<String, ChainChecker> chainTable = new Hashtable<String, ChainChecker>();

	private AccessControllerFactory() {
	}

	public static AccessControllerFactory getInstance() {
		return instance;
	}

	/**
	 * 
	 * @param id
	 * @return null if there is no chain with this id.
	 */
	public ChainChecker getChainChecker(String id) {
		return chainTable.get(id);
	}

	public ChainChecker createChainChecker(String id, boolean register) {
		ChainChecker cc = new ChainChecker(id);
		if (register) {
			chainTable.put(id, cc);
		}
		return cc;
	}

	public void registerChainChecker(ChainChecker cc) {
		chainTable.put(cc.getId(), cc);
	}

	public CommandParamChecker createParamChecker(String cmd) {
		if ((cmd != null) && (cmd.startsWith("Infobox"))) {
			return new InfoboxParamChecker();
		} else {
			return null;
		}
	}

	public RuleChecker createRuleChecker(Rule rule) {
		RuleChecker rc;
		rc = new RuleChecker(rule.getId());
		Command cmd = rule.getCommand();
		if (cmd != null) {
			rc.setCommandName(cmd.getName());
			for (Param p : cmd.getParam()) {
				rc.addParameter(p.getName(), p.getValue());
			}
		}
		rc.setAuthenticationClass(rule.getAuthClass());
		if (rule.getIPv4Address() != null) {
			rc.setPeerId(rule.getIPv4Address(), PEER_TYPE.IP);
		} else if (rule.getDomainName() != null) {
			rc.setPeerId(rule.getDomainName(), PEER_TYPE.HOST);
		} else if (rule.getURL() != null) {
			rc.setPeerId(rule.getURL(), PEER_TYPE.URL);
		}
		rc.setAction(rule.getAction().getRuleAction());
		rc.setChainId(rule.getAction().getChainRef());
		rc.setUserAction(rule.getUserInteraction());
		return rc;
	}

	public void init(InputStream is) throws JAXBException {
		chainTable.clear();
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		AccessControl ac = (AccessControl) unmarshaller.unmarshal(is);
		List<Chain> chainList = ac.getChains().getChain();
		log.debug("Found {} chains in config.", chainList.size());
		for (Chain chain : chainList) {
			log.trace("Creating chain: {}.", chain.getId());
			ChainChecker cc = createChainChecker(chain.getId(), false);
			List<Rule> ruleList = chain.getRules().getRule();
			log.debug("Found {} rules in chain {}.", ruleList.size(), chain.getId());
			for (Rule rule : ruleList) {
				log.trace("Creating rule: {}.", rule.getId());
				cc.addRule(createRuleChecker(rule));
			}
			registerChainChecker(cc);
		}
		validate();
	}
	
	private void validate() {
		for (ChainChecker chain : chainTable.values()) {
			for (RuleChecker rule : chain.getRules()) {
				if (rule.getChainId() != null) {
					log.trace("Checking reference to chain: {}.", rule.getChainId());
					if (getChainChecker(rule.getChainId()) == null) {
						throw new SLRuntimeException("Invalid reference to unknown chain: "+rule.getChainId());
					}
				}
			}
		}
	}

}
