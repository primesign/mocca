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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.bku.slcommands.InfoboxReadCommand;
import at.gv.egiz.bku.slcommands.SLCommand;
import at.gv.egiz.bku.slexceptions.SLRuntimeException;

public class InfoboxParamChecker extends CommandParamChecker {
	
    private final Logger log = LoggerFactory.getLogger(InfoboxParamChecker.class);

	public final static String INFOBOX_ID = "InfoboxIdentifier";
	public final static String PERSON_ID = "PersonIdentifier";
	public final static String DERIVED = "derived";

	@Override
	public boolean checkParameter(SLCommand cmd) {
		if (paramList.size() == 0) {
			return true;
		}

		if (cmd instanceof InfoboxReadCommand) {
			InfoboxReadCommand irc = (InfoboxReadCommand) cmd;
			for (Tupel<String, String> param : paramList) {
				if (param.getKey().equals(INFOBOX_ID)) {
					if (!param.getVal().equals(irc.getInfoboxIdentifier())) {
						return false;
					}
				} else if (param.getKey().equals(PERSON_ID)) {
					if (param.getVal().equals(DERIVED)) {
						if (irc.getIdentityLinkDomainId() == null) {
							return false;
						}
					} else {
						Pattern p = Pattern.compile(param.getVal());
						Matcher m = p.matcher(irc.getIdentityLinkDomainId());
						if (!m.matches()) {
							return false;
						}
					}

				} else {
					throw new SLRuntimeException("Cannot handle parameter "
							+ param.getKey());
				}
			}
			return true;
		} else {
			log.error("Cannot handle parameter for command: {}.", cmd.getName());
			throw new SLRuntimeException("Cannot handle parameters for command: "
					+ cmd.getName());
		}
	}
}
