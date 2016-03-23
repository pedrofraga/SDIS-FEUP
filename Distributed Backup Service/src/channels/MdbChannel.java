package channels;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.ThreadLocalRandom;

import messages.Header;
import messages.Message;
import peers.Peer;
import utilities.Constants;

public class MdbChannel extends Channel{
	
	public MdbChannel(String mdbAddress, String mdbPort) throws IOException {
		super(mdbAddress, mdbPort);
		this.thread = new MdbThread();
	}
	
	public void listen() {
		this.thread.start();
	}
	
	public class MdbThread extends Thread {
		public void run() {
			while(true) {
				System.out.println("Listening the MDB channel...");
				try {
					String data = Peer.rcvMultiCastData(socket, address);
					String[] splittedMsg = Message.splitArgs(data);
					if(!Peer.getServerId().equals(splittedMsg[Constants.SENDER_ID])) {
						Header header = new Header(Constants.STORED, splittedMsg[Constants.VERSION],
								Peer.getServerId(), splittedMsg[Constants.FILE_ID], splittedMsg[Constants.CHUNK_NO], null);
						
						Message reply = new Message(Peer.getMcChannel().getSocket(), Peer.getMcChannel().getAddress(), header, null);
						int timeout = ThreadLocalRandom.current().nextInt(0, 400);
						System.out.println("Waiting time: " + timeout);
						Thread.sleep(timeout);
						new Thread(reply).start();
					}
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
