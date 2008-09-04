package at.gv.egiz.bku.accesscontroller;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.bku.slcommands.InfoboxReadCommand;
import at.gv.egiz.bku.slcommands.SLCommand;
import at.gv.egiz.bku.slexceptions.SLRuntimeException;

public class InfoboxParamChecker extends CommandParamChecker {
	private static Log log = LogFactory.getLog(InfoboxParamChecker.class);

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
			log.error("Cannot handle parameter for command: " + cmd.getName());
			throw new SLRuntimeException("Cannot handle parameters for command: "
					+ cmd.getName());
		}
	}
}
