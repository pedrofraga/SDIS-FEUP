package subprotocols;

import java.io.File;
import java.util.ArrayList;

import data.FileInfo;
import messages.Header;
import messages.Message;
import peers.Peer;
import utilities.Constants;

public class ChunkBackup {
	private Message message;
	private ArrayList<Header> validReplies;
	public ChunkBackup(Header header, byte[] body) {
		this.message = new Message(peers.Peer.getMdbChannel().getSocket(), peers.Peer.getMdbChannel().getAddress(), header, body);
		this.validReplies = new ArrayList<>();
	}

	public void sendChunk() {
		new Thread(this.message).start();
		Peer.getMcChannel().setWaitingReplies(true);
	}
	

	private boolean validReply(Header replyHeader) {
		if (!replyHeader.getMsgType().equals(Message.STORED))
			return false;
		if (replyHeader.getSenderId().equals(Peer.getServerId()))
			return false;
		if (!replyHeader.getFileId().equals(message.getHeader().getFileId()))
			return false;
		if (!replyHeader.getChunkNo().equals(message.getHeader().getChunkNo()))
			return false;
		validReplies.add(replyHeader);
		return true;
	}

	public void checkReplies() {
		int replicationDeg = Integer.parseInt(message.getHeader().getReplicationDeg());
		Message reply;
		ArrayList<Message> storedReplies = Peer.getMcChannel().getStoredReplies();
		int counter = 0;
		for (int i = 0; i < storedReplies.size(); i++) {
			reply = storedReplies.get(i);
			if (validReply(reply.getHeader())) {
				counter++;
			} 
			Peer.getMcChannel().getStoredReplies().remove(storedReplies.get(i));
		}
		
		if (counter >= replicationDeg) 
			tellStorage();
		
		Peer.getMcChannel().setWaitingReplies(false);
	}
	
	private void tellStorage() {
		File file = Backup.getFile();
		int numberOfChunks = (int) (file.length() / Constants.CHUNK_SIZE + 1);
		if (Peer.getStorage().getBackedUpFiles().get(file.getName()) == null) 
			Peer.getStorage().getBackedUpFiles().markAsBackedUp(file.getName(), new FileInfo(file.getName(), message.getHeader().getFileId(), numberOfChunks, file.length()));
		String fileName = file.getName();
		FileInfo fileInfo = Peer.getStorage().getBackedUpFiles().get(fileName);
		int chunkNo = Integer.parseInt(message.getHeader().getChunkNo());
		fileInfo.getBackedUpChunks().put(chunkNo, validReplies);
	}


}
