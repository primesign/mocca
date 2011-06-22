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
