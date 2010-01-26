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
package at.gv.egiz.bku.binding;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.bku.accesscontroller.SecurityManagerFacade;
import at.gv.egiz.bku.slcommands.SLCommand;
import at.gv.egiz.bku.slcommands.SLCommandInvoker;
import at.gv.egiz.bku.slcommands.SLResult;
import at.gv.egiz.bku.slcommands.SLSourceContext;
import at.gv.egiz.bku.slcommands.SLTargetContext;
import at.gv.egiz.bku.slexceptions.SLException;

/**
 * This class implements the entry point for the CCEs security management.
 * 
 */
public class SLCommandInvokerImpl implements SLCommandInvoker {

	private static Log log = LogFactory.getLog(SLCommandInvokerImpl.class);

	protected SLCommand command;
	protected SLResult result;
	protected SecurityManagerFacade securityManager;

	/**
	 * Invokes a sl command.
	 * 
	 * @throws SLException
	 */
	public void invoke(SLSourceContext aContext) throws SLException {
		if (securityManager == null) {
			log.warn("Security policy not implemented yet, invoking command: "
					+ command);
			result = command.execute();
		} else {
			if (securityManager.mayInvokeCommand(command, aContext)) {
				result = command.execute();
			} else {
				throw new SLException(6002);
			}
		}
	}

	public SLResult getResult(SLTargetContext aContext) throws SLException {
		if (securityManager == null) {
			log
					.warn("Security policy not implemented yet, getting result of command: "
							+ command);
			return result;
		} else {
			if (securityManager.maySendResult(command, aContext)) {
				return result;
			} else {
				throw new SLException(6002);
			}
		}
	}

	public void setCommand(SLCommand aCmd) {
		command = aCmd;
	}

	@Override
	public SLCommandInvoker newInstance() {
		SLCommandInvokerImpl cmdInv = new SLCommandInvokerImpl();
		cmdInv.setSecurityManager(securityManager);
		return cmdInv;
	}

	public SecurityManagerFacade getSecurityManager() {
		return securityManager;
	}

	public void setSecurityManager(SecurityManagerFacade securityManager) {
		this.securityManager = securityManager;
	}

}