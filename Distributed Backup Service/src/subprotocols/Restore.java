package subprotocols;


import database.FileInfo;
import messages.Header;
import messages.Message;
import peers.Peer;
import utilities.Constants;
import utilities.Utilities;

public class Restore extends Thread {
	private static String fileName;
	private static byte[] file;
	private static int numOfChunks = 0;
	
	public Restore(String fileName) {
		this.fileName = fileName;
		file = new byte[0];
	}
	
	public void restore() {
		if (!Peer.getStorage().getBackedUpFiles().containsKey(fileName)) {
			System.out.println("This file '" + fileName + "' was not backed up yet");
			return;
		}
		
		FileInfo fileInfo = Peer.getStorage().getBackedUpFiles().get(fileName);
		int numberOfChunks = fileInfo.getNumberOfChunks();
		Header header = new Header(Message.GETCHUNK, Constants.PROTOCOL_VERSION, Peer.getServerId(), fileInfo.getFileId(), "0", null);
		
		Peer.getMdrChannel().setWaitingChunks(true);
		for (int i = 0; i < numberOfChunks; i++) {
			header.setChunkNo("" + i);
			ChunkRestore chunkRestore = new ChunkRestore(header);
			chunkRestore.sendMessage();
		}
	}
	
	public static void addToFile() {
		
	}
	
	public void run() {
		restore();
	}

	public static void addChunkToFile(byte[] body) {
		byte[] newFile = Utilities.concatenateBytes(file, body);
		file = newFile;
		numOfChunks++;
	}

	public static byte[] getFileBytes() {
		return file;
	}

	public static int getNumOfChunks() {
		return numOfChunks;
	}

	public static String getFileName() {
		return fileName;
	}

}
