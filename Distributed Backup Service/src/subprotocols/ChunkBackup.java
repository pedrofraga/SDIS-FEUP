package subprotocols;

import java.io.IOException;
import java.util.ArrayList;

import messages.Header;
import messages.Message;
import peers.Peer;
import utilities.Constants;

public class ChunkBackup implements Runnable{
	private Message message;
	private ArrayList<Header> validReplies;
	public ChunkBackup(Header header, byte[] body) {
		this.message = new Message(peers.Peer.getMdbChannel().getSocket(), peers.Peer.getMdbChannel().getAddress(), header, body);
		this.validReplies = new ArrayList<>();
	}
	
	public void sendChunk() {
		new Thread(this.message).start();
	}


	private boolean validReply(String reply) {
		String[] fields = Message.splitArgs(reply);
		if (!fields[Constants.MESSAGE_TYPE].equals(Message.STORED))
			return false;
		if (fields[Constants.SENDER_ID].equals(Peer.getServerId()))
			return false;
		if (!fields[Constants.FILE_ID].equals(message.getHeader().getFileId()))
			return false;
		if (!fields[Constants.CHUNK_NO].equals(message.getHeader().getChunkNo()))
			return false;
		Header header = new Header(fields[Constants.MESSAGE_TYPE], fields[Constants.VERSION],
				fields[Constants.SENDER_ID], fields[Constants.FILE_ID], fields[Constants.CHUNK_NO], null);
		validReplies.add(header);
		return true;
	}

	public void listenReplies() {
		int replicationDeg = Integer.parseInt(message.getHeader().getReplicationDeg());
		for (int i = 0; i < replicationDeg; i++) {
			String reply;
			try {
				reply = Peer.rcvMultiCastData(Peer.getMcChannel().getSocket(), Peer.getMcChannel().getAddress());
				if (!validReply(reply)) {
					System.out.println("Invalid reply: " + reply);
					i--;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {
		listenReplies();
		Peer.getStorage().getStoreConfirmations().put(message.getHeader(), validReplies);
	}
}
