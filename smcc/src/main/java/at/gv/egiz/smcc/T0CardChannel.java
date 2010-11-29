package at.gv.egiz.smcc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

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
		 
		 if(resp.getSW1() == (byte)0x61) {
			 
			 byte[] data = executeGetResponse(channel, (byte)resp.getSW2());
			 
			 byte[] result = new byte[data.length + 2];
			 System.arraycopy(data, 0, result, 0, data.length);
			 
			 // add SW "90 00"
			 result[result.length-2] = (byte)0x90;
			 result[result.length-1] = (byte)0x00;
			 
			 return new ResponseAPDU(result);
		 } else {
		 
			 return resp;
		 }
	 }
	
		private byte[] executeGetResponse(CardChannel channel, byte sw2)
		throws CardException {

	boolean done = false;
	ByteArrayOutputStream bof = new ByteArrayOutputStream();

	while (!done) {

		CommandAPDU command = new CommandAPDU(new byte[] { (byte) 0x00,
				(byte) 0xC0, (byte) 0x00, (byte) 0x00, (byte) sw2 });
		ResponseAPDU resp = channel.transmit(command);
		
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
