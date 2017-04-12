package src;

import java.io.*;
import java.text.*;
import java.util.*;

public class Logger {
	
	private static PrintWriter logfile;
	final static DateFormat time = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
	private static int peerID;
	
	public Logger(int pID)
	{
		try 
		{
			peerID = pID;
			logfile = new PrintWriter("log_peer_" +peerID+".log");
		}catch (FileNotFoundException e) 
		{
			System.out.println("Not able to create log writer");
		}
	}
	
	public void logTest()
	{
		System.out.println("Hello World");
	}
	
	//[Time]: Peer [peer_ID 1] <message> [peer_ID 2].
	
	public static void makeconnection(int ID)
	{
		try
		{
			logfile.println(time.format(new Date()) +": Peer " + peerID + " makes a connection to Peer " + ID);
			logfile.flush();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void connect(int ID)
	{
		try
		{
			logfile.println(time.format(new Date()) +": Peer " + peerID + " is connected from Peer " + ID);
			logfile.flush();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void connected(int ID)
	{
		try{
			logfile.println(time.format(new Date()) +": Peer " + peerID + " is connected from Peer " + ID);
			logfile.flush();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void preferredNeighbors(ArrayList<Integer> neighbors)
	{
		try
		{
			logfile.println(time.format(new Date()) +": Peer " + peerID + " has the preferred neighbors " + neighbors);
			logfile.flush();
		}catch(Exception e)
		{
			e.printStackTrace();
		}	
	}
	
	public static void optUnchoke(int ID)
	{
		try{
			logfile.println(time.format(new Date()) +": Peer " + peerID + " has the optimistically-unchocked neighbor " + ID);
			logfile.flush();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void unchoked(int ID)
	{
		try
		{
			logfile.println(time.format(new Date()) +": Peer " + peerID + " is unchocked by " + ID);
			logfile.flush();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void choked(int ID)
	{
		try
		{
			logfile.println(time.format(new Date()) +": Peer " + peerID + " is chocked by " + ID);
			logfile.flush();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void haveRecieved(int ID, int index)
	{
		try
		{
			logfile.println(time.format(new Date()) +": Peer " + peerID + " received a \'have\' message from " + ID + "for the piece " + index);
			logfile.flush();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void notIntRecieved(int ID)
	{
		try
		{
			logfile.println(time.format(new Date()) +": Peer " + peerID + " received a \'not interested\' message from " + ID);
			logfile.flush();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void intRecieved(int ID)
	{
		try
		{
			logfile.println(time.format(new Date()) +": Peer " + peerID + " received a \'interested\' message from " + ID);
			logfile.flush();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void downloading(int ID, int index, int count)
	{	
		try
		{
			logfile.println(time.format(new Date()) +": Peer " + peerID + " has downloaded the piece "+ index+ " from " + ID + ".\nNow the number of pieces it has is " + count);
			logfile.flush();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void downloadCompleted()
	{
		try
		{
			logfile.println(time.format(new Date()) +": Peer " + peerID + " has downloaded the complete file." );
			logfile.flush();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}