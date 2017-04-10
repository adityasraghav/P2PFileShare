package src;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Hashtable;

/**
 * @author Aditya Singh Raghav
 *
 */
public class FileManager 
{
	/** File pieces owned by the peer */
	private static boolean[] filePiecesOwned;
	
	/** Table to keep track of requested file pieces at any instant so that redundant requests are not made */
	private static Hashtable<Integer, Integer> requestedPieces = new Hashtable<Integer, Integer>();
	
	/** Number of file pieces the file can be broken into */
	private static final int noOfFilePieces = (int)Math.ceil((double)ConfigParser.getFileSize()/ConfigParser.getPieceSize());

	/** File pieces available by the peer */
	private static int noOfPiecesAvailable = 0;
	
	private String directory = null;
	private String fileName = null;
	private static int fileSize = 0;
	private static File file = null;

	
	/**
	 * Creates bitfield first time during initialization for the peer
	 * 
	 * @param pieces boolean representation of pieces available
	 * @return byte format of the pieces available
	 * @throws Exception
	 */
    public byte[] getBitField() throws Exception {

        /*int size = filePiecesOwned.length;
        BitSet bSet = new BitSet(size);

        for(int i =0;i<size;i++)
        {
                if(filePiecesOwned[i])
                        bSet.set(i);
                else
                        bSet.clear(i);
        }
        return bSet.toByteArray();*/
        int size = (int)Math.ceil((double)noOfFilePieces/8);
        byte[] bitfield = new byte[size];
        //if(noOfPiecesAvailable == 0)
        //      return null;

        int counter = 0;
        for(int i=0;i<noOfFilePieces;i=i+8){
                bitfield[counter++] = FileUtilities.boolToByte(Arrays.copyOfRange(filePiecesOwned, i, i+8));
        }

        return bitfield;

    }
	
	

	/**
	 * Constructor for the instance of FileManager
	 * @param peerid specifies the peerid for the file manager
	 * @param has specifies if the peer has the file
	 */
	public FileManager(int peerid , boolean has) 
	{
		directory = "../peer_" + peerid + "/";
		fileName = ConfigParser.getFileName();
		
		filePiecesOwned = new boolean[noOfFilePieces];
		
		if(has)
		{
			Arrays.fill(filePiecesOwned, true);
			noOfPiecesAvailable = noOfFilePieces;
		}
		
		File folder = new File(directory);

		if (!folder.exists()) 
		{
			folder.mkdirs();
		}

		file = new File(directory + fileName);
		
		if (!file.exists()) 
		{
			FileOutputStream fos = null;
			try 
			{
				fos = new FileOutputStream(file);
				fos.write(new byte[fileSize]);
				fos.close();
			}catch (IOException e) 
			{
				e.printStackTrace();
			}
		}	
	}
	
	/**
	 * @param index index of the file piece requested
	 * @return the file piece that was requested
	 */
	public static synchronized PiecePayload get(int index) 
	{
		try 
		{
			FileInputStream fis = new FileInputStream(file);
			int loc = ConfigParser.getPieceSize() * index;
			fis.skip(loc);
			int contentSize = ConfigParser.getPieceSize();
			if (fileSize - loc < ConfigParser.getPieceSize())
				contentSize = fileSize - loc;
			byte[] content = new byte[contentSize];
			fis.read(content);
			fis.close();
			return new PiecePayload(content, index);
		}catch (FileNotFoundException e) 
		{
			return null;
		}catch (IOException e) 
		{
			return null;
		}
	}

	
	/**
	 * @param piece The file piece that needs to be added to the file
	 * @throws Exception If unable to add the file piece to the file
	 */
	public static synchronized void store(PiecePayload piece) throws Exception
	{
		int loc = ConfigParser.getPieceSize() * piece.getIndex();
		RandomAccessFile fos = null;
		try {
			fos = new RandomAccessFile(file, "rw");
			fos.seek(loc);
			fos.write(piece.getContent());
			fos.close();
			
			noOfPiecesAvailable++;
			filePiecesOwned[piece.getIndex()] = true;
			
		}catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	


	
	/**
	 * Compares bitfields of two peers to decide interesting or not interesting
	 * 
	 * @param neighborBitfield Neighboring peer's bitfield
	 * @param bitfield Host peer's bitfield
	 * @return
	 */
	public static boolean compareBitfields(byte[] neighborBitfield, byte[] bitfield){
		boolean flag = false;
		int size = (int)Math.ceil((double)noOfFilePieces/8);
		byte[] interesting = new byte[size];
		if(neighborBitfield == null) return flag;
		for(int i=0;i<bitfield.length;i++){
			interesting[i] = (byte) ((bitfield[i]^neighborBitfield[i])&neighborBitfield[i]);
			if(interesting[i] != 0)
				flag = true;
		}
		return flag;
	}
	
	/**
	 * Randomly requests a file piece to a neighbor which it does not have and has not been requested to other peers
	 * 
	 * @param neighborBitfield Neighboring peer's bitfield
	 * @param bitfield Host peer's bitfield
	 * @return
	 */
	public static int requestPiece(byte[] neighborBitfield, byte[] bitfield)
	{
		int size = (int)Math.ceil((double)noOfFilePieces/8);
		byte[] interesting = new byte[size];
		boolean[] interestingPieces = new boolean[noOfFilePieces];
		
		for(int i=0,j=0;i<bitfield.length;i++,j=j+8){
			interesting[i] = (byte) ((bitfield[i]^neighborBitfield[i])&neighborBitfield[i]);
			System.arraycopy(FileUtilities.byteToBoolean(interesting[i]), 0, interestingPieces, j, 8);
		}
		for(int i=0; i<noOfFilePieces; i++){
			if(interestingPieces[i] == true && !requestedPieces.containsKey(i))
				return i;
		}
		// TODO make it fail safe
		return 0;
	}
	
	
	/**
	 * @param index The index of the piece we need to check
	 * @return true if the piece is not available
	 */
	public static boolean isInteresting(int index)
	{
		if(filePiecesOwned[index])
		{
			return true;
		}	
		return false;		
	}
	
	/**
	 * @return true if the file has all the file pieces/complete file
	 */
	public static boolean hasCompleteFile(){
		if(noOfPiecesAvailable < noOfFilePieces)
			return false;
		return true;
	}
}
