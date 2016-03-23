package subprotocols;

import java.io.IOException;

import messages.Header;
import messages.Message;
import peers.Peer;
import utilities.Constants;

public class ChunkBackup {
	private Message message;
	public ChunkBackup(Peer Peer, Header header, byte[] body) {
		this.message = new Message(peers.Peer.getMdbChannel().getSocket(), peers.Peer.getMdbChannel().getAddress(), header, body);
	}
	
	public void sendChunk() {
		new Thread(this.message).start();
	}

	public void listenReplications() throws IOException {
		int replicationDeg = Integer.parseInt(message.getHeader().getReplicationDeg());
		for (int i = 0; i < replicationDeg; i++) {
			String reply = Peer.rcvMultiCastData(Peer.getMcChannel().getSocket(), Peer.getMcChannel().getAddress());
			if (!validReply(reply)) i--;
		}
	}

	private boolean validReply(String reply) {
		String[] fields = Message.splitArgs(reply);
		if (!fields[Constants.MESSAGE_TYPE].equals(Constants.STORED))
			return false;
		if (fields[Constants.SENDER_ID].equals(Peer.getServerId()))
			return false;
		if (!fields[Constants.CHUNK_NO].equals(message.getHeader().getChunkNo()))
			return false;
		return true;
	}
}
