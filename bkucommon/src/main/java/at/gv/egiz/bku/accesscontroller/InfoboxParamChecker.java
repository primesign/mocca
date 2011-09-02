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
			for (Pair<String, String> param : paramList) {
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
