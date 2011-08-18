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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tkellner
 *
 */
public class AutostartMacOSX extends AbstractAutostart {
	private static Logger _log = LoggerFactory.getLogger(Autostart.class);

	class StreamGobbler extends Thread {
		InputStream is;

		StreamGobbler(InputStream is) {
			this.is = is;
		}

		public void run() {
			try {
				while (is.read() != -1)
					;
//				InputStreamReader isr = new InputStreamReader(is);
//				BufferedReader br = new BufferedReader(isr);
//				String line = null;
//				while ((line = br.readLine()) != null)
//					_log.info(">" + line);
			} catch (IOException ex) {
				_log.debug("Error consuming stream", ex);
			}
		}
	}

	private void exec(String cmd[]) {
		try {
			_log.info("Executing " + Arrays.toString(cmd));
			Process proc = Runtime.getRuntime().exec(cmd);
			StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream());
			errorGobbler.start();
			StreamGobbler inputGobbler = new StreamGobbler(proc.getInputStream());
			inputGobbler.start();
			int ret = proc.waitFor();
			_log.info("Done: " + ret);
		} catch (IOException ex) {
			_log.debug("Error executing " + cmd[0], ex);
		} catch (InterruptedException ex) {
			_log.debug("Interrupted executing " + cmd[0], ex);
		}
	}

	private boolean deleteRecursive(File f)
	{
		if (f.isDirectory())
		{
			String[] children = f.list();
			for (String child : children) {
				if (!deleteRecursive(new File(f, child)))
					return false;
			}
		}
		return f.delete();
	}

	@Override
	public boolean isPossible() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		File f = new File(_userHome + ".mocca/MoccaStartup.app");
		return f.exists();
	}

	@Override
	public boolean set(boolean enable) {
		if (isEnabled() == enable)
			return enable;

		if (enable)
		{
			String[] command1 = {
					"osacompile",
					"-e",
					"do shell script \"/usr/bin/javaws -Xnosplash " + _webstartName + "\"",
					"-x",
					"-o",
					_userHome + ".mocca/MoccaStartup.app"
				};
			String[] command2 = {
					"defaults",
					"write",
					"loginwindow",
					"AutoLaunchedApplicationDictionary",
					"-array-add",
					"{Hide=0;Path=\"" + _userHome + ".mocca/MoccaStartup.app\";}"
				};

			//"osacompile -e 'do shell script \"/usr/bin/javaws -Xnosplash " + _webstartName + "\"' -x -o \"" + _userHome + ".mocca/MoccaStartup.app\""
			exec(command1);
			//"defaults write loginwindow AutoLaunchedApplicationDictionary -array-add '{Hide=0;Path=\"" + _userHome + ".mocca/MoccaStartup.app\";}'"
			exec(command2);
			return isEnabled();
		}
		else
		{
			File f = new File(_userHome + ".mocca/MoccaStartup.app");
			return !deleteRecursive(f);
		}
	}
}
