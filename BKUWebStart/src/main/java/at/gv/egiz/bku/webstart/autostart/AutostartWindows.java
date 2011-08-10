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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutostartWindows extends AbstractAutostart {
	private static Logger log = LoggerFactory.getLogger(AutostartWindows.class);
	private static final String DESKTOP_SHORTCUT_NAME = "MOCCA Start.lnk";

	private String _linkname = null;

	public AutostartWindows() {
		try {
			// BKA Workaround: If shortcut exists on desktop, use it for autostart
			_linkname = WinRegistry.readString(
							WinRegistry.HKEY_CURRENT_USER,
							"Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders",
							"Desktop")
					+ "\\" + DESKTOP_SHORTCUT_NAME;
			File f = new File(_linkname);
			if (f.exists())
				return;
			_linkname = WinRegistry.readString(
							WinRegistry.HKEY_LOCAL_MACHINE,
							"Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders",
							"Common Desktop")
					+ "\\" + DESKTOP_SHORTCUT_NAME;
			f = new File(_linkname);
			if (f.exists())
				return;
			_linkname = null;
		} catch (Exception e) {
			log.debug("Registry reading failed", e);
		}
	}

	private static boolean copyFile(File srcF, File dstF) {
		boolean ret = false;
		try {
			if (!dstF.exists())
				dstF.createNewFile();
			FileChannel src = null;
			FileChannel dst = null;
			try {
				src = new FileInputStream(srcF).getChannel();
				dst = new FileOutputStream(dstF).getChannel();
				if (dst.transferFrom(src, 0, src.size()) < src.size()) {
					dst.close();
					dst = null;
					dstF.delete();
					log.error("Failed to copy autostart shortcut");
				} else
					ret = true;
			} catch (FileNotFoundException e) {
				log.error("Failed to copy autostart shortcut", e);
			} finally {
				if (src != null)
					src.close();
				if (dst != null)
					dst.close();
			}
		} catch (IOException e) {
			log.error("Failed to copy autostart shortcut", e);
		}
		return ret;
	}

	private static boolean createFile(File f, String content) {
		try {
			FileWriter fw = new FileWriter(f, false);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.flush();
			bw.close();
			fw.close();
			f.setExecutable(true);
			return true;
		} catch (Exception e) {
			log.error("Failed to add autostart file", e);
		}
		return false;
	}

	private String getAutostartFileName() {
		String fileName = AUTOSTART_FILENAME_PREFIX
				+ (_linkname != null ? ".lnk" : ".bat");
		String autostartFileName = null;
		try {
			autostartFileName = WinRegistry
					.readString(
							WinRegistry.HKEY_CURRENT_USER,
							"Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders",
							"Startup");
			if (autostartFileName == null)
				throw new Exception("Null returned");
			autostartFileName += "\\" + fileName;
		} catch (Exception e) {
			log.debug("Registry reading failed - trying fallback", e);

			String autostartFolderPaths[] = {
					_userHome + "Startmenü\\Programme\\Autostart", // German
																	// Windows
																	// default
																	// autostart
																	// path
					_userHome + "Startmenu\\Programs\\Startup" // English
																// Windows
																// default
																// autostart
																// path
			};

			for (String path : autostartFolderPaths) {
				File f = new File(path);
				if (f.exists()) {
					autostartFileName = _userHome + path + "\\" + fileName;
					break;
				}
			}
		}
		return autostartFileName;
	}

	@Override
	public boolean isPossible() {
		String autostartFileName = getAutostartFileName();

		return autostartFileName != null;
	}

	@Override
	public boolean isEnabled() {
		String autostartFileName = getAutostartFileName();

		if (autostartFileName == null)
			return false;

		try {
			File f = new File(autostartFileName);
			if (f.exists())
				return true;
		} catch (Exception e) {
			// ignore
		}
		return false;
	}

	@Override
	public boolean set(boolean enable) {
		String autostartFileName = getAutostartFileName();

		if (autostartFileName == null)
			return false;

		String javaws_loc = System.getProperty("java.home")
				+ "\\bin\\javaws.exe";
		File f = new File(javaws_loc);
		if (!f.exists())
			javaws_loc = "javaws.exe";
		String autostartFileContent = "@\"" + javaws_loc + "\" -Xnosplash "
				+ _webstartName + "\r\n";

		f = new File(autostartFileName);

		if (enable) {
			log.debug("Enabling AutoStart (" + autostartFileName + ")");
			if (_linkname != null)
				return copyFile(new File(_linkname), f);
			else
				return createFile(f, autostartFileContent);
		} else {
			log.debug("Disabling AutoStart (" + autostartFileName + ")");
			try {
				f.delete();
			} catch (Exception e) {
				log.error("Failed to remove autostart file", e);
			}
			return false;
		}
	}

}
