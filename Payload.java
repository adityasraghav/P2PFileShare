import java.io.Serializable;

public abstract class Payload implements Serializable{
	
	private static final long serialVersionUID = -380471140315874320L;
	private int msgLength = 0;
	
	public void setMsgLength(int length){
		msgLength = length;
	}
	
	public int getMsgLength(){
		return msgLength;
	}
}

class HavePayload extends Payload{
	
	// 4-byte piece index field we will index from -2,147,483,648 to 2,147,483,647
	private int index;
	public HavePayload(int idx){
		index = idx;
	}
	public int getIndex(){
		return index;
	}
}

class BitfieldPayload extends Payload{
	
	//Representing file pieces present in the corresponding bits
	// A four byte field
	private byte[] bitfield;
	
	public BitfieldPayload(byte[] bitfield){
		this.bitfield = bitfield;
	}
	
	public byte[] getBitfield(){
		return bitfield;
	}
}

class RequestPayload extends Payload{
	// 4-byte piece index field we will index from -2,147,483,648 to 2,147,483,647
	private int index;
	public RequestPayload(int idx){
		index = idx;
	}
	public int getIndex(){
		return index;
	}
}

class PiecePayload extends Payload{
	// 4-byte piece index field we will index from -2,147,483,648 to 2,147,483,647
	private byte[] content = null;
	private int index = 0;

	public PiecePayload(byte[] content, int index) {
		this.index = index;
		this.content = content;
	}

	public byte[] getContent() {
		return content;
	}

	public int getIndex() {
		return index;
	}
}
