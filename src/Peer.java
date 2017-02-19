package src;

public class Peer {

	private int peerId;
	private String hostname;
	private int portNo;
	private short filePresent;
	public int getPeerId() {
		return peerId;
	}
	public void setPeerId(int peerId) {
		this.peerId = peerId;
	}
	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	public int getPortNo() {
		return portNo;
	}
	public void setPortNo(int portNo) {
		this.portNo = portNo;
	}
	public short getFilePresent() {
		return filePresent;
	}
	public void setFilePresent(short filePresent) {
		this.filePresent = filePresent;
	}
	
	public Peer(){}
	
	public Peer(String pid, String hName, String portno, String present){
		this.setPeerId(Integer.parseInt(pid));
		this.setHostname(hName);
		this.setPortNo(Integer.parseInt(portno));
		this.setFilePresent(Short.parseShort(present));
	}
	
	public Peer(int pid, String hName, int portno, short present){
		this.setPeerId(pid);
		this.setHostname(hName);
		this.setPortNo(portno);
		this.setFilePresent(present);
	}
}
