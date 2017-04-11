package src;

import java.io.*;

public class HandShakeMsg implements Serializable 
{	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8034174401138365022L;

	static final String Header = "P2PFILESHARINGPROJ";
	private final String _header;
	private int peerID;
	
	public HandShakeMsg (int peerID) 
	{
		this.peerID = peerID;
		//TODO 10 byte zero bits using BitSet
		_header = Header;
	}
	public int getPeerId(){
		return peerID;
	}
	
	public String getHeader(){
		return _header;
	}
	
	@Override
    public String toString()
    {
        String msgString = "Header :"+this._header+"\n";
        msgString = msgString+"Peer ID: "+peerID; 
        return msgString;
    }
	
	public void SendHandShake (OutputStream out) throws IOException 
	{  
		ObjectOutputStream ostream = new ObjectOutputStream(out);  			  
		ostream.writeObject(this);
		System.out.println("sending handshake message with peer" + this.peerID);
	}
	
	//return value could be changed to HandShakeMsg if header is also needed
	public int ReceiveHandShake (InputStream in) throws IOException 
	{
		try 
		{
			ObjectInputStream istream = new ObjectInputStream(in);  
			HandShakeMsg Response = (HandShakeMsg)istream.readObject();  
			if (Response != null) 
			{		
				return Response.peerID;	
			}
			else {
				return -1;
			}	
		} 
		catch (ClassNotFoundException ex) {
			System.out.println(ex);
		}
		finally {
			//is.close();
			//ois.close();
		}
		return -1;
	}
}