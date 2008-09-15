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
package at.gv.egiz.bku.online.applet;

import at.gv.egiz.stal.HashDataInput;
import at.gv.egiz.bku.smccstal.SMCCSTALRequestHandler;
import at.gv.egiz.bku.smccstal.SignRequestHandler;
import at.gv.egiz.stal.impl.ByteArrayHashDataInput;
import at.gv.egiz.stal.service.GetHashDataInputResponseType;
import at.gv.egiz.stal.service.GetHashDataInputType;
import at.gv.egiz.stal.service.STALPortType;
import at.gv.egiz.stal.signedinfo.ReferenceType;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author clemens
 */
public class WSSignRequestHandler extends SignRequestHandler {

    private static final Log log = LogFactory.getLog(WSSignRequestHandler.class);
    STALPortType stalPort;
    String sessId;

    public WSSignRequestHandler(String sessId, STALPortType stalPort) {
        if (stalPort == null || sessId == null) {
            throw new NullPointerException("STAL port must not be null");
        }
        this.sessId = sessId;
        this.stalPort = stalPort;
    }

    @Override
    protected List<HashDataInput> getHashDataInputs(List<ReferenceType> dsigReferences) throws Exception {
        GetHashDataInputType request = new GetHashDataInputType();
        request.setSessionId(sessId);
        for (ReferenceType dsigRef : dsigReferences) {
            //don't get Manifest, QualifyingProperties, ...
            if (dsigRef.getType() == null) {
                String dsigRefId = dsigRef.getId();
                if (dsigRefId != null) {
                    GetHashDataInputType.Reference reference = new GetHashDataInputType.Reference();
                    reference.setID(dsigRefId);
                    request.getReference().add(reference);
                } else {
                    throw new Exception("Cannot get HashDataInput for dsig:Reference without Id attribute");
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Calling GetHashDataInput for session " + sessId);
        }
        GetHashDataInputResponseType response = stalPort.getHashDataInput(request);
        ArrayList<HashDataInput> hashDataInputs = new ArrayList<HashDataInput>();
        for (GetHashDataInputResponseType.Reference reference : response.getReference()) {
            byte[] hdi = reference.getValue();
            String id = reference.getID();
            String mimeType = reference.getMimeType();
            String encoding = reference.getEncoding();

            if (log.isDebugEnabled()) {
                log.debug("Got HashDataInput " + id + " (" + mimeType + ";" + encoding + ")");
            }
            hashDataInputs.add(new ByteArrayHashDataInput(hdi, id, mimeType, encoding));
        }
        return hashDataInputs;
    }

    @Override
    public SMCCSTALRequestHandler newInstance() {
        return new WSSignRequestHandler(this.sessId, this.stalPort);
    }
}
