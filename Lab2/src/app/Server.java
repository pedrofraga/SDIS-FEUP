package app;
import db.Owner;
import utilities.Constants;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.util.ArrayList;

import Exceptions.ArgsException;

import java.io.IOException;

public class Server {
	
	private String serviceAddress;
	private int portNumber;
	private String multicastAddress;
	private int multicastPort;
	
	private ArrayList<Owner> owners;
	
	private InetAddress group;
	private MulticastSocket mcsocket;
	
	private DatagramSocket socket;
	private AdvertiseThread thread;

	public Server(int portNumber, String multicastAddress, int multicastPort) throws IOException {
		this.portNumber = portNumber;
		this.multicastAddress = multicastAddress;
		this.multicastPort = multicastPort;
		
		this.owners =  new ArrayList<Owner>();
		
		this.group = InetAddress.getByName(this.multicastAddress);
		
		this.mcsocket = new MulticastSocket(this.multicastPort);
		this.mcsocket.setTimeToLive(1);
		this.mcsocket.joinGroup(group);
		
		this.socket = new DatagramSocket(this.portNumber);
		this.thread = new AdvertiseThread();
	}

	private int listenRequests() throws IOException {
		boolean listening = true;
		while(listening) {
			byte[] rbuf = new byte[utilities.Constants.MAX_MSG_SIZE];
			DatagramPacket packet = new DatagramPacket(rbuf, rbuf.length);
			this.socket.receive(packet);
			String received = new String(packet.getData(), 0, packet.getLength());
			System.out.println("'" + received + "' was received.");

			String[] tokens = received.split(" ");

			String response = null;
			if(tokens[0].equals("lookup")) {
				response = lookUpFor(tokens[1]);
			} else if(tokens[0].equals("register")) {
				response = register(tokens[1], tokens[2]);
			}

			rbuf = response.getBytes();
			InetAddress address = packet.getAddress();
			int port = packet.getPort();
			packet = new DatagramPacket(rbuf, rbuf.length, address, port);
			socket.send(packet);

			for (int i = 0; i < this.owners.size(); i++)
				System.out.println(this.owners.get(i).getPlateNumber() + " - " + this.owners.get(i).getName());
		}

		return Constants.OK;
	}


	private String register(String plateNumber, String ownerName) {
		for(int i = 0; i < this.owners.size(); i++)
			if(this.owners.get(i).getPlateNumber().equals(plateNumber) && this.owners.get(i).getName().equals(ownerName)) {
				System.out.println("Error! There is an user with that name and plate number.");
				return "" + Constants.ERROR;
			}
		this.owners.add(new Owner(plateNumber, ownerName));
		System.out.println("User was created!");
		return ""  + this.owners.size();
	}

	private String lookUpFor(String plateNumber) {
		for(int i = 0; i < this.owners.size(); i++)
			if(this.owners.get(i).getPlateNumber().equals(plateNumber)) return this.owners.get(i).getPlateNumber() + " " + this.owners.get(i).getName();

		System.out.println("Could not find an user with this plate number -> " + plateNumber + ".");
		return "NOT_FOUND";
	}

	public static void main(String[] args) throws IOException, ArgsException {
		if (args.length != 3) throw new ArgsException("Server args number doesn't match");
		Server sv = new Server(Integer.parseInt(args[0]), args[1], Integer.parseInt(args[2]));
		sv.thread.start();
		sv.listenRequests();
	}

	private class AdvertiseThread extends Thread {
		public void run() {
			String advertisement = serviceAddress + ":"
					+ Integer.toString(portNumber);
			DatagramPacket packet = new DatagramPacket(advertisement.getBytes(),
					advertisement.getBytes().length, (InetAddress) multicastAddress,
					multicastPort);
			mcsocket.send(packet);

			System.out.println("multicast: " + multicastAddress + " "
					+ multicastPort + ": " + serviceAddress + " "
					+ portNumber);  
		}
	}

}
