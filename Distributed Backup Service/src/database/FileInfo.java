package database;

import java.util.ArrayList;
import java.util.HashMap;

import messages.Header;

public class FileInfo {
	private String fileName;
	private String fileId;
	private int numberOfChunks;
	HashMap<Integer, ArrayList<Header>> backedUpChunks;
	
	public FileInfo(String fileName, String fileId, int numberOfChunks, long size) {
		this.fileName = fileName;
		this.fileId = fileId;
		this.numberOfChunks = numberOfChunks;
		this.backedUpChunks = new HashMap<Integer, ArrayList<Header>>();
	}
	
	public int countChunkBackUps(int chunkNo) {
		ArrayList<Header> headers = backedUpChunks.get(chunkNo);
		if (headers != null)
			return headers.size();
		else {
			return 0;
		}
	}
	
	public HashMap<Integer, ArrayList<Header>> getBackedUpChunks() {
		return backedUpChunks;
	}
	public String getFileId() {
		return fileId;
	}
	public int getNumberOfChunks() {
		return numberOfChunks;
	}
	
	public String getFileName() {
		return fileName;
	}	
	
}