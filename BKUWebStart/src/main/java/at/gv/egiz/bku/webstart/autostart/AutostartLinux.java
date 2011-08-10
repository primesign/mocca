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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutostartLinux extends AbstractAutostart {
	private static Logger log = LoggerFactory.getLogger(AutostartLinux.class);

	private Map<String, String> getAutostartFiles()
	{
		Map<String, String> autostartFiles = new HashMap<String, String>();
		String autostartFileName, autostartFileContent;
		String autostartCommand = "javaws -Xnosplash " + _webstartName;
		
		// KDE Autostart
		File f = new File(_userHome + ".kde/Autostart");
		if ((f.exists()) && (f.isDirectory())) {
			autostartFileName = _userHome + ".kde/Autostart/"
				+ AUTOSTART_FILENAME_PREFIX + ".sh";
			autostartFileContent = "#!/bin/sh\n" + autostartCommand + "\n";
			autostartFiles.put(autostartFileName, autostartFileContent);
		}

		// Gnome Autostart
		f = new File(_userHome + ".config/autostart");
		if ((f.exists()) && (f.isDirectory())) {
			autostartFileName = _userHome + ".config/autostart/"
					+ AUTOSTART_FILENAME_PREFIX + ".desktop";
			autostartFileContent =
				"[Desktop Entry]\n" +
				"Name=" + AUTOSTART_FILENAME_PREFIX + "\n" +
				"Type=Application\n" +
				"Exec=" + autostartCommand + "\n" +
				"Terminal=false\n" +
				"Hidden=false\n";
			autostartFiles.put(autostartFileName, autostartFileContent);
		}
		return autostartFiles;
	}

	@Override
	public boolean isPossible() {
		Map<String, String> autostartFiles = getAutostartFiles();

		return !autostartFiles.isEmpty();
	}

	@Override
	public boolean isEnabled() {
		Map<String, String> autostartFiles = getAutostartFiles();

		if (autostartFiles.isEmpty())
			return false;

		for (Map.Entry<String, String> file : autostartFiles.entrySet()) {
			String autostartFileName = file.getKey();
			try {
				File f = new File(autostartFileName);
				if (f.exists())
					return true;
			} catch (Exception e) {
				// ignore
			}
		}
		return false;
	}

	@Override
	public boolean set(boolean enable) {
		Map<String, String> autostartFiles = getAutostartFiles();

		if (autostartFiles.isEmpty())
			return false;

		boolean ret = false;
		for (Map.Entry<String, String> file : autostartFiles.entrySet()) {
			String autostartFileName = file.getKey();
			String autostartFileContent = file.getValue();
			File f = new File(autostartFileName);

			if (enable)
			{
				log.debug("Enabling AutoStart (" + autostartFileName + ")");
				try {
					FileWriter fw = new FileWriter(f, false);
					BufferedWriter bw = new BufferedWriter(fw);
					bw.write(autostartFileContent);
					bw.flush();
					bw.close();
					fw.close();
					if (autostartFileName.contains(".kde"))
						f.setExecutable(true, false);
					f.setReadable(true, false);
					ret = true;
				} catch (Exception e) {
					log.error("Failed to add autostart file", e);
				}
			}
			else
			{
				log.debug("Disabling AutoStart (" + autostartFileName + ")");
				try {
					f.delete();
				} catch (Exception e) {
					log.error("Failed to remove autostart file", e);
				}
			}
		}

		return ret;
	}

}
