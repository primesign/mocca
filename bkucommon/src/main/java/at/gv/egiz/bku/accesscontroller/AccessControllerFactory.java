package at.gv.egiz.bku.accesscontroller;

import java.util.Hashtable;

public class AccessControllerFactory {

	private static AccessControllerFactory instance;

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

}
