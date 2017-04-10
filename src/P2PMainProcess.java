package src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;

public class P2PMainProcess {
	
	private static HashMap<Integer,Peer> peers = new HashMap<Integer,Peer>();

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
			}
			in.close();
			
			for(Integer key: peers.keySet()){
				Peer p = peers.get(key);
				HashMap<Integer,Peer> temp = new HashMap<Integer,Peer>(peers);
				temp.remove(key);
				new Thread(new PeerProcess(p.getPeerId(),p.getHostname(),p.getPortNo(),p.getFilePresent(),temp)).start();
			}
		}
		catch (Exception ex) {
			System.out.println(ex.toString());
		}
	}
   
}
