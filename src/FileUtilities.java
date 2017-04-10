package src;

public class FileUtilities {
	
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
	
	public static boolean[] byteToBoolean(byte val){
		boolean[] bool = new boolean[8];
		for(int i=0;i<8;i++){
			bool[7-i] = (val&1)==1?true:false;
			val = (byte) (val>>1);
		}
		return bool;
	}

}
