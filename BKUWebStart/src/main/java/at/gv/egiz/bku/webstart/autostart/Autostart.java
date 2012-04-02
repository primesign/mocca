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

package at.gv.egiz.bku.webstart.autostart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Autostart {
	private static Logger _log = LoggerFactory.getLogger(Autostart.class);
	private static AutostartInterface _autostart = null;

	private String _webstartName = null;

	private AutostartInterface getAutostart()
	{
		if (_autostart == null)
		{
			String os = System.getProperty("os.name");
			if (os.equalsIgnoreCase("linux"))
				_autostart = new AutostartLinux();
			else if (os.toLowerCase().contains("windows"))
				_autostart = new AutostartWindows();
			else if (os.toLowerCase().contains("os x"))
				_autostart = new AutostartMacOSX();
			else
				_log.debug("Unsupported OS: " + os);
			if (_autostart != null && _webstartName != null)
				_autostart.setWebstartName(_webstartName);
		}

		return _autostart;
	}

	public boolean isPossible() {
		if (getAutostart() == null)
			return false;
		return getAutostart().isPossible();
	}

	public boolean isEnabled() {
		if (getAutostart() == null)
			return false;
		
		return getAutostart().isEnabled();
	}

	public boolean set(boolean enable) {
		if (getAutostart() == null)
			return false;
		
		return getAutostart().set(enable);
	}

	public void setWebstartName(String webstartName) {
		if (_autostart == null)
			_webstartName = webstartName;
		else
			_autostart.setWebstartName(webstartName);
	}

}
