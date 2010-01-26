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

/**
 * Result of the access controller
 * 
 */
public class ChainResult {
	private UserAction userAction;
	private Action action;
	private boolean matchFound;

	public ChainResult(Action action, UserAction userAction, boolean matchFound) {
		this.action = action;
		this.userAction = userAction;
		this.matchFound = matchFound;
	}
	
	public Action getAction() {
		return action;
	}

	public UserAction getUserAction() {
		return userAction;
	}
	
	/**
	 * 
	 * @return true if a matching rule has been found
	 */
	public boolean matchFound() {
		return matchFound;
	}
}
