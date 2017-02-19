package src;

public class Message {
	
	// Message Length in bytes excluding the length of message length field
	private int mLength;
	
	// Message Type
	private byte mType;
	
	// Message payload
	Payload mPayload;
	
	public Message(){}
	
	public Message(int length, byte type, Payload payload){
		mLength = length;
		mType = type;
		mPayload = payload;
	}
}
