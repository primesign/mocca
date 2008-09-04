package at.gv.egiz.bku.accesscontroller;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.bku.slexceptions.SLException;

public class ChainChecker implements AccessChecker {
	private static Log log = LogFactory.getLog(ChainChecker.class);
	
	private String id;
	private List<RuleChecker> rules = new LinkedList<RuleChecker>();
	
	/**
	 * 
	 * @param id must not be null
	 */
	public ChainChecker(String id) {
		if (id == null) {
			throw new NullPointerException("Id argument must not be null");
		}
		this.id = id;
	}
	

	public String getId() {
		return id;
	}

	public void addRule(RuleChecker rule) {
		if (rule != null) {
			rules.add(rule);
		}
	}
	
	public List<RuleChecker> getRules() {
		return Collections.unmodifiableList(rules);
	}

	@Override
	public ChainResult check(AccessCheckerContext checkCtx) throws SLException {
		log.debug("Processing chain: "+id);
		for (RuleChecker rule : rules) {
			log.trace("Checking rule: "+rule.getId());
			RuleResult result = rule.check(checkCtx);
			if (result.matchFound()) {
				if (result.getDelegateChainId() != null) {
					// process chain
					ChainChecker cc = AccessControllerFactory.getInstance().getChainChecker(result.getDelegateChainId());
					if (cc == null) {
						log.error("Cannot delegate to chain. Unknown chain id: "+result.getDelegateChainId());
						throw new SLException(4000);
					}
					ChainResult cr = cc.check(checkCtx);
					if (cr.matchFound()) {
						return cr;
					}
					// if chain does not contain matching rule
					// cont. here.		
					} else {
						return result;
					}
			}
		}
		log.debug("Did not find a matching rule here");
		return new ChainResult(null, null, false);
	}

	
	
}
