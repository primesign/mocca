package at.gv.egiz.smcc.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MSCMTransfer implements MSCMConstants {

	private final Logger log = LoggerFactory
			.getLogger(MSCMTransfer.class);
	
	private CardChannel channel;
	
	public MSCMTransfer(CardChannel channel) {
		this.channel = channel;
	}
	
	public byte[] transfer(byte[] payload, short expectedType) throws IOException, CardException, MSCMException {
		byte[] header = new byte[] { (byte) 0x80, (byte) 0xC2, (byte) 0x00,
				(byte) 0x00 };
		byte[] cmd = null;
		byte[] len = new byte[1];
		byte[] con = new byte[] { (byte) 0xD8, (byte) 0xFF, (byte) 0xFF };
		ByteArrayOutputStream cmdbuffer = new ByteArrayOutputStream();
		ResponseAPDU response = null;
		if(payload.length <= 0xFF) {
			cmdbuffer.write(header);
			len[0] = (byte) payload.length;
			cmdbuffer.write(len);
			cmdbuffer.write(payload);
			// Transfer cmdbuffer
			cmd = cmdbuffer.toByteArray();
			String str = MSCMService.bytArrayToHex(cmd);
			log.info("Sending: >>> " + str);
			//ByteBuffer bb = ByteBuffer.wrap(cmd);
			//ByteBuffer resp = ByteBuffer.allocate(18000);
			//response = channel.transmit(bb, resp);
			//int resps = channel.transmit(bb, resp);
			//ByteArrayOutputStream out = new ByteArrayOutputStream();
			//out.write(resp.array(), 0, resps);
			//out.close();
			//response = new ResponseAPDU(out.toByteArray());
			response = channel.transmit(new CommandAPDU(cmd));
		} else { // (> 0xFF)
			// divide payload
			int length = payload.length;
			int offset = 0;
			int curlen = 0;
			boolean first = true;
			while(length > 0) {
				curlen = (length > 0xF4) ? 0xF4 : length;
				ByteArrayOutputStream dpay = new ByteArrayOutputStream();
				dpay.write(con);
				if(first) {
					dpay.write(MSCMService.intToBytes(payload.length-18));
					dpay.write(MSCMService.intToBytes(curlen-18));
				} else {
					dpay.write(MSCMService.intToBytes(offset-18));
					dpay.write(MSCMService.intToBytes(curlen));
				}
				first = false;
				
				dpay.write(payload, offset, curlen);
				dpay.close();
				byte[] ppay = dpay.toByteArray();
				
				cmdbuffer.write(header);
				len[0] = (byte)ppay.length;
				cmdbuffer.write(len);
				cmdbuffer.write(ppay);
				
				length -= curlen;
				offset += curlen;
				
				cmd = cmdbuffer.toByteArray();
				String str = MSCMService.bytArrayToHex(cmd);
				log.info("Sending: >>> " + str);
				response = channel.transmit(new CommandAPDU(cmd));
				
				
				//ByteBuffer bb = ByteBuffer.wrap(cmd);
				//ByteBuffer resp = ByteBuffer.allocate(18000);
				//response = channel.transmit(bb, resp);
				//int resps = channel.transmit(bb, resp);
				//ByteArrayOutputStream out = new ByteArrayOutputStream();
				//out.write(resp.array(), 0, resps);
				//out.close();
				//response = new ResponseAPDU(out.toByteArray());
				log.info("Resp: <<< " + MSCMService.bytArrayToHex(MSCMService.intToBytes(response.getSW())));
				cmdbuffer.reset();
			}
		}
		
		log.info(" SW : <<< " + MSCMService.bytArrayToHex(
				MSCMService.intToBytes(response.getSW())));
		byte[] data = response.getData();
		log.info("DATA: <<< " + MSCMService.bytArrayToHex(data));
		
		if(data.length == 0) {
			return null;
		}
		
		MSCMDecoder.checkExceptionInResponse(data, 0);
		
		if(expectedType == HIVECODE_TYPE_SYSTEM_VOID)  {
			return data;
		}
		
//		String namespaceString = MSCMDecoder.namespaceHiveToString(data, 0);
//		String typeString = MSCMDecoder.typeHiveToString(data, 4);
		short resultType = MSCMService.bytesToShort(data, 4);
		if(expectedType == resultType) {
			ByteArrayOutputStream str = new ByteArrayOutputStream();
			str.write(data, 6, data.length-6);
			return str.toByteArray();
		} else {
			throw new CardException("Wrong result type");
		}/*
		if(typeString.equals(HIVECODE_TYPE_SYSTEM_STRING_STRING)) {
			System.out.println(namespaceString + "." + typeString + ": " + 
					MSCMDecoder.decodeString(data, 6));
		} else if(typeString.equals(HIVECODE_TYPE_SYSTEM_STRING_ARRAY_STRING)) {
			String[] results = MSCMDecoder.decodeStringArray(data, 6);
			System.out.println(typeString + ": ");
			for(String str : results) {
				System.out.println(str);
			}
		} else {
			System.out.println(namespaceString + "." + typeString + ": " + 
				MSCMService.bytArrayToHex(MSCMDecoder.decodeByteArray(data, 6)));
		}
		return data;*/
	}
	
}
