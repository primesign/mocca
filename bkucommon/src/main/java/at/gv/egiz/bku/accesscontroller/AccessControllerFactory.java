/*
* Copyright 2008 Federal Chancellery Austria and
* Graz University of Technology
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package at.gv.egiz.bku.accesscontroller;

import java.io.InputStream;
import java.util.Hashtable;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
	private static Log log = LogFactory.getLog(AccessControllerFactory.class);
	private static JAXBContext jaxbContext;
	public static String INPUT_CHAIN = "InputChain";
	public static String OUTPUT_CHAIN = "OutputChain";

	static {
		try {
			jaxbContext = JAXBContext.newInstance(ObjectFactory.class.getPackage()
					.getName());
		} catch (JAXBException e) {
			log.fatal("Cannot init jaxbContext", e);
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
		log.debug("Found " + chainList.size() + " chains in config");
		for (Chain chain : chainList) {
			log.trace("Creating chain: " + chain.getId());
			ChainChecker cc = createChainChecker(chain.getId(), false);
			List<Rule> ruleList = chain.getRules().getRule();
			log
					.debug("Found " + ruleList.size() + " rules in chain "
							+ chain.getId());
			for (Rule rule : ruleList) {
				log.trace("Creating rule: " + rule.getId());
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
					log.trace("Checking reference to chain: "+rule.getChainId());
					if (getChainChecker(rule.getChainId()) == null) {
						throw new SLRuntimeException("Invalid reference to unknown chain: "+rule.getChainId());
					}
				}
			}
		}
	}

}
