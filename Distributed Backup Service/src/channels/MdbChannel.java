package channels;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import messages.Header;
import messages.Message;
import peers.Peer;
import utilities.Constants;

public class MdbChannel extends Channel{
	
	public MdbChannel(String mdbAddress, String mdbPort) throws IOException {
		super(mdbAddress, mdbPort);
		this.thread = new MdbThread();
	}
	
	public void listen() {
		this.thread.start();
	}
	
	public void handlePutChunk(String[] splittedMsg) throws InterruptedException {
		
		//save chunk
		String[] chunkArray = Arrays.copyOfRange(splittedMsg, 5, splittedMsg.length);
		System.out.println("ChunkArray[0] = " + chunkArray[0]);
		String chunkStr = Arrays.toString(chunkArray);
		byte[] crlfdata = chunkStr.getBytes();
		System.out.println("That PUTCHUNK had " + crlfdata.length + " bytes of data.");
		byte[] data = Arrays.copyOfRange(crlfdata, 4, crlfdata.length);
		Long chunkNo = Long.parseLong(splittedMsg[Constants.CHUNK_NO]);
		try {
			Peer.getStorage().saveChunk(splittedMsg[Constants.FILE_ID], chunkNo, data);
		} catch (IOException e) {
			System.out.println("Could not save the chunk number " + splittedMsg[Constants.CHUNK_NO] + "from file " + splittedMsg[Constants.FILE_ID]);
			return;
		}
		System.out.println("Chunk number " + splittedMsg[Constants.CHUNK_NO] + " from file " + splittedMsg[Constants.FILE_ID] + " was saved! Replying...");
		
		//reply
		Header header = new Header(Constants.STORED, splittedMsg[Constants.VERSION],
				Peer.getServerId(), splittedMsg[Constants.FILE_ID], splittedMsg[Constants.CHUNK_NO], null);
		Message reply = new Message(Peer.getMcChannel().getSocket(), Peer.getMcChannel().getAddress(), header, null);
		int timeout = ThreadLocalRandom.current().nextInt(0, 400);
		Thread.sleep(timeout);
		new Thread(reply).start();
	}
	
	public class MdbThread extends Thread {
		public void run() {
			while(true) {
				System.out.println("Listening the MDB channel...");
				try {
					socket.joinGroup(address);
					// separate data
					String data = Peer.rcvMultiCastData(socket, address);
					String[] splittedMsg = Message.splitArgs(data);
					
					//analising data
					if(!Peer.getServerId().equals(splittedMsg[Constants.SENDER_ID])) {
						switch (splittedMsg[Constants.MESSAGE_TYPE]) {
						case Constants.PUTCHUNK:
							System.out.println("Received a PUTCHUNK message, will handle it... with " + splittedMsg.length + " fields.");
							handlePutChunk(splittedMsg);
							break;
						default:
							System.out.println("Ignoring message from type " + splittedMsg[Constants.MESSAGE_TYPE]);
							break;
						}
					}
					socket.leaveGroup(address);
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}