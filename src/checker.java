package src;

import java.io.File;
import java.util.Arrays;

public class checker {

	public static void main(String[] ar){
		
//		FileManager fileManager = new FileManager(1001, true);
//		FileManager fileManager2= new FileManager(1002, false);
//		
//		try {
//			byte [] ss= fileManager.getBitField();
//			byte [] sv= fileManager2.getBitField();
//
//			
//			while(!fileManager2.hasCompleteFile())
//			{
//				int ll = fileManager.requestPiece(ss, sv);
//				PiecePayload payload= fileManager.get(ll);
//				fileManager2.store(payload);
//			}
//			
//		} catch (Exception e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		
		
	 	
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
		int h = FileManager.requestPiece(l, m,1);
		
		FileUtilities.updateBitfield(1, new byte[]{0});
		
		PeerProcess.getLogger().println("check");
	}
	
	
}
