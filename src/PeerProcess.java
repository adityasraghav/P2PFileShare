package src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;

import jdk.nashorn.internal.runtime.regexp.joni.Config;

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
	private static HashMap<Integer,Peer> peers = new HashMap<Integer,Peer>();
	
	private final PeerHandler pHandler;
	
	//Singleton object for Peer process
	//private static PeerProcess peerProcess;
	
	//Server socket for this peer
	private ServerSocket sSocket;
	
	private FileManager fileData;
	
	private Logger logger;
	
	private boolean runner = true;
	
	
	
	public PeerProcess(String pid, String hName, String portno, String present){
		super(pid,hName,portno,present);
		logger = new Logger(Integer.parseInt(pid));
		pHandler = new PeerHandler(sSocket, this.getInstance(), peers);
	}
	
	public PeerProcess(int pid, String hName, int portno, boolean present){
		super(pid,hName,portno,present);
		logger = new Logger(pid);
		pHandler = new PeerHandler(sSocket, this.getInstance(), peers);
	}
	
	/**
	 * All the peers will be interested initially except the peer with the complete file, so need to send
	 * handshake messages to neighboring peers to check their bitfields, essentially broadcasting handshake messages
	 */
	public void startSender() {
		long timeout = 60000;
		new Thread(){
			public void run(){
				while(true){
					try {
						//Sending Handshake message to all other peers
						for (Peer pNeighbor : peers.values()){
							if(!pNeighbor.isPeerUp()){
								Socket s = new Socket(pNeighbor.getHostname(), pNeighbor.getPortNo());
								ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
								out.flush();
								System.out.println("Handshake Message sent from peer "+getPeerId()+" to peer "+pNeighbor.getPeerId());
								logger.connect(pNeighbor.getPeerId());
								out.writeObject(new HandShakeMsg(getPeerId()));
								out.flush();
								out.reset();
								pNeighbor.setHostSocket(s);
								pNeighbor.setPeerUp(true);
							}
						}
						Thread.sleep(timeout);
					} catch (UnknownHostException e) {
						e.printStackTrace();
					} catch (ConnectException e) {
						//System.out.println("Peer not accepting connections");
					} catch (IOException e) {
						e.printStackTrace();
					} catch (Exception e){
						e.printStackTrace();
					}
				}
			}
		}.start();
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
					} catch (SocketException e) {
						System.out.println("Peer "+getPeerId()+" is shutting down");
					} catch (IOException | ClassNotFoundException e) {
						e.printStackTrace();
					} catch (Exception e) {
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
		ObjectOutputStream out = new ObjectOutputStream(lSocket.getOutputStream());
		out.flush();
		ObjectInputStream in = new ObjectInputStream(lSocket.getInputStream());

		//Receiving Handshake message
		HandShakeMsg incoming = (HandShakeMsg)in.readObject();
		if(peers.get(incoming.getPeerId()) == null || !incoming.getHeader().equals(HandShakeMsg.Header)){
			System.out.println("Error performing Handshake : PeerId or Header unknown");
		}
		System.out.println("Received Handshake Message : "+
				incoming.getPeerId()+" Header - "+incoming.getHeader());
		logger.connected(incoming.getPeerId());
		
		// No need to send Handshake message here again, since all peers will send handshake messages to
		// neighboring peers in startSender method as well as there is no place we are receiving second handshake

		//Creating connection irrespective of peers being interested
		Peer neighbor = peers.get(incoming.getPeerId());
		ConnectionHandler conn = new ConnectionHandler(this, neighbor,in, out, lSocket, pHandler);
		conn.start();
		neighbor.setConnHandler(conn);

		//Sending Bitfield message
		BitfieldPayload out_payload = new BitfieldPayload(FileManager.getBitField());
		conn.sendMessage(new Message(MessageType.BITFIELD, out_payload));
		System.out.println("Sending Bitfield Message from: "+
				getPeerId()+" to: "+incoming.getPeerId());

		return conn;
	}
    
	@Override
	public void run() {
		System.out.println("Starting peer "+getPeerId());
		fileData = new FileManager(getPeerId(), getFilePresent());
		try {
			setBitfield(FileManager.getBitField());
			sSocket = new ServerSocket(this.getPortNo());
			System.out.println("Server socket created for peer "+getHostname());
		} catch (Exception e) {
			System.out.println("Error opening socket");
			e.printStackTrace();
		}
		startServer();
		startSender();
		terminator();
		pHandler.setSocket(sSocket);
		pHandler.start();

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				try {
					sSocket.close();
					for(Peer p: peers.values()){
						p.getHostSocket().close();
					}
				} catch (IOException e) {
					System.out.println("Error closing socket of peer "+getPeerId());
					e.printStackTrace();
				}
			}
		}));
		
	}
	
	public void terminator(){

		(new Thread() {
			@Override
			public void run() 
			{
				
				try {
					
					int peerWithFile = 0;
					int end = peers.size()+1;
					HashMap<Integer,Peer> temp =peers;
					int pieces = (int)Math.ceil((double)ConfigParser.getFileSize()/ConfigParser.getPieceSize());
					while(peerWithFile<end)
					{
						Thread.sleep(ConfigParser.getUnchokingInterval()*2000);
						if(FileManager.hasCompleteFile())
							peerWithFile++;
						
						for(Integer ind : temp.keySet())
						{
								Peer peer = temp.get(ind);
								if(FileUtilities.checkComplete(peer.getBitfield(),pieces))
								{
									peerWithFile++;
								}
								
						}
					}
					System.out.println("Shutting Down");
					System.exit(0);
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();

	}
	
	public static void main(String[] ar){
		getConfiguration(ar);
	}
	public static void getConfiguration(String[] ar)
	{
		String st;
		try {
			String hostname = InetAddress.getLocalHost().getHostName();
			String FileName = "PeerInfo.cfg";
			BufferedReader in = new BufferedReader(new FileReader(FileName));
			
			PeerProcess hostPeer = null;
					
			while((st = in.readLine()) != null) {	
				String[] tokens = st.split("\\s+");
				if(ar[0].equalsIgnoreCase(tokens[0])){
					hostPeer = new PeerProcess(tokens[0],tokens[1],tokens[2],tokens[3]);
				}else{
					Peer peer = new Peer(tokens[0],tokens[1],tokens[2],tokens[3]);
					peers.put(Integer.parseInt(tokens[0]),peer);
				}
			}
			in.close();
			
			new Thread(hostPeer).start();
		}
		catch (Exception ex) {
			System.out.println(ex.toString());
		}
	}
}
