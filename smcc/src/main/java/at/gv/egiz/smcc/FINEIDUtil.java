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
package at.gv.egiz.smcc;

public class FINEIDUtil {

	public static byte[] removeMFPath(byte[] completePath) {

		byte[] result = null;

		// remove MF path
		if (completePath.length >= 2 && completePath[0] == 0x3F
				&& completePath[1] == 0x00) {
			result = new byte[completePath.length - 2];
			System.arraycopy(completePath, 2, result, 0,
					completePath.length - 2);
		} else {
			result = completePath;
		}

		return result;
	}
}
