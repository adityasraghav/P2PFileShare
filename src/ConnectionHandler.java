package src;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
/**
 * Contains connection related information between host peer and neighboring peer including object streams
 * to read and write from the connection.
 * 
 * @author Jagan
 *
 */
public class ConnectionHandler extends Thread{
	
	//private Socket sock;
		
	private Peer host;
	
	private Peer neighbor;
	
	private ObjectOutputStream sout;
	
	private ObjectInputStream sin;
	
	private ObjectInputStream host_sin;
	
	private int piecesDownloaded;
	
	private PeerHandler pHandler;
	
	public ConnectionHandler(){
	}
	
	public ConnectionHandler(Peer h, Peer n, ObjectInputStream i, ObjectOutputStream o, Socket s, PeerHandler p) throws IOException{
		host = h;
		neighbor = n;
		sin = i;
		sout = o;
		//sock = s;
		pHandler = p;
		piecesDownloaded = 0;
		host_sin = new ObjectInputStream(n.getHostSocket().getInputStream());
	}
	
	/*public void setSocket(Socket s){
		sock = s;
	}*/
	
	public void setHostInputStream(ObjectInputStream in){
		host_sin = in;
	}
	
	public void sendMessage(Message msg){
		try {
			sout.writeObject(msg);
			sout.reset();
			sout.flush();
		} catch (ConnectException e){
			neighbor.setPeerUp(false);
		} catch (IOException e) {
			System.out.println(host.getPeerId()+": Error sending message to "+neighbor.getPeerId());
			e.printStackTrace();
		}
	}
	
	public void receiveMessage(){
		// Waits on input stream of the connection to read incoming messages
		new Thread(){
			public void run(){

				Message recv = null;
				// flag to check choke and unchoke status
				boolean flagUnchoke = false;
				while(true){
					try {
						recv = (Message) host_sin.readObject();
						System.out.println("Received message type: "+ recv.getMsgType() +" from: "+neighbor.getPeerId());
						if(recv != null){
							switch (recv.getMsgType()){
							case UNCHOKE:{
								flagUnchoke = true;
								PeerProcess.getLogger().unchoked(neighbor.getPeerId());
								sendRequest();
								break;}
							case CHOKE:
								//TODO stop sending file pieces
								PeerProcess.getLogger().choked(neighbor.getPeerId());
								flagUnchoke = false;
								break;
							case HAVE:{
								HavePayload have = (HavePayload)(recv.mPayload);
								FileUtilities.updateBitfield(have.getIndex(),neighbor.getBitfield());
								System.out.println("Peer "+neighbor.getPeerId()+" contains interesting file pieces");
								PeerProcess.getLogger().haveRecieved(neighbor.getPeerId(), have.getIndex());
								//Check whether the piece is interesting and send interested message	
								if(!FileManager.isInteresting(have.getIndex()))
								{
									Message interested = new Message(MessageType.INTERESTED,null);
									sendMessage(interested);
								}
								break;}
							case REQUEST: {
								int requestedIndex = ((RequestPayload)recv.mPayload).getIndex();
								byte [] pieceContent = FileManager.get(requestedIndex).getContent();
								int pieceIndex = FileManager.get(requestedIndex).getIndex();
								Message pieceToSend = new Message(MessageType.PIECE, new PiecePayload(pieceContent, pieceIndex));
								sendMessage(pieceToSend);
								break;}
							case INTERESTED:
								pHandler.add(neighbor);
								PeerProcess.getLogger().intRecieved(neighbor.getPeerId());
								break;
							case NOT_INTERESTED:
								pHandler.remove(neighbor);
								PeerProcess.getLogger().notIntRecieved(neighbor.getPeerId());
								break;
							case BITFIELD:{
								BitfieldPayload in_payload = (BitfieldPayload)(recv.mPayload);
								//setting bitfield for the neighboring peer
								neighbor.setBitfield(in_payload.getBitfield());
								if(!FileManager.compareBitfields(in_payload.getBitfield(),host.getBitfield() )){
									System.out.println("Peer "+neighbor.getPeerId()+" does not contain any interesting file pieces");
									Message notInterested = new Message(MessageType.NOT_INTERESTED,null);
									sendMessage(notInterested);
									break;
								}
								System.out.println("Peer "+neighbor.getPeerId()+" contains interesting file pieces");
								Message interested = new Message(MessageType.INTERESTED,null);
								sendMessage(interested);
								// No need to add peers that you are interested in.

								break;}
							case PIECE:{

								try
								{
									FileManager.store((PiecePayload)recv.mPayload);
								}catch (Exception e) {
									// TODO: handle exception
									e.printStackTrace();
								}
								FileUtilities.updateBitfield(((PiecePayload)recv.mPayload).getIndex(),host.getBitfield());
								
								pHandler.sendHaveAll(((PiecePayload)recv.mPayload).getIndex());
								piecesDownloaded++;
								PeerProcess.getLogger().downloading(neighbor.getPeerId(), ((PiecePayload)recv.mPayload).getIndex(), piecesDownloaded);
								if(flagUnchoke)sendRequest();
								break;}
							}
						}
					}catch (SocketException e) {						
						neighbor.setPeerUp(false);
					} 
					catch (ClassNotFoundException | IOException e) {		
						
						System.out.println(host.getPeerId()+": Error recieving message from "+neighbor.getPeerId());
						e.printStackTrace();
					} catch (Exception e){
						e.printStackTrace();
					}
					//}
				}
			}
			/**
			 * Sends request message with piece index to neighbor
			 */
			void sendRequest(){
				int pieceIdx = FileManager.requestPiece(neighbor.getBitfield(), host.getBitfield(),neighbor.getPeerId());
				if(pieceIdx == -1){
					System.out.println("No more interesting pieces to request from peer "+neighbor.getPeerId());
					return;
				}
				Payload requestPayload = new RequestPayload(pieceIdx);
				Message msgRequest = new Message(MessageType.REQUEST, requestPayload);
				try {
					sout.writeObject(msgRequest);
					sout.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}.start();
	}
	
	public void run(){
		
		receiveMessage();
		FileManager.checker();
		neighbor.setDownloadSpeed(piecesDownloaded);
	}
	
	/**
	 * After p seconds of time interval this will be called to reset download rate
	 */
	public void resetPiecesDownloaded()
	{
		piecesDownloaded = 0;
	}
	
}
