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

import java.util.LinkedList;
import java.util.List;

import at.gv.egiz.bku.slcommands.SLCommand;

public abstract class CommandParamChecker {

	protected List<Tupel<String, String>> paramList = new LinkedList<Tupel<String, String>>();

	public static class Tupel<T, Q> {
		private T key;
		private Q val;

		public Tupel(T key, Q val) {
			if ((key == null) || (val == null)) {
				throw new NullPointerException("Tupel key and value must not be null");
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

		@SuppressWarnings("unchecked")
		public boolean equals(Object other) {
			if (other instanceof Tupel) {
				Tupel ot = (Tupel) other;
				return (key.equals(ot.key) && val.equals(ot.val));
			}
			return false;
		}

		public int hashCode() {
			return key.hashCode();
		}
	}

	public void addParameter(String key, String value) {
		paramList.add(new Tupel<String, String>(key, value));
	}

	public abstract boolean checkParameter(SLCommand cmd);

}
