package src;

import java.io.*;

public class HandShakeMsg implements Serializable 
{	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String Header;
	int PeerID;
	
	public HandShakeMsg (int peerID) 
	{
		this.PeerID = peerID;
		//TODO 10 byte zero bits using BitSet
		Header = "P2PFILESHARINGPROJ";
	}
	
	@Override
    public String toString()
    {
        String msgString = "Header :"+this.Header+"\n";
        msgString = msgString+"Peer ID: "+PeerID; 
        return msgString;
    }
	
	public void SendHandShake (OutputStream out) throws IOException 
	{  
		ObjectOutputStream ostream = new ObjectOutputStream(out);  			  
		ostream.writeObject(this);
		System.out.println("sending handshake message with peer" + this.PeerID);
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
				return Response.PeerID;	
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