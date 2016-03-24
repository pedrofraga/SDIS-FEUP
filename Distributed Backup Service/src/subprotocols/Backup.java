package subprotocols;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import exceptions.ArgsException;
import messages.Header;
import messages.Message;
import peers.Peer;
import utilities.Constants;
import utilities.Hash;

public class Backup extends Thread{
	private String fileName;
	private String version = "1.0";
	private int replicationDeg;
	
	public Backup(String fileName, String replicationDeg) throws ArgsException {
		this.fileName = fileName;
		this.replicationDeg = Integer.parseInt(replicationDeg);
		if (this.replicationDeg > 9 && this.replicationDeg < 1)
			throw new ArgsException("ReplicationDeg must be a number between 1 and 9...");
	}
	
	public void run() {
		Path path = Paths.get(Constants.FILES_ROOT + fileName);
		try {
			byte[] data = Files.readAllBytes(path);
			sendChunks(data);
		} catch (IOException e) {
			System.out.println("The file '" + fileName + "' does not exist.");
		} catch (InterruptedException e) {
			System.out.println("Could not sleep while sending chunks, aborting...");
		}
	}

	private void sendChunks(byte[] data) throws InterruptedException {
		int waitingTime = Constants.DEFAULT_WAITING_TIME;
		int chunksNum = data.length / Constants.CHUNK_SIZE + 1;
		String fileId = Hash.sha256(fileName + version);
		Header header = new Header(Message.PUTCHUNK, version, Peer.getServerId(), fileId, "0", replicationDeg + "");
		for (int i = 0; i < chunksNum; i++) {
			byte[] chunk = getChunkData(i, header, data);
			ChunkBackup backupChunk = new ChunkBackup(header, chunk);
			int chunksSent = 0;
			while (chunksSent < Constants.MAX_CHUNK_RETRY) {
				System.out.println("Sending chunk number " + i + " with " + chunk.length + " bytes, waiting " + waitingTime + "ms after that.");
				backupChunk.sendChunk();
				Thread thread = new Thread(backupChunk);
				thread.start();
				Thread.sleep(waitingTime);
				if (Peer.getStorage().countConfirmedChunks(header) < replicationDeg) {
					chunksSent++;
					thread.interrupt();
					waitingTime *= 2;
				} else {
					break;
				}
			}
			waitingTime = Constants.DEFAULT_WAITING_TIME;
		}
		System.out.println("All chunks were sent");
	}

	private byte[] getChunkData(int i, Header header, byte[] data) {
		header.setChunkNo(i + "");
		int dataSize = Constants.CHUNK_SIZE - header.toString().getBytes().length;
		int lastIndex = (i + 1) * dataSize < data.length ? (i + 1) * dataSize : data.length;
		return Arrays.copyOfRange(data, i * Constants.CHUNK_SIZE, lastIndex);
	}
}
