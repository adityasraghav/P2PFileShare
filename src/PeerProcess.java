package src;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;

/**
 * This is the main process which will run on each host, this class takes the responsibility
 * of performing all the peer responsibilities for that particular Peer which represents that host
 * 
 * For now this class takes responsibility for all peer processes for testing
 * 
 * @author Jagan
 *
 */
public class PeerProcess extends Peer implements Runnable{
	
	//Stores the peer process objects in a map
	private HashMap<Integer,Peer> peers = new HashMap<Integer,Peer>();
	
	private final PeerHandler pHandler;
	
	//Singleton object for Peer process
	//private static PeerProcess peerProcess;
	
	//Server socket for this peer
	private ServerSocket sSocket;
	
	private FileManager fileData;
	
	public PeerProcess(String pid, String hName, String portno, String present, HashMap<Integer,Peer> peers){
		super(pid,hName,portno,present);
		this.peers = peers;
		pHandler = new PeerHandler(sSocket, this.getInstance());
	}
	
	public PeerProcess(int pid, String hName, int portno, boolean present, HashMap<Integer,Peer> peers){
		super(pid,hName,portno,present);
		this.peers = peers;
		pHandler = new PeerHandler(sSocket, this.getInstance());
	}
	
	/**
	 * All the peers will be interested initially except the peer with the complete file, so need to send
	 * handshake messages to neighboring peers to check their bitfields, essentially broadcasting handshake messages
	 */
	public void startSender() {
		
		try {
			//Sending Handshake message to all other peers
			for (Peer pNeighbor : peers.values()){
				Socket s = new Socket(pNeighbor.getHostname(), pNeighbor.getPortNo());
				ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
				System.out.println("Handshake Message sent from peer "+getPeerId()+" to peer "+pNeighbor.getPeerId());                    	                   	
				out.writeObject(new HandShakeMsg(getPeerId()));
				out.flush();
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * Starts listening on a socket for handshake message, calls establishConnection method internally
	 */
	public void startServer(){

		(new Thread() {
			@Override
			public void run() {
				while(!sSocket.isClosed()){
					try {
						ConnectionHandler conn = establishConnection();
						conn.start();
					} catch (IOException | ClassNotFoundException e) {
						e.printStackTrace();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}).start();

	}
	
	/**
	 * Establishes connection between host peer and neighboring peer	 * 
	 * @return ConnectionHandler object
	 * @throws Exception
	 */
    public ConnectionHandler establishConnection() throws Exception{

    	Socket lSocket = sSocket.accept();
    	ObjectInputStream in = new ObjectInputStream(lSocket.getInputStream());
    	ObjectOutputStream out = new ObjectOutputStream(lSocket.getOutputStream());

    	//Receiving Handshake message
    	HandShakeMsg incoming = (HandShakeMsg)in.readObject();
    	if(peers.get(incoming.getPeerId()) == null || !incoming.getHeader().equals(HandShakeMsg.Header)){
    		System.out.println("Error performing Handshake : PeerId or Header unknown");
    	}
    	System.out.println("Received Handshake Message : "+
    			incoming.getPeerId()+" Header - "+incoming.getHeader());
    	
    	// No need to send Handshake message here again, since all peers will send handshake messages to
    	// neighboring peers in startSender method as well as there is no place we are receiving second handshake
    	
    	//Sending Bitfield message
    	BitfieldPayload out_payload = new BitfieldPayload(fileData.getBitField());
        out.writeObject(new Message(MessageType.BITFIELD, out_payload));
        out.flush();
    	
    	Peer neighbor = peers.get(incoming.getPeerId());
		
		//Creating connection irrespective of peers being interested
		ConnectionHandler conn = new ConnectionHandler(neighbor, peers.get(incoming.getPeerId()),
				in, out, lSocket, pHandler);
		neighbor.setConnHandler(conn);
		return conn;
    }
    
	@Override
	public void run() {
		fileData = new FileManager(getPeerId(), getFilePresent());
		try {
			setBitfield(fileData.getBitField());
    		sSocket = new ServerSocket(this.getPortNo());
    	} catch (Exception e) {
    		System.out.println("Error opening socket");
    		e.printStackTrace();
    	}
		startServer();
		pHandler.start();
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
    	    public void run() {
    	    	try {
					sSocket.close();
				} catch (IOException e) {
					System.out.println("Error closing socket of peer "+getPeerId());
					e.printStackTrace();
				}
    	    }
    	}));
	}
}