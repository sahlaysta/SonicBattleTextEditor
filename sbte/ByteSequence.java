package sbte;

public class ByteSequence {
	private byte[] bytes;
	private String hex;
	public ByteSequence(String si) {
		String s = si.replace(" ", "").replace("-", "");
		byte[] arr = new byte[s.length()/2];
		for (int i = 0; i < arr.length; i++) {
			int ind = i * 2;
			arr[i] = (byte) Integer.parseInt(s.substring(ind, ind + 2), 16);
		}
		bytes = arr;
		
	    StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
	        sb.append(String.format("%02X", b));
	    }
		hex = sb.toString();
	}
	public ByteSequence(byte[] bs) {
		bytes = bs;
		
	    StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
	        sb.append(String.format("%02X", b));
	    }
		hex = sb.toString();
	}
	public byte[] getByteArray() {
		return bytes;
	}
	public String getHex() {
		return hex;
	}
}
