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

import at.gv.egiz.smcc.SignatureCardException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;

/**
 *
 * @author clemens
 */
public abstract class CIOCertificateDirectory extends CIODirectoryFile {

    protected List<CIOCertificate> cios;

    public CIOCertificateDirectory(List<byte[]> DF_FIDs) {
        super(DF_FIDs);
    }

    @Override
    protected void addCIO(byte[] cio) throws IOException {

        CIOCertificate cioCert = new CIOCertificate(cio);
        
        log.debug("adding {}", cioCert);
        cios.add(cioCert);

    }

    @Override
    public List<CIOCertificate> getCIOs(CardChannel channel) throws CardException, SignatureCardException, IOException {
        if (cios == null) {
            cios = new ArrayList<CIOCertificate>();
            readCIOs(channel);
        }
        return cios;
    }
}
