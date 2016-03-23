package utilities;

import java.security.MessageDigest;

public class Constants {
	public static final String IP = "225.0.0.0";
	public static final int MC_PORT = 4444;
	public static final int MDB_PORT = 4445;
	public static final int MDR_PORT = 4446;
	
	public static final char CR = 0xD;
	public static final char LF = 0xA;
	
	public static final String BACKUP = "PUTCHUNK";
	public static final String STORED = "STORED";
	public static final String RESTORE = "GETCHUNK";
	public static final String DELETE = "DELETE";
	
	public static final int CHUNK_SIZE = 64000;
	
	public static final int VERSION = 1;
	public static final int SENDER_ID = 2;
	public static final int FILE_ID = 3;
	public static final int CHUNK_NO = 4;
	
	public static String sha256(String base) {
	    try{
	        MessageDigest digest = MessageDigest.getInstance("SHA-256");
	        byte[] hash = digest.digest(base.getBytes("UTF-8"));
	        StringBuffer hexString = new StringBuffer();

	        for (int i = 0; i < hash.length; i++) {
	            String hex = Integer.toHexString(0xff & hash[i]);
	            if(hex.length() == 1) hexString.append('0');
	            hexString.append(hex);
	        }

	        return hexString.toString();
	    } catch(Exception ex){
	       throw new RuntimeException(ex);
	    }
	}
}
