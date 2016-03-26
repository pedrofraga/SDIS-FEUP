package data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import exceptions.SizeException;
import peers.Peer;
import subprotocols.Restore;
import subprotocols.SpaceReclaim;
import utilities.Constants;


public class Data {
	static HashMap<String, ChunksList> chunksBackedUp; //FileId as key, Array of ChunkNo as value
	static HashMap<String, ChunksList> chunksSaved; //FileId as key, Array of ChunkNo as value

	static BackedUpFiles backedUpFiles; //HashMap containing which files are backed up, fileId as Keys
	static int usedSpace;
	

	static File chunks;
	public Data() {
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


	public void saveChunk(String fileId, long chunkNo, byte[] data) throws IOException {
		File chunkFolder = new File(chunks.getPath() + "/" + fileId + "/");
		if (!chunkFolder.exists())
			chunkFolder.mkdirs();
		FileOutputStream stream = new FileOutputStream(chunkFolder.getPath() + "/" + chunkNo + ".data");
		try {
		    stream.write(data);
		} finally {
		    stream.close();
		    ChunksList chunks = chunksSaved.get(fileId) != null ? chunksSaved.get(fileId) : new ChunksList();
		    ChunkInfo chunk = new ChunkInfo(fileId, (int)chunkNo, (int)data.length);
		    chunks.addChunk(chunk);
		    chunksSaved.put(fileId, chunks);
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

	/*public static int deleteChunk(String fileId, Long long1) {
		File chunk = new File(chunks.getPath() + "/" + fileId + "/" + long1 + ".data");
		int size = (int) chunk.length();
		if (SpaceReclaim.getSpace() - size < 0) 
			//SpaceReclaim.updateSmallerChunk(chunk);
		
		if(!chunk.delete()){
			System.out.println("Could not delete chunk.");
			return 0;
		}
		usedSpace -= size;
		return size;
	}*/

}
