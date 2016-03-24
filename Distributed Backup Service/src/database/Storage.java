package database;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import messages.Header;
import utilities.Constants;


public class Storage {
	
	HashMap<Header, ArrayList<Header>> storeConfirmations; //Header from sent chunk is the key and an ArrayList with headers from replies is the value
	HashMap<String, ArrayList<Long>> storedChunks; //FileId as key, Array of ChunkNo as value
	File chunks;
	public Storage() {
		storeConfirmations = new HashMap<Header, ArrayList<Header>>();
		storedChunks = new HashMap<String, ArrayList<Long>>();
		chunks = new File(Constants.FILES_ROOT + Constants.CHUNKS_ROOT);
		createFolders();
	}

	private void createFolders() {
		if (!chunks.exists())
			chunks.mkdirs();
	}

	public HashMap<Header, ArrayList<Header>> getStoreConfirmations() {
		return storeConfirmations;
	}

	public int countConfirmedChunks(Header header) {
		ArrayList<Header> headers = (ArrayList<Header>)storeConfirmations.get(header);
		if (headers != null)
			return headers.size();
		else
			return 0;
	}

	public void saveChunk(String fileId, Long chunkNo, byte[] data) throws IOException {
		File chunkFolder = new File(chunks.getPath() + "/" + fileId + "/");
		if (!chunkFolder.exists())
			chunkFolder.mkdirs();
		FileOutputStream stream = new FileOutputStream(chunkFolder.getPath() + "/" + chunkNo + ".data");
		try {
		    stream.write(data);
		} finally {
		    stream.close();
		} 
	};
}