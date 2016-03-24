package messages;

import utilities.Constants;

public class Header {
	private String stringType;
	private String version;
	private String senderId;
	private String fileId;
	private String chunkNo;
	private String replicationDeg;
	
	public Header(String stringType, String version, String senderId,
			String fileId, String chunkNo, String replicationDeg) {
		this.stringType = stringType;
		this.version = version;
		this.senderId = senderId;
		this.fileId = fileId;
		this.chunkNo = chunkNo;
		this.replicationDeg = replicationDeg;
	}
	
	public String getMsgType() {
		return stringType;
	}

	public String getVersion() {
		return version;
	}

	public String getSenderId() {
		return senderId;
	}

	public String getFileId() {
		return fileId;
	}

	public String getChunkNo() {
		return chunkNo;
	}

	public String getReplicationDeg() {
		return replicationDeg;
	}
	
	public String toString() {
		String string = "";
		string += this.getMsgType() != null ? this.getMsgType() + " " : "";
		string += this.getVersion() != null ? this.getVersion() + " " : "";
		string += this.getSenderId() != null ? this.getSenderId() + " " : "";
		string += this.getFileId() != null ? this.getFileId() + " " : "";
		string += this.getChunkNo() != null ? this.getChunkNo() + " " : "";
		string += this.getReplicationDeg() != null ? this.getReplicationDeg() + " " : "";
		string += Constants.CR + Constants.LF +
				Constants.CR + Constants.LF;
		return string;
	}

	public void setChunkNo(String chunkNo) {
		this.chunkNo = chunkNo;
	}
}
