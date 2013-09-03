package at.gv.egiz.smcc.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class MSCMDecoder implements MSCMConstants {
	
	private static Map<Integer, String> namespaces;
	
	private static Map<Short, String> types;
	
	private static Map<Short, String> exceptions;
	
	static {
		namespaces = new HashMap<Integer, String>();
		namespaces.put(HIVECODE_NAMESPACE_SYSTEM, HIVECODE_NAMESPACE_SYSTEM_STRING);
		namespaces.put(HIVECODE_NAMESPACE_SYSTEM_IO, HIVECODE_NAMESPACE_SYSTEM_IO_STRING);
		namespaces.put(HIVECODE_NAMESPACE_SYSTEM_RUNTIME_REMOTING_CHANNELS, HIVECODE_NAMESPACE_SYSTEM_RUNTIME_REMOTING_CHANNELS_STRING);
		namespaces.put(HIVECODE_NAMESPACE_NETCARD_FILESYSTEM, HIVECODE_NAMESPACE_NETCARD_FILESYSTEM_STRING);
		namespaces.put(HIVECODE_NAMESPACE_SYSTEM_RUNTIME_REMOTING, HIVECODE_NAMESPACE_SYSTEM_RUNTIME_REMOTING_STRING);
		namespaces.put(HIVECODE_NAMESPACE_SYSTEM_SECURITY_CRYPTOGRAPHY, HIVECODE_NAMESPACE_SYSTEM_SECURITY_CRYPTOGRAPHY_STRING);
		namespaces.put(HIVECODE_NAMESPACE_SYSTEM_COLLECTIONS, HIVECODE_NAMESPACE_SYSTEM_COLLECTIONS_STRING);
		namespaces.put(HIVECODE_NAMESPACE_SYSTEM_RUNTIME_REMOTING_CONTEXTS, HIVECODE_NAMESPACE_SYSTEM_RUNTIME_REMOTING_CONTEXTS_STRING);
		namespaces.put(HIVECODE_NAMESPACE_SYSTEM_SECURITY, HIVECODE_NAMESPACE_SYSTEM_SECURITY_STRING);
		namespaces.put(HIVECODE_NAMESPACE_SYSTEM_REFLECTION, HIVECODE_NAMESPACE_SYSTEM_REFLECTION_STRING);
		namespaces.put(HIVECODE_NAMESPACE_SYSTEM_RUNTIME_SERIALIZATION, HIVECODE_NAMESPACE_SYSTEM_RUNTIME_SERIALIZATION_STRING);
		namespaces.put(HIVECODE_NAMESPACE_SYSTEM_RUNTIME_REMOTING_MESSAGING, HIVECODE_NAMESPACE_SYSTEM_RUNTIME_REMOTING_MESSAGING_STRING);
		namespaces.put(HIVECODE_NAMESPACE_SYSTEM_DIAGNOSTICS, HIVECODE_NAMESPACE_SYSTEM_DIAGNOSTICS_STRING);
		namespaces.put(HIVECODE_NAMESPACE_SYSTEM_RUNTIME_COMPILERSERVICES, HIVECODE_NAMESPACE_SYSTEM_RUNTIME_COMPILERSERVICES_STRING);
		namespaces.put(HIVECODE_NAMESPACE_SYSTEM_TEXT, HIVECODE_NAMESPACE_SYSTEM_TEXT_STRING);
		namespaces.put(HIVECODE_NAMESPACE_SMARTCARD, HIVECODE_NAMESPACE_SMARTCARD_STRING);
		
		
		types = new HashMap<Short, String>();
		
		types.put(HIVECODE_TYPE_SYSTEM_VOID, HIVECODE_TYPE_SYSTEM_VOID_STRING);
		types.put(HIVECODE_TYPE_SYSTEM_INT32, HIVECODE_TYPE_SYSTEM_INT32_STRING);
		types.put(HIVECODE_TYPE_SYSTEM_INT32_ARRAY, HIVECODE_TYPE_SYSTEM_INT32_ARRAY_STRING);
		types.put(HIVECODE_TYPE_SYSTEM_BOOLEAN, HIVECODE_TYPE_SYSTEM_BOOLEAN_STRING);
		types.put(HIVECODE_TYPE_SYSTEM_BOOLEAN_ARRAY, HIVECODE_TYPE_SYSTEM_BOOLEAN_ARRAY_STRING);
		types.put(HIVECODE_TYPE_SYSTEM_SBYTE, HIVECODE_TYPE_SYSTEM_SBYTE_STRING);
		types.put(HIVECODE_TYPE_SYSTEM_SBYTE_ARRAY, HIVECODE_TYPE_SYSTEM_SBYTE_ARRAY_STRING);
		types.put(HIVECODE_TYPE_SYSTEM_UINT16, HIVECODE_TYPE_SYSTEM_UINT16_STRING);
		types.put(HIVECODE_TYPE_SYSTEM_UINT16_ARRAY, HIVECODE_TYPE_SYSTEM_UINT16_ARRAY_STRING);
		types.put(HIVECODE_TYPE_SYSTEM_UINT32, HIVECODE_TYPE_SYSTEM_UINT32_STRING);
		types.put(HIVECODE_TYPE_SYSTEM_UINT32_ARRAY, HIVECODE_TYPE_SYSTEM_UINT32_ARRAY_STRING);
		types.put(HIVECODE_TYPE_SYSTEM_BYTE, HIVECODE_TYPE_SYSTEM_BYTE_STRING);
		types.put(HIVECODE_TYPE_SYSTEM_BYTE_ARRAY, HIVECODE_TYPE_SYSTEM_BYTE_ARRAY_STRING);
		types.put(HIVECODE_TYPE_SYSTEM_CHAR, HIVECODE_TYPE_SYSTEM_CHAR_STRING);
		types.put(HIVECODE_TYPE_SYSTEM_CHAR_ARRAY, HIVECODE_TYPE_SYSTEM_CHAR_ARRAY_STRING);
		types.put(HIVECODE_TYPE_SYSTEM_INT16, HIVECODE_TYPE_SYSTEM_INT16_STRING);
		types.put(HIVECODE_TYPE_SYSTEM_INT16_ARRAY, HIVECODE_TYPE_SYSTEM_INT16_ARRAY_STRING);
		types.put(HIVECODE_TYPE_SYSTEM_STRING, HIVECODE_TYPE_SYSTEM_STRING_STRING);
		types.put(HIVECODE_TYPE_SYSTEM_STRING_ARRAY, HIVECODE_TYPE_SYSTEM_STRING_ARRAY_STRING);
		types.put(HIVECODE_TYPE_SYSTEM_INT64, HIVECODE_TYPE_SYSTEM_INT64_STRING);
		types.put(HIVECODE_TYPE_SYSTEM_INT64_ARRAY, HIVECODE_TYPE_SYSTEM_INT64_ARRAY_STRING);
		types.put(HIVECODE_TYPE_SYSTEM_UINT64, HIVECODE_TYPE_SYSTEM_UINT64_STRING);
		types.put(HIVECODE_TYPE_SYSTEM_UINT64_ARRAY, HIVECODE_TYPE_SYSTEM_UINT64_ARRAY_STRING);
		types.put(HIVECODE_TYPE_SYSTEM_IO_MEMORYSTREAM, HIVECODE_TYPE_SYSTEM_IO_MEMORYSTREAM_STRING);
		
		exceptions = new HashMap<Short, String>();
		exceptions.put(HIVECODE_EXCEPTION_SYSTEM_EXCEPTION, HIVECODE_EXCEPTION_SYSTEM_EXCEPTION_STRING);
		exceptions.put(HIVECODE_EXCEPTION_SYSTEM_SYSTEMEXCEPTION, HIVECODE_EXCEPTION_SYSTEM_SYSTEMEXCEPTION_STRING);
		exceptions.put(HIVECODE_EXCEPTION_SYSTEM_OUTOFMEMORYEXCEPTION, HIVECODE_EXCEPTION_SYSTEM_OUTOFMEMORYEXCEPTION_STRING);
		exceptions.put(HIVECODE_EXCEPTION_SYSTEM_ARGUMENTEXCEPTION, HIVECODE_EXCEPTION_SYSTEM_ARGUMENTEXCEPTION_STRING);
		exceptions.put(HIVECODE_EXCEPTION_SYSTEM_ARGUMENTNULLEXCEPTION, HIVECODE_EXCEPTION_SYSTEM_ARGUMENTNULLEXCEPTION_STRING);
		exceptions.put(HIVECODE_EXCEPTION_SYSTEM_NULLREFERENCEEXCEPTION, HIVECODE_EXCEPTION_SYSTEM_NULLREFERENCEEXCEPTION_STRING);
		exceptions.put(HIVECODE_EXCEPTION_SYSTEM_ARGUMENTOUTOFRANGEEXCEPTION, HIVECODE_EXCEPTION_SYSTEM_ARGUMENTOUTOFRANGEEXCEPTION_STRING);
		exceptions.put(HIVECODE_EXCEPTION_SYSTEM_NOTSUPPORTEDEXCEPTION, HIVECODE_EXCEPTION_SYSTEM_NOTSUPPORTEDEXCEPTION_STRING);
		exceptions.put(HIVECODE_EXCEPTION_SYSTEM_INVALIDCASTEXCEPTION, HIVECODE_EXCEPTION_SYSTEM_INVALIDCASTEXCEPTION_STRING);
		exceptions.put(HIVECODE_EXCEPTION_SYSTEM_INVALIDOPERATIONEXCEPTION, HIVECODE_EXCEPTION_SYSTEM_INVALIDOPERATIONEXCEPTION_STRING);
		exceptions.put(HIVECODE_EXCEPTION_SYSTEM_NOTIMPLEMENTEDEXCEPTION, HIVECODE_EXCEPTION_SYSTEM_NOTIMPLEMENTEDEXCEPTION_STRING);
		exceptions.put(HIVECODE_EXCEPTION_SYSTEM_OBJECTDISPOSEDEXCEPTION, HIVECODE_EXCEPTION_SYSTEM_OBJECTDISPOSEDEXCEPTION_STRING);
		exceptions.put(HIVECODE_EXCEPTION_SYSTEM_UNAUTHORIZEDACCESSEXCEPTION, HIVECODE_EXCEPTION_SYSTEM_UNAUTHORIZEDACCESSEXCEPTION_STRING);
		exceptions.put(HIVECODE_EXCEPTION_SYSTEM_INDEXOUTOFRANGEEXCEPTION, HIVECODE_EXCEPTION_SYSTEM_INDEXOUTOFRANGEEXCEPTION_STRING);
		exceptions.put(HIVECODE_EXCEPTION_SYSTEM_FORMATEXCEPTION, HIVECODE_EXCEPTION_SYSTEM_FORMATEXCEPTION_STRING);
		exceptions.put(HIVECODE_EXCEPTION_SYSTEM_ARITHMETICEXCEPTION, HIVECODE_EXCEPTION_SYSTEM_ARITHMETICEXCEPTION_STRING);
		exceptions.put(HIVECODE_EXCEPTION_SYSTEM_OVERFLOWEXCEPTION, HIVECODE_EXCEPTION_SYSTEM_OVERFLOWEXCEPTION_STRING);
		exceptions.put(HIVECODE_EXCEPTION_SYSTEM_BADIMAGEFORMATEXCEPTION, HIVECODE_EXCEPTION_SYSTEM_BADIMAGEFORMATEXCEPTION_STRING);
		exceptions.put(HIVECODE_EXCEPTION_SYSTEM_APPLICATIONEXCEPTION, HIVECODE_EXCEPTION_SYSTEM_APPLICATIONEXCEPTION_STRING);
		exceptions.put(HIVECODE_EXCEPTION_SYSTEM_ARRAYTYPEMISMATCHEXCEPTION, HIVECODE_EXCEPTION_SYSTEM_ARRAYTYPEMISMATCHEXCEPTION_STRING);
		exceptions.put(HIVECODE_EXCEPTION_SYSTEM_DIVIDEBYZEROEXCEPTION, HIVECODE_EXCEPTION_SYSTEM_DIVIDEBYZEROEXCEPTION_STRING);
		exceptions.put(HIVECODE_EXCEPTION_SYSTEM_MEMBERACCESSEXCEPTION, HIVECODE_EXCEPTION_SYSTEM_MEMBERACCESSEXCEPTION_STRING);
		exceptions.put(HIVECODE_EXCEPTION_SYSTEM_MISSINGMEMBEREXCEPTION, HIVECODE_EXCEPTION_SYSTEM_MISSINGMEMBEREXCEPTION_STRING);
		exceptions.put(HIVECODE_EXCEPTION_SYSTEM_MISSINGFIELDEXCEPTION, HIVECODE_EXCEPTION_SYSTEM_MISSINGFIELDEXCEPTION_STRING);
		exceptions.put(HIVECODE_EXCEPTION_SYSTEM_MISSINGMEHTODEXCEPTION, HIVECODE_EXCEPTION_SYSTEM_MISSINGMEHTODEXCEPTION_STRING);
		exceptions.put(HIVECODE_EXCEPTION_SYSTEM_RANKEXCEPTION, HIVECODE_EXCEPTION_SYSTEM_RANKEXCEPTION_STRING);
		exceptions.put(HIVECODE_EXCEPTION_SYSTEM_STACKOVERFLOWEXCEPTION, HIVECODE_EXCEPTION_SYSTEM_STACKOVERFLOWEXCEPTION_STRING);
		exceptions.put(HIVECODE_EXCEPTION_SYSTEM_TYPELOADEXCEPTION, HIVECODE_EXCEPTION_SYSTEM_TYPELOADEXCEPTION_STRING);
		exceptions.put(HIVECODE_EXCEPTION_SYSTEM_IO_IOEXCEPTION, HIVECODE_EXCEPTION_SYSTEM_IO_IOEXCEPTION_STRING);
		exceptions.put(HIVECODE_EXCEPTION_SYSTEM_IO_DIRECTORYNOTFOUNDEXCEPTION, HIVECODE_EXCEPTION_SYSTEM_IO_DIRECTORYNOTFOUNDEXCEPTION_STRING);
		exceptions.put(HIVECODE_EXCEPTION_SYSTEM_IO_FILENOTFOUNDEXCEPTION, HIVECODE_EXCEPTION_SYSTEM_IO_FILENOTFOUNDEXCEPTION_STRING);
		exceptions.put(HIVECODE_EXCEPTION_SYSTEM_RUNTIME_REMOTING_REMOTINGEXCEPTION, HIVECODE_EXCEPTION_SYSTEM_RUNTIME_REMOTING_REMOTINGEXCEPTION_STRING);
		exceptions.put(HIVECODE_EXCEPTION_SYSTEM_SECURITY_CRYPTOGRAPHY_CRYPTOGRAPHICEXCEPTION, HIVECODE_EXCEPTION_SYSTEM_SECURITY_CRYPTOGRAPHY_CRYPTOGRAPHICEXCEPTION_STRING);

	}
	
	public static void checkExceptionInResponse(byte[] data, int offset) throws MSCMException {
		short value;
		String optional = null;
		if(data[offset] == (byte)0xFF) {
			value = MSCMService.bytesToShort(data, offset+5);
			if(data.length > offset + 7) {
				try {
					optional = decodeString(data, offset + 7);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		} else {
			value = MSCMService.bytesToShort(data, offset+4);
			if(data.length > offset + 6) {
				try {
					optional = decodeString(data, offset + 6);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}
		String message = exceptions.get(value);
		if(message != null) {
			
			if(optional == null) {
				throw new MSCMException(value, message);
			} else {
				throw new MSCMException(value, message, optional);
			}
		}
		
	}
	
	public static String namespaceHiveToString(byte[] hive, int offset) {
		int value = MSCMService.bytesToInt(hive, offset);
		String str = namespaces.get(value);
		if(str == null) {
			str =  MSCMService.bytArrayToHex(hive);
		}
		return str;
	}
	
	public static String typeHiveToString(byte[] hive, int offset) {
		short value = MSCMService.bytesToShort(hive, offset);
		String str = types.get(value);
		if(str == null) {
			str =  MSCMService.bytArrayToHex(hive);
		}
		return str;
	}
	
	public static boolean decodeBoolean(byte data) {
		return (data == (byte) 0x01);
	}
	
	public static byte[] decodeByteArray(byte[] data, int offset) {
		int length = MSCMService.bytesToInt(data, offset);
		if(length > data.length - offset - 4) {
			// encoding error
			return null;
		}
		byte[] result = new byte[length];
		for(int i = 0; i < length; i++) {
			result[i] = data[offset + 4 + i];
		}
		return result;
	}
	
	public static short[] decodeShortArray(byte[] data, int offset) {
		int length = MSCMService.bytesToInt(data, offset);
		if(length  * 2 > data.length - offset - 4) {
			// encoding error
			return null;
		}
		short[] result = new short[length];
		for(int i = 0; i < length; i++) {
			result[i] = MSCMService.bytesToShort(data, offset + 4 + i * 2);
		}
		return result;
	}
	
	public static int[] decodeIntArray(byte[] data, int offset) {
		int length = MSCMService.bytesToInt(data, offset);
		if(length  * 4 > data.length - offset - 4) {
			// encoding error
			return null;
		}
		int[] result = new int[length];
		for(int i = 0; i < length; i++) {
			result[i] = MSCMService.bytesToInt(data, offset + 4 + i * 4);
		}
		return result;
	}
	
	public static String[] decodeStringArray(byte[] data, int offset) throws UnsupportedEncodingException {
		int length = MSCMService.bytesToInt(data, offset);
		String[] result = new String[length];
		int curoffset = 4;
		for(int i = 0; i < length; i++) {
			short strlength = MSCMService.bytesToShort(data, offset + curoffset);
			result[i] = decodeString(data, offset + curoffset);
			curoffset += strlength + 2;
		}
		return result;
	}
	
	
	public static String decodeString(byte[] data, int offset) throws UnsupportedEncodingException {
		short length = MSCMService.bytesToShort(data, offset);
		if(length == 0xFFFF) {
			return "";
		}
		
		byte[] utf8Data = new byte[length*2];
		for(int i = 0; i < length; i++) {
			utf8Data[i] = data[offset + 2 + i];
		}
		
		String str = new String(utf8Data, "UTF-8");
		return str;
	}
	
	
	public static byte[] encodeString(String str) throws IOException {
		if(str == null) {
			return new byte[] { (byte) 0xFF, (byte) 0xFF };
		}
		byte[] utf8data = str.getBytes("UTF-8");
		short strinlen = (short)utf8data.length;
		byte[] lendata = MSCMService.shortToBytes(strinlen);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream(strinlen + 2);
		out.write(lendata);
		out.write(utf8data);
		out.close();
		return out.toByteArray();
	}
	
	public static byte[] encodeByteArray(byte[] data) throws IOException {
		int strinlen = (int)data.length;
		byte[] lendata = MSCMService.intToBytes(strinlen);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream(strinlen + 4);
		out.write(lendata);
		out.write(data);
		out.close();
		return out.toByteArray();
	}
	
	public static byte[] concatByteArrays(byte[] a, byte[] b) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream(a.length + b.length);
		out.write(a);
		out.write(b);
		out.close();
		return out.toByteArray();
	}
}
 