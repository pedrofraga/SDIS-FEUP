package subprotocols;

import java.io.File;
import java.util.ArrayList;

import data.ChunksList;
import data.FileInfo;
import messages.Header;
import messages.Message;
import peers.Peer;
import utilities.Constants;

public class ChunkBackup {
	private Message message;
	private ArrayList<Header> validReplies;
	private boolean iHaveIt;
	public ChunkBackup(Header header, byte[] body, boolean iHaveIt) {
		this.message = new Message(peers.Peer.getMdbChannel().getSocket(), peers.Peer.getMdbChannel().getAddress(), header, body);
		this.validReplies = new ArrayList<>();
		this.iHaveIt = iHaveIt;
	}

	public void sendChunk() {
		new Thread(this.message).start();
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
		if (iHaveIt) counter++;
		if (counter >= replicationDeg) {
			System.out.println("RepDeg achieved! Telling storage");
			tellStorage();
		}
		
	}
	
	private void tellStorage() {
		File file = Backup.getFile();
		int numberOfChunks = (int) (file.length() / Constants.CHUNK_SIZE + 1);
		if (Peer.getStorage().getBackedUpFiles().get(file.getName()) == null) 
			Peer.getStorage().getBackedUpFiles().markAsBackedUp(file.getName(), new FileInfo(file.getName(), message.getHeader().getFileId(), numberOfChunks, file.length()));
		String fileName = file.getName();
		
		int chunkNo = Integer.parseInt(message.getHeader().getChunkNo());
		FileInfo fileInfo = Peer.getStorage().getBackedUpFiles().get(fileName) != null ? Peer.getStorage().getBackedUpFiles().get(fileName) : new FileInfo(file.getName(), message.getHeader().getFileId(), numberOfChunks, (int)file.length());
		ArrayList<Header> headers = fileInfo.getBackedUpChunks().get(chunkNo) != null ? fileInfo.getBackedUpChunks().get(chunkNo) : new ArrayList<Header>();
		for (int i = 0; i < validReplies.size(); i++) {
			if (!headers.contains(validReplies.get(i)))
				headers.add(validReplies.get(i));
		}
		Peer.getStorage().getBackedUpFiles().put(file.getName(), fileInfo);
		fileInfo.getBackedUpChunks().put(chunkNo, validReplies);
	}


}
