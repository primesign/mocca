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
import at.gv.egiz.bku.accesscontrol.config.Rule;
import at.gv.egiz.bku.slcommands.impl.InfoboxReadCommandImpl;
import at.gv.egiz.bku.slexceptions.SLRuntimeException;

public class AccessControllerFactory {

	private static AccessControllerFactory instance = new AccessControllerFactory();
	private static Log log = LogFactory.getLog(AccessControllerFactory.class);
	private static JAXBContext jaxbContext;

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
	
	public RuleChecker createRuleChecker(Rule rule) {
		RuleChecker rc;
		Command cmd = rule.getCommand();
		if (cmd != null) {
			if ((cmd.getParam() != null) && (cmd.getParam().size()>0)) {
				if (cmd.getName().startsWith("Infobox")) {
					rc = new InfoboxRuleChecker(rule.getId());
				} else {
					throw new SLRuntimeException("Cannot handle parameters for command "+cmd.getName());
				}
			} else {
				rc = new RuleChecker(rule.getId());
			}
		} else {
			rc = new RuleChecker(rule.getId());
		}
		// FIXME TODO cont. here
		
		
	return  rc;	
	}
	
	
	public void init(InputStream is) throws JAXBException {
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		AccessControl ac = (AccessControl) unmarshaller.unmarshal(is);
		List<Chain> chainList = ac.getChains().getChain();
		log.debug("Found "+chainList.size()+" chains in config");
		for (Chain chain : chainList) {
			List<Rule> ruleList = chain.getRules().getRule();
			log.debug("Found "+ruleList.size()+" rules in chain "+chain.getId());
			for (Rule rule : ruleList) {
				//rule.g
			}
		}
		
	}

}
