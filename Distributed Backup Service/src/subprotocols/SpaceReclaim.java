package subprotocols;

import java.util.HashMap;

import data.ChunkInfo;
import data.ChunksList;
import data.Data;
import utilities.Knapsack;

public class SpaceReclaim extends Thread {
	int space; //space in bytes
	
	public SpaceReclaim (int space) {
		this.space = space;
	}
	
	public void run() {
		if (space > Data.getUsedSpace()) {
			System.out.println("Space to recover is bigger than the space used by this peer, will delete all chunks.");
			space = Data.getUsedSpace();
		}
		
		ChunksList allChunks = new ChunksList();
		HashMap<String, ChunksList> chunksSaved = Data.getChunksSaved();
		for (ChunksList chunks : chunksSaved.values()) {
		    allChunks.addAll(chunks);
		}
		Knapsack knapsack = new Knapsack(space, allChunks);
		ChunkInfo[] chunksToDelete = knapsack.solve();
	}
	
}
