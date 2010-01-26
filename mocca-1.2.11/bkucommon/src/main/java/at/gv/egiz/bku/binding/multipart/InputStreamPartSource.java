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
package at.gv.egiz.bku.binding.multipart;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.httpclient.methods.multipart.PartSource;

/**
 * InputStream source for FilePart. 
 * DOES NOT RETURN A CORRECT LENGTH OF THE INPUT DATA. (but we don't care, since we use chunked encoding)
 * 
 * @author clemens
 */
public class InputStreamPartSource implements PartSource {

    protected String name;
    protected InputStream data;
    
    public InputStreamPartSource(String name, InputStream data) {
        this.name = name;
        this.data = data;
    }
    
    /**
     * Just a dummy value to make Part work
     * @return 42
     */
    @Override
    public long getLength() {
        //System.out.println("***********GETLENGTH");
        return 42;
    }

    @Override
    public String getFileName() {
        return name;
    }

    @Override
    public InputStream createInputStream() throws IOException {
        if (data == null) 
            throw new IOException("Failed to get stream for part: no data was set.");
        return data;
    }

}
