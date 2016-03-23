package messages;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utilities.Constants;

public class Message implements Runnable {
	private MulticastSocket socket;
	private InetAddress address;
	private Header header;
	private String body;
	
	public Message(MulticastSocket socket, InetAddress address, Header header, String body) {
		this.socket = socket;
		this.address = address;
		this.header = header;
		this.body = body;
	}
	
	public void setBody(String body) {
		this.body = body;
	}
	
	public String toString() {
		String message = "";
		message += header.getMessageType() != null ? header.getMessageType() + " " : "";
		message += header.getVersion() != null ? header.getVersion() + " " : "";
		message += header.getSenderId() != null ? header.getSenderId() + " " : "";
		message += header.getFileId() != null ? header.getFileId() + " " : "";
		message += header.getReplicationDeg() != null ? header.getReplicationDeg() + " " : "";
		message += Constants.CR + Constants.LF +
				Constants.CR + Constants.LF;
		message += body != null ? body : "";
		return message;
	}
	
	public static String[] splitArgs(String message) {
		return message.split("\\s+");
	}

	@Override
	public void run() {
		String message = this.toString();
		DatagramPacket packet = new DatagramPacket(message.getBytes(),
				message.getBytes().length, address,
				socket.getPort());
		try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
