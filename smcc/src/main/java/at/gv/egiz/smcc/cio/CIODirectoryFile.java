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



package at.gv.egiz.smcc.cio;

import at.gv.egiz.smcc.SignatureCardException;
import at.gv.egiz.smcc.util.ISO7816Utils;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author clemens
 */
public abstract class CIODirectoryFile {

    protected static final Logger log = LoggerFactory.getLogger(CIODirectoryFile.class);
    
    protected List<byte[]> DF_FIDs;

    public CIODirectoryFile(List<byte[]> DF_FIDs) {
        this.DF_FIDs = DF_FIDs;
    }

    /**
     * assume DF.CIA selected
     * (one of) CIO.CD selected afterwards
     *
     * TODO: make abstract, implementation knows how to read file. only provide utility methods
     *
     * @param channel
     * @throws CardException
     * @throws SignatureCardException
     * @throws IOException if ASN.1 structure cannot be parsed
     */
    public void readCIOs(CardChannel channel)
            throws CardException, SignatureCardException, IOException {

        for (byte[] fid : DF_FIDs) {
            byte[] fd = selectDirectoryFile(channel, fid);
            if ((fd[0] & 0x04) > 0) {
                readCIOsFromRecords(channel, fd);
            } else if ((fd[0] & 0x05) == 0x01) {
                readCIOsFromTransparentFile(channel);
            }
        }
    }

    /**
     * card specific implementation to select a CIO DF file and return its file descriptor
     * @param channel
     * @param fid
     * @return file descriptor
     * @throws CardException
     */
    protected abstract byte[] selectDirectoryFile(CardChannel channel, byte[] fid) throws CardException;


    protected void readCIOsFromRecords(CardChannel channel, byte[] fd) throws CardException, SignatureCardException, IOException {

        for (int r = 1; r < fd[fd.length - 1]; r++) {
            log.trace("read CIO record {}", r);
            byte[] record = ISO7816Utils.readRecord(channel, r);
            addCIO(record);
        }
    }


    protected void readCIOsFromTransparentFile(CardChannel channel) throws CardException, SignatureCardException, IOException {

    	byte[] ef = ISO7816Utils.readTransparentFile(channel, -1);

        int i = 0;
        int j;

        do {
            int length = 0;
            int ll = 0;
            if ((ef[i + 1] & 0xf0) == 0x80) {
                ll = ef[i + 1] & 0x7f;
                for (int it = 0; it < ll; it++) {
                    length = (length << 8) + (ef[i + it + 2] & 0xff);
                }
            } else {
                length = (ef[i + 1] & 0xff);
            }

            log.trace("read CIO transparent file entry: tag 0x{}, length 0x{}",
                    Integer.toHexString(ef[i]),
                    Integer.toHexString(length));

            j = i + 2 + ll + length;
            addCIO(Arrays.copyOfRange(ef, i, j));
            i = j;
        } while (i < ef.length && ef[i] > 0);

    }


    
    /**
     * CIO specific (Cert/PrK/AO/... CIO)
     * @param cio
     */
    protected abstract void addCIO(byte[] cio) throws IOException;

    public abstract List<? extends CIO> getCIOs(CardChannel channel) throws CardException, SignatureCardException, IOException;
}
