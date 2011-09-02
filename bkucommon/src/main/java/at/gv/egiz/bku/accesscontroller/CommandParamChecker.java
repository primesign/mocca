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

import java.util.LinkedList;
import java.util.List;

import at.gv.egiz.bku.slcommands.SLCommand;

public abstract class CommandParamChecker {

	protected List<Pair<String, String>> paramList = new LinkedList<Pair<String, String>>();

	public static class Pair<T, Q> {
		private T key;
		private Q val;

		public Pair(T key, Q val) {
			if ((key == null) || (val == null)) {
				throw new NullPointerException("Pair key and value must not be null");
			}
			this.key = key;
			this.val = val;
		}

		public T getKey() {
			return key;
		}

		public Q getVal() {
			return val;
		}

		public boolean equals(Object other) {
			if (other instanceof Pair) {
				Pair<?, ?> ot = (Pair<?, ?>) other;
				return (key.equals(ot.key) && val.equals(ot.val));
			}
			return false;
		}

		public int hashCode() {
			return key.hashCode();
		}
	}

	public void addParameter(String key, String value) {
		paramList.add(new Pair<String, String>(key, value));
	}

	public abstract boolean checkParameter(SLCommand cmd);

}
