package at.gv.egiz.smcc.util;

public interface MSCMConstants {
	public static final int HIVECODE_METHOD_GET_PROPERTIES = 0x8187;
	
	
	public static final short DEFAULT_SERVICE_PORT = 5;
	
	public static final String URI = "MSCM";
	
	public static final short HIVECODE_TYPE_DEFAULT									= (short)0x7FBD;
	
	// Methods
	public static final short HIVECODE_METHOD_GETCARDPROPERTY 						= (short)0x8187;
	public static final short HIVECODE_METHOD_GETCONTAINERPROPERTY 					= (short)0x279C;
	public static final short HIVECODE_METHOD_GETVERSION 							= (short)0xDEEC;
	public static final short HIVECODE_METHOD_GETFILES 								= (short)0xE72B;
	public static final short HIVECODE_METHOD_GETTRIESREMAINING						= (short)0x6D08;
	public static final short HIVECODE_METHOD_PRIVATEKEYDECRYPT						= (short)0x6144;
	public static final short HIVECODE_METHOD_GETFILEPROPERTIES						= (short)0xA01B;
	public static final short HIVECODE_METHOD_QUERYFREESPACE						= (short)0x00E5;
	public static final short HIVECODE_METHOD_FORCEGC								= (short)0x3D38;
	public static final short HIVECODE_METHOD_READFILE								= (short)0x744C;
	public static final short HIVECODE_METHOD_VERIFYPIN								= (short)0x506B;
	public static final short HIVECODE_METHOD_CHANGEREFDATA							= (short)0xE08A;
	public static final short HIVECODE_METHOD_GETCHALLENGE							= (short)0xFA3B;
	public static final short HIVECODE_METHOD_EXTAUTH								= (short)0x24FE;
	
	
	// Property Fields
	public static final byte PROPERTY_CARD_FREE_SPACE								= (byte)0x00;
	public static final byte PROPERTY_CARD_KEY_SIZES								= (byte)0x02;
	public static final byte PROPERTY_CARD_READ_ONLY								= (byte)0x03;
	public static final byte PROPERTY_CARD_CACHE_MODE								= (byte)0x04;
	public static final byte PROPERTY_CARD_GUID										= (byte)0x05;
	public static final byte PROPERTY_CARD_SERIAL_NUMBER							= (byte)0x06;
	public static final byte PROPERTY_CARD_PIN_INFO									= (byte)0x07;
	public static final byte PROPERTY_CARD_ROLES_LIST								= (byte)0x08;
	public static final byte PROPERTY_CARD_AUTHENTICATED_ROLES						= (byte)0x09;
	public static final byte PROPERTY_CARD_PIN_STRENGTH								= (byte)0x0A;
	public static final byte PROPERTY_CARD_UNBLOCK_FP_SYNC							= (byte)0xF9;
	public static final byte PROPERTY_CARD_PIN_POLICY								= (byte)0x80;
	public static final byte PROPERTY_CARD_X509_ENROLL								= (byte)0x0D;
	public static final byte PROPERTY_CARD_CHANGE_PIN_FIRST							= (byte)0xFA;
	public static final byte PROPERTY_CARD_IMPORT_ALLOWED							= (byte)0x90;
	public static final byte PROPERTY_CARD_IMPORT_CHANGE_ALLOWED					= (byte)0x91;
	public static final byte PROPERTY_CARD_PKI_OFF									= (byte)0xF7;
	public static final byte PROPERTY_CARD_VERSION_INFO								= (byte)0xFF;
	
	public static final byte PROPERTY_CONTAINER_INFO								= (byte)0x00;
	public static final byte PROPERTY_CONTAINER_PIN_IDENTIFIER						= (byte)0x01;
	public static final byte PROPERTY_CONTAINER_TYPE								= (byte)0x80;
	
	// Types
	// =============================================================================================
	public static final short HIVECODE_TYPE_SMARTCARD_CONTENTMANAGER                = (short)0xB18C;
	public static final short HIVECODE_TYPE_SYSTEM_VOID								= (short)0xCE81;
	public static final short HIVECODE_TYPE_SYSTEM_INT32							= (short)0x61C0;
	public static final short HIVECODE_TYPE_SYSTEM_INT32_ARRAY						= (short)0x61C1;
	public static final short HIVECODE_TYPE_SYSTEM_BOOLEAN							= (short)0x2227;
	public static final short HIVECODE_TYPE_SYSTEM_BOOLEAN_ARRAY					= (short)0x2228;
	public static final short HIVECODE_TYPE_SYSTEM_SBYTE							= (short)0x767E;
	public static final short HIVECODE_TYPE_SYSTEM_SBYTE_ARRAY						= (short)0x767F;
	public static final short HIVECODE_TYPE_SYSTEM_UINT16 							= (short)0xD98B;
	public static final short HIVECODE_TYPE_SYSTEM_UINT16_ARRAY						= (short)0xD98C;
	public static final short HIVECODE_TYPE_SYSTEM_UINT32 							= (short)0x95E7;
	public static final short HIVECODE_TYPE_SYSTEM_UINT32_ARRAY						= (short)0x95E8;
	public static final short HIVECODE_TYPE_SYSTEM_BYTE 							= (short)0x45A2;
	public static final short HIVECODE_TYPE_SYSTEM_BYTE_ARRAY						= (short)0x45A3;
	public static final short HIVECODE_TYPE_SYSTEM_CHAR 							= (short)0x958E;
	public static final short HIVECODE_TYPE_SYSTEM_CHAR_ARRAY						= (short)0x958F;
	public static final short HIVECODE_TYPE_SYSTEM_INT16 							= (short)0xBC39;
	public static final short HIVECODE_TYPE_SYSTEM_INT16_ARRAY						= (short)0xBC3A;
	public static final short HIVECODE_TYPE_SYSTEM_STRING 							= (short)0x1127;
	public static final short HIVECODE_TYPE_SYSTEM_STRING_ARRAY						= (short)0x1128;
	public static final short HIVECODE_TYPE_SYSTEM_INT64 							= (short)0xDEFB;
	public static final short HIVECODE_TYPE_SYSTEM_INT64_ARRAY						= (short)0xDEFC;
	public static final short HIVECODE_TYPE_SYSTEM_UINT64 							= (short)0x71AF;
	public static final short HIVECODE_TYPE_SYSTEM_UINT64_ARRAY						= (short)0x71B0;
	public static final short HIVECODE_TYPE_SYSTEM_IO_MEMORYSTREAM					= (short)0xFED7;
	
	
	
	public static final String HIVECODE_TYPE_SYSTEM_VOID_STRING								= "System.Void";
	public static final String HIVECODE_TYPE_SYSTEM_INT32_STRING							= "System.Int32";
	public static final String HIVECODE_TYPE_SYSTEM_INT32_ARRAY_STRING						= "System.Int32[]";
	public static final String HIVECODE_TYPE_SYSTEM_BOOLEAN_STRING							= "System.Boolean";
	public static final String HIVECODE_TYPE_SYSTEM_BOOLEAN_ARRAY_STRING					= "System.Boolean[]";
	public static final String HIVECODE_TYPE_SYSTEM_SBYTE_STRING							= "System.SByte";
	public static final String HIVECODE_TYPE_SYSTEM_SBYTE_ARRAY_STRING						= "System.SByte[]";
	public static final String HIVECODE_TYPE_SYSTEM_UINT16_STRING 							= "System.UInt16";
	public static final String HIVECODE_TYPE_SYSTEM_UINT16_ARRAY_STRING						= "System.UInt16[]";
	public static final String HIVECODE_TYPE_SYSTEM_UINT32_STRING 							= "System.UInt32";
	public static final String HIVECODE_TYPE_SYSTEM_UINT32_ARRAY_STRING						= "System.UInt32[]";
	public static final String HIVECODE_TYPE_SYSTEM_BYTE_STRING 							= "System.Byte";
	public static final String HIVECODE_TYPE_SYSTEM_BYTE_ARRAY_STRING						= "System.Byte[]";
	public static final String HIVECODE_TYPE_SYSTEM_CHAR_STRING 							= "System.Char";
	public static final String HIVECODE_TYPE_SYSTEM_CHAR_ARRAY_STRING						= "System.Char[]";
	public static final String HIVECODE_TYPE_SYSTEM_INT16_STRING 							= "System.Int16";
	public static final String HIVECODE_TYPE_SYSTEM_INT16_ARRAY_STRING						= "System.Int16[]";
	public static final String HIVECODE_TYPE_SYSTEM_STRING_STRING 							= "System.String";
	public static final String HIVECODE_TYPE_SYSTEM_STRING_ARRAY_STRING						= "System.String[]";
	public static final String HIVECODE_TYPE_SYSTEM_INT64_STRING 							= "System.Int64";
	public static final String HIVECODE_TYPE_SYSTEM_INT64_ARRAY_STRING						= "System.Int64[]";
	public static final String HIVECODE_TYPE_SYSTEM_UINT64_STRING 							= "System.UInt64";
	public static final String HIVECODE_TYPE_SYSTEM_UINT64_ARRAY_STRING						= "System.UInt64[]";
	public static final String HIVECODE_TYPE_SYSTEM_IO_MEMORYSTREAM_STRING					= "System.IO.MemoryStream";
	
	
	// Namespaces
	// ==========================================================================================
	public static final int HIVECODE_NAMESPACE_SYSTEM                               = 0x00D25D1C;
	public static final int HIVECODE_NAMESPACE_SYSTEM_IO                            = 0x00D5E6DB;
	public static final int HIVECODE_NAMESPACE_SYSTEM_RUNTIME_REMOTING_CHANNELS     = 0x0000886E;
	public static final int HIVECODE_NAMESPACE_NETCARD_FILESYSTEM                   = 0x00A1AC39;
	public static final int HIVECODE_NAMESPACE_SYSTEM_RUNTIME_REMOTING              = 0x00EB3DD9;
	public static final int HIVECODE_NAMESPACE_SYSTEM_SECURITY_CRYPTOGRAPHY         = 0x00ACF53B;
	public static final int HIVECODE_NAMESPACE_SYSTEM_COLLECTIONS                   = 0x00C5A010;
	public static final int HIVECODE_NAMESPACE_SYSTEM_RUNTIME_REMOTING_CONTEXTS     = 0x001F4994;
	public static final int HIVECODE_NAMESPACE_SYSTEM_SECURITY                      = 0x00964145;
	public static final int HIVECODE_NAMESPACE_SYSTEM_REFLECTION                    = 0x0008750F;
	public static final int HIVECODE_NAMESPACE_SYSTEM_RUNTIME_SERIALIZATION         = 0x008D3B3D;
	public static final int HIVECODE_NAMESPACE_SYSTEM_RUNTIME_REMOTING_MESSAGING    = 0x00DEB940;
	public static final int HIVECODE_NAMESPACE_SYSTEM_DIAGNOSTICS                   = 0x0097995F;
	public static final int HIVECODE_NAMESPACE_SYSTEM_RUNTIME_COMPILERSERVICES      = 0x00F63E11;
	public static final int HIVECODE_NAMESPACE_SYSTEM_TEXT                          = 0x00702756;
	public static final int HIVECODE_NAMESPACE_SMARTCARD                            = 0x00F5EFBF;
	
	public static final String HIVECODE_NAMESPACE_SYSTEM_STRING								= "System";
	public static final String HIVECODE_NAMESPACE_SYSTEM_IO_STRING							= "System.IO";
	public static final String HIVECODE_NAMESPACE_SYSTEM_RUNTIME_REMOTING_CHANNELS_STRING	= "System.Runtime.Remoting.Channels";
	public static final String HIVECODE_NAMESPACE_NETCARD_FILESYSTEM_STRING					= "Netcard.Filesystem";
	public static final String HIVECODE_NAMESPACE_SYSTEM_RUNTIME_REMOTING_STRING			= "System.Runtime.Remoting";
	public static final String HIVECODE_NAMESPACE_SYSTEM_SECURITY_CRYPTOGRAPHY_STRING		= "System.Security.Cryptography";
	public static final String HIVECODE_NAMESPACE_SYSTEM_COLLECTIONS_STRING					= "System.Collections";
	public static final String HIVECODE_NAMESPACE_SYSTEM_RUNTIME_REMOTING_CONTEXTS_STRING	= "System.Runtime.Remoting.Contexts";
	public static final String HIVECODE_NAMESPACE_SYSTEM_SECURITY_STRING					= "System.Security";
	public static final String HIVECODE_NAMESPACE_SYSTEM_REFLECTION_STRING					= "System.Reflection";
	public static final String HIVECODE_NAMESPACE_SYSTEM_RUNTIME_SERIALIZATION_STRING		= "System.Runtime.Serialization";
	public static final String HIVECODE_NAMESPACE_SYSTEM_RUNTIME_REMOTING_MESSAGING_STRING	= "System.Runtime.Remoting.Messaging";
	public static final String HIVECODE_NAMESPACE_SYSTEM_DIAGNOSTICS_STRING					= "System.Diagnostics";
	public static final String HIVECODE_NAMESPACE_SYSTEM_RUNTIME_COMPILERSERVICES_STRING	= "System.Runtime.Compilerservices";
	public static final String HIVECODE_NAMESPACE_SYSTEM_TEXT_STRING						= "System.Text";
	public static final String HIVECODE_NAMESPACE_SMARTCARD_STRING							= "Smartcard";
	
	public static final int HIVECODE_NAMESPACE_GEMALTO								= 0x00C04B4E;
	
	// Exceptions
	// ============================================================================================
	public static final short HIVECODE_EXCEPTION_SYSTEM_EXCEPTION										= (short) 0xD4B0;
	public static final short HIVECODE_EXCEPTION_SYSTEM_SYSTEMEXCEPTION									= (short) 0x28AC;
	public static final short HIVECODE_EXCEPTION_SYSTEM_OUTOFMEMORYEXCEPTION							= (short) 0xE14E;
	public static final short HIVECODE_EXCEPTION_SYSTEM_ARGUMENTEXCEPTION								= (short) 0xAB8C;
	public static final short HIVECODE_EXCEPTION_SYSTEM_ARGUMENTNULLEXCEPTION							= (short) 0x2138;
	public static final short HIVECODE_EXCEPTION_SYSTEM_NULLREFERENCEEXCEPTION							= (short) 0xC5B8;
	public static final short HIVECODE_EXCEPTION_SYSTEM_ARGUMENTOUTOFRANGEEXCEPTION						= (short) 0x6B11;
	public static final short HIVECODE_EXCEPTION_SYSTEM_NOTSUPPORTEDEXCEPTION							= (short) 0xAA74;
	public static final short HIVECODE_EXCEPTION_SYSTEM_INVALIDCASTEXCEPTION							= (short) 0xD24F;
	public static final short HIVECODE_EXCEPTION_SYSTEM_INVALIDOPERATIONEXCEPTION						= (short) 0xFAB4;
	public static final short HIVECODE_EXCEPTION_SYSTEM_NOTIMPLEMENTEDEXCEPTION							= (short) 0x3CE5;
	public static final short HIVECODE_EXCEPTION_SYSTEM_OBJECTDISPOSEDEXCEPTION							= (short) 0x0FAC;
	public static final short HIVECODE_EXCEPTION_SYSTEM_UNAUTHORIZEDACCESSEXCEPTION						= (short) 0x4697;
	public static final short HIVECODE_EXCEPTION_SYSTEM_INDEXOUTOFRANGEEXCEPTION						= (short) 0xBF1D;
	public static final short HIVECODE_EXCEPTION_SYSTEM_FORMATEXCEPTION									= (short) 0xF3BF;
	public static final short HIVECODE_EXCEPTION_SYSTEM_ARITHMETICEXCEPTION								= (short) 0x6683;
	public static final short HIVECODE_EXCEPTION_SYSTEM_OVERFLOWEXCEPTION								= (short) 0x20A0;
	public static final short HIVECODE_EXCEPTION_SYSTEM_BADIMAGEFORMATEXCEPTION							= (short) 0x530A;
	public static final short HIVECODE_EXCEPTION_SYSTEM_APPLICATIONEXCEPTION							= (short) 0xB1EA;
	public static final short HIVECODE_EXCEPTION_SYSTEM_ARRAYTYPEMISMATCHEXCEPTION						= (short) 0x3F88;
	public static final short HIVECODE_EXCEPTION_SYSTEM_DIVIDEBYZEROEXCEPTION							= (short) 0xDFCF;
	public static final short HIVECODE_EXCEPTION_SYSTEM_MEMBERACCESSEXCEPTION							= (short) 0xF5F3;
	public static final short HIVECODE_EXCEPTION_SYSTEM_MISSINGMEMBEREXCEPTION							= (short) 0x20BB;
	public static final short HIVECODE_EXCEPTION_SYSTEM_MISSINGFIELDEXCEPTION							= (short) 0x7366;
	public static final short HIVECODE_EXCEPTION_SYSTEM_MISSINGMEHTODEXCEPTION							= (short) 0x905B;
	public static final short HIVECODE_EXCEPTION_SYSTEM_RANKEXCEPTION									= (short) 0xB2AE;
	public static final short HIVECODE_EXCEPTION_SYSTEM_STACKOVERFLOWEXCEPTION							= (short) 0x0844;
	public static final short HIVECODE_EXCEPTION_SYSTEM_TYPELOADEXCEPTION								= (short) 0x048E;
	public static final short HIVECODE_EXCEPTION_SYSTEM_IO_IOEXCEPTION									= (short) 0x3BBE;
	public static final short HIVECODE_EXCEPTION_SYSTEM_IO_DIRECTORYNOTFOUNDEXCEPTION					= (short) 0x975A;
	public static final short HIVECODE_EXCEPTION_SYSTEM_IO_FILENOTFOUNDEXCEPTION						= (short) 0x07EB;
	public static final short HIVECODE_EXCEPTION_SYSTEM_RUNTIME_REMOTING_REMOTINGEXCEPTION				= (short) 0xD52A;
	public static final short HIVECODE_EXCEPTION_SYSTEM_SECURITY_CRYPTOGRAPHY_CRYPTOGRAPHICEXCEPTION	= (short) 0x8FEB;

	
	public static final String HIVECODE_EXCEPTION_SYSTEM_EXCEPTION_STRING										= "System.Exception";
	public static final String HIVECODE_EXCEPTION_SYSTEM_SYSTEMEXCEPTION_STRING									= "System.SystemException";
	public static final String HIVECODE_EXCEPTION_SYSTEM_OUTOFMEMORYEXCEPTION_STRING							= "System.OutOfMemoryException";
	public static final String HIVECODE_EXCEPTION_SYSTEM_ARGUMENTEXCEPTION_STRING								= "System.ArgumentException";
	public static final String HIVECODE_EXCEPTION_SYSTEM_ARGUMENTNULLEXCEPTION_STRING							= "System.ArgumentNullException";
	public static final String HIVECODE_EXCEPTION_SYSTEM_NULLREFERENCEEXCEPTION_STRING							= "System.NullReferenceException";
	public static final String HIVECODE_EXCEPTION_SYSTEM_ARGUMENTOUTOFRANGEEXCEPTION_STRING						= "System.ArgumentOutOfRangeException";
	public static final String HIVECODE_EXCEPTION_SYSTEM_NOTSUPPORTEDEXCEPTION_STRING							= "System.NotSupportedException";
	public static final String HIVECODE_EXCEPTION_SYSTEM_INVALIDCASTEXCEPTION_STRING							= "System.InvalidCastException";
	public static final String HIVECODE_EXCEPTION_SYSTEM_INVALIDOPERATIONEXCEPTION_STRING						= "System.InvalidOperationException";
	public static final String HIVECODE_EXCEPTION_SYSTEM_NOTIMPLEMENTEDEXCEPTION_STRING							= "System.NotImplementedException";
	public static final String HIVECODE_EXCEPTION_SYSTEM_OBJECTDISPOSEDEXCEPTION_STRING							= "System.ObjectDisposed Exception";
	public static final String HIVECODE_EXCEPTION_SYSTEM_UNAUTHORIZEDACCESSEXCEPTION_STRING						= "System.UnauthorizedAccessException";
	public static final String HIVECODE_EXCEPTION_SYSTEM_INDEXOUTOFRANGEEXCEPTION_STRING						= "System.IndexOutOfRangeException";
	public static final String HIVECODE_EXCEPTION_SYSTEM_FORMATEXCEPTION_STRING									= "System.FormatException";
	public static final String HIVECODE_EXCEPTION_SYSTEM_ARITHMETICEXCEPTION_STRING								= "System.ArithmeticException";
	public static final String HIVECODE_EXCEPTION_SYSTEM_OVERFLOWEXCEPTION_STRING								= "System.OverflowException";
	public static final String HIVECODE_EXCEPTION_SYSTEM_BADIMAGEFORMATEXCEPTION_STRING							= "System.BadImageFormatException";
	public static final String HIVECODE_EXCEPTION_SYSTEM_APPLICATIONEXCEPTION_STRING							= "System.ApplicationException";
	public static final String HIVECODE_EXCEPTION_SYSTEM_ARRAYTYPEMISMATCHEXCEPTION_STRING						= "System.ArrayTypeMismatchException";
	public static final String HIVECODE_EXCEPTION_SYSTEM_DIVIDEBYZEROEXCEPTION_STRING							= "System.DivideByZeroException";
	public static final String HIVECODE_EXCEPTION_SYSTEM_MEMBERACCESSEXCEPTION_STRING							= "System.MemberAccessException";
	public static final String HIVECODE_EXCEPTION_SYSTEM_MISSINGMEMBEREXCEPTION_STRING							= "System.MissingMemberException";
	public static final String HIVECODE_EXCEPTION_SYSTEM_MISSINGFIELDEXCEPTION_STRING							= "System.MissingFieldException";
	public static final String HIVECODE_EXCEPTION_SYSTEM_MISSINGMEHTODEXCEPTION_STRING							= "System.MissingMethodException";
	public static final String HIVECODE_EXCEPTION_SYSTEM_RANKEXCEPTION_STRING									= "System.RankException";
	public static final String HIVECODE_EXCEPTION_SYSTEM_STACKOVERFLOWEXCEPTION_STRING							= "System.StackOverflowException";
	public static final String HIVECODE_EXCEPTION_SYSTEM_TYPELOADEXCEPTION_STRING								= "System.TypeLoadException";
	public static final String HIVECODE_EXCEPTION_SYSTEM_IO_IOEXCEPTION_STRING									= "System.IO.IOException";
	public static final String HIVECODE_EXCEPTION_SYSTEM_IO_DIRECTORYNOTFOUNDEXCEPTION_STRING					= "System.IO.DirectoryNotFoundException";
	public static final String HIVECODE_EXCEPTION_SYSTEM_IO_FILENOTFOUNDEXCEPTION_STRING						= "System.IO.FileNotFoundException";
	public static final String HIVECODE_EXCEPTION_SYSTEM_RUNTIME_REMOTING_REMOTINGEXCEPTION_STRING				= "System.Runtime.Remoting.RemotingException";
	public static final String HIVECODE_EXCEPTION_SYSTEM_SECURITY_CRYPTOGRAPHY_CRYPTOGRAPHICEXCEPTION_STRING	= "System.Security.Cryptography.CryptographicException";

	
}
