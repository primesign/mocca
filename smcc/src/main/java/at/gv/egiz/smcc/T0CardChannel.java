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


package at.gv.egiz.smcc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

public class T0CardChannel extends LogCardChannel {

	public T0CardChannel(CardChannel channel) {
		super(channel);
	}
	
	@Override
	public ResponseAPDU transmit(CommandAPDU command) throws CardException {

		ResponseAPDU resp = super.transmit(command);

		if (resp.getSW1() == (byte) 0x61) {

			byte[] initData = resp.getData();
			byte[] data = executeGetResponse((byte) resp.getSW2());

			byte[] result = new byte[initData.length + data.length + 2];
			System.arraycopy(initData, 0, result, 0, initData.length);
			System.arraycopy(data, 0, result, initData.length, data.length);

			// add SW "90 00"
			result[result.length - 2] = (byte) 0x90;
			result[result.length - 1] = (byte) 0x00;

			return new ResponseAPDU(result);
		} else {

			return resp;
		}
	}

	private byte[] executeGetResponse(byte sw2) throws CardException {

		boolean done = false;
		ByteArrayOutputStream bof = new ByteArrayOutputStream();

		while (!done) {

			CommandAPDU command = new CommandAPDU(new byte[] { (byte) 0x00,
					(byte) 0xC0, (byte) 0x00, (byte) 0x00, (byte) sw2 });
			// ResponseAPDU resp = channel.transmit(command);
			ResponseAPDU resp = super.transmit(command);

			try {
				bof.write(resp.getData());
			} catch (IOException e) {

				throw new CardException(
						"Error during fetching gesponse from card.", e);
			}

			if (resp.getSW1() == (byte) 0x61) {

				// more data to be read
				sw2 = (byte) resp.getSW2();
				continue;
			}

			if (resp.getSW() == 0x9000) {

				// all data read
				done = true;
			} else {

				throw new CardException(
						"An error has occured during fetching response from card: "
								+ Integer.toHexString(resp.getSW()));
			}

		}

		return bof.toByteArray();
	}

}
