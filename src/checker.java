package src;


public class checker {

	public static void main(String[] ar){
		
		byte b = (byte) 5;
		
		
		byte c = (byte) 0;
		
		boolean [] x = FileUtilities.byteToBoolean(c);
		
		boolean [] ba = FileUtilities.byteToBoolean(b);
		
		byte [] l = new byte[1];
		byte [] m = new byte [1];
		
		try {
			m[0] = FileUtilities.boolToByte(x);
			l[0] = FileUtilities.boolToByte(ba);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		boolean f = FileManager.compareBitfields(l, m);
		int h = FileManager.requestPiece(l, m);
		
		System.out.println("check");
		
	}
	
	
}
