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

import at.gv.egiz.smcc.cio.CIOCertificateDirectory;
import at.gv.egiz.smcc.util.ISO7816Utils;
import at.gv.egiz.smcc.util.TLVSequence;
import java.util.List;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

/**
 *
 * @author clemens
 */
public class LIEZertifikatCertificateDirectory extends CIOCertificateDirectory {

    public LIEZertifikatCertificateDirectory(List<byte[]> DF_FIDs) {
        super(DF_FIDs);
    }

    @Override
    protected byte[] selectDirectoryFile(CardChannel channel, byte[] fid) throws CardException {

        CommandAPDU cmd = new CommandAPDU(0x00, 0xA4, 0x02, ISO7816Utils.P2_FCP, fid, 256);
        ResponseAPDU resp = channel.transmit(cmd);

        byte[] fcp = new TLVSequence(resp.getBytes()).getValue(ISO7816Utils.TAG_FCP);
        return new TLVSequence(fcp).getValue(0x82);

    }
}
