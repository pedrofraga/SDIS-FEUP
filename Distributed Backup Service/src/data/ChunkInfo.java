package data;

import messages.Header;

public class ChunkInfo {
	String fileId;
	int chunkNo;
	int chunkSize;
	int replicationDeg;

	public ChunkInfo(Header header, int chunkSize) {
		this.fileId = header.getFileId();
		this.chunkNo = Integer.parseInt(header.getChunkNo());
		this.replicationDeg = header.getReplicationDeg() != null ? Integer.parseInt(header.getReplicationDeg()) : -1;
		this.chunkSize = chunkSize;
	}
	public ChunkInfo(Header header) {
		this.fileId = header.getFileId();
		this.chunkNo = Integer.parseInt(header.getChunkNo());
		this.chunkSize = -1;
	}
	public String getFileId() {
		return fileId;
	}
	public int getChunkNo() {
		return chunkNo;
	}
	public int getChunkSize() {
		return chunkSize;
	}
	public int getReplicationDeg() {
		return replicationDeg;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + chunkNo;
		result = prime * result + ((fileId == null) ? 0 : fileId.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChunkInfo other = (ChunkInfo) obj;
		if (chunkNo != other.chunkNo)
			return false;
		if (fileId == null) {
			if (other.fileId != null)
				return false;
		} else if (!fileId.equals(other.fileId))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "ChunkInfo [fileId=" + fileId + ", chunkNo=" + chunkNo + ", chunkSize=" + chunkSize + "]";
	}
}