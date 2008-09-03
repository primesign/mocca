package at.gv.egiz.bku.accesscontroller;

import at.gv.egiz.bku.slexceptions.SLException;

public interface AccessChecker {
	public ChainResult check(AccessCheckerContext checkCtx) throws SLException;
}
