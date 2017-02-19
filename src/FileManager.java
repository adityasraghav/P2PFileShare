package src;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileManager 
{
	private int pieceSize = 0;
	private String directory = null;
	private String fileName = null;
	private int fileSize = 0;
	private File file = null;
	
//	private BitField bitField = null;
	
//	public BitField getBitField() {
//		return bitField;
//	}

	public FileManager(int peerid, boolean has) 
	{
		directory = "../peer_" + peerid + "/";
		pieceSize = ConfigParser.getPieceSize();
		fileName = ConfigParser.getFileName();
		fileSize = ConfigParser.getFileSize();
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
		
//		bitField = new BitField((int) (Math.ceil((float) fileSize / pieceSize)));
//		if (has) {
//			bitField.setAll();
//		}
	}
	
	public FilePiece get(int index) 
	{
		try 
		{
			FileInputStream fis = new FileInputStream(file);
			int loc = pieceSize * index;
			fis.skip(loc);
			int contentSize = pieceSize;
			if (fileSize - loc < pieceSize)
				contentSize = fileSize - loc;
			byte[] content = new byte[contentSize];
			fis.read(content);
			fis.close();
			return new FilePiece(content, index);
		}catch (FileNotFoundException e) 
		{
			return null;
		}catch (IOException e) 
		{
			return null;
		}
	}


	public void store(FilePiece piece) 
	{
		int loc = pieceSize * piece.getIndex();
		RandomAccessFile fos = null;
		try {
			fos = new RandomAccessFile(file, "rw");
			fos.seek(loc);
			fos.write(piece.getContent());
			fos.close();
			
//			bitField.set(piece.getIndex());
//			if (bitField.hasAll()) {
//			}
		}catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
}