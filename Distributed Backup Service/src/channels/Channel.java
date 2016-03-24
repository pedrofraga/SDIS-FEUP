package channels;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;


public class Channel {
	Thread thread;
	MulticastSocket socket;
	InetAddress address;
	private int port;
	
	Channel(String address, String port) throws IOException {
		this.address = InetAddress.getByName(address);
		this.port = Integer.parseInt(port);
		this.socket = new MulticastSocket(this.port);
		this.socket.setTimeToLive(1);
	}
	
	public void listen() {
		this.thread.start();
	}
	
	public MulticastSocket getSocket() {
		return socket;
	}

	public InetAddress getAddress() {
		return address;
	}
	
}
