package src;

import java.util.Arrays;

/**
 * File utility class implementing helper functions
 * 
 * @author Jagan
 */
public class FileUtilities {
	
	/**
	 * Converts a boolean array to byte
	 * 
	 * @param bool boolean array to be converted
	 * @return byte representation of bool
	 * @throws Exception
	 */
	public static byte boolToByte(boolean[] bool) throws Exception{
		if(bool.length > 8)
			throw new Exception("boolean array length exceeded: not compatible with byte");
		byte val=0;
		for(boolean x:bool){
			val=(byte)(val << 1);
			val=(byte)(val| (x?1:0));
		}
		return val;
	}
	
	/**
	 * Converts a byte array to boolean
	 * 
	 * @param val byte to be converted into boolean array
	 * @return boolean representation of byte
	 */
	public static boolean[] byteToBoolean(byte val){
		boolean[] bool = new boolean[8];
		for(int i=0;i<8;i++){
			bool[7-i] = (val&1)==1?true:false;
			val = (byte) (val>>1);
		}
		return bool;
	}

	/**
	 * Updates bitfield of a Peer on receiving have message
	 * @param index
	 */
	public static void updateBitfield(long index, byte[] bitfield){
		int i = (int)(index/8);
		int u = (int)(index%8);
		byte update = 1;
		update = (byte)(update << u);
		System.out.println("Bitfield with index "+u+" in bitfield before update "+Arrays.toString(FileUtilities.byteToBoolean(bitfield[i])));
		bitfield[i] = (byte)(bitfield[i]|update);
		System.out.println("Update bitfield with index "+u+" in bitfield "+Arrays.toString(FileUtilities.byteToBoolean(bitfield[i])));
	}
}
