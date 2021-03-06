package app;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;

import Exceptions.ArgsException;

public class Client {
	
	private InetAddress mcadress;
	private int mcport;
	
	private InetAddress adress;
	private int port;
	
	private String oper;
	private ArrayList<String> args;
	
	private MulticastSocket mcsocket;
	private DatagramSocket socket;
	
	
	Client(String [] args) throws ArgsException, IOException {	
		this.mcadress = InetAddress.getByName(args[0]);
		this.mcport = Integer.parseInt(args[1]);
		
		this.mcsocket = new MulticastSocket(mcport);
		
		this.oper = args[2];
		this.args = new ArrayList<>();
		for (int i = 3; i < args.length; i++)
			this.args.add(args[i]);
		
		this.socket = new DatagramSocket();
		
		if (!checkOperAndArgs()) throw new ArgsException("Oper and Opnd doesn't match");	
	}
	
	
	private boolean checkOperAndArgs() {
		switch (this.oper) {
		case "lookup":
			if(this.args.size() != 1)
				return false;
			break;
		case "register":
			if(this.args.size() != 2)
				return false;
			break;
		default:
			return false;
		}
		return true;
	}
	
	private void rcvMultiCastInfo() throws IOException {
        mcsocket.joinGroup(mcadress);
        byte[] rbuf = new byte[utilities.Constants.MAX_MSG_SIZE];
		DatagramPacket packet = new DatagramPacket(rbuf, rbuf.length);
		mcsocket.receive(packet);
		String received = new String(packet.getData(), 0, packet.getLength());
		this.adress = packet.getAddress();
		this.port = Integer.parseInt(received);	
		System.out.println("multicast: " + mcadress + " "
				+ mcport + ": " + adress + " "
				+ port);
        mcsocket.leaveGroup(mcadress);
	}
	
	public void sendRequest() throws IOException {	
		String request = oper;
		for (int i = 0; i < this.args.size(); i++)
			request += " " + this.args.get(i);
		byte[] buf = request.getBytes();
		DatagramPacket packet = new DatagramPacket(buf, buf.length, this.adress, this.port);
		this.socket.send(packet);
		
		buf = new byte[utilities.Constants.MAX_MSG_SIZE];
		packet = new DatagramPacket(buf, buf.length);
		this.socket.receive(packet);
		String response = new String(packet.getData(), 0, packet.getLength());
		System.out.println(request + " : " + response);
	}
	
	
	public static void main(String [] args) throws IOException, ArgsException {
		if (args.length != 4 && args.length != 5) throw new ArgsException("Usage: java client <mcast_addr> <mcast_port> <oper> <opnd> *");
		Client cl = new Client(args);
		cl.rcvMultiCastInfo();
		cl.sendRequest();
	}



}
