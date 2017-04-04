package src;

import java.net.ServerSocket;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;
import java.util.Random;
import java.util.TreeSet;

public class PeerHandler extends Thread{
		
	private ServerSocket sSocket;
	
	//Stores the peer process objects in a map
	private static HashMap<Integer,Peer> peers;

	private ArrayList<Peer> interested = new ArrayList<Peer>();
	
	private ArrayList<Peer> kPeers;
	
	private Peer optUnchokedPeer;
	
	private Peer hostPeer;
	
	public PeerHandler(){}
	
	public PeerHandler(ServerSocket s, Peer host){
		sSocket = s;
		hostPeer = host;
	}
	
	public void add(Peer i){
		interested.add(i);
	}
	
	public void kPreferredPeers(){
		
		long timeout = ConfigParser.getUnchokingInterval()*1000;
		new Thread(){
			public void run(){
				try{
					synchronized(interested){
						// reselecting k preferred peers in time intervals of 'UnchokingInterval' from config
						do{
							kPeers = new ArrayList<Peer>();
							// Sorts interested peers with respect to downloading rates only when host does not have the complete file
							if(!FileManager.hasCompleteFile()){
								interested.sort(new Comparator<Peer>() {
									Random r = new Random();
									@Override
									public int compare(Peer o1, Peer o2) {
										if(o1.getDownloadSpeed() == o2.getDownloadSpeed())
											return r.nextInt(2); //Randomly sequencing equal elements
										return (int)-(o1.getDownloadSpeed()-o2.getDownloadSpeed());
									}
								});
							}
							Iterator<Peer> it = interested.iterator();
							for(int i=0; i<ConfigParser.getNumberOfPreferredNeighbors()
									&& it.hasNext();i++){
								Peer p = it.next();
								// chooses peer adds it to k preferred peers list and unchokes them
								kPeers.add(p);
								unchokePeer(p);
							}
							chokePeers();
							wait(timeout);
						}while(!sSocket.isClosed());
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}.start();
		
	}
	
	/**
	 * Optimistically unchokes a peer from interested peer list at regular intervals
	 */
	public void optUnchokePeer(){
		//time interval in seconds to find and unchoke next optimistic peer
		long timeout = ConfigParser.getOptimisticUnchokingInterval()*1000;
		new Thread(){
			public void run(){
				try{
					synchronized(interested){
						// reselecting optimistic peer in time intervals of 'OptimisticUnchokingInterval' from config
						do{
							Peer p;
							Random r = new Random();
							Peer[] prs = (Peer[]) interested.toArray();
							do{
								p = prs[r.nextInt(prs.length)];
							}while(!p.isUnchoked());
							optUnchokedPeer = p;
							unchokePeer(p);
							wait(timeout);
						}while(!sSocket.isClosed());
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}.start();
		
	}
	
	/**
	 * Unchokes a peer by sending unchoke message to the peer
	 * 
	 * @param p peer to be unchoked
	 */
	public void unchokePeer(Peer p){
		p.unChoke(true);
		//send unchoke message to peer p
		Message msgUnchoke = new Message(MessageType.UNCHOKE, null);
		p.getConn().sendMessage(msgUnchoke);
	}
	
	/**
	 * Chokes all peers which are neither k preferred peers nor optimistically unchoked peer
	 */
	public void chokePeers(){
		//choke all other peers not in map kPeers
		Iterator itr = peers.entrySet().iterator();
		while(itr.hasNext()){
			Map.Entry entry = (Map.Entry)itr.next();
			Peer temp =(Peer)entry.getValue();
			if(!kPeers.contains(temp) && temp != optUnchokedPeer){
				temp.unChoke(false);
				Message chokeMsg = new Message(MessageType.CHOKE, null);
				temp.getConn().sendMessage(chokeMsg);
				// TODO call method to stop sending data to neighbor
			}
		}
	}
	
	public void run(){
		kPreferredPeers();
		optUnchokePeer();
	}
}
