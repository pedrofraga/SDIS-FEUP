package data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import channels.McChannel;
import exceptions.SizeException;
import messages.Header;
import messages.Message;
import peers.Peer;
import subprotocols.Restore;
import utilities.Constants;


public class Data {
	static HashMap<ChunkInfo, ArrayList<Header>> receivedStoreMessages;
	static HashMap<String, ChunksList> chunksBackedUp; //FileId as key, Array of ChunkNo as value
	static HashMap<String, ChunksList> chunksSaved; //FileId as key, Array of ChunkNo as value

	static BackedUpFiles backedUpFiles; //HashMap containing which files are backed up, fileId as Keys
	static int usedSpace;
	

	static File chunks;
	public Data() {
		receivedStoreMessages = new HashMap<ChunkInfo, ArrayList<Header>>();
		chunksBackedUp = new HashMap<String, ChunksList>();
		chunksSaved = new HashMap<String, ChunksList>();
		backedUpFiles = new BackedUpFiles();
		chunks = new File(Constants.FILES_ROOT + Constants.CHUNKS_ROOT);
		usedSpace = 0;
		createFolders();
	}

	private void createFolders() {
		if (!chunks.exists())
			chunks.mkdirs();
	}


	public void saveChunk(Header header, byte[] data) throws IOException {
		File chunkFolder = new File(chunks.getPath() + "/" + header.getFileId() + "/");
		if (!chunkFolder.exists())
			chunkFolder.mkdirs();
		FileOutputStream stream = new FileOutputStream(chunkFolder.getPath() + "/" + header.getChunkNo() + ".data");
		try {
		    stream.write(data);
		} finally {
		    stream.close();
		    ChunksList chunks = chunksSaved.get(header.getFileId()) != null ? chunksSaved.get(header.getFileId()) : new ChunksList();
		    ChunkInfo chunk = new ChunkInfo(header, (int)data.length);
		    chunks.addChunk(chunk);
		    chunksSaved.put(header.getFileId(), chunks);
		    usedSpace += data.length;
		}
		
	};
	
	public BackedUpFiles getBackedUpFiles() {
		return backedUpFiles;
	}
	public HashMap<String, ChunksList> getStoredChunks() {
		return chunksBackedUp;
	}

	public static byte[] getChunkBody(String fileId, String chunkNo) throws IOException {
		Path restorableChunk = Paths.get(chunks.getPath() + "/" + fileId + "/" + chunkNo + ".data");
		return Files.readAllBytes(restorableChunk);
	}

	public static boolean chunkIsStored(String fileId, int chunkNo) {
		ChunksList chunksList = chunksSaved.get(fileId);
		if (chunksList == null) {
			System.out.println("chunksList not found");
			return false;
		}
		for (int i = 0; i < chunksList.size(); i++)  {
			if (chunksList.get(i).getChunkNo() == chunkNo)
				return true;
		}
		System.out.println("ChunkNo not found");
		return false;
	}

	public static void saveRestoredFile(String fileName) throws IOException, SizeException {
		Peer.getMdrChannel().setWaitingChunks(false);
		FileInfo fileInfo = backedUpFiles.get(fileName);
		if (Restore.getNumOfChunks() != fileInfo.getNumberOfChunks()) 
			throw new SizeException("The restored file does not have the right number of chunks: " 
		+ Restore.getNumOfChunks() + "/" + fileInfo.getNumberOfChunks());
		FileOutputStream out = new FileOutputStream(Constants.FILES_ROOT + Constants.RESTORED + fileName);
		out.write(Restore.getFileBytes());
		out.close();
	}

	public static void clearStoredChunks(String fileId) {
		if (chunksSaved.get(fileId) != null)
			chunksSaved.remove(fileId);
	}
	public static int getUsedSpace() {
		return usedSpace;
	}
	public static HashMap<String, ChunksList> getChunksSaved() {
		return chunksSaved;
	}

	public static int deleteChunk(ChunkInfo chunkInfo) {
		File chunk = new File(chunks.getPath() + "/" + chunkInfo.getFileId() + "/" + chunkInfo.getChunkNo() + ".data");
		int size = (int) chunk.length();
		if(!chunk.delete()){
			System.out.println("Could not delete chunk.");
			return 0;
		}
		deleteFromChunksSaved(chunkInfo);
		McChannel.sendRemoved(chunkInfo);
		usedSpace -= size;
		return size;
	}

	private static void deleteFromChunksSaved(ChunkInfo chunkInfo) {
		ChunksList chunks = chunksSaved.get(chunkInfo.getFileId());
		chunks.remove(chunkInfo);
	}

	public static void addToReceivedStoreMessages(Header header) {
		ChunkInfo chunkInfo = new ChunkInfo(header);
		ArrayList<Header> headers = receivedStoreMessages.get(chunkInfo) != null ? receivedStoreMessages.get(chunkInfo) : new ArrayList<Header>();
		if(!headers.contains(header)) {
			headers.add(header);
			receivedStoreMessages.put(chunkInfo, headers);
		}
	}

	public static ChunkInfo removeFromReceivedStoreMessages(Header header) {
		header.setMsgType(Message.STORED);
		ChunkInfo chunkInfo = new ChunkInfo(header);
		ArrayList<Header> headers = receivedStoreMessages.get(chunkInfo) != null ? receivedStoreMessages.get(chunkInfo) : new ArrayList<Header>();
		if(headers.contains(header)) {
			headers.remove(header);
		} 
		boolean iHaveIt = chunksSaved.get(header.getFileId()) != null  && chunksSaved.get(header.getFileId()).contains(chunkInfo) ? true : false;
		if (!iHaveIt)
			return null;
		int replication = headers.size() + 1;
		int replicationDeg = -1;
		for (ChunkInfo info : chunksSaved.get(header.getFileId())) {
			if (info.equals(chunkInfo)) {
				replicationDeg = info.getReplicationDeg();
				if (replication < replicationDeg)
					return info;
				else
					break;
			}
		}
		return null;
	}

}