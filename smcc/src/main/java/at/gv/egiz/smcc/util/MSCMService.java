package at.gv.egiz.smcc.util;

import iaik.me.security.Cipher;
import iaik.me.security.CryptoBag;
import iaik.me.security.CryptoException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;

public class MSCMService implements MSCMConstants {

	MSCMTransfer transfer;

	public MSCMService(CardChannel channel) {
		transfer = new MSCMTransfer(channel);
	}

	public String[] getFiles(String path) throws IOException, CardException, MSCMException {

		byte[] cmd = buildAPDU(DEFAULT_SERVICE_PORT,
				HIVECODE_NAMESPACE_GEMALTO, HIVECODE_TYPE_DEFAULT,
				HIVECODE_METHOD_GETFILES, URI, MSCMDecoder.encodeString(path));

		return MSCMDecoder.decodeStringArray(
				transfer.transfer(cmd, HIVECODE_TYPE_SYSTEM_STRING_ARRAY), 0);

	}
	
	public byte[] getFileProperties(String path) throws IOException, CardException, MSCMException {
		byte[] cmd = buildAPDU(DEFAULT_SERVICE_PORT,
				HIVECODE_NAMESPACE_GEMALTO, HIVECODE_TYPE_DEFAULT,
				HIVECODE_METHOD_GETFILEPROPERTIES, URI, 
				 MSCMDecoder.encodeString(path));
		
		return MSCMDecoder.decodeByteArray(
				transfer.transfer(cmd, HIVECODE_TYPE_SYSTEM_BYTE_ARRAY), 0);
	}
	
	public void verifyPin(byte role, byte[] pin) throws IOException, CardException, MSCMException {
		byte[] cmd = buildAPDU(DEFAULT_SERVICE_PORT,
				HIVECODE_NAMESPACE_GEMALTO, HIVECODE_TYPE_DEFAULT,
				HIVECODE_METHOD_VERIFYPIN, URI, 
				MSCMDecoder.concatByteArrays(new byte[] { role }, 
						MSCMDecoder.encodeByteArray(pin)));
		
		transfer.transfer(cmd, HIVECODE_TYPE_SYSTEM_VOID);
	}
	
	public void changePIN(byte role, byte[] oldpin , byte[] newpin, int maxtries) throws IOException, CardException, MSCMException {
		byte[] p = MSCMDecoder.concatByteArrays(new byte[] { (byte)0, role }, 
				MSCMDecoder.encodeByteArray(oldpin));
		byte[] pr = MSCMDecoder.concatByteArrays(p, MSCMDecoder.encodeByteArray(newpin));
		byte[] pre = MSCMDecoder.concatByteArrays(pr, intToBytes(maxtries));
		
		byte[] cmd = buildAPDU(DEFAULT_SERVICE_PORT,
				HIVECODE_NAMESPACE_GEMALTO, HIVECODE_TYPE_DEFAULT,
				HIVECODE_METHOD_CHANGEREFDATA, URI, 
				pre);
		
		transfer.transfer(cmd, HIVECODE_TYPE_SYSTEM_VOID);
	}
	
	public void unblockPIN(byte role, byte[] oldpin , byte[] newpin, int maxtries) throws IOException, CardException, MSCMException {
		byte[] p = MSCMDecoder.concatByteArrays(new byte[] { (byte)1, role }, 
				MSCMDecoder.encodeByteArray(oldpin));
		byte[] pr = MSCMDecoder.concatByteArrays(p, MSCMDecoder.encodeByteArray(newpin));
		byte[] pre = MSCMDecoder.concatByteArrays(pr, intToBytes(maxtries));
		
		byte[] cmd = buildAPDU(DEFAULT_SERVICE_PORT,
				HIVECODE_NAMESPACE_GEMALTO, HIVECODE_TYPE_DEFAULT,
				HIVECODE_METHOD_CHANGEREFDATA, URI, 
				pre);
		
		transfer.transfer(cmd, HIVECODE_TYPE_SYSTEM_VOID);
	}
	
	public int[] queryFreeSpace() throws IOException, CardException, MSCMException {
		byte[] cmd = buildAPDU(DEFAULT_SERVICE_PORT,
				HIVECODE_NAMESPACE_GEMALTO, HIVECODE_TYPE_DEFAULT,
				HIVECODE_METHOD_QUERYFREESPACE, URI, 
				 null);
		
		return MSCMDecoder.decodeIntArray(
				transfer.transfer(cmd, HIVECODE_TYPE_SYSTEM_INT32_ARRAY), 0);
	}
	
	public void forceGC() throws IOException, CardException, MSCMException {
		byte[] cmd = buildAPDU(DEFAULT_SERVICE_PORT,
				HIVECODE_NAMESPACE_GEMALTO, HIVECODE_TYPE_DEFAULT,
				HIVECODE_METHOD_FORCEGC, URI, 
				 null);
		transfer.transfer(cmd, HIVECODE_TYPE_SYSTEM_VOID);
	}
	
	public byte[] readFile(String path) throws IOException, CardException, MSCMException {
		byte[] cmd = buildAPDU(DEFAULT_SERVICE_PORT,
				HIVECODE_NAMESPACE_GEMALTO, HIVECODE_TYPE_DEFAULT,
				HIVECODE_METHOD_READFILE, URI, 
				 MSCMDecoder.concatByteArrays(MSCMDecoder.encodeString(path), 
						 intToBytes(0) ));
		
		return MSCMDecoder.decodeByteArray(
				transfer.transfer(cmd, HIVECODE_TYPE_SYSTEM_BYTE_ARRAY), 0);
	}

	public byte[] privateKeyDecrypt(byte ctrIdx, byte keyType, byte[] data) 
			throws IOException, CardException, MSCMException {
		byte[] cmd = buildAPDU(DEFAULT_SERVICE_PORT,
				HIVECODE_NAMESPACE_GEMALTO, HIVECODE_TYPE_DEFAULT,
				HIVECODE_METHOD_PRIVATEKEYDECRYPT, URI, 
				 MSCMDecoder.concatByteArrays(new byte[] { ctrIdx,
				keyType }, MSCMDecoder.encodeByteArray(data)));
		
		return MSCMDecoder.decodeByteArray(
				transfer.transfer(cmd, HIVECODE_TYPE_SYSTEM_BYTE_ARRAY), 0);
	}
	
	public int getTriesRemaining(byte role) throws IOException, CardException, MSCMException {
		byte[] cmd = buildAPDU(DEFAULT_SERVICE_PORT,
				HIVECODE_NAMESPACE_GEMALTO, HIVECODE_TYPE_DEFAULT,
				HIVECODE_METHOD_GETTRIESREMAINING, URI, new byte[] { role });

		return bytesToInt(transfer.transfer(cmd, HIVECODE_TYPE_SYSTEM_INT32), 0);
	}

	public String getVersion() throws IOException, CardException, MSCMException {

		byte[] cmd = buildAPDU(DEFAULT_SERVICE_PORT,
				HIVECODE_NAMESPACE_GEMALTO, HIVECODE_TYPE_DEFAULT,
				HIVECODE_METHOD_GETVERSION, URI, null);

		return MSCMDecoder.decodeString(
				transfer.transfer(cmd, HIVECODE_TYPE_SYSTEM_STRING), 0);

	}

	public byte[] getContainerProperty(byte ctrIndex, byte property, byte flags)
			throws IOException, CardException, MSCMException {
		byte[] cmd = buildAPDU(DEFAULT_SERVICE_PORT,
				HIVECODE_NAMESPACE_GEMALTO, HIVECODE_TYPE_DEFAULT,
				HIVECODE_METHOD_GETCONTAINERPROPERTY, URI, new byte[] {
						ctrIndex, property, flags });

		return transfer.transfer(cmd, HIVECODE_TYPE_SYSTEM_BYTE_ARRAY);
	}

	public byte[] getCardProperty(byte property, byte flags)
			throws IOException, CardException, MSCMException {

		byte[] cmd = buildAPDU(DEFAULT_SERVICE_PORT,
				HIVECODE_NAMESPACE_GEMALTO, HIVECODE_TYPE_DEFAULT,
				HIVECODE_METHOD_GETCARDPROPERTY, URI, new byte[] { property,
						flags });

		return transfer.transfer(cmd, HIVECODE_TYPE_SYSTEM_BYTE_ARRAY);
	}

	public static String bytArrayToHex(byte[] a) {
		StringBuilder sb = new StringBuilder();
		for (byte b : a)
			sb.append(String.format("%02X ", b & 0xff));
		return sb.toString();
	}

	public static String bytArrayToHex(byte[] a, int offset) {
		StringBuilder sb = new StringBuilder();
		for (int i = offset; i < a.length; i++) {
			byte b = a[i];
			sb.append(String.format("%02X ", b & 0xff));
		}
		return sb.toString();
	}

	public static byte[] shortToBytes(short x) {
		byte[] ret = new byte[2];
		ret[1] = (byte) (x & 0xff);
		ret[0] = (byte) ((x >> 8) & 0xff);
		return ret;
	}

	public static byte[] intToBytes(int x) {
		byte[] ret = new byte[4];
		ret[3] = (byte) (x & 0xff);
		ret[2] = (byte) ((x >> 8) & 0xff);
		ret[1] = (byte) ((x >> 16) & 0xff);
		ret[0] = (byte) ((x >> 24) & 0xff);
		return ret;
	}

	public static int bytesToInt(byte[] by, int offset) {
		int value = 0;
		for (int i = 0; i < 4 && i + offset < by.length; i++) {
			value <<= 8;
			value |= (int) by[offset + i] & 0xFF;
		}
		return value;
	}

	public static short bytesToShort(byte[] by, int offset) {
		short value = 0;
		for (int i = 0; i < 2 && i + offset < by.length; i++) {
			value <<= 8;
			value |= (short) by[i + offset] & 0xFF;
		}
		return value;
	}

	public byte[] buildAPDU(short servicePort, int hiveServiceNamespace,
			short hiveServiceType, short hiveMethod, String serviceName,
			byte[] arguments) throws IOException {
		byte[] c1 = new byte[1];
		byte[] c2 = new byte[1];
		byte[] encodedServiceName = serviceName.getBytes("UTF-8");
		c1[0] = (byte) 0xD8;
		c2[0] = (byte) 0x6F;

		ByteArrayOutputStream payloadbuffer = new ByteArrayOutputStream();

		payloadbuffer.write(c1);
		payloadbuffer.write(shortToBytes(servicePort));
		payloadbuffer.write(c2);
		payloadbuffer.write(intToBytes(hiveServiceNamespace));
		payloadbuffer.write(shortToBytes(hiveServiceType));
		payloadbuffer.write(shortToBytes(hiveMethod));
		payloadbuffer.write(shortToBytes((short) encodedServiceName.length));
		payloadbuffer.write(encodedServiceName);
		if (arguments != null) {
			payloadbuffer.write(arguments);
		}

		return payloadbuffer.toByteArray();
	}
	

	public byte[] cryptoResponse(byte[] challenge, byte[] key) {
		try {
			Cipher cipher = Cipher.getInstance("DESede/ECB/NoPadding");
	        cipher.init(Cipher.ENCRYPT_MODE, CryptoBag.makeSecretKey(key));
			
			/*System.out.println("Crypto IV: "
					+ MSCMService.bytArrayToHex(cipher.getIV().getEncoded()));*/
			byte[] result = cipher.doFinal(challenge);
			System.out.println("Crypto result: " + MSCMService.bytArrayToHex(result));
			return result;
		} catch (CryptoException e) {
			System.out.println("Failed to get crypto stuff" + e.getMessage());
			return null;
		}
	}
	
	public void doExternalAuthentication(byte[] response)
			throws IOException, CardException, MSCMException {
		byte[] cmd = buildAPDU(DEFAULT_SERVICE_PORT,
				HIVECODE_NAMESPACE_GEMALTO, HIVECODE_TYPE_DEFAULT,
				HIVECODE_METHOD_EXTAUTH, URI, 
				MSCMDecoder.encodeByteArray(response));
		
		transfer.transfer(cmd, HIVECODE_TYPE_SYSTEM_VOID);
	}
	
	public byte[] getChallenge() throws IOException, CardException, MSCMException {
		byte[] cmd = buildAPDU(DEFAULT_SERVICE_PORT,
				HIVECODE_NAMESPACE_GEMALTO, HIVECODE_TYPE_DEFAULT,
				HIVECODE_METHOD_GETCHALLENGE, URI, 
				 null);
		
		return MSCMDecoder.decodeByteArray(
				transfer.transfer(cmd, HIVECODE_TYPE_SYSTEM_BYTE_ARRAY), 0);
	}
}
