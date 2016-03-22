package peers;

import java.net.InetAddress;
import java.net.UnknownHostException;

import exceptions.ArgsException;

public class Peer {
	private String serverId;
	
	private InetAddress mcAddress;
	private int mcPort;
	
	private InetAddress mdbAddress;
	private int mdbPort;
	
	private InetAddress mdrAddress;
	private int mdrPort;
	
	public Peer(String serverId, String mcAddress, String mcPort,
			String mdbAddress, String mdbPort, String mdrAddress,
			String mdrPort) throws UnknownHostException {
		this.serverId = serverId;
		
		this.mcAddress = InetAddress.getByName(mcAddress);
		this.mcPort = Integer.parseInt(mcPort);
		
		this.mdbAddress = InetAddress.getByName(mdbAddress);
		this.mdbPort = Integer.parseInt(mdbPort);
		
		this.mdrAddress = InetAddress.getByName(mdrAddress);
		this.mdrPort = Integer.parseInt(mdrPort);
	}

	public static void main(String[] args) throws ArgsException, UnknownHostException {
		if (args.length != 7)
			throw new ArgsException("peer <Server ID> <MC> <MC port> <MDB> <MDB port> <MDR> <MDR port>");
		
		Peer peer = new Peer(args[0], args[1], args[2], args[3], args[4], args[5], args[6]);
	}
}
