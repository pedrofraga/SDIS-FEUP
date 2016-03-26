package utilities;

public class Utilities {
	// From internet: http://stackoverflow.com/questions/5513152/easy-way-to-concatenate-two-byte-arrays
	public static byte[] concatenateBytes(byte[]a, byte[] b) {
		byte[] c = new byte[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
	}
}
