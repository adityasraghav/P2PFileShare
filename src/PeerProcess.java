package src;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

public class PeerProcess extends Peer implements Runnable{
	
	//Stores the peer process objects in a map
	private static HashMap<Integer,Peer> peers = new HashMap<Integer,Peer>();
	
	//Singleton object for Peer process
	//private static PeerProcess peerProcess;
	
	//Server socket for this peer
	private ServerSocket sSocket;
	//listening socket
	//private Socket lSocket;
	
	public PeerProcess(String pid, String hName, String portno, String present){
		super(pid,hName,portno,present);
	}
	
	public PeerProcess(int pid, String hName, int portno, short present){
		super(pid,hName,portno,present);
	}
	
	public void startSender(Peer pNeighbor) {
    	Peer pHost = this;
        (new Thread() {
            @Override
            public void run() {
                try {
                    Socket s = new Socket(pNeighbor.getHostname(), pNeighbor.getPortNo());
                    ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());

                    	System.out.println("Handshake Message sent from peer "+pHost.getPeerId()+" to peer "+pNeighbor.getPeerId());
                    	//String message = pHost.getPeerId()+": Hello Peer "+pNeighbor.getPeerId();
                    	                   	
                        out.writeObject(new HandShakeMsg(pHost.getPeerId()));
                        out.flush();


                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void startServer(){

    	(new Thread() {
    		@Override
    		public void run() {
    			while(!sSocket.isClosed()){
    				try {
    					Socket lSocket = sSocket.accept();
    					ObjectInputStream in = new ObjectInputStream(lSocket.getInputStream());
    					HandShakeMsg incoming = (HandShakeMsg)in.readObject();
    					System.out.println("Received Handshake Message : "+incoming.PeerID+" Header - "+incoming.Header);
    				} catch (IOException | ClassNotFoundException e) {
    					e.printStackTrace();
    				}
    			}
    		}
    	}).start();
    }
    
	public static void main(String[] args) throws IOException 
	{			
			System.out.println(ConfigParser.getFileName());
	    	getConfiguration();
	    	
	}
	
    public static void getConfiguration()
	{
		String st;
		try {
			String hostname = InetAddress.getLocalHost().getHostName();
			
			String FileName = "PeerInfo.cfg";
			
			BufferedReader in = new BufferedReader(new FileReader(FileName));
			
			while((st = in.readLine()) != null) {	
				String[] tokens = st.split("\\s+");
				Peer peer = new Peer(tokens[0],tokens[1],tokens[2],tokens[3]);
				peers.put(Integer.parseInt(tokens[0]),peer);
				if(tokens[1].trim().equals(hostname)){
					new Thread(new PeerProcess(tokens[0],tokens[1],tokens[2],tokens[3])).start();
				}
			}
			
			in.close();
		}
		catch (Exception ex) {
			System.out.println(ex.toString());
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
    		sSocket = new ServerSocket(this.getPortNo());
    	} catch (Exception e) {
    		// TODO Auto-generated catch block
    		System.out.println("Error opening socket");
    		e.printStackTrace();
    	}
		for(Integer id: peers.keySet()){
			if(id!=this.getPeerId()){
				startServer();
				startSender(peers.get(id));
			}
		}
	}
}