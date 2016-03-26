package channels;

import java.io.IOException;

import database.Storage;
import exceptions.SizeException;
import messages.Header;
import messages.Message;
import peers.Peer;
import subprotocols.Restore;
import utilities.Constants;


public class MdrChannel extends Channel{
	private boolean waitingChunks = false;
	public MdrChannel(String mdrAddress, String mdrPort) throws IOException {
		super(mdrAddress, mdrPort);
		this.thread = new MdrThread();
	}
	
	private void handleChunk(byte[] body) throws SizeException, IOException {
		if (body == null) {
			Storage.saveRestoredFile(Restore.getFileName());
		} else if (body.length < Constants.CHUNK_SIZE) {
			System.out.println("Handling CHUNK with " + body.length + " bytes.");
			Restore.addChunkToFile(body);
			Storage.saveRestoredFile(Restore.getFileName());
		} else if (body.length == Constants.CHUNK_SIZE) {
			System.out.println("Handling CHUNK with " + body.length + " bytes.");
			Restore.addChunkToFile(body);
		} else {
			throw new SizeException("The received chunk is bigger than 64KB, it has " + body.length + " bytes.");
		}
	}
	
	public class MdrThread extends Thread {
		public void run() {
			System.out.println("Listening the MDR channel...");
			while(true) {
				try {
					socket.joinGroup(address);
					// separate data
					String data = rcvMultiCastData();
					Message message = Message.getMessageFromData(data);
					Header header = message.getHeader();
					byte[] body = message.getBody() == null ? null : message.getBody();
					// analyzing data
					if(!Peer.getServerId().equals(header.getSenderId())) {
						switch (header.getMsgType()) {
						case Message.CHUNK:
							if (waitingChunks)
								handleChunk(body);
							break;
						}
					}
					socket.leaveGroup(address);
				} catch (IOException | SizeException e) {
					e.printStackTrace();
				} 
			}
		}
	}

	public void setWaitingChunks(boolean waitingChunks) {
		this.waitingChunks = waitingChunks;
	}
}
