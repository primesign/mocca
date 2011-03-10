/*
* Copyright 2009 Federal Chancellery Austria and
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

import at.gv.egiz.smcc.cio.ObjectDirectory;

public class LtEIDObjectDirectory extends ObjectDirectory {

	public LtEIDObjectDirectory() {
		
		super(new byte[]{(byte)0x50, (byte)0x00, (byte)0x50, (byte)0x31});	
		this.setP1((byte)0x08);		
	}
}
