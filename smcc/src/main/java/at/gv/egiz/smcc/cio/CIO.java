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

package at.gv.egiz.smcc.cio;

/**
 *
 * @author clemens
 */
public abstract class CIO {

    /** CommonObjectAttributes */
    protected String label;
    protected byte[] authId;

    /**
     * @return the authId
     */
    public byte[] getAuthId() {
        return authId;
    }

    public String getLabel() {
        return label;
    }

    /**
     * @deprecated
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @deprecated 
     * @param authId the authId to set
     */
    public void setAuthId(byte[] authId) {
        this.authId = authId;
    }

    @Override
    public String toString() {
        return "CIO " + label;
    }

}
