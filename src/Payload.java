package src;

public interface Payload {

}

class HavePayload implements Payload{
	
	// 4-byte piece index field we will index from -2,147,483,648 to 2,147,483,647
	private int index;
}

class BitfieldPayload implements Payload{
	
	//
}

class RequestPayload implements Payload{
	// 4-byte piece index field we will index from -2,147,483,648 to 2,147,483,647
	private int index;
}

class PiecePayload implements Payload{
	// 4-byte piece index field we will index from -2,147,483,648 to 2,147,483,647
	private int index;
	
	//max byte buffer of length 4,294,967,295 bytes minus 5 bytes to accommodate mType and index fields
	private byte buffer[];
}